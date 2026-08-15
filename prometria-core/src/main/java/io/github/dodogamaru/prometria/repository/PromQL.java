package io.github.dodogamaru.prometria.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the PromQL template of a repository method.
 *
 * <p>{@code ${key}} placeholders in the template are replaced with method
 * arguments bound via {@link Param} or the compile-time parameter name.
 * The template must not be blank; condition-only operations have no template
 * and are provided by the built-in API beans instead of repository methods.
 *
 * @author Daehwan Baek
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PromQL {

    /**
     * @return the PromQL template
     */
    String value();
}
