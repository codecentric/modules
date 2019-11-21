package com.reedelk.esb.execution;

import com.reedelk.esb.component.RouterWrapper;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.runtime.component.Stop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.reedelk.esb.execution.AbstractExecutionTest.newExecutionNode;

class RouterTestGraphBuilder extends AbstractTestGraphBuilder {

    private ExecutionNode router;
    private ExecutionNode inbound;
    private List<ExecutionNode> followingSequence = new ArrayList<>();
    private List<ConditionWithSequence> conditionWithSequences = new ArrayList<>();

    static RouterTestGraphBuilder get() {
        return new RouterTestGraphBuilder();
    }

    RouterTestGraphBuilder router(ExecutionNode router) {
        this.router = router;
        return this;
    }

    RouterTestGraphBuilder inbound(ExecutionNode inbound) {
        this.inbound = inbound;
        return this;
    }

    RouterTestGraphBuilder conditionWithSequence(String condition, ExecutionNode... sequence) {
        conditionWithSequences.add(new ConditionWithSequence(condition, sequence));
        return this;
    }

    RouterTestGraphBuilder afterRouterSequence(ExecutionNode... following) {
        this.followingSequence = Arrays.asList(following);
        return this;
    }

    ExecutionGraph build() {
        ExecutionGraph graph = ExecutionGraph.build();
        graph.putEdge(null, inbound);
        graph.putEdge(inbound, router);

        ExecutionNode endOfRouter = newExecutionNode(new Stop());

        RouterWrapper routerWrapper = (RouterWrapper) router.getComponent();
        routerWrapper.setEndOfRouterStopNode(endOfRouter);
        for (ConditionWithSequence item : conditionWithSequences) {
            if (item.sequence.size() > 0) {
                routerWrapper.addExpressionAndPathPair(DynamicString.from(item.condition), item.sequence.get(0));
                buildSequence(graph, router, endOfRouter, item.sequence);
            }
        }

        ExecutionNode endOfGraph = newExecutionNode(new Stop());
        if (followingSequence.size() > 0) {
            buildSequence(graph, endOfRouter, endOfGraph, followingSequence);
        } else {
            graph.putEdge(endOfRouter, endOfGraph);
        }
        return graph;
    }

    class ConditionWithSequence {
        String condition;
        List<ExecutionNode> sequence;

        ConditionWithSequence(String condition, ExecutionNode[] sequence) {
            this.sequence = Arrays.asList(sequence);
            this.condition = condition;
        }
    }
}
