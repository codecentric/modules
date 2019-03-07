package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.commons.ESBExecutionGraph;
import com.esb.flow.ExecutionNode;

import java.util.Collection;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

public class FlowExecutor {

    private final ESBExecutionGraph graph;

    public FlowExecutor(ESBExecutionGraph graph) {
        this.graph = graph;
    }

    public Message execute(Message message) {
        ExecutionNode root = graph.getRoot();

        Collection<ExecutionNode> nextExecutorNodes = graph.successors(root);

        ExecutionNode nodeAfterRoot = checkAtLeastOneAndGetOrThrow(
                nextExecutorNodes.stream(),
                "Root must be followed by exactly one node");

        ExecutionResult result = Executors.execute(nodeAfterRoot, message, graph);

        return result.getMessage();
    }

}
