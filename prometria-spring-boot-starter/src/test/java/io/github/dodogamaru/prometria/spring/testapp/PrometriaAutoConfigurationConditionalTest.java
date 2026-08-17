package io.github.dodogamaru.prometria.spring.testapp;

import io.github.dodogamaru.prometria.query.QueryHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = PrometriaTestApplication.class)
class PrometriaAutoConfigurationConditionalTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void noPrometriaBeansWhenBaseUrlIsNotSet() {
        assertTrue(applicationContext.getBeansOfType(QueryHandler.class).isEmpty());
        assertFalse(applicationContext.containsBean("prometheusRepositoryScanner"));
        assertTrue(applicationContext.getBeansOfType(PrometheusIntegrationRepository.class).isEmpty());
    }
}
