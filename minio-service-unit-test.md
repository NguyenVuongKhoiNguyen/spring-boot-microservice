## Pattern: minio service unit test

Unit tests for the service layer of the `minio` service. Follows `service-unit-test.md`, adapted because this service has no repository and no mapper.

### Location and naming
- `minio/src/test/java/com/poly/minio/services/MinioServiceTest.java`
- Method names: `<method>_when<Condition>_<expectedResult>`.

### Setup
- `@ExtendWith(MockitoExtension.class)`, `@Mock MinioClient` (and the properties object if needed), `@InjectMocks MinioService`.
- No Spring context, no Docker, no real MinIO. Use AssertJ and Arrange / Act / Assert.
- Build test data in private helper methods, e.g. a mock png `MultipartFile`.

### 4 `@Nested` categories

**1. Upload and link** (the CRUD equivalent)
- A valid upload calls `putObject` once with the right bucket, key and content type, and returns the key plus a url.
- Getting a link for an existing key returns a presigned URL.

**2. Bucket startup check**
- Passes when the bucket exists.
- Fails with a clear message when it doesn't.
- Never creates, deletes or changes the policy of the bucket (`verify(minioClient, never())`).

**3. Validation**
- Allowed content types: jpeg, png, webp. Other types are rejected.
- Files over 10MB are rejected.
- Folder and filename sanitization: rejects `..`, a leading `/`, backslashes and empty segments. Accepts nested folders like `hotels/9_Hostel_and_Bar`.
- The key format is `<folder>/<uuid>-<filename>`.
- The public-endpoint swap is applied in links.

**4. Errors and edge cases**
- A missing object throws the not-found exception.
- `MinioClient` exceptions propagate correctly.
- A null or empty file is rejected.
- `verify(minioClient, never()).putObject(...)` whenever validation fails.

### Rules
- Test only behavior that exists in `MinioService`. Do not invent methods or behavior.
- Every public method needs the happy path and at least one failure case.
- One behavior per test. No test depends on another.
- Do not change production code to make a test pass. If you find a bug, report it and ask me.
- Run `gradlew test` inside the `minio` folder and report the results.
- If a behavior is unclear, ask me before writing the test.