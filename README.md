
## 1. Présentation du projet

**SkillSwap** est une plateforme web d’échange de compétences permettant aux utilisateurs de partager leurs connaissances et d'apprendre de nouvelles compétences à travers des échanges organisés.

Le backend de l'application est développé avec **Java et Spring Boot**. Il fournit une API REST permettant de gérer l'authentification, les utilisateurs, les compétences, les demandes d'échange, les conversations, les sessions, les notifications et les avis.

Le backend assure également la sécurité de l'application avec **Spring Security et JWT**, ainsi que la communication en temps réel pour le chat avec **WebSocket / STOMP**.

---

## 2. Problématique

De nombreuses personnes possèdent des compétences qu'elles souhaitent partager, tout en souhaitant apprendre d'autres compétences.

SkillSwap propose une solution permettant de :

* créer un compte utilisateur ;
* gérer son profil ;
* proposer des compétences ;
* indiquer les compétences recherchées ;
* découvrir les compétences des autres utilisateurs ;
* envoyer des demandes d'échange ;
* accepter ou refuser une demande ;
* communiquer avec l'autre utilisateur ;
* organiser une session d'échange ;
* laisser un avis après la session.

Le backend permet de centraliser et sécuriser toutes ces fonctionnalités.

---

## 3. Fonctionnalités principales

### Authentification

* Inscription d'un utilisateur
* Connexion
* Authentification avec JWT
* Chiffrement des mots de passe avec BCrypt
* Gestion des rôles `USER` et `ADMIN`
* Protection des endpoints avec Spring Security

### Gestion des utilisateurs

* Création d'un compte
* Consultation du profil
* Modification du profil
* Gestion des informations personnelles
* Gestion de la photo de profil
* Gestion de la ville et de la description

### Gestion des compétences

* Création d'une compétence
* Modification d'une compétence
* Suppression d'une compétence
* Consultation des compétences
* Association d'une compétence à un utilisateur
* Gestion du type :

    * `OFFER`
    * `WANTED`
* Gestion du niveau de compétence

### Découverte

Le backend fournit les données nécessaires pour permettre aux utilisateurs de découvrir les compétences proposées par les autres utilisateurs.

### Demandes d'échange

Un utilisateur peut :

* envoyer une demande d'échange ;
* consulter ses demandes reçues ;
* consulter ses demandes envoyées ;
* accepter une demande ;
* refuser une demande ;
* annuler une demande.

Les demandes peuvent avoir différents statuts :

```text
PENDING
ACCEPTED
REJECTED
COMPLETED
CANCELLED
```

### Conversations

Une conversation est créée lorsqu'une demande d'échange est acceptée.

Le backend permet :

* de créer une conversation ;
* de récupérer les conversations d'un utilisateur ;
* de récupérer les messages ;
* d'envoyer des messages en temps réel.

### Chat en temps réel

Le système de chat utilise :

* WebSocket
* STOMP
* Spring WebSocket

Configuration principale :

```text
/ws
/app
/topic
```

Les messages d'une conversation sont diffusés sur :

```text
/topic/conversation/{conversationId}
```

### Sessions d'échange

Les utilisateurs peuvent organiser une session après l'acceptation d'une demande.

Une session contient notamment :

* date ;
* durée ;
* mode ;
* statut ;
* conversation associée ;
* lien de réunion si nécessaire.

Modes disponibles :

```text
ONLINE
PRESENTIEL
```

Statuts :

```text
PROPOSED
CONFIRMED
CANCELLED
COMPLETED
```

### Notifications

Le backend gère les notifications liées aux actions importantes de la plateforme :

* nouvelle demande d'échange ;
* demande acceptée ;
* demande refusée ;
* nouveau message ;
* session ;
* autres événements importants.

### Avis

Après la réalisation d'une session, un utilisateur peut laisser une note à l'autre utilisateur.

La note est comprise entre :

```text
1 à 5
```

### Dashboard

Le backend fournit les informations nécessaires au dashboard utilisateur :

* demandes en attente ;
* prochaines sessions ;
* messages non lus ;
* notifications.

### Administration

Un utilisateur avec le rôle `ADMIN` peut gérer certaines données de la plateforme, notamment :

* utilisateurs ;
* compétences.

---

## 4. Technologies utilisées

