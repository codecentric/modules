package com.esb.flow.component.builder;

import com.esb.component.ForkWrapper;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import com.esb.graph.ExecutionGraph;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.esb.internal.commons.JsonParser.ForkJoin;
import static com.esb.internal.commons.JsonParser.Implementor;

class ForkJoinComponentBuilder extends AbstractBuilder {

    ForkJoinComponentBuilder(ExecutionGraph graph, FlowBuilderContext context) {
        super(graph, context);
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = Implementor.name(componentDefinition);

        ExecutionNode stopComponent = context.instantiateComponent(Stop.class);
        ExecutionNode forkExecutionNode = context.instantiateComponent(componentName);

        ForkWrapper forkComponent = (ForkWrapper) forkExecutionNode.getComponent();

        int threadPoolSize = ForkJoin.threadPoolSize(componentDefinition);
        forkComponent.setThreadPoolSize(threadPoolSize);

        graph.putEdge(parent, forkExecutionNode);

        JSONArray fork = ForkJoin.fork(componentDefinition);
        for (int i = 0; i < fork.length(); i++) {

            JSONObject nextObject = fork.getJSONObject(i);
            JSONArray nextComponents = ForkJoin.next(nextObject);

            ExecutionNode currentNode = forkExecutionNode;
            for (int j = 0; j < nextComponents.length(); j++) {

                JSONObject currentComponentDefinition = nextComponents.getJSONObject(j);
                ExecutionNode lastNode = ExecutionNodeBuilder.get()
                        .componentDefinition(currentComponentDefinition)
                        .parent(currentNode)
                        .context(context)
                        .graph(graph)
                        .build();

                // The first nextObject of A GIVEN fork path,
                // must be added as a fork execution node.
                if (j == 0) forkComponent.addForkNode(lastNode);

                currentNode = lastNode;
            }

            graph.putEdge(currentNode, stopComponent);
        }

        JSONObject joinComponent = ForkJoin.join(componentDefinition);
        ExecutionNode joinExecutionNode = ExecutionNodeBuilder.get()
                .componentDefinition(joinComponent)
                .parent(stopComponent)
                .context(context)
                .graph(graph)
                .build();

        forkComponent.addJoin(joinExecutionNode);
        return joinExecutionNode;
    }

}
