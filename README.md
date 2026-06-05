# SkillSwap

Plateforme d'échange de compétences entre particuliers — chaque utilisateur déclare ce qu'il **enseigne** et ce qu'il **veut apprendre**. Un moteur de matching propose les échanges bilatéraux les plus pertinents, triés par score.

> Projet d'apprentissage Java Spring Boot + React, construit pas à pas.

---

## Démo rapide

```
Alice enseigne Java  ↔  Bob enseigne Guitar
Alice veut apprendre Guitar  ↔  Bob veut apprendre Java
→ Score élevé : échange mutuel parfait
```

---

## Stack

| Couche | Technologie |
|---|---|
| Backend | Spring Boot 3.3 · Java 21 · Maven |
| Sécurité | Spring Security · JWT (jjwt 0.12) · HttpOnly cookies |
| Persistance | Spring Data JPA · Hibernate · PostgreSQL 16 |
| Migrations | Flyway |
| Tests | JUnit 5 · Mockito · H2 (tests d'intégration) |
| API Docs | springdoc-openapi · Swagger UI |
| Frontend | React 18 · Vite · Tailwind CSS · Framer Motion · Axios |
| Conteneurs | Docker · Docker Compose |

---

## Architecture backend

Découpage par **domaine métier** :

```
com.skillswap
├── auth        — register, login, refresh token, logout (JWT en cookies HttpOnly)
├── user        — profil, skills offerts/voulus, upload avatar/bannière
├── matching    — moteur de score de compatibilité (isolé, testable seul)
├── session     — demandes d'échange, planification, complétion
├── rating      — notation mutuelle après session
├── media       — stockage fichiers uploadés (avatars, bannières)
└── common      — enums partagés (SkillType, SessionStatus…)
```

---

## Moteur de matching

Le score entre deux utilisateurs A et B est calculé en pur Java après une requête SQL de pré-filtrage :

```
score(A, B) = (theyTeachMe + iTeachThem) × ratingFactor

theyTeachMe  = | WANTED(A) ∩ OFFERED(B) |   — nombre de skills que B peut enseigner à A
iTeachThem   = | OFFERED(A) ∩ WANTED(B) |   — nombre de skills que A peut enseigner à B
ratingFactor = 0.5 + (B.averageRating / 10)  — facteur de réputation (0.5 → 1.0)
               0.75 si B n'a pas encore de note
```

Seuls les candidats avec `theyTeachMe > 0` apparaissent (B doit pouvoir apprendre quelque chose à A).
Les skills de chaque candidat sont chargés en **une seule requête batch** (`WHERE user_id IN (...)`) pour éviter le problème N+1.

---

## Architecture frontend

```
src/
├── api/          — un fichier par domaine (auth, users, skills, matching, media, feed)
├── hooks/        — useMatching (état loading/error/reload isolé du composant)
├── components/
│   ├── cards/    — SwipeCard (drag & drop Tinder-style)
│   ├── layout/   — Sidebar, BottomNav
│   └── ui/       — Avatar, Button, SkillPicker, ImageUpload, StarRating…
├── context/      — AuthContext (user, login, register, logout, updateUser)
├── pages/        — DiscoverPage, ProfilePage, RequestsPage, SessionsPage, AuthPage
└── utils/        — mappers.js (transformations API → UI)
```

---

## Lancement rapide

### Avec Docker Compose (tout en un)

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 (skillswap / skillswap) |

### En développement local (recommandé)

```bash
# 1. Base de données uniquement dans Docker
docker compose up -d postgres

# 2. Backend
cd backend && mvn spring-boot:run

# 3. Frontend (autre terminal)
cd frontend && npm install && npm run dev   # → http://localhost:5173
```

### Peupler la base avec des données de test

```bash
bash seed.sh
```

Crée 6 utilisateurs fictifs (Alice, Bob, Carol, David, Emma, Farid) avec des skills complémentaires pour tester le moteur de matching. Le script est idempotent.

---

## Tests

```bash
cd backend && mvn test
```

**51 tests** en tout :

| Fichier | Type | Ce qu'il teste |
|---|---|---|
| `MatchingServiceTest` | Unitaire (Mockito) | Calcul du score, tri, enrichissement des skills |
| `MatchingRepositoryTest` | Intégration (H2) | Requêtes SQL de matching, comptage intersections |
| `UserServiceTest` | Unitaire | Création de compte, gestion des skills |
| `SessionServiceTest` | Unitaire | Cycle de vie demande → session → complétion |
| `RatingServiceTest` | Unitaire | Notation, mise à jour de la note moyenne |

---

## Endpoints principaux

```
POST   /api/auth/register           — créer un compte
POST   /api/auth/login              — se connecter (cookie JWT)
POST   /api/auth/logout             — déconnexion
POST   /api/auth/refresh            — renouveler le token

GET    /api/matching/candidates     — liste des candidats scorés pour l'utilisateur connecté

GET    /api/users/me                — profil connecté
PUT    /api/users/me                — modifier displayName / bio
POST   /api/users/me/avatar         — uploader une photo de profil (multipart, max 5 MB)
POST   /api/users/me/banner         — uploader une bannière (multipart, max 5 MB)
GET    /api/users/me/skills         — mes compétences
POST   /api/users/me/skills         — ajouter une compétence
DELETE /api/users/me/skills/{id}    — retirer une compétence

GET    /api/skills                  — catalogue global des skills
GET    /api/skills?category=Music   — filtrer par catégorie
POST   /api/skills                  — créer un nouveau skill

POST   /api/sessions/requests       — envoyer une demande d'échange
POST   /api/sessions/requests/{id}/accept  — accepter
POST   /api/sessions/requests/{id}/decline — refuser
POST   /api/sessions/{id}/complete  — marquer comme terminée
POST   /api/ratings                 — noter un partenaire après session
```

Documentation complète : http://localhost:8080/swagger-ui.html

---

## Variables d'environnement

| Variable | Défaut | Usage |
|---|---|---|
| `DB_HOST` | `localhost` | Hôte PostgreSQL |
| `DB_PORT` | `5432` | Port PostgreSQL |
| `DB_NAME` | `skillswap` | Nom de la base |
| `DB_USER` | `skillswap` | Utilisateur |
| `DB_PASSWORD` | `skillswap` | Mot de passe |
| `JWT_SECRET` | valeur de dev | **À changer en production** (min 256 bits) |
| `VITE_API_URL` | `http://localhost:8080` | URL du backend pour le frontend |

---

## Fonctionnalités implémentées

- [x] Authentification JWT (access token + refresh token, cookies HttpOnly)
- [x] Gestion des compétences avec niveaux 1-5 et catégories
- [x] Moteur de matching bilatéral scoré
- [x] Upload de photo de profil et bannière
- [x] Interface swipe (style Tinder) pour découvrir les profils
- [x] Demandes d'échange, sessions planifiées, notations
- [x] Swagger UI pour explorer l'API
- [x] 51 tests (unitaires + intégration)
- [x] Seed script pour les données de développement

## À venir

- [ ] Stockage des médias sur Cloudinary (remplace le stockage local)
- [ ] Déploiement AWS (EC2 + RDS)
- [ ] Fil d'actualité des matches (posts, tutos)
- [ ] Notifications en temps réel (WebSocket)