| Technologie       | Utilisation                   |
| ----------------- | ----------------------------- |
| Java 21           | Langage de programmation      |
| Spring Boot 3.4.1 | Framework backend             |
| Spring Security   | Sécurité                      |
| JWT               | Authentification              |
| BCrypt            | Chiffrement des mots de passe |
| Spring Data JPA   | Accès aux données             |
| Hibernate         | ORM                           |
| MySQL             | Base de données               |
| Flyway            | Gestion des migrations        |
| MapStruct         | Mapping Entity / DTO          |
| WebSocket         | Communication temps réel      |
| STOMP             | Messaging WebSocket           |
| Swagger / OpenAPI | Documentation API             |
| JUnit             | Tests                         |
| Maven             | Gestion du projet             |
| Git / GitHub      | Gestion de versions           |
| Postman           | Tests des API                 |

---

## 5. Architecture du backend

Le backend suit une architecture en couches afin de séparer les différentes responsabilités de l'application.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controller

Les Controllers exposent les endpoints REST de l'application.

Exemples :

```text
AuthController
UserController
SkillController
SkillDetailsController
SwapRequestController
ConversationController
MessageController
SessionController
ReviewController
NotificationController
```

### Service

Les Services contiennent la logique métier de l'application.

Exemples :

```text
AuthService
UserService
SkillService
SkillDetailsService
SwapRequestService
ConversationService
SessionService
ReviewService
NotificationService
```

### Repository

Les repositories permettent d'interagir avec la base de données grâce à Spring Data JPA.

Exemples :

```text
UserRepository
SkillRepository
SkillDetailsRepository
SwapRequestRepository
ConversationRepository
MessageRepository
SessionRepository
ReviewRepository
NotificationRepository
```

### Entity

Les principales entités du projet sont :

```text
User
Skill
SkillDetails
SwapRequest
Conversation
Message
Session
Review
Notification
```

---

## 6. Base de données

Le projet utilise **MySQL** comme système de gestion de base de données.

Nom de la base :

```text
skillswap_db
```

Port utilisé en développement :

```text
3307
```

La structure de la base de données est gérée avec **Flyway**.

Les migrations sont placées dans :

```text
src/main/resources/db/migration
```

La configuration JPA utilise :

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Cela permet de vérifier que la structure des entités correspond à la structure de la base de données.

---

## 7. Authentification et sécurité

La sécurité est basée sur :

```text
Spring Security
+
JWT
+
BCrypt
```

Lors de la connexion, l'utilisateur reçoit un token JWT.

Le frontend utilise ensuite ce token pour accéder aux endpoints protégés.

Le token contient notamment les informations nécessaires à l'identification de l'utilisateur et à la gestion de son rôle.

Les rôles utilisés sont :

```text
USER
ADMIN
```

Les endpoints protégés nécessitent une authentification valide.

---

## 8. Principales API REST

### Authentification

```http
POST /api/auth/register
POST /api/auth/login
```

### Utilisateurs

```http
GET /api/users/{id}
PUT /api/users/{id}
```

### Compétences

```http
GET /api/skills
POST /api/skills
PUT /api/skills/{id}
DELETE /api/skills/{id}
```

### Skill Details

```http
GET /api/skill-details
POST /api/skill-details
PUT /api/skill-details/{id}
DELETE /api/skill-details/{id}
```

### Demandes d'échange

```http
POST /api/swap-requests
GET /api/swap-requests/received/{userId}
GET /api/swap-requests/sent/{userId}
PUT /api/swap-requests/{id}/accept
PUT /api/swap-requests/{id}/reject
PUT /api/swap-requests/{id}/cancel
```

### Conversations

```http
GET /api/conversations
POST /api/conversations
```

### Sessions

```http
GET /api/sessions
POST /api/sessions
PUT /api/sessions/{id}
```

### Avis

```http
POST /api/reviews
GET /api/reviews
```

### Notifications

```http
GET /api/notifications
PUT /api/notifications/{id}/read
```

> Les endpoints présentés peuvent évoluer selon la version finale du backend.

---

## 9. WebSocket

Le chat utilise une communication temps réel avec Spring WebSocket et STOMP.

Configuration :

```text
Endpoint :
/ws

Application prefix :
/app

Broker :
/topic
```

Pour envoyer un message :

