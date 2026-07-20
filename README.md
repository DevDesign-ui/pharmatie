# PharmaSante - Gestion distribuee des stocks de medicaments

Projet realisé dans le cadre du module **P8S1 MIL UNCHK - Base de Donnees Avancees**
(Session de rattrapage, Dr Mahamadou TOURE).

L'application met en oeuvre une architecture de base de donnees distribuee
permettant de gerer le stock de medicaments de trois pharmacies appartenant
a la chaine PharmaSante : **Medina**, **Liberte**, **Almadies**.

Chaque pharmacie possede sa propre base PostgreSQL et fonctionne de maniere
autonome. Une application centrale Spring Boot consolide les donnees, offre
une vue globale du stock et synchronise automatiquement les informations
entre les sites.

---

## 1. Stack technique (technologies imposees)

| Domaine          | Technologie                     |
|------------------|---------------------------------|
| Langage          | Java 21                         |
| Framework        | Spring Boot 3.3.x               |
| Persistance      | Spring Data JPA (Multi-DataSource) |
| Base de donnees  | MySQL / MariaDB (XAMPP)         |
| Moteur de vues   | Thymeleaf                       |
| Build            | Maven                           |
| Planification    | `@Scheduled` (Spring)           |

## 2. Architecture

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
| PostgreSQL|    | PostgreSQL   |    | PostgreSQL  |
| Medina    |    | Liberte      |    | Almadies    |
+-----------+    +--------------+    +-------------+
```

- Chaque pharmacie est materialisee par un `DataSource` Spring dedie,
  possedant son propre `EntityManagerFactory` et son propre
  `PlatformTransactionManager`.
- Trois entites JPA (`MedicamentMedina`, `MedicamentLiberte`,
  `MedicamentAlmadies`) heritent d'une super-classe mappee
  `AbstractMedicament`. Elles sont scannees par leur persistence unit
  respectif, ce qui permet de manipuler chaque base de facon autonome.
- Un DTO neutre (`MedicamentDto`) sert de structure d'echange unifiee pour
  la consolidation, la synchronisation et les vues Thymeleaf.
- Deux services orchestrant la logique metier :
  - `MultiStockService` : CRUD, recherche, consolidation.
  - `SyncService` : synchronisation planifiee + resolution de conflits.

## 3. Fonctionnalites

### Gestion des medicaments (par pharmacie)
- Ajouter un medicament
- Modifier un medicament
- Supprimer un medicament
- Rechercher un medicament par code (dans les trois bases)
- Consulter tous les medicaments d'une pharmacie

### Gestion des stocks
- Stock de la Pharmacie Medina
- Stock de la Pharmacie Liberte
- Stock de la Pharmacie Almadies

### Vue consolidee
- Fusion des stocks des trois pharmacies dans une seule liste

### Synchronisation automatique
- Service `SyncService` execute toutes les minutes via `@Scheduled`
- Parcourt les trois bases, compare les medicaments par code
- Copie les medicaments absents dans les autres bases
- Met a jour les medicaments modifies
- **Resolution de conflits** : si un meme code possede des versions
  differentes, la version dont `updatedAt` est le plus recent est conservee.

## 4. Installation et execution

### Pre-requis
- Java 21
- Maven 3.9+ (ou utiliser le wrapper `./mvnw` fourni)
- **XAMPP** (MySQL / MariaDB) installe et demarre

### Etape 1 - Demarrer MySQL via XAMPP

Lancer le panneau de controle XAMPP et demarrer le module **MySQL**.
Par defaut, le serveur MySQL ecoute sur `localhost:3306` avec
l'utilisateur `root` et **aucun mot de passe**.

### Etape 2 - Creer les trois bases

Au choix :

- En ligne de commande (depuis la racine du projet) :
  ```bash
  mysql -u root < sql/01_create_medina.sql
  mysql -u root < sql/02_create_liberte.sql
  mysql -u root < sql/03_create_almadies.sql
  ```
- Ou depuis **phpMyAdmin** : onglet "SQL" puis copier-coller le contenu
  de chacun des trois scripts l'un apres l'autre.

Cela cree les bases `pharmacie_medina`, `pharmacie_liberte`,
`pharmacie_almadies` et y insere des donnees de demonstration.

### Etape 3 - Configurer les connexions

Editer `src/main/resources/application.properties` et adapter les
identifiants MySQL si necessaire. Par defaut, les valeurs correspondent
a une installation XAMPP standard :
- URL : `jdbc:mysql://localhost:3306/pharmacie_<nom>?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
- utilisateur : `root`
- mot de passe : (vide)

