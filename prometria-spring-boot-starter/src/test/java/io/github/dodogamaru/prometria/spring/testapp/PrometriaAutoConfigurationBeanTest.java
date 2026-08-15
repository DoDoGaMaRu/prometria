package io.github.dodogamaru.prometria.spring.testapp;

import io.github.dodogamaru.prometria.client.PrometheusHttpClient;
import io.github.dodogamaru.prometria.query.QueryHandler;
import io.github.dodogamaru.prometria.query.format.FormatQueryHandler;
import io.github.dodogamaru.prometria.query.instant.InstantQueryHandler;
import io.github.dodogamaru.prometria.query.range.RangeQueryHandler;
import io.github.dodogamaru.prometria.api.DiscoveryApi;
import io.github.dodogamaru.prometria.api.RulesAlertsApi;
import io.github.dodogamaru.prometria.api.StatusApi;
import io.github.dodogamaru.prometria.api.TargetsApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = PrometriaTestApplication.class,
        properties = "prometria.prometheus.base-url=http://localhost:1/api/v1"
)
class PrometriaAutoConfigurationBeanTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void registersClientHandlersAndRepositoryBean() {
        assertNotNull(applicationContext.getBean(PrometheusHttpClient.class));
        assertEquals(3, applicationContext.getBeansOfType(QueryHandler.class).size());
        assertNotNull(applicationContext.getBean(InstantQueryHandler.class));
        assertNotNull(applicationContext.getBean(RangeQueryHandler.class));
        assertNotNull(applicationContext.getBean(FormatQueryHandler.class));

        assertNotNull(applicationContext.getBean(DiscoveryApi.class));
        assertNotNull(applicationContext.getBean(TargetsApi.class));
        assertNotNull(applicationContext.getBean(RulesAlertsApi.class));
        assertNotNull(applicationContext.getBean(StatusApi.class));
    }
}
