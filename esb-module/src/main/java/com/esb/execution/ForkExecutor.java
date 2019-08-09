package com.esb.execution;

import com.esb.api.component.Component;
import com.esb.api.component.Join;
import com.esb.api.message.Message;
import com.esb.commons.Preconditions;
import com.esb.component.ForkWrapper;
import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.esb.execution.ExecutionUtils.nextNodeOrThrow;
import static java.util.stream.Collectors.toList;
import static reactor.core.publisher.Mono.*;

public class ForkExecutor implements FlowExecutor {

    @Override
    public Publisher<EventContext> execute(ExecutionNode executionNode, ExecutionGraph graph, Publisher<EventContext> publisher) {

        // TODO: Fork pool property needs to be hooked up in the framework
        ForkWrapper fork = (ForkWrapper) executionNode.getComponent();

        List<ExecutionNode> nextExecutionNodes = fork.getForkNodes();

        ExecutionNode stopNode = fork.getStopNode();

        ExecutionNode joinNode = nextNodeOrThrow(stopNode, graph);

        Component joinComponent = joinNode.getComponent();
        Preconditions.checkState(joinComponent instanceof Join,
                String.format("Fork must be followed by a component implementing %s interface", Join.class.getName()));

        Join join = (Join) joinComponent;

        Mono<EventContext> mono = from(publisher).flatMap(messageContext -> {

            // Create fork branches (Fork step)
            List<Mono<EventContext>> forkBranches = nextExecutionNodes.stream()
                    .map(nextExecutionNode -> createForkBranch(nextExecutionNode, messageContext, graph))
                    .collect(toList());

            // Join fork branches (Join step)
            return zip(forkBranches, messagesCombinator())
                    .flatMap(reactiveMessageContexts ->
                            create(new JoinConsumer(messageContext, reactiveMessageContexts, join))
                                    .publishOn(Schedulers.elastic()));
        });


        // Continue to execute the flow after join
        ExecutionNode nodeAfterJoin = nextNodeOrThrow(joinNode, graph);

        return FlowExecutorFactory.get().build(nodeAfterJoin, graph, mono);
    }

    private Mono<EventContext> createForkBranch(ExecutionNode executionNode, EventContext context, ExecutionGraph graph) {
        EventContext messageCopy = context.copy();
        Mono<EventContext> parent = Mono.just(messageCopy).publishOn(Schedulers.parallel());
        return Mono.from(FlowExecutorFactory.get().build(executionNode, graph, parent));
    }

    private static Function<Object[], EventContext[]> messagesCombinator() {
        return objects -> {
            EventContext[] eventContexts = new EventContext[objects.length];
            for (int i = 0; i < objects.length; i++) {
                eventContexts[i] = (EventContext) objects[i];
            }
            return eventContexts;
        };
    }

    class JoinConsumer implements Consumer<MonoSink<EventContext>> {

        private final Join join;
        private final EventContext context;
        private final EventContext[] messages;

        JoinConsumer(EventContext originalMessage, EventContext[] messagesToJoin, Join join) {
            this.join = join;
            this.context = originalMessage;
            this.messages = messagesToJoin;
        }

        @Override
        public void accept(MonoSink<EventContext> sink) {
            try {
                List<Message> collect = Arrays
                        .stream(messages)
                        .map(EventContext::getMessage)
                        .collect(toList());
                Message outMessage = join.apply(collect);
                context.replaceWith(outMessage);
                sink.success(context);
            } catch (Exception e) {
                context.onError(e);
                sink.success();
            }
        }
    }
}


