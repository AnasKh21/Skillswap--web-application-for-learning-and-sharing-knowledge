# SkillSwap

Plateforme d'échange de compétences entre particuliers.  
Chaque utilisateur déclare ce qu'il **OFFRE** et ce qu'il **CHERCHE** ; un moteur de matching propose des échanges bilatéraux scorés par pertinence.

---

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Spring Boot 3.3, Java 21, Maven |
| Sécurité | Spring Security + JWT (jjwt 0.12) |
| Persistance | Spring Data JPA / Hibernate |
| Base de données | PostgreSQL 16 |
| Migrations | Flyway |
| Documentation API | springdoc-openapi / Swagger UI |
| Tests | JUnit 5 + Mockito |
| Frontend | React + Vite + TypeScript, React Router, Axios, Tailwind CSS |
| Conteneurs | Docker + Docker Compose |

---

## Architecture backend

Découpage par **domaine métier** (pas par couche technique) :

```
com.skillswap
├── auth        — inscription, login, refresh, JWT
├── user        — profil, compétences offertes / recherchées
├── matching    — moteur de score de compatibilité (isolé, testable seul)
├── session     — demandes de session, planification
├── rating      — notation mutuelle, réputation
└── common      — config Spring Security, filtre JWT, gestion d'exceptions
```

---

## Prérequis

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+ (développement frontend)

---

## Lancement rapide (Docker Compose)

```bash
# Depuis la racine du projet
docker compose up --build
```

| Service | URL |
|---|---|
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Frontend | http://localhost:3000 |
| PostgreSQL | localhost:5432 (user: skillswap / pass: skillswap) |

> **En production**, surcharger `JWT_SECRET` via variable d'environnement.

---

## Développement local (sans Docker)

### Backend

```bash
cd backend
# Démarrer PostgreSQL localement (ou via Docker seul)
docker compose up postgres -d

mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

---

## Tests

```bash
cd backend
mvn test
```

Les tests unitaires du moteur de matching sont dans :  
`src/test/java/com/skillswap/matching/MatchingServiceTest.java`

---

## Étapes de construction

| # | Étape | Statut |
|---|---|---|
| 1 | Squelette Maven + packages + docker-compose + application.yml | ✅ |
| 2 | Entités JPA + repositories + migrations Flyway (schéma + seed) | ⬜ |
| 3 | Auth complète (JWT) + tests | ⬜ |
| 4 | Moteur de matching + tests unitaires | ⬜ |
| 5 | Domaines user / session / rating | ⬜ |
| 6 | Frontend React | ⬜ |
| 7 | Dockerfiles finalisés + README final | ⬜ |
