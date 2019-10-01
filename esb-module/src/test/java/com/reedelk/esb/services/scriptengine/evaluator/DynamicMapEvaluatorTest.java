package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicmap.DynamicFloatMap;
import com.reedelk.runtime.api.script.dynamicmap.DynamicIntegerMap;
import com.reedelk.runtime.api.script.dynamicmap.DynamicStringMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DynamicMapEvaluatorTest {

    @Mock
    private FlowContext context;

    private DynamicMapEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new DynamicMapEvaluator(JavascriptEngineProvider.INSTANCE);
    }


    @Test
    void shouldCorrectlyEvaluateMapWithScriptAndTextAndNumericValues() {
        // Given
        Message message = MessageBuilder.get().text("test").build();
        message.getAttributes().put("property1", "test");

        DynamicStringMap dynamicMap = DynamicStringMap.from(of(
                "script", "#[message.attributes.property1]",
                "text", "This is a text",
                "numeric", "23532"));

        // When
        Map<String, String> evaluated = evaluator.evaluate(dynamicMap, message, context);

        // Then
        assertThat(evaluated.get("script")).isEqualTo("test");
        assertThat(evaluated.get("text")).isEqualTo("This is a text");
        assertThat(evaluated.get("numeric")).isEqualTo("23532");
    }

    @Test
    void shouldCorrectlyEvaluateEmptyMap() {
        // Given
        Message message = MessageBuilder.get().empty().build();

        // When
        Map<String, String> evaluated = evaluator.evaluate(DynamicStringMap.empty(), message, context);

        // Then
        assertThat(evaluated).isEmpty();
    }

    @Test
    void shouldCorrectlyEvaluateMapWithValueContainingQuotes() {
        // Given
        Message message = MessageBuilder.get().text("test").build();
        DynamicStringMap dynamicMap = DynamicStringMap.from(
                of("text", "a simple text 'with quotes'"));

        // When
        Map<String, String> evaluated = evaluator.evaluate(dynamicMap, message, context);

        // Then
        assertThat(evaluated.get("text")).isEqualTo("a simple text 'with quotes'");
    }

    @Test
    void shouldCorrectlyEvaluateMapWithIntegerValues() {
        // Given
        Message message = MessageBuilder.get().text("a text").build();
        DynamicIntegerMap dynamicMap = DynamicIntegerMap.from(of(
                "aNumericValue", 23,
                "aScriptedNumericValue", "#[45 + 23]"));

        // When
        Map<String,Integer> evaluated = evaluator.evaluate(dynamicMap, message, context);

        // Then
        assertThat(evaluated.get("aNumericValue")).isEqualTo(23);
        assertThat(evaluated.get("aScriptedNumericValue")).isEqualTo(68);
    }

    @Test
    void shouldCorrectlyEvaluateMapWithFloatValues() {
        // Given
        Message message = MessageBuilder.get().text("a text").build();
        DynamicFloatMap dynamicMap = DynamicFloatMap.from(of(
                "aFloatValue", 23.23f,
                "aScriptedFloatValue", "#[34.23 + 12.1]"));

        // When
        Map<String,Float> evaluated = evaluator.evaluate(dynamicMap, message, context);

        // Then
        assertThat(evaluated.get("aFloatValue")).isEqualTo(23.23f);
        assertThat(evaluated.get("aScriptedFloatValue")).isEqualTo(46.33f);
    }
}