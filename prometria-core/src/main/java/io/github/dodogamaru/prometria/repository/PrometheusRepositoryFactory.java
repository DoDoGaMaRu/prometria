package io.github.dodogamaru.prometria.repository;

import io.github.dodogamaru.prometria.query.Condition;
import io.github.dodogamaru.prometria.query.QueryHandler;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Creates dynamic proxy instances of Prometheus repository interfaces.
 *
 * @author Daehwan Baek
 */
public final class PrometheusRepositoryFactory {

    private PrometheusRepositoryFactory() {
    }

    /**
     * Creates a JDK dynamic proxy implementing the given repository interface.
     *
     * @param repositoryInterface repository interface to proxy
     * @param handlers            handlers keyed by the condition type they accept
     * @param <T>                 type of the repository interface
     * @return proxy instance of the repository interface
     * @throws io.github.dodogamaru.prometria.exception.PrometheusRepositoryException if the interface is misconfigured
     */
    public static <T> T createRepository(
            Class<T> repositoryInterface,
            Map<Class<? extends Condition>, QueryHandler<?, ?>> handlers
    ) {
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class[]{repositoryInterface},
                new PrometheusRepositoryProxy(repositoryInterface, handlers)
        );
        return proxy;
    }
}
