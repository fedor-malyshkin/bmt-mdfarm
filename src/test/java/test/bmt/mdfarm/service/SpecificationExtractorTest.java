package test.bmt.mdfarm.service;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spockframework.util.IoUtil;
import org.springframework.core.io.ClassPathResource;

class SpecificationExtractorTest {

    @SneakyThrows
    @Test
    public void parseSpec() {
        var resource = new ClassPathResource("spec-sample.html");
        var html = IoUtil.getText(resource.getInputStream());
        var testable = new SpecificationExtractor();
        var result = testable.createUpdatedFromHtml(html);

        Assertions.assertThat(result.technology()).isEqualTo("2G, 3G, 4G");
        Assertions.assertThat(result.bands2g()).isEqualTo("GSM 850/900/1800/1900");
        Assertions.assertThat(result.bands3g()).isEqualTo("UMTS 800/850/900/1700/1800/1900/2100");
        Assertions.assertThat(result.bands4g()).isEqualTo("LTE Cat4 (Bands 1, 3, 5, 7, 8, 9, 19, 20, 28) TD-LTE (Bands 41)");

    }

}