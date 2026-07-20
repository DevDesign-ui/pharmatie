package com.pharmasante.pharmadb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Configuration JPA dediee a la Pharmacie Medina.
 *
 * - Scope les entites du package {@code com.pharmasante.pharmadb.entity.medina}
 * - relie les repositories du package {@code com.pharmasante.pharmadb.repository.medina}
 * - expose son propre EntityManagerFactory et PlatformTransactionManager afin
 *   que chaque pharmacie soit traitee de maniere totalement autonome.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages  = "com.pharmasante.pharmadb.repository.medina",
        entityManagerFactoryRef = "medinaEntityManagerFactory",
        transactionManagerRef   = "medinaTransactionManager"
)
public class MedinaConfig {

    public static final String EM  = "medinaEntityManagerFactory";
    public static final String TM  = "medinaTransactionManager";

    @Bean(name = EM)
    @Primary
    public LocalContainerEntityManagerFactoryBean medinaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier(DataSourcesConfig.MEDINA_DS) DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.pharmasante.pharmadb.entity.medina")
                .persistenceUnit("medina")
                .build();
    }

    @Bean(name = TM)
    @Primary
    public PlatformTransactionManager medinaTransactionManager(
            @Qualifier(EM) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
