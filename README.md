# CHARGE. — Backend

API Spring Boot pour l'application de suivi de préparation physique CHARGE.
Correspond au cahier des charges : gestion de l'effectif, exercices assignés
avec notifications, questionnaire de bien-être quotidien (bloquant), fiches
de musculation avec calcul automatique du 1RM à 20RM.

## Stack

- Java 17, Spring Boot 3.2 (Web, Security, Data JPA, Validation)
- MySQL
- Authentification par JWT (io.jsonwebtoken / jjwt)
- Point d'intégration Firebase Cloud Messaging prêt (voir `PushNotificationSender`)

## Prérequis

- JDK 17+
- Maven 3.9+
- MySQL 8+ (une base vide, ex. `charge_db`)

## Configuration

Toute la config sensible passe par variables d'environnement
(voir `src/main/resources/application.yml` pour les valeurs par défaut) :

| Variable | Rôle | Défaut |
|---|---|---|
| `DB_URL` | URL JDBC MySQL | `jdbc:mysql://localhost:3306/charge_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USER` | Utilisateur MySQL | `charge_user` |
| `DB_PASSWORD` | Mot de passe MySQL | `changeme` |
| `JWT_SECRET` | Clé de signature JWT (256 bits mini) | valeur de dev, **à changer en prod** |
| `JWT_EXPIRATION_MS` | Durée de validité du token | `86400000` (24h) |
| `CORS_ORIGINS` | Origines autorisées (séparées par virgule) | `http://localhost:4200` |

## Créer la base

```sql
CREATE DATABASE charge_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'charge_user'@'%' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON charge_db.* TO 'charge_user'@'%';
FLUSH PRIVILEGES;
```

## Lancer le projet

```bash
mvn spring-boot:run
```

L'API démarre sur `http://localhost:8080`. Le schéma de base de données est
créé/mis à jour automatiquement au démarrage (`ddl-auto: update`) — à
remplacer par des migrations Flyway/Liquibase avant la mise en production.

## Prise en main rapide (curl)

```bash
# 1. Créer un compte préparateur physique
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Alex Dubois","email":"alex@club.com","password":"motdepasse123"}'

# 2. Se connecter (récupère un token JWT)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alex@club.com","password":"motdepasse123"}'

# 3. Créer un joueur (avec le token du préparateur)
curl -X POST http://localhost:8080/api/players \
  -H "Authorization: Bearer <TOKEN_COACH>" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Lucas Bernard","email":"lucas@club.com","password":"motdepasse123","poste":"Milieu offensif"}'

# 4. Le joueur se connecte, puis remplit son bien-être du jour (obligatoire avant les exercices)
curl -X POST http://localhost:8080/api/wellness \
  -H "Authorization: Bearer <TOKEN_JOUEUR>" \
  -H "Content-Type: application/json" \
  -d '{"mood":4,"sleep":3,"fatigue":3,"soreness":4,"stress":3}'

# 5. Le joueur peut maintenant voir ses exercices
curl http://localhost:8080/api/exercises/mine -H "Authorization: Bearer <TOKEN_JOUEUR>"
```

## Aperçu des endpoints

| Méthode | Route | Rôle | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Inscription d'un préparateur |
| POST | `/api/auth/login` | public | Connexion (retourne un JWT) |
| POST | `/api/players` | COACH | Ajouter un joueur à l'effectif |
| GET | `/api/players` | COACH | Lister l'effectif |
| POST | `/api/players/{id}/exercises` | COACH | Assigner un exercice |
| GET | `/api/exercises/mine` | PLAYER | Mes exercices (**HTTP 428** si bien-être du jour non rempli) |
| PATCH | `/api/exercises/{id}/complete` | PLAYER | Cocher/décocher — notifie le préparateur |
| POST | `/api/players/{id}/fiche` | COACH | Ajouter un test (poids × reps) |
| GET | `/api/fiche/mine` | PLAYER | Mes charges, avec 1RM et tableau 1RM→20RM |
| POST | `/api/wellness` | PLAYER | Remplir le questionnaire du jour |
| GET | `/api/wellness/today` | PLAYER | Statut du questionnaire du jour |
| GET | `/api/players/{id}/wellness` | COACH | Historique de bien-être d'un joueur |
| GET | `/api/notifications` | COACH | Notifications reçues |

Toutes les routes protégées attendent un en-tête `Authorization: Bearer <token>`.

## Points à compléter avant la production

- **Firebase Cloud Messaging** : `PushNotificationSender` journalise seulement pour l'instant ; brancher le SDK Admin Firebase (voir la javadoc dans le fichier) et stocker un token FCM par utilisateur.
- **Migrations** : remplacer `ddl-auto: update` par Flyway ou Liquibase.
- **Réinitialisation de mot de passe**, gestion des comptes désactivés.
- **Tests** : un test unitaire de `RMCalculator` est fourni à titre d'exemple ; compléter avec des tests de services et des tests d'intégration (`spring-boot-starter-test` + H2 déjà en dépendance test).

## Limite de cet environnement

Ce projet a été généré sans accès à Maven Central depuis ce sandbox : la
compilation n'a donc pas pu être vérifiée ici. Lancez `mvn clean install`
en local pour la première vérification.
