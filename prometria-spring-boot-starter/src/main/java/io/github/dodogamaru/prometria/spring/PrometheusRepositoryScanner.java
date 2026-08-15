package io.github.dodogamaru.prometria.spring;

import io.github.dodogamaru.prometria.repository.PrometheusRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;

/**
 * Scans the classpath for interfaces annotated with {@link PrometheusRepository}
 * and registers a {@link PrometheusRepositoryFactoryBean} for each.
 *
 * <p>Scanning starts from the package that contains the
 * {@code @SpringBootApplication} class.
 *
 * @author Daehwan Baek
 */
public class PrometheusRepositoryScanner implements
        BeanDefinitionRegistryPostProcessor,
        PriorityOrdered,
        ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * Discovers repository interfaces and registers their factory beans.
     *
     * @param registry bean definition registry to register the factory beans in
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return beanDefinition.getMetadata().isInterface();
                    }
                };
        scanner.addIncludeFilter(new AnnotationTypeFilter(PrometheusRepository.class));

        String springBootApplicationName = Arrays.stream(applicationContext.getBeanNamesForAnnotation(SpringBootApplication.class)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Spring Boot Application found"));

        String basePackage = applicationContext.getBean(springBootApplicationName).getClass().getPackage().getName();

        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());

                BeanDefinitionBuilder builder = BeanDefinitionBuilder
                        .genericBeanDefinition(PrometheusRepositoryFactoryBean.class);
                builder.addConstructorArgValue(clazz);
                builder.addConstructorArgValue(applicationContext);

                registry.registerBeanDefinition(clazz.getSimpleName(), builder.getBeanDefinition());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return {@link Ordered#HIGHEST_PRECEDENCE} so scanning runs before other post-processors
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * No bean factory post-processing is required.
     *
     * @param beanFactory the bean factory
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    /**
     * @param applicationContext the application context
     * @throws BeansException if the context cannot be set
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
