package test.bmt.mdfarm;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@OpenAPIDefinition(info = @Info(title = "MD Farm API", version = "1.0.0"))
// --
@Slf4j
@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class,
        R2dbcAutoConfiguration.class})
public class ApiApplication {

    public static void main(String[] args) {
        collectDebugInfo();
        SpringApplication.run(ApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    private static void collectDebugInfo() {
        dumpEnvVariables();
        dumpProperties();
    }

    private static void dumpProperties() {
        var props = System.getProperties();
        log.info("--- System property entries: START");
        for (var entry : props.entrySet()) {
            log.info("PROP: <{}>:<{}>", entry.getKey(), entry.getValue());
        }
        log.info("--- System property entries: END");
    }

    private static void dumpEnvVariables() {
        var envs = System.getenv();
        log.info("--- System environment entries: START");
        for (var entry : envs.entrySet()) {
            log.info("ENV: <{}>:<{}>", entry.getKey(), entry.getValue());
        }
        log.info("--- System environment entries: END");
    }
}
