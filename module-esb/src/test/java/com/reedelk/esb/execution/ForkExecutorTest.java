package com.reedelk.esb.execution;

import com.reedelk.esb.component.ForkWrapper;
import com.reedelk.esb.execution.testutils.ForkTestGraphBuilder;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Join;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class ForkExecutorTest extends AbstractExecutionTest {

    private ForkExecutor executor = spy(new ForkExecutor());

    private ExecutionNode forkNode;
    private ExecutionNode fork1Node;
    private ExecutionNode fork2Node;
    private ExecutionNode joinNode;
    private ExecutionNode nodeFollowingJoin;

    @BeforeEach
    void setUp() {
        ForkWrapper forkWrapper = spy(new ForkWrapper());
        doReturn(Schedulers.elastic()).when(executor).flowScheduler();

        forkNode = newExecutionNode(forkWrapper);
        joinNode = newExecutionNode(new JoinString());
        fork1Node = newExecutionNode(new AddPostfixSyncProcessor("-fork1"));
        fork2Node = newExecutionNode(new AddPostfixSyncProcessor("-fork2"));
        nodeFollowingJoin = newExecutionNode(new AddPostfixSyncProcessor("-following-join"));
    }

    @Test
    void shouldForkAndJoinCorrectlyThePayload() {
        // Given
        ExecutionGraph graph = ForkTestGraphBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(joinNode)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("ForkTest-fork1,ForkTest-fork2"))
                .verifyComplete();
    }

    @Test
    void shouldForkAndJoinCorrectlyForAnyMessageInTheStream() {
        // Given
        ExecutionGraph graph = ForkTestGraphBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(joinNode)
                .build();

        MessageAndContext event1 = newEventWithContent("ForkTest1");
        MessageAndContext event2 = newEventWithContent("ForkTest2");
        Publisher<MessageAndContext> publisher = Flux.just(event1, event2);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("ForkTest1-fork1,ForkTest1-fork2"))
                .assertNext(assertMessageContains("ForkTest2-fork1,ForkTest2-fork2"))
                .verifyComplete();
    }

    @Test
    void shouldForkAndJoinCorrectlyAndContinueExecutionUntilTheEndOfTheGraph() {
        // Given
        ExecutionGraph graph = ForkTestGraphBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(joinNode)
                .afterForkSequence(nodeFollowingJoin)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher = executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("ForkTest-fork1,ForkTest-fork2-following-join"))
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionAndStopExecutionWhenBranchProcessorThrowsException() {
        // Given
        String exceptionMessage = "ForkException thrown";
        ExecutionNode processorThrowingException = newExecutionNode(new ProcessorThrowingIllegalStateExceptionSync(exceptionMessage));

        ExecutionGraph graph = ForkTestGraphBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(processorThrowingException)
                .join(joinNode)
                .afterForkSequence(nodeFollowingJoin)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .verifyErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().equals(exceptionMessage + " (ForkTest)"));
    }

    @Test
    void shouldThrowExceptionAndStopExecutionWhenJoinProcessorThrowsException() {
        // Given
        ExecutionNode joinThrowingException = newExecutionNode(new JoinThrowingException());

        ExecutionGraph graph = ForkTestGraphBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(joinThrowingException)
                .afterForkSequence(nodeFollowingJoin)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        Publisher<MessageAndContext> endPublisher =
                executor.execute(publisher, forkNode, graph);

        // Then
        StepVerifier.create(endPublisher)
                .verifyErrorMatches(throwable -> throwable instanceof IllegalStateException);
    }

    @Test
    void shouldThrowExceptionWhenJoinDoesNotImplementJoinInterface() {
        // Given
        ExecutionNode incorrectJoinType = newExecutionNode(new AddPostfixSyncProcessor("incorrect-join"));

        ExecutionGraph graph = ForkTestGraphBuilder.get()
                .fork(forkNode)
                .inbound(inbound)
                .forkSequence(fork1Node)
                .forkSequence(fork2Node)
                .join(incorrectJoinType)
                .build();

        MessageAndContext event = newEventWithContent("ForkTest");
        Publisher<MessageAndContext> publisher = Mono.just(event);

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> executor.execute(publisher, forkNode, graph));

        // Then
        Assertions.assertThat(thrown.getMessage())
                .isEqualTo("Fork must be followed by a component implementing [com.reedelk.runtime.api.component.Join] interface");
    }

    static class JoinString implements Join {
        @Override
        public Message apply(List<Message> messages, FlowContext flowContext) {
            String joined = messages.stream()
                    .map(message -> (String) message.getContent().data())
                    .collect(joining(","));
            return MessageBuilder.get().withText(joined).build();
        }
    }

    static class JoinThrowingException implements Join {
        @Override
        public Message apply(List<Message> messagesToJoin, FlowContext flowContext) {
            throw new IllegalStateException("Join not valid");
        }
    }
}
