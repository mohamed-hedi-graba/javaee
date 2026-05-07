# Compte Rendu - TP3 : Application Événements Étudiants

## PARTIE A : Prototypes et Fonctionnalités Spring 6

### 1. Contrôleur et Internal View Resolver (JSP)
*Capture d'écran à insérer ici*

```java
// Configuration
@Bean
public InternalResourceViewResolver viewResolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix("/WEB-INF/jsp/");
    resolver.setSuffix(".jsp");
    return resolver;
}

// Contrôleur
@Controller
public class JspController {
    @GetMapping("/hello-jsp")
    public String helloJsp(Model model) {
        model.addAttribute("message", "Bonjour depuis JSP !");
        return "hello"; // Résolu vers /WEB-INF/jsp/hello.jsp
    }
}
```
**Commentaire** : C'est l'approche classique de Spring MVC avant l'essor de Thymeleaf. Le contrôleur renvoie un nom logique de vue, que le ViewResolver traduit en chemin physique vers un fichier JSP.

---

### 2. Contrôleur et Thymeleaf View Resolver
*Capture d'écran à insérer ici*

```java
// Configuration WebConfig.java
@Bean
public ThymeleafViewResolver viewResolver() {
    ThymeleafViewResolver resolver = new ThymeleafViewResolver();
    resolver.setTemplateEngine(templateEngine());
    resolver.setCharacterEncoding("UTF-8");
    return resolver;
}
```
**Commentaire** : Thymeleaf est l'alternative moderne aux JSP. Il permet d'écrire des vues qui restent des fichiers HTML valides, interprétables sans serveur, tout en injectant des données dynamiques via l'attribut `th:`.

---

### 3. Contrôleur et XML View Resolver
*Capture d'écran à insérer ici*

```java
// Contrôleur renvoyant du XML via Jackson
@RestController // Combinaison de @Controller et @ResponseBody
public class XmlController {
    
    @GetMapping(value = "/api/event", produces = MediaType.APPLICATION_XML_VALUE)
    public Event getEventXml() {
        return new Event("Conférence", "Description de test", new Date());
    }
}
```
**Commentaire** : Avec l'annotation `@RestController` et la librairie `jackson-dataformat-xml`, Spring convertit automatiquement les objets Java en flux XML, ce qui est très utile pour concevoir des Web Services.

---

### 4. Contrôleur et Multilingue
*Capture d'écran à insérer ici*

```java
// WebConfig.java (Configuration i18n)
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.FRENCH);
    return slr;
}

@Bean
public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
    lci.setParamName("lang");
    return lci;
}

// Vue Thymeleaf
// <span th:text="#{nav.events}">Événements</span>
```
**Commentaire** : L'internationalisation est gérée par des fichiers `messages_xx.properties`. Le `LocaleChangeInterceptor` intercepte la requête contenant le paramètre `?lang=en` et modifie la langue de la session utilisateur.

---

### 5. Contrôleur et Validation des Champs de Formulaires
*Capture d'écran à insérer ici*

```java
// Modèle (User.java)
@NotBlank(message = "{user.email.notblank}")
@Email(message = "{user.email.invalid}")
private String email;

@Size(min = 6, message = "{user.password.size}")
private String password;

// Contrôleur (AuthController.java)
@PostMapping("/register")
public String register(@Valid @ModelAttribute("user") User user, BindingResult result) {
    if (result.hasErrors()) {
        return "register"; // Renvoie au formulaire avec les erreurs
    }
    // Sauvegarde en base de données
    return "redirect:/login";
}
```
**Commentaire** : L'utilisation de Jakarta Validation (`@Valid`) permet de vérifier les données côté serveur avant même d'entrer dans la méthode métier. Les messages d'erreur sont récupérés de nos fichiers `.properties`.

---

### 6. Contrôleur et Hibernate
*Capture d'écran à insérer ici*

