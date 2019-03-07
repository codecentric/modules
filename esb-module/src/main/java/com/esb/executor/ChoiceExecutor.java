package com.esb.executor;

import com.esb.api.message.Message;
import com.esb.commons.ExecutionGraph;
import com.esb.component.Choice;
import com.esb.flow.ExecutionNode;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.esb.commons.Preconditions.checkAtLeastOneAndGetOrThrow;
import static com.esb.commons.Preconditions.checkState;

public class ChoiceExecutor implements Executor {

    @Override
    public ExecutionResult execute(ExecutionNode executionNode, Message message, ExecutionGraph graph) {
        Choice choice = (Choice) executionNode.getComponent();

        List<ExecutionNode> nextExecutionNodes = choice.apply(message);

        ExecutionNode next = checkAtLeastOneAndGetOrThrow(
                nextExecutionNodes.stream(),
                "Choice must be followed by exactly one node");

        // This one stops until it find stop. Then we need to keep going
        ExecutionResult execute = Executors.execute(next, message, graph);

        Collection<ExecutionNode> nextNode = graph.successors(execute.getLastExecutedNode());
        checkState(nextExecutionNodes.size() == 1, "Outgoing execution Nodes for Stop must have size 1");

        Optional<ExecutionNode> moreNodeAfterStop = nextNode.stream().findFirst();

        return moreNodeAfterStop.isPresent() ?
                Executors.execute(moreNodeAfterStop.get(), message, graph) :
                execute;
    }

}
