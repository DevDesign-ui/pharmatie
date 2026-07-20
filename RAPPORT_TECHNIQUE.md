# Rapport technique - Projet BDD Avancees

**Projet :** Gestion distribuee des stocks de medicaments (PharmaSante)
**Module :** P8S1 MIL UNCHK - Session de rattrapage
**Encadrant :** Dr Mahamadou TOURE

---

## 1. Description de l'architecture retenue

Le projet met en oeuvre une architecture de bases de donnees distribuee
ou chaque pharmacie de la chaine PharmaSante dispose de sa propre base
MySQL (via XAMPP / MariaDB), independante et autonome. Une application
centrale Spring Boot se connecte simultanement aux trois bases via le
mecanisme **Multi-DataSource** de Spring Data JPA.

### Vue d'ensemble

```
            +-----------------------+
            |   Application Spring  |
            |  (PharmaSante)        |
            +-----------+-----------+
                        |
   +--------------------+--------------------+
   |                    |                    |
   v                    v                    v
+-----------+    +--------------+    +-------------+
|   MySQL   |    |    MySQL     |    |    MySQL    |
| Medina    |    | Liberte      |    | Almadies    |
+-----------+    +--------------+    +-------------+
```

### Composants principaux

1. **DataSources** : trois `DataSource` HikariCP, un par pharmacie, declares
   dans `DataSourcesConfig` et configures via `application.properties`.
   Chaque `DataSource` pointe vers une base MySQL dediee (`pharmacie_medina`,
   `pharmacie_liberte`, `pharmacie_almadies`) hebergee par le serveur MySQL
   de XAMPP (`localhost:3306`).

