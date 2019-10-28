package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.JavascriptEngineProvider;
import com.reedelk.runtime.api.commons.ImmutableMap;
import com.reedelk.runtime.api.commons.StackTraceUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.*;
import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.StringContent;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.dynamicvalue.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DynamicValueStreamEvaluatorTest {

    @Mock
    private FlowContext context;

    private DynamicValueStreamEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new DynamicValueStreamEvaluator(JavascriptEngineProvider.INSTANCE);
    }

    @Nested
    @DisplayName("Evaluate dynamic string value with message and context")
    class EvaluateDynamicStringValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateMessageAttributeProperty() {
            // Given
            MessageAttributes attributes = new DefaultMessageAttributes(ImmutableMap.of("property1", "test1"));
            Message message = MessageBuilder.get().text("this is a test").attributes(attributes).build();
            DynamicString dynamicString = DynamicString.from("#[message.attributes.pRoperty1]");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("test1")
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateTextPayload() {
            // Given
            Message message = MessageBuilder.get().text("this is a test").build();
            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("this is a test")
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateStreamPayload() {
            // Given
            TypedContent<String> typedContent = new StringContent(Flux.just("one", "two"), MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("one")
                    .expectNext("two")
                    .verifyComplete();
        }

        // Here the original stream MUST be consumed
        // in order to evaluate the script.
        @Test
        void shouldCorrectlyConcatenateStreamWithString() {
            // Given
            Flux<String> content = Flux.just("Hello", ", this", " is", " just", " a");

            TypedContent<String> typedContent = new StringContent(content, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.content.data() + ' test.']");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("Hello, this is just a test.")
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyConcatenateWithString() {
            // Given
            String payload = "Hello, this is just a";
            TypedContent<String> typedContent = new StringContent(payload, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicString dynamicString = DynamicString.from("#[message.content.data() + ' test.']");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("Hello, this is just a test.")
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateString() {
            // Given
            Message message = MessageBuilder.get().text("test").build();

            DynamicString dynamicString = DynamicString.from("#['evaluation test']");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("evaluation test")
                    .verifyComplete();
        }

        @Test
        void shouldReturnTextFromDynamicValue() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("Expected text");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("Expected text")
                    .verifyComplete();
        }

        @Test
        void shouldReturnEmptyString() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("")
                    .verifyComplete();
        }

        @Test
        void shouldResultBeNullWhenDynamicValueIsNull() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = null;

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            assertThat(publisher).isNull();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueScriptIsEmpty() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from("#[]");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .verifyComplete();
        }

        @Test
        void shouldResultNotBePresentWhenDynamicValueStringIsNull() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicString dynamicString = DynamicString.from(null);

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateInteger() {
            // Given
            Message message = MessageBuilder.get().javaObject(23432).build();
            DynamicString dynamicString = DynamicString.from("#[message.payload()]");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("23432")
                    .verifyComplete();
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
            TypedPublisher<Integer> publisher = evaluator.evaluateStream(dynamicInteger, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(506)
                    .verifyComplete();
        }

        // Testing optimistic typing (Nashorn uses optimistic typing (since JDK 8u40))
        // http://openjdk.java.net/jeps/196.
        @Test
        void shouldCorrectlySumNumber() {
            // Given
            Message message = MessageBuilder.get().text("12").build();
            DynamicInteger dynamicInteger = DynamicInteger.from("#[parseInt(message.payload()) + 10]");

            // When
            TypedPublisher<Integer> publisher = evaluator.evaluateStream(dynamicInteger, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(22)
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateIntegerFromText() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicInteger dynamicInteger = DynamicInteger.from(53);

            // When
            TypedPublisher<Integer> publisher = evaluator.evaluateStream(dynamicInteger, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(53)
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateIntegerFromMessagePayload() {
            // Given
            Message message = MessageBuilder.get().javaObject(120).build();
            DynamicInteger dynamicInteger = DynamicInteger.from("#[message.payload()]");

            // When
            TypedPublisher<Integer> publisher = evaluator.evaluateStream(dynamicInteger, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(120)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic boolean value with message and context")
    class EvaluateDynamicBooleanValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateBoolean() {
            // Given
            Message message = MessageBuilder.get().text("a test").build();
            DynamicBoolean dynamicBoolean = DynamicBoolean.from("#[1 == 1]");

            // When
            TypedPublisher<Boolean> publisher = evaluator.evaluateStream(dynamicBoolean, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateBooleanFromPayload() {
            // Given
            Message message = MessageBuilder.get().text("true").build();
            DynamicBoolean dynamicBoolean = DynamicBoolean.from("#[message.payload()]");

            // When
            TypedPublisher<Boolean> publisher = evaluator.evaluateStream(dynamicBoolean, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(true)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic byte array value with message and context")
    class EvaluateDynamicByteArrayWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateByteArrayFromPayload() {
            // Given
            String payload = "My sample payload";
            Message message = MessageBuilder.get().text(payload).build();
            DynamicByteArray dynamicByteArray = DynamicByteArray.from("#[message.payload()]");

            // When
            TypedPublisher<byte[]> publisher = evaluator.evaluateStream(dynamicByteArray, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNextMatches(bytes -> Arrays.equals(bytes, payload.getBytes()))
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateByteArrayFromPayloadByteArrayStream() {
            // Given
            Flux<byte[]> stream = Flux.just("one".getBytes(), "two".getBytes());
            ByteArrayContent streamContent = new ByteArrayContent(stream, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(streamContent).build();
            DynamicByteArray dynamicByteArray = DynamicByteArray.from("#[message.payload()]");

            // When
            TypedPublisher<byte[]> publisher = evaluator.evaluateStream(dynamicByteArray, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNextMatches(bytes -> Arrays.equals(bytes, "one".getBytes()))
                    .expectNextMatches(bytes -> Arrays.equals(bytes, "two".getBytes()))
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateByteArrayFromPayloadStringStream() {
            // Given
            Flux<String> stream =  Flux.just("one","two");
            StringContent streamContent = new StringContent(stream, MimeType.TEXT);
            Message message = MessageBuilder.get().typedContent(streamContent).build();
            DynamicByteArray dynamicByteArray = DynamicByteArray.from("#[message.payload()]");

            // When
            TypedPublisher<byte[]> publisher = evaluator.evaluateStream(dynamicByteArray, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNextMatches(bytes -> Arrays.equals(bytes, "one".getBytes()))
                    .expectNextMatches(bytes -> Arrays.equals(bytes, "two".getBytes()))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic object value with message and context")
    class EvaluateDynamicObjectValueWithMessageAndContext {

        @Test
        void shouldCorrectlyEvaluateDynamicObject() {
            // Given
            Flux<String> content = Flux.just("Hello", ", this", " is", " just", " a");
            TypedContent<String> typedContent = new StringContent(content, MimeType.TEXT);

            Message message = MessageBuilder.get().typedContent(typedContent).build();

            DynamicObject dynamicObject = DynamicObject.from("#[message.content]");

            // When
            TypedPublisher<Object> publisher = evaluator.evaluateStream(dynamicObject, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(typedContent)
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateMessage() {
            // Given
            Message message = MessageBuilder.get().text("test").build();
            DynamicObject dynamicString = DynamicObject.from("#[message]");

            // When
            TypedPublisher<Object> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(message)
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateMessagePayload() {
            // Given
            MyObject given = new MyObject();
            Message message = MessageBuilder.get().javaObject(given).build();
            DynamicObject dynamicString = DynamicObject.from("#[message.payload()]");

            // When
            TypedPublisher<Object> publisher = evaluator.evaluateStream(dynamicString, message, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(given)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic string with throwable and context")
    class EvaluateDynamicStringWithThrowableAndContext {

        @Test
        void shouldCorrectlyEvaluateErrorPayload() {
            // Given
            Throwable myException = new ESBException("Test error");
            DynamicString dynamicString = DynamicString.from("#[error]");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, myException, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(StackTraceUtils.asString(myException))
                    .verifyComplete();
        }

        @Test
        void shouldCorrectlyEvaluateExceptionMessage() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = DynamicString.from("#[error.getMessage()]");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, myException, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("My exception message")
                    .verifyComplete();
        }

        @Test
        void shouldReturnEmptyWhenScriptIsEmpty() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = DynamicString.from("#[]");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, myException, context);

            // Then
            StepVerifier.create(publisher)
                    .verifyComplete();
        }

        @Test
        void shouldReturnEmptyWhenNullString() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = DynamicString.from(null);

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, myException, context);

            // Then
            StepVerifier.create(publisher)
                    .verifyComplete();
        }

        @Test
        void shouldReturnStringValue() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = DynamicString.from("my text");

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, myException, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("my text")
                    .verifyComplete();
        }

        @Test
        void shouldReturnNullWhenNullDynamicValue() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicString dynamicString = null;

            // When
            TypedPublisher<String> publisher = evaluator.evaluateStream(dynamicString, myException, context);

            // Then
            assertThat(publisher).isNull();
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic object value with throwable and context")
    class EvaluateDynamicObjectValueWithThrowableAndContext {

        @Test
        void shouldCorrectlyEvaluateDynamicObject() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicObject dynamicObject = DynamicObject.from("#[error]");

            // When
            TypedPublisher<Object> publisher = evaluator.evaluateStream(dynamicObject, myException, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext(myException)
                    .verifyComplete();
        }

        @Test
        void shouldReturnStringDynamicObject() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicObject dynamicObject = DynamicObject.from("my text");

            // When
            TypedPublisher<Object> publisher = evaluator.evaluateStream(dynamicObject, myException, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNext("my text")
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Evaluate dynamic byte array with throwable and context")
    class EvaluateDynamicByteArrayWithThrowableAndContext {

        @Test
        void shouldCorrectlyEvaluateDynamicByteArrayFromException() {
            // Given
            Throwable myException = new ESBException("My exception message");
            DynamicByteArray dynamicByteArray = DynamicByteArray.from("#[error]");

            // When
            TypedPublisher<byte[]> publisher = evaluator.evaluateStream(dynamicByteArray, myException, context);

            // Then
            StepVerifier.create(publisher)
                    .expectNextMatches(bytes -> Arrays.equals(bytes, StackTraceUtils.asByteArray(myException)))
                    .verifyComplete();
        }
    }

    private class MyObject {
    }
}