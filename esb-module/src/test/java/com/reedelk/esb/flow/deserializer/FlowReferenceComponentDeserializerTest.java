package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.test.utils.ComponentsBuilder;
import com.reedelk.runtime.component.FlowReference;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FlowReferenceComponentDeserializerTest extends AbstractDeserializerTest {

    @Test
    void shouldCorrectlyHandleFlowReferenceComponent() {
        // Given
        JSONObject mySubflowDefinition = ComponentsBuilder.create()
                .with("id", "subflow1")
                .with("subflow", ComponentsBuilder.createNextComponentsArray(component3Name, component4Name, component2Name))
                .build();

        Set<JSONObject> subflows = new HashSet<>();
        subflows.add(mySubflowDefinition);

        DeserializedModule deserializedModule = new DeserializedModule(emptySet(), subflows, emptySet());

        doReturn(deserializedModule).when(context).deserializedModule();

        JSONObject componentDefinition = ComponentsBuilder.forComponent(FlowReference.class)
                .with("ref", "subflow1")
                .build();

        FlowReferenceDeserializer builder = new FlowReferenceDeserializer(graph, context);

        // When
        ExecutionNode lastNode = builder.deserialize(component1, componentDefinition);

        // Then
        assertThat(lastNode).isEqualTo(component2);

        verify(graph).putEdge(component1, component3);
        verify(graph).putEdge(component3, component4);
        verify(graph).putEdge(component4, component2);
        verifyNoMoreInteractions(graph);
    }
}
