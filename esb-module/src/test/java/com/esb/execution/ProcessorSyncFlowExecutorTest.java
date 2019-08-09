package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.api.component.OnResult;
import com.esb.api.component.ProcessorSync;
import com.esb.api.message.Message;
import com.esb.api.message.MessageBuilder;
import com.esb.api.message.MimeType;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import com.esb.system.component.Stop;
import com.esb.test.utils.TestInboundComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessorSyncFlowExecutorTest {

    private ProcessorSyncFlowExecutor builder = new ProcessorSyncFlowExecutor();

    private ExecutionNode stopExecutionNode;
    private ExecutionNode inboundExecutionNode;
    private ExecutionGraph executionGraph = ExecutionGraph.build();

    @BeforeEach
    void setUp() {
        stopExecutionNode = new ExecutionNode(new ExecutionNode.ReferencePair<>(new Stop()));
        inboundExecutionNode = new ExecutionNode(new ExecutionNode.ReferencePair<>(new TestInboundComponent()));
        executionGraph.putEdge(null, inboundExecutionNode);
    }

    @Test
    void shouldCorrectlyApplyProcessorToMessage() {
        // Given
        ExecutionNode processor = newExecutionNode(new TestSyncProcessor());
        executionGraph.putEdge(inboundExecutionNode, processor);
        executionGraph.putEdge(processor, stopExecutionNode);

        Message originalMessage = MessageBuilder.get()
                .mimeType(MimeType.TEXT)
                .content("inputContent")
                .build();

        MessageContext inputMessageContext = new NoActionResultMessageContext(originalMessage);

        Flux<MessageContext> parentFlux = Flux.just(inputMessageContext);

        // When
        Publisher<MessageContext> flux = builder.execute(processor, executionGraph, parentFlux);

        // Then
        String expectedOutput = "inputContent-postfix";
        StepVerifier.create(flux).assertNext(messageContext -> {
            String out = (String) messageContext.getMessage().getTypedContent().getContent();
            assertThat(out).isEqualTo(expectedOutput);
        }).verifyComplete();
    }

    @Test
    void shouldCorrectlyCallGlobalErrorHandlerWhenProcessorThrowsException() {
        // Given
        ExecutionNode processor = newExecutionNode(new TestSyncProcessorThrowingException());
        executionGraph.putEdge(inboundExecutionNode, processor);
        executionGraph.putEdge(processor, stopExecutionNode);

        Message originalMessage = MessageBuilder.get()
                .mimeType(MimeType.TEXT)
                .content("input")
                .build();

        OnResultVerifier onResultVerifier = new OnResultVerifier();
        MessageContext inputMessageContext = new MessageContext(originalMessage, onResultVerifier);

        Flux<MessageContext> parentFlux = Flux.just(inputMessageContext);

        // When
        Publisher<MessageContext> flux = builder.execute(processor, executionGraph, parentFlux);

        // Then
        StepVerifier.create(flux).verifyComplete();

        assertThat(onResultVerifier.throwable).isInstanceOf(IllegalStateException.class);
        assertThat(onResultVerifier.throwable).hasMessage("Input not valid");
    }

    @Test
    void shouldThrowExceptionIfProcessorNotFollowedByOneNode() {
        // Given
        ExecutionNode processor = newExecutionNode(new TestSyncProcessor());
        executionGraph.putEdge(inboundExecutionNode, processor);

        // When
        Assertions.assertThrows(IllegalStateException.class, () ->
                        builder.execute(processor, executionGraph, Flux.just()),
                "Expected processor sync to be followed by one node");
    }

    private ExecutionNode newExecutionNode(Component component) {
        return new ExecutionNode(new ExecutionNode.ReferencePair<>(component));
    }

    private class EmptyResult implements OnResult {
    }

    private class NoActionResultMessageContext extends MessageContext {
        NoActionResultMessageContext(Message message) {
            super(message, new EmptyResult());
        }
    }

    private class TestSyncProcessor implements ProcessorSync {
        @Override
        public Message apply(Message input) {
            String inputString = (String) input.getTypedContent().getContent();
            String outputString = inputString + "-postfix";
            return MessageBuilder.get()
                    .mimeType(MimeType.TEXT)
                    .content(outputString)
                    .build();
        }
    }

    private class TestSyncProcessorThrowingException implements ProcessorSync {
        @Override
        public Message apply(Message input) {
            throw new IllegalStateException("Input not valid");
        }
    }

    class OnResultVerifier implements OnResult {
        Throwable throwable;

        @Override
        public void onError(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}