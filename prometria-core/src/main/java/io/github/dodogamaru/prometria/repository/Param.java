package io.github.dodogamaru.prometria.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a repository method argument to a {@code ${...}} placeholder in a
 * {@link PromQL} template by an explicit key.
 *
 * <p>Without this annotation, the compile-time parameter name is used as the key.
 *
 * @author Daehwan Baek
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

    /**
     * @return the placeholder key to replace in the template
     */
    String value();
}