Si vous avez defini un mot de passe root dans XAMPP, renseignez-le dans
les trois `pharmasante.datasource.<nom>.password`.

### Etape 4 - Lancer l'application

```bash
./mvnw spring-boot:run
```

ou, si Maven est installe systemiquement :

```bash
mvn spring-boot:run
```

### Etape 5 - Acceder a l'interface

Ouvrir <http://localhost:8080>

## 5. Structure du projet

```
pharma-stock/
├── pom.xml
├── sql/
│   ├── 00_init_all.sql
│   ├── 01_create_medina.sql
│   ├── 02_create_liberte.sql
│   └── 03_create_almadies.sql
├── src/main/java/com/pharmasante/pharmadb/
│   ├── PharmaSanteApplication.java
│   ├── config/
│   │   ├── DataSourcesConfig.java
│   │   ├── MedinaConfig.java
│   │   ├── LiberteConfig.java
│   │   └── AlmadiesConfig.java
│   ├── entity/
│   │   ├── AbstractMedicament.java
│   │   ├── medina/MedicamentMedina.java
│   │   ├── liberte/MedicamentLiberte.java
│   │   └── almadies/MedicamentAlmadies.java
│   ├── model/
│   │   ├── Pharmacie.java
│   │   └── MedicamentDto.java
│   ├── mapper/
│   │   └── MedicamentMapper.java
│   ├── repository/
│   │   ├── medina/MedicamentMedinaRepository.java
│   │   ├── liberte/MedicamentLiberteRepository.java
│   │   └── almadies/MedicamentAlmadiesRepository.java
│   ├── service/
│   │   ├── MultiStockService.java
│   │   └── SyncService.java
│   └── web/
│       └── PharmaController.java
└── src/main/resources/
    ├── application.properties
    ├── static/css/style.css
    └── templates/
        ├── fragments.html
        ├── dashboard.html
        ├── pharmacie.html
        ├── consolide.html
        ├── recherche.html
        └── form.html
```

## 6. Points techniques notables

### Multi-DataSource
Les `DataSource` sont declares dans `DataSourcesConfig`. Chaque
configuration specialisee (`MedinaConfig`, `LiberteConfig`,
`AlmadiesConfig`) construit son `EntityManagerFactory` en ciblant le
package d'entites et de repositories qui le concerne, puis expose son
propre `PlatformTransactionManager`. Les repositories sont ainsi cables
automatiquement sur la bonne base.

### Transactions
Les services utilisent `@Transactional(transactionManager = "...")` pour
preciser explicitement la transaction a utiliser. Chaque operation sur une
pharmacie s'execute dans sa propre transaction, sans recourir a une
transaction distribuee (XA).

### Synchronisation
- `SyncService.synchroniser()` :
  1. charge tous les medicaments des trois bases ;
  2. construit une map "code -> version la plus recente" en comparant
     `updatedAt` (conflits comptabilises) ;
  3. replique cette version de reference dans chaque base : insertion si
     absent, mise a jour si `updatedAt` cible < reference, rien sinon.
- Declenchement : `@Scheduled(fixedRateString = "...")` toutes les 60 s
  par defaut. Un bouton "Synchroniser maintenant" permet aussi un
  declenchement manuel depuis l'interface.

### Resolution de conflits
Deux medicaments sont consideres identiques s'ils ont le meme `code`.
En cas de conflit (versions differentes), le champ `updatedAt` est
compare : la version la plus recente est conservee et replicate.

## 7. Demo

Une fois l'application demarree :
1. Ouvrir le tableau de bord : <http://localhost:8080>
2. Visiter chaque pharmacie, ajouter / modifier / supprimer des medicaments.
3. Consulter la vue consolidee.
4. Attendre 1 minute (ou cliquer sur "Synchroniser maintenant") et observer
   la replication des medicaments entre les bases.
5. Modifier un meme medicament dans deux pharmacies avec des valeurs
   differentes : la version la plus recente l'emporte a la synchronisation
   suivante.
