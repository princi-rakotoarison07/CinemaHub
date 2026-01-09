# CinemaHub

Monorepo contenant :

- `cinema-management` : backend **Spring Boot** (Java 17) + PostgreSQL
- `cinema-front` : frontend **Vue 3 + Vite** (template NiceAdmin)

## Prérequis

- Java 17
- Maven (ou Maven Wrapper si ajouté plus tard)
- Node.js (LTS recommandé)
- PostgreSQL

## Base de données (PostgreSQL)

Créer la base  //  configurena mitovy anle anla user sy pass:

- DB : `cinema_management`
- user : `postgres`
- pass : `postgres`

Le backend est configuré dans :

- `cinema-management/src/main/resources/application.properties`

Tu peux exécuter tes scripts SQL depuis :

- `cinema-management/database/*.sql`

## Lancer le backend (cinema-management)

Depuis `cinema-management` :

```bash
mvn spring-boot:run
```

Notes :

- Le backend utilise un context-path : `/cinema-management`
- Exemple d’API :
  - `GET http://localhost:8080/cinema-management/api/tests`

### Configuration backend (`application.properties`)

Le fichier suivant est **ignoré par git** (config locale, user/pass DB, etc.) :

- `cinema-management/src/main/resources/application.properties`

Exemple de configuration tena izy :

```properties
spring.application.name=cinema-management
server.servlet.context-path=/cinema-management

spring.datasource.url=jdbc:postgresql://localhost:5432/cinema_management
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### CORS

Le CORS global est configuré dans :

- `cinema-management/src/main/java/com/cinema/management/config/WebConfig.java`

Par défaut, le front en dev est autorisé sur :

- `http://localhost:5173`

## Lancer le frontend (cinema-front)

Depuis `cinema-front` :

```bash
npm install
npm run dev
```

### Base path (front)

Le front est configuré pour être servi sous :

- `http://localhost:5173/cinema-front/`

Routes principales :

- Dashboard : `http://localhost:5173/cinema-front/`
- Test (liste) : `http://localhost:5173/cinema-front/tests`
- Test (nouveau) : `http://localhost:5173/cinema-front/tests/new`

### URL du backend (front)

L’URL du backend est centralisée via une variable Vite :

- `cinema-front/.env`

Exemple :

```
VITE_API_BASE_URL=http://localhost:8080/cinema-management
```

Après modification du `.env`, **redémarrer Vite**.

## Endpoints (module Test)

- `GET /cinema-management/api/tests`
- `GET /cinema-management/api/tests/{id}`
- `POST /cinema-management/api/tests`
- `PUT /cinema-management/api/tests/{id}`
- `DELETE /cinema-management/api/tests/{id}`

## Git

Un `.gitignore` est présent à la racine (Java/Maven + Node/Vite + IDE).

Recommandation : ne pas versionner les fichiers `.env` contenant des secrets.