2. **EntityManagerFactory / TransactionManager** : pour chaque pharmacie,
   une classe de configuration dediee (`MedinaConfig`, `LiberteConfig`,
   `AlmadiesConfig`) construit son propre `EntityManagerFactory` (qui
   scanne uniquement le package d'entites qui le concerne) et expose son
   propre `PlatformTransactionManager`. Les repositories du package
   correspondant sont automatiquement cables sur la bonne base via
   `@EnableJpaRepositories`.

3. **Entites** : une super-classe mappee `AbstractMedicament` centralise
   les attributs communs (`code`, `nom`, `prix`, `quantite`,
   `updatedAt`, etc.). Trois entites concretes heritent de cette classe
   (`MedicamentMedina`, `MedicamentLiberte`, `MedicamentAlmadies`), une
   par pharmacie. Le scan par persistence unit permet a chaque base
   d'avoir son propre schema sans conflit de noms.

4. **DTO** : `MedicamentDto` est un objet neutre, independant de toute
   source de donnees. Il sert de structure d'echange unifiee pour la
   consolidation, la synchronisation et les vues Thymeleaf. Un mapper
   (`MedicamentMapper`) assure les conversions entites <-> DTO.

5. **Services** :
   - `MultiStockService` : operations CRUD, recherche par code,
     consolidation des trois stocks.
   - `SyncService` : synchronisation planifiee + resolution de conflits.

6. **Couche web** : un controleur Spring MVC + templates Thymeleaf pour
   le tableau de bord, la consultation par pharmacie, la vue consolidee,
   la recherche et le formulaire d'ajout / modification.

## 2. Choix techniques realises

### Multi-DataSource sans XA
Plutot que d'utiliser une transaction distribuee (XA / two-phase commit),
chaque operation sur une pharmacie s'execute dans la transaction de la
base concernee, designee explicitement via
`@Transactional(transactionManager = "...")`. Ce choix privilegie la
simplicite et l'autonomie des pharmacies : si une base est indisponible,
les autres continuent de fonctionner.

### Identifiant fonctionnel `code`
Le code du medicament est l'identifiant fonctionnel utilise pour comparer
les medicaments entre les bases. Il est declare `UNIQUE` dans chaque
table. L'identifiant technique `id` (auto-incrmente) reste local a chaque
base et n'est pas utilise pour la synchronisation.

### Trace temporelle `updatedAt`
Chaque entite possede `createdAt` et `updatedAt`, renseignes
automatiquement par les callbacks JPA `@PrePersist` et `@PreUpdate`.
`updatedAt` est la cle de la resolution de conflits : en cas de versions
differentes pour un meme code, la version la plus recente est conservee.

### Planification `@Scheduled`
L'annotation `@EnableScheduling` active au niveau de l'application permet
au `SyncService` d'executer la synchronisation toutes les 60 secondes
(`@Scheduled(fixedRateString = "${pharmasante.sync.fixed-rate-ms:60000}")`).
Le rate est configurable et la synchronisation peut etre desactivee via
`pharmasante.sync.enabled`. Un bouton permet aussi un declenchement
manuel.

### Strategie de schema
`spring.jpa.hibernate.ddl-auto=update` permet a Hibernate de creer /
mettre a jour les tables au demarrage, ce qui facilite la mise en route.
Les scripts SQL fournis dans `sql/` constituent la version de reference
et permettent de creer les bases et d'inserer des donnees de
demonstration manuellement.

## 3. Algorithme de synchronisation

```
1. Charger tous les medicaments des trois bases.
   -> trois maps : { code -> MedicamentDto }

2. Construire la "version de reference" pour chaque code :
   pour chaque map source :
       pour chaque medicament (code, dto) :
           si le code est nouveau -> ajouter a la reference
           sinon :
               si dto.updatedAt != reference.updatedAt -> conflit++
               si dto.updatedAt > reference.updatedAt -> remplacer

3. Replication de la version de reference dans chaque base :
   pour chaque base (medina, liberte, almadies) :
       si le code n'existe pas -> INSERT (copie++) 
       sinon si base.updatedAt < reference.updatedAt -> UPDATE (maj++)
       sinon -> ne rien faire
```

### Exemple de resolution de conflit
Le medicament `MED-002` (Amoxicilline) existe a Medina (prix 3200,
updatedAt = T) et a Liberte (prix 3500, updatedAt = T+1 min). Lors de la
synchronisation, la version de Liberte, plus recente, devient la
reference. La base de Medina est mise a jour (prix = 3500) et la base des
Almadies, qui ne possedait pas ce code, recoit une copie.

## 4. Captures d'ecran

> A joindre dans la version finale du rapport :
> 1. Tableau de bord - vue d'ensemble avec les trois pharmacies.
> 2. Stock d'une pharmacie - liste des medicaments.
> 3. Formulaire d'ajout / modification d'un medicament.
> 4. Vue consolidee - fusion des trois stocks.
> 5. Recherche par code - resultats dans les trois bases.
> 6. Apres synchronisation - logs applicatifs montrant le nombre de
>    copies, mises a jour et conflits resolus.

## 5. Principales difficultes rencontres

1. **Configuration Multi-DataSource sous Spring Boot** : Spring Boot ne
   peut pas auto-configurer plusieurs `EntityManagerFactory`. Il a fallu
   desactiver l'auto-configuration JPA implicite et definir manuellement,
   pour chaque pharmacie, son `DataSource`, son
   `LocalContainerEntityManagerFactoryBean` et son
   `JpaTransactionManager`, en veillant a bien separer les packages
   d'entites et de repositories pour eviter tout conflit de scanning.

2. **Scanning des entites par persistence unit** : chaque
   `EntityManagerFactory` ne doit scanner que le sous-package d'entites
   qui le concerne (`entity.medina`, `entity.liberte`,
   `entity.almadies`). Une entite concrete par pharmacie herite d'une
   super-classe `@MappedSuperclass` commune pour factoriser les
   attributs.

3. **Transactions croisees** : les services doivent preciser
   explicitement le `transactionManager` a utiliser dans chaque methode,
   sinon Spring utilise le `TransactionManager` primaire par defaut, ce
   qui produit des erreurs d'entity manager au runtime.

4. **Comparaison de `updatedAt` pouvant etre null** : les dates etant
   initialisees par les callbacks JPA, elles peuvent etre null sur des
   lignes importees manuellement. Une logique defensive substitue
   `LocalDateTime.MIN` a null avant comparaison.

5. **Idempotence de la synchronisation** : pour eviter de copier / mettre
   a jour inutilement a chaque cycle, l'algorithme ne declenche une
   action que si la version de reference est strictement plus recente
   que la version cible (`r.isAfter(c)`). Apres stabilisation, le
   rapport affiche 0 copie et 0 mise a jour.

6. **Boucle de synchronisation sans doublons** : un medicament ne doit
   pas etre re-copie dans une base ou il existe deja. La methode
   `findByCode` verifie l'existence avant toute insertion.

## 6. Conclusion

Le projet realise repond a l'integralite du cahier des charges :

- gestion autonome des stocks de trois pharmacies independantes ;
- interface web de consultation (tableau de bord, stock par pharmacie,
  vue consolidee, recherche) ;
- operations CRUD completes sur les medicaments ;
- synchronisation automatique toutes les minutes via `@Scheduled` ;
- resolution de conflits basee sur `updatedAt`.

Les notions principales du module de Bases de Donnees Avancees sont
couvertes : gestion de plusieurs bases, configuration Multi-DataSource
avec Spring Boot, synchronisation de donnees, resolution de conflits,
planification de taches et consolidation de donnees.
