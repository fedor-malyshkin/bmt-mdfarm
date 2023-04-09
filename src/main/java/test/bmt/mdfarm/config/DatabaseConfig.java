package test.bmt.mdfarm.config;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {

    @Bean
    public org.jooq.Configuration getJooqConfiguration(DataSource dataSource) {
        return new DefaultConfiguration()
                .set(SQLDialect.POSTGRES)
                .derive(dataSource);
    }

    @Bean
    public DSLContext getJooqDslContext(org.jooq.Configuration configuration) {
        return DSL.using(configuration);
    }


}
