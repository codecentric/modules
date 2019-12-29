package com.reedelk.esb.graph;

import com.reedelk.runtime.api.component.Inbound;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.reedelk.runtime.api.commons.Preconditions.checkArgument;
import static com.reedelk.runtime.api.commons.Preconditions.checkState;

public class ExecutionGraph {

    private ExecutionGraphDirected graph;
    private ExecutionNode root;

    private ExecutionGraph() {
        graph = new ExecutionGraphDirected();
    }

    public static ExecutionGraph build() {
        return new ExecutionGraph();
    }

    public void putEdge(ExecutionNode n1, ExecutionNode n2) {
        checkArgument(n2 != null, "n2 must not be null");

        // If the parent is null, then the current execution node is the FIRST node of the graph
        if (n1 == null) {
            checkState(root == null, "Root must be null for first component");
            checkState(n2.getComponent() instanceof Inbound, "First component must be Inbound");
            root = n2;
            graph.addNode(n2);
        } else {
            graph.putEdge(n1, n2);
        }
    }

    public ExecutionNode getRoot() {
        return root;
    }

    public Collection<ExecutionNode> successors(ExecutionNode executionNode) {
        checkArgument(executionNode != null, "executionNode");
        return graph.successors(executionNode);
    }

    public void applyOnNodes(Consumer<ExecutionNode> consumer) {
        checkArgument(consumer != null, "consumer");
        if (hasRoot()) {
            graph.breadthFirstTraversal(root, consumer);
        }
    }

    public Optional<ExecutionNode> findOne(Predicate<ExecutionNode> predicate) {
        checkArgument(predicate != null, "predicate");
        return graph.findOne(predicate);
    }

    private boolean hasRoot() {
        return root != null;
    }
}
