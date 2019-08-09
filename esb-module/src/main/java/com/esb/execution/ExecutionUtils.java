package com.esb.execution;

import com.esb.graph.ExecutionGraph;
import com.esb.graph.ExecutionNode;
import reactor.core.publisher.SynchronousSink;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

class ExecutionUtils {

    static ExecutionNode nextNodeOrThrow(ExecutionNode previous, ExecutionGraph graph) {
        Collection<ExecutionNode> nextExecutorNodes = graph.successors(previous);
        return checkAtLeastOneAndGetOrThrow(
                nextExecutorNodes.stream(),
                "Expected only one following node");
    }

    static BiConsumer<EventContext, SynchronousSink<EventContext>> nullSafeMap(Function<EventContext, EventContext> mapper) {
        return (event, sink) -> {
            if (event != null) {
                EventContext result = mapper.apply(event);
                sink.next(result);
            }
        };
    }
}
