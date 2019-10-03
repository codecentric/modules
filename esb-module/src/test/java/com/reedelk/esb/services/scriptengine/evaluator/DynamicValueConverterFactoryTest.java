    package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.type.TypedPublisher;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class DynamicValueConverterFactoryTest {

    @Test
    void shouldConvertStringStreamToIntStream() {
        // Given
        TypedPublisher<String> input = TypedPublisher.from(Flux.just("1","2","4"), String.class);

        // When
        Publisher<Integer> converted =
                DynamicValueConverterFactory.convertStream(input, String.class, Integer.class);

        // Then
        StepVerifier.create(converted)
                .expectNext(1,2, 4)
                .verifyComplete();
    }

    @Test
    void shouldStreamPropagateErrorWhenConversionIsFailed() {
        // Given
        TypedPublisher<String> input = TypedPublisher.from(Flux.just("1", "not a number", "2"), String.class);

        // When
        Publisher<Integer> converted =
                DynamicValueConverterFactory.convertStream(input, String.class, Integer.class);

        // Then
        StepVerifier.create(converted)
                .expectNext(1)
                .expectError()
                .verify();
    }
}