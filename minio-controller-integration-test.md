## Pattern: minio controller integration test

Integration tests for `ImageController` against a **real MinIO** started by Testcontainers.

### Location and naming
- `minio/src/test/java/com/poly/minio/controllers/ImageControllerTest.java`
- Method names: `<endpoint>_when<Condition>_<expectedResult>`.

### Setup
- `@Tag("integration")`, `@SpringBootTest`, `@AutoConfigureMockMvc`, `MockMvc`, `@Testcontainers`.
- Do not mock the service or `MinioClient`.
- Start a MinIO container with Testcontainers (`org.testcontainers:minio`). No Postgres, no database, no Eureka. Never use my running MinIO or `booking-bucket`. Docker must be running.
- Create a test bucket in the container before the Spring context starts, because the service checks the bucket on startup.
- Point the `minio.*` properties at the container with `@DynamicPropertySource`, and set the public endpoint to the container endpoint.
- `src/test/resources/application.yml`: `eureka.client.enabled: false` and any property the service needs to start. Do not read the root `.env`.
- Use a separate `MinioClient` inside the test to seed objects and to verify results.

### Test cases
- `POST /api/images` with a valid png returns 200 with `objectKey` and `url`. The key starts with the given folder, and the object really exists in MinIO.
- `POST` with a nested folder (`hotels/test`) works.
- `GET /api/images/url` for an uploaded key returns a presigned URL (contains `X-Amz-Signature`).
- `GET /api/images/url` for a key the test put in the bucket directly, with a nested name like `hotels/9_Hostel_and_Bar/0.jpg`, returns a link. Keys not created by this service must work.
- `GET` for a missing key returns 404.
- `POST` with a pdf or txt returns 400.
- `POST` over 10MB returns 400 with the consistent error JSON.
- `POST` with a folder containing `..` or a leading `/` returns 400.
- `POST` without the file part returns 400.
- Every 4xx case asserts the error response shape.

### Rules
- Test only behavior that exists in the controller and service. Do not invent behavior.
- Each test creates its own data and does not depend on another test.
- Do not change production code to make a test pass. If you find a bug, report it and ask me.
- Check that `build.gradle` has the `integrationTest` task and the `excludeTags 'integration'` config from `AGENTS.md`, and add them only if missing. Add `testcontainers:junit-jupiter` and `testcontainers:minio` if missing.
- Run `gradlew integrationTest` inside the `minio` folder (Docker running) and report the results.
- If a behavior is unclear, ask me before writing the test.