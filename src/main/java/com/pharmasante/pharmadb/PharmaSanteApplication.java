package com.pharmasante.pharmadb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entree de l'application PharmaSante.
 *
 * L'activation de la planification (@EnableScheduling) permet au service
 * {@code SyncService} d'executer la synchronisation automatique des donnees
 * entre les trois bases de pharmacies.
 *
 * Les auto-configurations JPA et DataSource de Spring Boot sont exclues car
 * le projet definit explicitement trois sources de donnees independantes
 * (Medina, Liberte, Almadies), chacune avec son propre EntityManagerFactory
 * et son propre TransactionManager (voir le package {@code config}).
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableScheduling
public class PharmaSanteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaSanteApplication.class, args);
    }
}
