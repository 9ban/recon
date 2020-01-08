package com.nineban.finance.recon.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.cloud.task.configuration.DefaultTaskConfigurer;
import org.springframework.cloud.task.configuration.TaskConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.baoxian")
    public DataSource  baoxianDataSource(){
        return DataSourceBuilder.create().build();
    }


    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.h2")
    public DataSource dataSource(){
        return DataSourceBuilder.create().build();
    }


    @Bean
    public TaskConfigurer taskConfigurer()
    {
        return new DefaultTaskConfigurer(dataSource());
    }

}