```java
// AppConfig.java
@Bean
public LocalSessionFactoryBean sessionFactory() {
    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
    sessionFactory.setDataSource(dataSource());
    sessionFactory.setPackagesToScan("ma.tp.studentevents.model");
    // hibernate.hbm2ddl.auto = update
    return sessionFactory;
}

// UserDao.java
public void save(User user) {
    Session session = sessionFactory.getCurrentSession();
    session.persist(user);
}
```
**Commentaire** : Hibernate est notre ORM. Nous configurons une `SessionFactory` qui scanne le package `model` pour trouver les classes `@Entity`. La propriété `hbm2ddl.auto=update` permet de créer automatiquement les tables `utilisateurs` et `evenements` dans MySQL.

---

### 7. Nouvelle fonctionnalité Spring 6 : "Problem Details API" (RFC 7807)
*Capture d'écran à insérer ici*

```java
// Exception Handler Global
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ProblemDetail handleEventNotFound(EventNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        
        problemDetail.setTitle("Événement introuvable");
        problemDetail.setType(URI.create("https://mon-app.com/erreurs/event-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        
        return problemDetail;
    }
}
```
**Commentaire et réalisation** : Spring 6 a introduit le support natif de la RFC 7807 (Problem Details for HTTP APIs). Plutôt que de créer des classes personnalisées pour renvoyer des erreurs JSON dans nos APIs, Spring 6 fournit la classe `ProblemDetail`. Cela permet d'uniformiser le format des erreurs HTTP à travers tous les microservices et applications web de l'entreprise.

---

## PARTIE B : Projet Student Events

### 1. ARCHITECTURE OPÉRATIONNELLE (3 TIERS)

L'application repose sur une architecture 3 Tiers physique :
1. **Tier Présentation (Client)** : Le navigateur web (Chrome, Firefox) qui affiche les vues HTML générées par Thymeleaf.
2. **Tier Application (Serveur Web/App)** : Le serveur Tomcat qui exécute l'application Spring MVC 6. Il reçoit les requêtes HTTP, exécute la logique métier et génère les vues HTML.
3. **Tier Données (Serveur de BDD)** : Le serveur MariaDB/MySQL (sur `localhost:3306`) qui héberge la base de données `ma_db` et stocke de manière persistante les entités (Users et Events).

### 2. ARCHITECTURE APPLICATIVE (COUCHES LOGICIELLES)

Le code est organisé selon le patron Modèle-Vue-Contrôleur (MVC) structuré en couches logicielles strictes :

- **Couche Présentation (Vues et Contrôleurs)**
  - *Composants* : Fichiers Thymeleaf (`.html`), Contrôleurs Spring (`AuthController`, `EventController`, `ProfileController`).
  - *Rôle* : Interception des requêtes web, validation des formulaires, gestion des sessions web et redirection.

- **Couche Métier (Services)**
  - *Composants* : Classes avec `@Service` (`UserService`, `EventService`).
  - *Rôle* : Cœur de l'application. Applique les règles de gestion (hachage du mot de passe avec BCrypt, initialisation des événements par défaut, vérification des identifiants). Elle est transactionnelle (`@Transactional`).

- **Couche Accès aux Données (DAO)**
  - *Composants* : Classes avec `@Repository` (`UserDao`, `EventDao`).
  - *Rôle* : Fournit les méthodes CRUD (Create, Read, Update, Delete) en manipulant l'objet `Session` d'Hibernate (HQL et persistance JPA).

- **Couche Modèle (Entités)**
  - *Composants* : Classes avec `@Entity` (`User`, `Event`).
  - *Rôle* : Représentation objet des tables relationnelles.

**Fonctionnalités réalisées :**
1. **Inscription** : Formulaire avec validation stricte (Email valide, mot de passe > 6 chars) et hachage du mot de passe.
2. **Connexion & Session** : Vérification en base de données et stockage de l'utilisateur dans la `HttpSession`.
3. **Multilingue** : Barre de recherche et portail d'événements traduits en Français, Anglais et Arabe.
4. **Authentification Obligatoire** : Les pages `/events` et `/profile` vérifient la présence de l'utilisateur dans la session avant l'affichage.
