package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.StringContent;
import com.reedelk.runtime.api.message.type.Type;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.script.DynamicInteger;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicObject;
import com.reedelk.runtime.api.script.DynamicString;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;

import static com.reedelk.runtime.api.commons.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JavascriptEngineTest {

    private ScriptEngineService service = JavascriptEngine.INSTANCE;

    @Mock
    private FlowContext context;

    @Nested
    @DisplayName("Evaluate dynamic string value with message and context")
    class EvaluateDynamicStringValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateMessageAttributeProperty() {
            // Given
            Message message = MessageBuilder.get().text("this is a test").build();
            message.getAttributes().put("property1", "test1");
            DynamicString dynamicString = DynamicString.from("#[message.attributes.property1]");

            // When
            Optional<String> evaluated = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains("test1");
        }

        @Test
        void shouldCorrectlyEvaluateTextPayload() {
            // Given
            Message message = MessageBuilder.get().text("this is a test").build();
            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            Optional<String> evaluated = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains("this is a test");
        }

        @Test
        void shouldCorrectlyEvaluateStreamPayload() {
            // Given
            TypedContent<String> typedContent = new StringContent(Flux.just("one", "two"), MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            Optional<String> evaluated = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains("onetwo");
        }

        @Test
        void shouldCorrectlyConcatenateStreamWithString() {
            // Given
            Flux<String> content = Flux.just("Hello", ", this", " is", " just", " a");

            TypedContent<String> typedContent = new StringContent(content, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.content.data() + ' test.']");

            // When
            Optional<String> result = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("Hello, this is just a test.");
        }

        @Test
        void shouldCorrectlyConcatenateWithString() {
            // Given
            String payload = "Hello, this is just a";
            TypedContent<String> typedContent = new StringContent(payload, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.content.data() + ' test.']");

            // When
            Optional<String> result = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("Hello, this is just a test.");
        }

        @Test
        void shouldCorrectlyEvaluateString() {
            // Given
            Message message = MessageBuilder.get().text("test").build();

            DynamicString dynamicString = DynamicString.from("#['evaluation test']");

            // When
            Optional<String> result = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("evaluation test");
        }

        @Test
        void shouldReturnTextFromDynamicValue() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("Expected text");

            // When
            Optional<String> result = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("Expected text");
        }

        @Test
        void shouldReturnEmptyString() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("");

            // When
            Optional<String> result = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isPresent().contains("");
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueIsNull() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = null;

            // When
            Optional<String> result = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isNotPresent();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueScriptIsEmpty() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("#[]");

            // When
            Optional<String> result = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isNotPresent();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueStringIsNull() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from(null);

            // When
            Optional<String> result = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(result).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic integer value with message and context")
    class EvaluateDynamicIntegerValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateInteger() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicInteger dynamicInteger = DynamicInteger.from("#[506]");

            // When
            Optional<Integer> evaluated = service.evaluate(dynamicInteger, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(506);
        }

        // Testing optimistic typing (Nashorn uses optimistic typing (since JDK 8u40))
        // http://openjdk.java.net/jeps/196.
        @Test
        void shouldCorrectlySumNumber() {
            // Given
            Message message = MessageBuilder.get().text("12").build();
            DynamicInteger dynamicInteger = DynamicInteger.from("#[parseInt(message.payload()) + 10]");

            // When
            Optional<Integer> evaluated = service.evaluate(dynamicInteger, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(22);
        }

        @Test
        void shouldCorrectlyEvaluateIntegerFromText() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicInteger dynamicInteger = DynamicInteger.from("53");

            // When
            Optional<Integer> evaluated = service.evaluate(dynamicInteger, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(53);
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic object value with message and context")
    class EvaluateDynamicObjectValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateDynamicObject() {
            // Given
            Flux<String> content = Flux.just("Hello", ", this", " is", " just", " a");
            Type type = new Type(MimeType.TEXT, String.class);
            TypedContent<String> typedContent = new StringContent(content, type);

            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicObject dynamicObject = DynamicObject.from("#[message.content]");

            // When
            Optional<Object> result = service.evaluate(dynamicObject, message, context);

            // Then
            assertThat(result).isPresent().containsSame(typedContent);
        }

        @Test
        void shouldCorrectlyEvaluateMessage() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicObject dynamicString = DynamicObject.from("#[message]");

            // When
            Optional<Object> evaluated = service.evaluate(dynamicString, message, context);

            // Then
            assertThat(evaluated).isPresent().contains(message);
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic map with message and context")
    class EvaluateDynamicMapWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateMapWithScriptAndTextAndNumericValues() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            message.getAttributes().put("property1", "test");

            DynamicMap<String> dynamicMap = DynamicMap.from(of(
                    "script", "#[message.attributes.property1]",
                    "text", "This is a text",
                    "numeric", "23532"));

            // When
            Map<String, String> evaluated = service.evaluate(message, context, dynamicMap);

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
            Map<String, Object> evaluated = service.evaluate(message, context, DynamicMap.empty());

            // Then
            assertThat(evaluated).isEmpty();
        }
    }
}
