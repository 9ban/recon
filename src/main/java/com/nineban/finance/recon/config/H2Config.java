package com.nineban.finance.recon.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef="h2EntityManagerFactory",
        transactionManagerRef="h2TransactionManager")
public class H2Config {

    @Autowired
    @Qualifier("dataSource")
    public DataSource dataSource;

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean h2EntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages("com.nineban.finance.recon.repository.h2")
                .persistenceUnit("default")
                .build();
    }

    @Bean(name = "h2EntityManager")
    @Primary
    public EntityManager h2EntityManager(EntityManagerFactoryBuilder builder) {
        return h2EntityManagerFactory(builder).getObject().createEntityManager();
    }

    @Primary
    @Bean(name = "h2TransactionManager")
    public PlatformTransactionManager h2TransactionManager(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(h2EntityManagerFactory(builder).getObject());
    }

}
