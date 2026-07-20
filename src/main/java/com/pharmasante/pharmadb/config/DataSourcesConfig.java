package com.pharmasante.pharmadb.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Definitions centralisees des trois sources de donnees representant
 * les trois pharmacies de la chaine PharmaSante.
 *
 * Chaque pharmacie possede sa propre base MySQL (XAMPP) et fonctionne de
 * maniere autonome. Les DataSource sont ici declares puis relies a leur
 * propre EntityManagerFactory et PlatformTransactionManager dans les
 * classes de configuration specialisees (MedinaConfig, LiberteConfig,
 * AlmadiesConfig).
 */
@Configuration
@EntityScan(basePackages = "com.pharmasante.pharmadb.entity")
public class DataSourcesConfig {

    public static final String MEDINA_DS    = "medinaDataSource";
    public static final String LIBERTE_DS   = "liberteDataSource";
    public static final String ALMADIES_DS  = "almadiesDataSource";

    /**
     * Builder reuse par les trois configurations JPA pour construire leur
     * EntityManagerFactory. Spring Boot ne fournit plus ce bean depuis qu'on
     * a exclu {@code HibernateJpaAutoConfiguration}, on le declare donc ici.
     */
    @Bean
    @Primary
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        java.util.Map<String, String> jpaProperties = java.util.Map.of(
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.format_sql", "true",
                "hibernate.jdbc.time_zone", "UTC"
        );
        return new EntityManagerFactoryBuilder(
                new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter(),
                jpaProperties,
                null);
    }

    @Bean(name = MEDINA_DS)
    @Primary
    @ConfigurationProperties(prefix = "pharmasante.datasource.medina")
    public DataSource medinaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = LIBERTE_DS)
    @ConfigurationProperties(prefix = "pharmasante.datasource.liberte")
    public DataSource liberteDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = ALMADIES_DS)
    @ConfigurationProperties(prefix = "pharmasante.datasource.almadies")
    public DataSource almadiesDataSource() {
        return DataSourceBuilder.create().build();
    }
}
