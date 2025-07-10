package com.market.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();

        Map<Object, Object> dataSources = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.postgresql.Driver");
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/my_database_" + i);
            config.setUsername("testuser");
            config.setPassword("testpass");
            config.setMaximumPoolSize(8); // Set pool size as needed
            HikariDataSource dataSource = new HikariDataSource(config);
            dataSources.put("my_database_" + i, dataSource);
        }

        dynamicDataSource.setTargetDataSources(dataSources);

        DatabaseContextHolder.setCurrentDatabase("my_database_1");

        return dynamicDataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dynamicDataSource) {
        return builder
                .dataSource(dynamicDataSource)
                .packages("com.market.streamline.entity") // Replace with your entity package
                .build();
    }

    @Bean
    public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}