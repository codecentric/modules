package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


class ProcessorSyncExecutorTest extends AbstractExecutionTest {

    private ProcessorSyncExecutor executor = new ProcessorSyncExecutor();
    private ExecutionNode processor = newExecutionNode(new AddPostfixSyncProcessor("-postfix"));

    @Test
    void shouldCorrectlyApplyProcessorToMessage() {
        // Given
        ExecutionGraph graph = newGraphSequence(inbound, processor, stop);
        EventContext event = newEventWithContent("input");
        Publisher<EventContext> publisher = Mono.just(event);

        // When
        Publisher<EventContext> endPublisher =
                executor.execute(publisher, processor, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("input-postfix"))
                .verifyComplete();
    }

    @Test
    void shouldCorrectlyApplyProcessorToEachMessageInTheStream() {
        // Given
        ExecutionGraph graph = newGraphSequence(inbound, processor, stop);
        EventContext event1 = newEventWithContent("input1");
        EventContext event2 = newEventWithContent("input2");
        Publisher<EventContext> publisher = Flux.just(event1, event2);

        // When
        Publisher<EventContext> endPublisher =
                executor.execute(publisher, processor, graph);

        // Then
        StepVerifier.create(endPublisher)
                .assertNext(assertMessageContains("input1-postfix"))
                .assertNext(assertMessageContains("input2-postfix"))
                .verifyComplete();
    }

    @Test
    void shouldCorrectlyThrowErrorWhenProcessorThrowsException() {
        // Given
        ExecutionNode processor = newExecutionNode(new ProcessorThrowingExceptionSync());
        ExecutionGraph graph = newGraphSequence(inbound, processor, stop);
        Message message = MessageBuilder.get().text("input").build();

        OnResultVerifier onResultVerifier = new OnResultVerifier();
        EventContext inputEventContext = new EventContext(message, onResultVerifier);

        Publisher<EventContext> publisher = Flux.just(inputEventContext);

        // When
        Publisher<EventContext> endPublisher = executor.execute(publisher, processor, graph);

        // Then
        StepVerifier.create(endPublisher)
                .verifyErrorMatches(throwable -> throwable instanceof IllegalStateException);
    }

    // If the processor is the last node, then it must be present a Stop node.
    // If a Stop node is not there, it means there has been an error while
    // building the graph.
    @Test
    void shouldThrowExceptionIfProcessorNotFollowedByAnyOtherNode() {
        // Given
        ExecutionNode processor = newExecutionNode(new AddPostfixSyncProcessor("exception"));
        ExecutionGraph graph = newGraphSequence(inbound, processor);

        // When
        Assertions.assertThrows(IllegalStateException.class, () ->
                        executor.execute(Flux.just(), processor, graph),
                "Expected processor sync to be followed by one node");
    }

    class OnResultVerifier implements OnResult {
        Throwable throwable;
        @Override
        public void onError(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}