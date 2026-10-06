# AGENTS.md

## Project
Hotel booking backend built as Spring Boot microservices.

## Build tool: GRADLE (NOT Maven)
- Every module uses **Gradle** (`build.gradle`, `settings.gradle`, `gradlew` / `gradlew.bat`).
- **Never** create or edit `pom.xml`. Never suggest `mvn`, `mvnw` or Maven dependency XML.
- If a service still contains `pom.xml`, `mvnw`, `mvnw.cmd` or a `.mvn` folder, they are leftovers from project generation and are **not used**. Do not build with them, edit them, or add dependencies to them. Always use `build.gradle`.
- Add dependencies in `build.gradle` under `dependencies { ... }`, e.g. `implementation 'group:artifact'`.
- Run a module from inside its own folder: `gradlew bootRun` (Windows cmd, no `.\` prefix).
- Build: `gradlew build`. Test: `gradlew test`.

## Stack
- Java 21, Spring Boot, Spring Cloud (versions managed by the Gradle plugins/BOM)
- groupId / base package: `com.poly`
- Config files: `application.yml` (not `.properties`)

## Modules (each is a separate Gradle project, not a multi-module build)
| Folder | Role | Port |
|---|---|---|
| `discovery-server` | Eureka registry (`@EnableEurekaServer`) | 8761 |
| `api-gateway` | Spring Cloud Gateway, single entry point | 8080 |
| `user` | users, user images, device tokens | 8081 |
| `hotel` | hotels, hotel images, rooms, room types | 8082 |
| `booking` | bookings, booking status history | 8083 |
| `payment` | payments | 8084 |
| `notification` | notifications | 8085 |
| `minio` | images / object storage | 8086 |

## Package structure (every business service)
Each business service uses the same layout under its base package, e.g. `com.poly.hotel`:

```
src/main/java/com/poly/<service>/
├── controllers/        # REST controllers (@RestController), HTTP only, no business logic
├── services/           # Business logic (@Service)
├── repositories/       # Spring Data JPA repositories (interfaces)
├── models/             # JPA entities (@Entity)
├── dtos/
│   ├── requests/       # <Entity>Request records (input, with validation)
│   └── responses/      # <Entity>Response records (output)
├── mappers/            # MapStruct mappers (Java interfaces, NOT .xml files)
├── configs/            # @Configuration classes (CORS, beans, Feign, security)
└── <Service>Application.java
```

Rules:
- Dependencies flow one way: `controllers → services → repositories → models`. Controllers and services use DTOs, and services use mappers to convert between DTOs and models.
- Controllers never call repositories directly.
- Controllers never accept or return entities. Use `requests/` and `responses/` DTOs.
- Folder names are plural.
- `api-gateway` and `discovery-server` do not use this layout. They stay config-only.

## Architecture rules
- Each business service sets `spring.application.name` to its folder name (`user`, `hotel`, ...).
- Every service registers with Eureka: `eureka.client.service-url.defaultZone: http://localhost:8761/eureka`.
- Gateway routes use `lb://<service-name>`, e.g. `lb://hotel` with `Path=/api/hotels/**`.
- Services call each other by service name (OpenFeign or `@LoadBalanced`), never hardcoded ports.
- Each service owns its own database. No cross-service foreign keys; store plain IDs (e.g. `userId`, `roomId`).
- Clients (React Native app) only talk to the gateway on port 8080.

## Conventions
- Lombok (`compileOnly` + `annotationProcessor`) and MapStruct are used in the five business services only.
- Business code goes in the business services. Gateway and discovery stay config-only unless a cross-cutting filter is needed.
- Start order: `discovery-server` → business services → `api-gateway`.

## Environment
- Windows, running commands in cmd or PowerShell.
- Project root: `D:\My documents\JAVAMICROSERVICE\app` (path contains a space, so quote it).

## Testing

Stack: JUnit 5, Mockito, AssertJ, Spring Boot Test, Testcontainers (all via Gradle `testImplementation`).

| Layer | Type | Annotations / tools | Spring context |
|---|---|---|---|
| `services` | **Unit test** | `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks` | No |
| `repositories` | **Integration test** | `@DataJpaTest`, `@AutoConfigureTestDatabase(replace = NONE)`, Testcontainers | Partial (JPA only) |
| `controllers` | **Integration test** | `@SpringBootTest`, `@AutoConfigureMockMvc`, `MockMvc`, Testcontainers | Full |

Rules:
- **Service tests are unit tests.** Mock every repository and Feign client. Never start Spring or touch a database.
- **Repository tests use a real database** through Testcontainers, the same engine as production. Do not use H2.
- **Controller tests go through the full stack** (controller → service → repository → real DB). Do not mock the service layer. Call endpoints with `MockMvc` and assert status and JSON body.
- Disable Eureka in tests: set `eureka.client.enabled: false` in `src/test/resources/application.yml`.
- Test only the service itself. Do not start `discovery-server` or `api-gateway` in tests.
- One test class per production class, named `<ClassName>Test`, in the same package under `src/test/java`.
- Test method names describe behavior, e.g. `createBooking_whenRoomUnavailable_throwsException`.
- Use the Arrange / Act / Assert structure.
- Run with `gradlew test` from inside the service folder.

Test layout (mirrors the main package):

```
src/test/java/com/poly/<service>/
├── controllers/    # *ControllerTest  (integration)
├── services/       # *ServiceTest     (unit)
└── repositories/   # *RepositoryTest  (integration)
```

Gradle dependencies (Lombok too, if tests use it):

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.boot:spring-boot-testcontainers'
testImplementation 'org.testcontainers:junit-jupiter'
testImplementation 'org.testcontainers:<db>'   // e.g. postgresql or mysql
testCompileOnly 'org.projectlombok:lombok'
testAnnotationProcessor 'org.projectlombok:lombok'
```

Integration tests need Docker running.

## Working rules

### Keep AGENTS.md up to date
- After you finish building or changing anything (a new service, model, endpoint, dependency, folder, port, config or pattern file), update this file **before** reporting the task as done.
- Update in this order:
  1. **Read** the current `AGENTS.md` in full.
  2. **Compare** each statement in it with the actual codebase (folders, `build.gradle`, `application.yml`, ports, package structure, file names, commands).
  3. **Remove** or correct anything that no longer matches the code.
  4. **Add** what is new and missing: new modules, folders, dependencies, patterns and commands.
- The code is the source of truth. If the file and the code disagree, change the file, never the code.
- Only edit the project description sections: Project, Stack, Modules, Package structure, Architecture rules, Conventions, Environment, and Reference docs. Never edit or remove the "Working rules" sections, or any rule I wrote about how you should behave, without asking me first. If one looks wrong, tell me instead.
- Keep it short. One line per fact, no duplicates, and no history or change logs. Describe only what is true now.
- Edit the file directly with your normal edit tools. Do not use scripts.
- If you're not sure whether something in the file is still correct, ask me instead of guessing.
- When done, tell me what you removed, what you added and what you changed in `AGENTS.md`.

### Ask when unsure
- If the request is ambiguous, or you're missing information (table names, field types, endpoints, ports, config values, versions), **stop and ask me questions before writing code**.
- Never guess, invent, or assume class names, methods, dependencies, properties, or file paths. Check the codebase first. If it isn't there, ask.
- If you're not sure a library or API exists, say so instead of making it up.

### Current codebase is the source of truth
- Treat the code as it exists right now as correct. Read the relevant files before changing anything.
- **Never revert, undo, or restore anything** to an earlier version, including my manual edits, renamed files, or removed code.
- Do not "fix" my changes back to a previous approach or re-add code I removed.
- If my code conflicts with these rules or looks wrong, point it out and ask. Do not change it silently.
- Make the smallest change that does the job. Don't refactor or reformat unrelated code.

### Edit files directly, no scripts
- Edit source files with your normal file-edit tools. **Never write or run Python, JavaScript, Node, PowerShell, or shell scripts to modify, generate, or refactor project files** (no regex or find-and-replace scripts, no bulk rewrites).
- Do not create helper or temporary script files in the project (`.py`, `.js`, `.sh`, `.ps1`). Only create files that belong in the project: Java source, `build.gradle`, `application.yml`, tests, and docs.
- The project is Java with Gradle. Only run `gradlew` commands to build or test.
- If a change seems to need a script, make the edits one file at a time instead. If that's impractical, stop and ask me first.
- Do not touch files outside the task. If you need to change more files than I asked for, ask first.

### Unit and integration tests run separately
- Unit tests (`services` layer) and integration tests (`repositories` and `controllers` layers) must run as **two separate Gradle tasks**.
- Every repository and controller integration test class **must** have `@Tag("integration")`. Service unit tests **must not** have it.
- Each service's `build.gradle` must contain this config. If it's missing, add it. Don't change anything else in the file:

```groovy
tasks.named('test') {
    useJUnitPlatform {
        excludeTags 'integration'
    }
}

tasks.register('integrationTest', Test) {
    description = 'Runs integration tests (needs Docker)'
    group = 'verification'
    testClassesDirs = sourceSets.test.output.classesDirs
    classpath = sourceSets.test.runtimeClasspath
    useJUnitPlatform {
        includeTags 'integration'
    }
    shouldRunAfter tasks.named('test')
}
```

- Run unit tests with `gradlew test` (fast, no Docker).
- Run integration tests with `gradlew integrationTest` (needs Docker running).
- Run both with `gradlew test integrationTest`.
- When you write or run tests, use the matching task. Never run integration tests with `gradlew test`, and never skip the tag.
- If a test's type is unclear (unit or integration), ask me before writing it.

## Reference docs
- Infrastructure (Postgres, MinIO) runs from `docker-compose.yml` in the project root: `docker compose up -d`. Never run `docker compose down -v` (it deletes the data volumes). Schema changes live only in the Flyway folder run by `flyway-init`. Spring services are not in the compose file; run them with `gradlew bootRun`.
- Any "get list" endpoint must follow the pattern in `filter-and-paginate.md` (project root). Read it before writing a `filterAndPaginate` method or a `PageResponse`.
- Service unit tests must follow `service-unit-test.md` (project root). Read it before writing any `*ServiceTest`.
- Repository integration tests must follow `repository-integration-test.md` (project root). Read it before writing any `*RepositoryTest`.