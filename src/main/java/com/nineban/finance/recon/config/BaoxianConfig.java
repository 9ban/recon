package com.nineban.finance.recon.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableJpaRepositories(basePackages = "com.nineban.finance.recon.repository.wb",
        entityManagerFactoryRef="baoxianEntityManagerFactory",
        transactionManagerRef="baoxianTransactionManager")
public class BaoxianConfig {

    @Autowired
    @Qualifier("baoxianDataSource")
    public DataSource  baoxianDataSource;

    @Bean
    public LocalContainerEntityManagerFactoryBean baoxianEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        Map<String,String> properties = new HashMap<>();
        properties.put("hibernate.dialect","org.hibernate.dialect.MySQL5Dialect");
        return builder
                .dataSource(baoxianDataSource)
                .packages("com.nineban.finance.recon.repository.wb")
                .persistenceUnit("baoxian")
                .properties(properties)
                .build();
    }

    @Bean(name = "baoxianEntityManager")
    public EntityManager baoxianEntityManager(EntityManagerFactoryBuilder builder) {
        return baoxianEntityManagerFactory(builder).getObject().createEntityManager();
    }

    @Bean(name = "baoxianTransactionManager")
    public PlatformTransactionManager baoxianTransactionManager(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(baoxianEntityManagerFactory(builder).getObject());
    }


}
