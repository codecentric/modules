package com.reedelk.esb.execution.testutils;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.component.Component;

import java.util.List;

class AbstractTestGraphBuilder {

    void buildSequence(ExecutionGraph graph, ExecutionNode start, ExecutionNode end, List<ExecutionNode> sequence) {
        ExecutionNode previous = start;
        for (ExecutionNode node : sequence) {
            graph.putEdge(previous, node);
            previous = node;
        }
        graph.putEdge(previous, end);
    }

    ExecutionNode newExecutionNode(Component component) {
        return new ExecutionNode(new ExecutionNode.ReferencePair<>(component));
    }
}
