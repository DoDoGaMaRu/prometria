package io.github.dodogamaru.prometria.repository;

import io.github.dodogamaru.prometria.exception.PrometheusRepositoryException;
import io.github.dodogamaru.prometria.query.Condition;
import io.github.dodogamaru.prometria.query.QueryHandler;
import io.github.dodogamaru.prometria.query.instant.InstantQueryCondition;
import io.github.dodogamaru.prometria.query.instant.InstantQueryResult;
import io.github.dodogamaru.prometria.query.range.RangeQueryCondition;
import io.github.dodogamaru.prometria.query.range.RangeQueryResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrometheusRepositoryProxyTest {

    private static Map<Class<? extends Condition>, QueryHandler<?, ?>> handlersOf(QueryHandler<?, ?>... handlers) {
        Map<Class<? extends Condition>, QueryHandler<?, ?>> handlerMap = new HashMap<>();
        for (QueryHandler<?, ?> handler : handlers) {
            handlerMap.put(handler.conditionType(), handler);
        }
        return handlerMap;
    }

    @Test
    void substitutesParameterNamesWhenParamAnnotationIsAbsent() {
        RecordingInstantHandler handler = new RecordingInstantHandler();
        SampleRepository repository = PrometheusRepositoryFactory
                .createRepository(SampleRepository.class, handlersOf(handler, new RecordingRangeHandler()));
        InstantQueryCondition condition = InstantQueryCondition.builder().build();

        repository.up("application", condition);

        assertEquals("up{job=\"application\"}", handler.lastQuery);
        assertSame(condition, handler.lastCondition);
    }

    @Test
    void substitutesValuesDeclaredWithParamAnnotation() {
        RecordingRangeHandler handler = new RecordingRangeHandler();
        SampleRepository repository = PrometheusRepositoryFactory
                .createRepository(SampleRepository.class, handlersOf(handler, new RecordingInstantHandler()));
        RangeQueryCondition condition = RangeQueryCondition.builder()
                .start(LocalDateTime.of(2026, 8, 15, 0, 0))
                .end(LocalDateTime.of(2026, 8, 15, 1, 0))
                .build();

        repository.averageRate("application", "15s", condition);

        assertEquals("avg by (job) (rate(http_requests_total{job=\"application\"}[15s]))", handler.lastQuery);
        assertSame(condition, handler.lastCondition);
    }

    @Test
    void rejectsMethodWithoutPromqlAnnotationAtCreation() {
        assertThrows(PrometheusRepositoryException.class,
                () -> PrometheusRepositoryFactory.createRepository(
                        NoPromqlRepository.class, handlersOf(new RecordingInstantHandler())));
    }

    @Test
    void rejectsMethodWithEmptyPromqlTemplateAtCreation() {
        PrometheusRepositoryException exception = assertThrows(PrometheusRepositoryException.class,
                () -> PrometheusRepositoryFactory.createRepository(
                        EmptyTemplateRepository.class, handlersOf(new RecordingInstantHandler())));

        assertTrue(exception.getMessage().contains(
                "template must not be empty"));
    }

    @Test
    void rejectsMethodWithoutConditionArgumentAtCreation() {
        assertThrows(PrometheusRepositoryException.class,
                () -> PrometheusRepositoryFactory.createRepository(
                        NoConditionRepository.class, handlersOf(new RecordingInstantHandler())));
    }

    @Test
    void rejectsUnsupportedConditionTypeAtCreation() {
        assertThrows(PrometheusRepositoryException.class,
                () -> PrometheusRepositoryFactory.createRepository(
                        UnsupportedConditionRepository.class,
                        handlersOf(new RecordingInstantHandler(), new RecordingRangeHandler())));
    }

    @Test
    void rejectsMismatchedReturnTypeAtCreation() {
        PrometheusRepositoryException exception = assertThrows(PrometheusRepositoryException.class,
                () -> PrometheusRepositoryFactory.createRepository(
                        MismatchedReturnRepository.class,
                        handlersOf(new RecordingInstantHandler(), new RecordingRangeHandler())));

        String message = exception.getMessage();
        assertTrue(message.contains(
                "return type InstantQueryResult does not match the result type RangeQueryResult "
                        + "of the handler for RangeQueryCondition"));
        assertTrue(message.contains(
                "so declare RangeQueryResult or use InstantQueryCondition instead"));
    }

    @Test
    void mismatchWithoutAlternativeSuggestsDeclaringTheExpectedType() {
        PrometheusRepositoryException exception = assertThrows(PrometheusRepositoryException.class,
                () -> PrometheusRepositoryFactory.createRepository(
                        UnknownResultRepository.class,
                        handlersOf(new RecordingInstantHandler(), new RecordingRangeHandler())));

        assertTrue(exception.getMessage().endsWith(
                "so declare InstantQueryResult."));
    }

    @Test
    void rejectsMultipleConditionParametersAtCreation() {
        assertThrows(PrometheusRepositoryException.class,
                () -> PrometheusRepositoryFactory.createRepository(
                        MultipleConditionRepository.class,
                        handlersOf(new RecordingInstantHandler(), new RecordingRangeHandler())));
    }

    @Test
    void rejectsUnresolvedTemplatePlaceholderAtCreation() {
        assertThrows(PrometheusRepositoryException.class,
                () -> PrometheusRepositoryFactory.createRepository(
                        UnresolvedPlaceholderRepository.class, handlersOf(new RecordingInstantHandler())));
    }

    @Test
    void supportsObjectMethods() {
        SampleRepository repository = PrometheusRepositoryFactory.createRepository(
                SampleRepository.class, handlersOf(new RecordingInstantHandler(), new RecordingRangeHandler()));

        assertDoesNotThrow(repository::toString);
        assertDoesNotThrow(repository::hashCode);
        assertDoesNotThrow(() -> repository.equals(repository));
    }

    interface SampleRepository {

        @PromQL(value = "up{job=\"${job}\"}")
        InstantQueryResult up(String job, InstantQueryCondition condition);

        @PromQL(value = "avg by (job) (rate(http_requests_total{job=\"${job}\"}[${window}]))")
        RangeQueryResult averageRate(@Param("job") String serviceName, @Param("window") String window,
                                     RangeQueryCondition condition);
    }

    interface NoPromqlRepository {

        InstantQueryResult missing(String job, InstantQueryCondition condition);
    }

    interface EmptyTemplateRepository {

        @PromQL("")
        InstantQueryResult missing(InstantQueryCondition condition);
    }

    interface NoConditionRepository {

        @PromQL(value = "up")
        InstantQueryResult missing(String job);
    }

    interface UnsupportedConditionRepository {

        @PromQL(value = "up{job=\"${job}\"}")
        InstantQueryResult custom(TestCondition condition, String job);
    }

    interface MismatchedReturnRepository {

        @PromQL(value = "up{job=\"${job}\"}")
        InstantQueryResult rangeAsInstant(RangeQueryCondition condition, String job);
    }

    interface UnknownResultRepository {

        @PromQL(value = "up")
        String customResult(InstantQueryCondition condition);
    }

    interface MultipleConditionRepository {

        @PromQL(value = "up{job=\"${job}\"}")
        InstantQueryResult both(InstantQueryCondition instant, RangeQueryCondition range, String job);
    }

    interface UnresolvedPlaceholderRepository {

        @PromQL(value = "up{job=\"${job}\", instance=\"${missing}\"}")
        InstantQueryResult unresolved(String job, InstantQueryCondition condition);
    }

    static class RecordingInstantHandler implements QueryHandler<InstantQueryCondition, InstantQueryResult> {

        String lastQuery;
        Condition lastCondition;

        @Override
        public Class<InstantQueryCondition> conditionType() {
            return InstantQueryCondition.class;
        }

        @Override
        public Class<InstantQueryResult> resultType() {
            return InstantQueryResult.class;
        }

        @Override
        public InstantQueryResult getResult(String query, InstantQueryCondition condition) {
            this.lastQuery = query;
            this.lastCondition = condition;
            return null;
        }
    }

    static class RecordingRangeHandler implements QueryHandler<RangeQueryCondition, RangeQueryResult> {

        String lastQuery;
        Condition lastCondition;

        @Override
        public Class<RangeQueryCondition> conditionType() {
            return RangeQueryCondition.class;
        }

        @Override
        public Class<RangeQueryResult> resultType() {
            return RangeQueryResult.class;
        }

        @Override
        public RangeQueryResult getResult(String query, RangeQueryCondition condition) {
            this.lastQuery = query;
            this.lastCondition = condition;
            return null;
        }
    }
}
