package com.reedelk.esb.execution;

import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;

import java.util.Collection;

import static com.reedelk.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;

class ExecutionUtils {

    private ExecutionUtils() {
    }
    
    /**
     * Returns the successor node of the current node and it throws
     * an exception if a node was not found.
     *
     * @param current the current node for which we want to get the successor.
     * @param graph   the execution graph the current node belongs to.
     * @return the following execution node of the current node.
     * @throws IllegalStateException if  the next node is not present.
     */
    static ExecutionNode nextNode(ExecutionNode current, ExecutionGraph graph) {
        Collection<ExecutionNode> nextExecutorNodes = graph.successors(current);
        return checkAtLeastOneAndGetOrThrow(
                nextExecutorNodes.stream(),
                "Expected only one following node");
    }
}
