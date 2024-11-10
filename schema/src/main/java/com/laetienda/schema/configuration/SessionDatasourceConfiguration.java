package com.laetienda.schema.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
public class SessionDatasourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties getSessionDatasourceProperties(){
        return new DataSourceProperties();
    }

    @Bean
    @SpringSessionDataSource
    public DataSource getSessionDataSource(){
        return getSessionDatasourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate getSessionJdbcTemplate(@Qualifier("getSessionDataSource") DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}
