package com.pharmasante.pharmadb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Configuration JPA dediee a la Pharmacie Almadies.
 *
 * - Scope les entites du package {@code com.pharmasante.pharmadb.entity.almadies}
 * - relie les repositories du package {@code com.pharmasante.pharmadb.repository.almadies}
 * - expose son propre EntityManagerFactory et PlatformTransactionManager afin
 *   que chaque pharmacie soit traitee de maniere totalement autonome.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages  = "com.pharmasante.pharmadb.repository.almadies",
        entityManagerFactoryRef = "almadiesEntityManagerFactory",
        transactionManagerRef   = "almadiesTransactionManager"
)
public class AlmadiesConfig {

    public static final String EM  = "almadiesEntityManagerFactory";
    public static final String TM  = "almadiesTransactionManager";

    @Bean(name = EM)
    public LocalContainerEntityManagerFactoryBean almadiesEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier(DataSourcesConfig.ALMADIES_DS) DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.pharmasante.pharmadb.entity.almadies")
                .persistenceUnit("almadies")
                .build();
    }

    @Bean(name = TM)
    public PlatformTransactionManager almadiesTransactionManager(
            @Qualifier(EM) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
