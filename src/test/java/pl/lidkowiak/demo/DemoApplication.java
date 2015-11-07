package pl.lidkowiak.demo;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lidkowiak.DropwizardMetricsFilter;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

/**
 * Disabling default {@link org.springframework.boot.actuate.autoconfigure.MetricsFilter} in Boot 1.3.0.RC1.
 * Please see:
 * https://github.com/spring-projects/spring-boot/issues/2599
 * https://github.com/spring-projects/spring-boot/issues/4365
 */
@SpringBootApplication(exclude = MetricFilterAutoConfiguration.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    DropwizardMetricsFilter metricFilter(MetricRegistry metricRegistry) {
        return new DropwizardMetricsFilter(metricRegistry);
    }

    @Bean(destroyMethod = "close")
    Slf4jReporter slf4jReporter(MetricRegistry metricRegistry) {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);

        return reporter;
    }

    @RestController
    static class DemoRestController {

        private final Random random = new Random();

        @RequestMapping("/hello/{name}")
        String sayHello(@PathVariable String name, Long avgDuration, Long stdDev) throws InterruptedException {
            sleepWithNormalDistribution(avgDuration, stdDev);
            return "Hello " + name + "!";
        }

        void sleepWithNormalDistribution(Long avgDuration, Long stdDev) throws InterruptedException {
            avgDuration = isNull(avgDuration) ? 100 : avgDuration;
            stdDev = isNull(stdDev) ? 10 : stdDev;
            Thread.sleep((long) (avgDuration + random.nextGaussian() * stdDev));
        }

    }

}
