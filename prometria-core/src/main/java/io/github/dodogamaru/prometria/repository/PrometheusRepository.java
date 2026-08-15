package io.github.dodogamaru.prometria.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a Prometheus repository.
 *
 * <p>Annotated interfaces are scanned from the classpath and exposed as Spring
 * beans backed by a dynamic proxy.
 *
 * @author Daehwan Baek
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrometheusRepository {
}
