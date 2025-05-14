package com.laetienda.schema.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "getSchemaEntityManagerFactory",
        transactionManagerRef = "getSchemaTransactionManager",
        basePackages = {"com.laetienda.schema.repository"}
)
@EntityScan(basePackages={"com.laetienda.model.schema"})
@ComponentScan(basePackages={"com.laetienda.model.schema"})
public class SchemaDatasourceConfiguration {

    @Autowired Environment env;

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean getSchemaEntityManagerFactory(
            @Qualifier("getSchemaDatasource") DataSource dataSource,
            EntityManagerFactoryBuilder builder)
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.schema-generation.database.action", env.getProperty("schema-generation.database.action"));

        return builder
                .dataSource(dataSource)
                .packages("com.laetienda.schema.repository","com.laetienda.model.schema")
                .persistenceUnit("schema")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager getSchemaTransactionManager(
            @Qualifier("getSchemaEntityManagerFactory") LocalContainerEntityManagerFactoryBean getSchemaEntityManagerFactory)
    {
        return new JpaTransactionManager(Objects.requireNonNull(getSchemaEntityManagerFactory.getObject()));
    }

    @Bean
    @ConfigurationProperties("schema.datasource")
    public DataSourceProperties getSchemaDatasourceProperties(){
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource getSchemaDatasource(){
        return getSchemaDatasourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate getJdbcTemplate(@Qualifier("getSchemaDatasource") DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}