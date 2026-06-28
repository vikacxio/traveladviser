package com.kahanchale.splitexpense.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class TravelAppDataSourceConfig {

    @Bean(name = "travelAppDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.travel-app")
    public DataSource travelAppDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "travelAppJdbcTemplate")
    public JdbcTemplate travelAppJdbcTemplate(@Qualifier("travelAppDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
