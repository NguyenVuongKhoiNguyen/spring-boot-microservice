## Pattern: service unit test

Every service class gets one unit test class. Tests are split into 4 `@Nested` categories: **CRUD, FilterAndPaginate, Validation and business rules, Errors and edge cases**.

### Location and naming
- `src/test/java/com/poly/<service>/services/<Entity>ServiceTest.java`
- One test class per service class.
- Method names describe behavior: `<method>_when<Condition>_<expectedResult>`, e.g. `createBooking_whenRoomUnavailable_throwsException`.

### Setup
- `@ExtendWith(MockitoExtension.class)`. No Spring context, no database, no Testcontainers.
- `@Mock` for the repository, the mapper and any Feign client. `@InjectMocks` for the service.
- Use AssertJ for assertions and the Arrange / Act / Assert structure.
- Build test data in private helper methods inside the test class (e.g. `buildHotel()`, `buildHotelRequest()`).

### Skeleton
```java
@ExtendWith(MockitoExtension.class)
class <Entity>ServiceTest {

    @Mock private <Entity>Repository repository;
    @Mock private <Entity>Mapper mapper;
    @InjectMocks private <Entity>Service service;

    @Nested
    @DisplayName("CRUD")
    class Crud {
        @Test
        void create_whenValidRequest_savesAndReturnsResponse() { }
        @Test
        void getById_whenExists_returnsResponse() { }
        @Test
        void update_whenExists_updatesAndReturnsResponse() { }
        @Test
        void delete_whenExists_deletesEntity() { }
    }

    @Nested
    @DisplayName("FilterAndPaginate")
    class FilterAndPaginate {
        @Test
        void filterAndPaginate_whenNoFilters_returnsAllPaginated() { }
        @Test
        void filterAndPaginate_whenFiltersGiven_passesThemToRepository() { }
        @Test
        void filterAndPaginate_whenNoMatch_returnsEmptyPage() { }
    }

    @Nested
    @DisplayName("Validation and business rules")
    class BusinessRules {
        // one test per rule that exists in the service
    }

    @Nested
    @DisplayName("Errors and edge cases")
    class ErrorsAndEdgeCases {
        @Test
        void getById_whenNotFound_throwsNotFoundException() { }
        @Test
        void update_whenNotFound_neverSaves() { }
        @Test
        void delete_whenNotFound_neverDeletes() { }
    }
}
```

### Category rules

**1. CRUD**
- create: the repository saves, the mapper is called, and the response DTO is returned.
- getById: the entity is found and mapped to a response.
- update: the existing entity is loaded, updated and saved, and the response is returned.
- delete: the repository deletes the entity.

**2. FilterAndPaginate**
- No filters returns everything, paginated.
- Each filter parameter is passed to the repository through the Specification. Use `ArgumentCaptor<Specification>` or `any(Specification.class)` with the `Pageable`.
- Range filters (`From` / `To`) and relation ID filters are covered.
- No match returns an empty `PageResponse`.
- `PageResponse` fields (`page`, `size`, `totalElements`, `totalPages`, `first`, `last`) are correct.
- Content is `Response` DTOs, never entities.

**3. Validation and business rules**
- One test per rule that really exists in the service: duplicates, status transitions, required related entities, and so on.
- A related entity from the same service must exist before saving.
- Test both the allowed case and the rejected case of each rule.

**4. Errors and edge cases**
- A missing ID in getById, update and delete throws the not-found exception.
- Null and boundary inputs behave correctly.
- Repository and Feign client failures propagate.
- On any exception, `verify(repository, never()).save(any())` and `verify(repository, never()).delete(any())`.

### Rules
- Test only methods that exist in the service. Do not invent methods or behavior.
- Every public method needs the happy path and at least one failure case.
- One behavior per test. No test depends on another.
- No `Thread.sleep`, no real network, no real database.
- Do not change production code to make a test pass. If you find a bug, report it and ask me.
- Run `gradlew test` inside the service folder and report the results.
- If a method's behavior is unclear, ask me before writing the test.