```text
/chat/{conversationId}/{userId}
```

Pour recevoir les messages :

```text
/topic/conversation/{conversationId}
```

Le système permet aux deux utilisateurs d'une conversation d'échanger des messages en temps réel.

---

## 10. Installation et lancement

### Prérequis

Avant de lancer le projet, il faut installer :

* Java 21
* Maven 3.9+
* MySQL 8+
* Git
* Postman pour tester les API

### Cloner le projet

```bash
git clone <URL_DU_REPOSITORY>
```

### Accéder au backend

```bash
cd skillswap_backend
```

### Configuration de la base de données

Créer la base :

```text
skillswap_db
```

Configurer ensuite le fichier :

```text
src/main/resources/application.properties
```

Exemple :

```properties
spring.application.name=SkillSwap

spring.datasource.url=jdbc:mysql://localhost:3307/skillswap_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

spring.flyway.enabled=true
```

### Installer les dépendances

```bash
mvn clean install
```

### Lancer le projet

```bash
mvn spring-boot:run
```

Le backend sera disponible sur :

```text
http://localhost:8080
```

---

## 11. Documentation Swagger

La documentation des API est disponible avec Swagger / OpenAPI.

URL :

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger permet de :

* consulter les endpoints ;
* voir les paramètres ;
* tester les requêtes ;
* consulter les réponses API.

---

## 12. Tests

Les tests backend sont réalisés avec **JUnit**.

Les tests permettent notamment de vérifier :

* la logique métier ;
* les services ;
* les endpoints ;
* les fonctionnalités principales.

Les tests peuvent être exécutés avec :

```bash
mvn test
```

---

## 13. Structure du projet

```text
skillswap_backend/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/skillswap/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── enums/
│   │   │       ├── exception/
│   │   │       ├── mapper/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```

---

## 14. Conception UML

### Diagramme de cas d'utilisation

Le diagramme présente les principales interactions entre les utilisateurs, l'administrateur et le système.

![Use Case Diagram](docs/uml/![use_case.PNG](use_case.PNG))

### Diagramme de classes

Le diagramme présente les principales entités du backend et leurs relations.

![Class Diagram](docs/uml/![diagram de class.PNG](diagram%20de%20class.PNG))

Principales classes :

```text
User
Skill
SkillDetails
SwapRequest
Conversation
Message
Session
Review
Notification
```

### Diagramme de séquence

Le diagramme présente le scénario principal d'un échange de compétences :

```text
Utilisateur
    ↓
Découverte d'une compétence
    ↓
Demande d'échange
    ↓
Acceptation
    ↓
Création de conversation
    ↓
Chat
    ↓
Création d'une session
    ↓
Réalisation de la session
    ↓
Avis
```

![Sequence Diagram](docs/uml/![sequence.PNG](sequence.PNG))

---

## 15. Difficultés rencontrées

### Authentification JWT

La mise en place de l'authentification nécessite la gestion :

* du token JWT ;
* des rôles ;
* des endpoints protégés ;
* du chiffrement des mots de passe ;
* de la validation du token.

### Communication WebSocket

L'intégration du chat en temps réel nécessite la configuration de :

* WebSocket ;
* STOMP ;
* broker ;
* destinations ;
* gestion des conversations.

### Gestion du workflow d'échange

Le système doit respecter les différentes étapes :

```text
PENDING
→ ACCEPTED
→ Conversation
→ Session
→ COMPLETED
→ Review
```

Cela nécessite plusieurs règles métier entre les différentes entités.

---

## 16. Améliorations possibles

Plusieurs améliorations peuvent être ajoutées dans les futures versions :

* Ajouter davantage de tests unitaires et d'intégration ;
* Ajouter des filtres avancés pour les compétences ;
* Ajouter une recherche par ville ;
* Ajouter une recherche par niveau ;
* Améliorer la pagination ;
* Améliorer le système de notifications en temps réel ;
* Ajouter davantage de fonctionnalités d'administration ;
* Déployer le backend sur le cloud ;
* Améliorer la gestion des erreurs ;
* Ajouter une surveillance et des logs plus avancés.

---

## 17. Auteur

**Samira El Boussidi**

Projet réalisé dans le cadre de la formation en développement informatique.

**SkillSwap — Plateforme d'Échange de Compétences**


