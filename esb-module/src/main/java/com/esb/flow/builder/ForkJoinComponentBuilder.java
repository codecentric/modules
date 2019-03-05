package com.esb.flow.builder;

import com.esb.commons.Graph;
import com.esb.commons.JsonParser;
import com.esb.component.Fork;
import com.esb.component.Stop;
import com.esb.flow.ExecutionNode;
import com.esb.flow.FlowBuilderContext;
import org.json.JSONArray;
import org.json.JSONObject;

class ForkJoinComponentBuilder implements Builder {

    private final Graph graph;
    private final FlowBuilderContext context;

    ForkJoinComponentBuilder(Graph graph, FlowBuilderContext context) {
        this.graph = graph;
        this.context = context;
    }

    @Override
    public ExecutionNode build(ExecutionNode parent, JSONObject componentDefinition) {
        String componentName = JsonParser.Implementor.name(componentDefinition);

        ExecutionNode stopComponent = context.instantiateComponent(Stop.class);
        ExecutionNode forkExecutionNode = context.instantiateComponent(componentName);

        Fork forkComponent = (Fork) forkExecutionNode.getComponent();

        int threadPoolSize = JsonParser.ForkJoin.getThreadPoolSize(componentDefinition);
        forkComponent.setThreadPoolSize(threadPoolSize);

        graph.putEdge(parent, forkExecutionNode);

        JSONArray fork = JsonParser.ForkJoin.getFork(componentDefinition);
        for (int i = 0; i < fork.length(); i++) {

            JSONObject component = fork.getJSONObject(i);
            JSONArray next = JsonParser.ForkJoin.getNext(component);

            ExecutionNode currentNode = forkExecutionNode;
            for (int j = 0; j < next.length(); j++) {

                JSONObject currentComponentDefinition = next.getJSONObject(j);
                ExecutionNode lastNode = ExecutionNodeBuilder.get()
                        .componentDefinition(currentComponentDefinition)
                        .parent(currentNode)
                        .context(context)
                        .graph(graph)
                        .build();

                // The first component of A GIVEN fork path,
                // must be added as a fork execution node.
                if (j == 0) forkComponent.addForkNode(lastNode);

                currentNode = lastNode;
            }

            graph.putEdge(currentNode, stopComponent);
        }

        JSONObject joinComponent = JsonParser.ForkJoin.getJoin(componentDefinition);
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
