package test.bmt.mdfarm.service;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import test.bmt.mdfarm.model.dto.MobileDeviceSpecDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public
class SpecificationExtractor {
    public record Specification (String technology,
                                 String bands2g,
                                 String bands3g,
                                 String bands4g) {}

    private final IntervalFunction intervalWithCustomExponentialBackoff = IntervalFunction
            .ofExponentialBackoff(Duration.ofMillis(50), 2);
    // with 50 ms the last try will be done in 6.4 seconds (initial_time * 2^(tries-1))
    private final RetryConfig config = RetryConfig.<Boolean>custom()
            .maxAttempts(7)
            .failAfterMaxAttempts(true)
            .retryExceptions(Exception.class)
            .intervalFunction(intervalWithCustomExponentialBackoff)
            .build();
    private final HttpClient httpClient = newHttpClient();

    Optional<Specification> extract(MobileDeviceSpecDto s) {
        var retry = Retry.of("X", config);
        try {
            Supplier<Specification> supplier = () -> createUpdated(s.source());
            return Optional.of(retry.executeSupplier(supplier));
        } catch (Exception ex) {
            log.error("Exception while updating specification: <{}>", ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(1500))
                .build();
    }


    @SneakyThrows
    private Specification createUpdated(String source) {
        log.info("An attempt to read specification from <{}>", source);
        var request = HttpRequest.newBuilder()
                .uri(new URI(source))
                .timeout(Duration.ofMillis(4000))
                .GET()
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpStatus.OK.value()) {
            log.warn("Unsuccessfully called <{}> -  status code was <{}>", source, response.statusCode());
            throw new IllegalStateException();
        }
        return createUpdatedFromHtml(response.body());
    }

    Specification createUpdatedFromHtml(@NonNull String body) {
        Document doc = Jsoup.parse(body);
        var trs = doc.select("table#tb_specs tbody tr td");
        var texts = trs.eachText();
        var technology = getTr("Network technology", texts);
        var bands2g = getTr("Primary 2G network", texts);
        var bands3g = getTr("Primary 3G network", texts);
        var bands4g = getTr("Primary 4G network", texts);
        return new Specification(technology,
                bands2g,
                bands3g,
                bands4g);
    }

    private String getTr(String header, List<String> trs) {
        var index = -1;
        for (int i = 0; i < trs.size(); i++)
            if (StringUtils.containsAnyIgnoreCase(trs.get(i), header)) {
                index = i;
                break;
            }

        if (index != -1)
            return trs.get(index + 1);
        else return null;
    }

}
