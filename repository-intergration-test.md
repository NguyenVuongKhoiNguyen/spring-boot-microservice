## Pattern: repository integration test

Every repository gets one integration test class that runs against a **real PostgreSQL database** through Testcontainers. Never H2.

### Location and naming
- `src/test/java/com/poly/<service>/repositories/<Entity>RepositoryTest.java`
- One test class per repository.
- Method names: `<method>_when<Condition>_<expectedResult>`, e.g. `findAll_whenStarRatingFromGiven_returnsOnlyMatchingHotels`.

### Setup
- `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)` + `@Testcontainers`.
- One static `PostgreSQLContainer` with `@Container @ServiceConnection`, image `postgres:16-alpine`. Docker must be running.
- Use `TestEntityManager` to insert test data and to `flush()` / `clear()` before assertions, so the query hits the database instead of the first-level cache.
- Build test data in private helper methods (e.g. `persistHotel(...)`).
- `src/test/resources/application.yml`: `spring.jpa.hibernate.ddl-auto: create-drop` and `eureka.client.enabled: false`.
- Each test runs in a rolled-back transaction (the `@DataJpaTest` default), so tests don't affect each other.

### Skeleton
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class <Entity>RepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private <Entity>Repository repository;
    @Autowired private TestEntityManager em;

    @Nested
    @DisplayName("CRUD")
    class Crud {
        @Test
        void save_whenValidEntity_persistsAndGeneratesId() { }
        @Test
        void findById_whenExists_returnsEntity() { }
        @Test
        void save_whenExistingEntity_updatesFields() { }
        @Test
        void deleteById_whenExists_removesRow() { }
    }

    @Nested
    @DisplayName("Specification filters")
    class SpecificationFilters {
        @Test
        void findAll_whenNoFilters_returnsAllRows() { }
        @Test
        void findAll_whenStringFilterGiven_matchesCaseInsensitiveContains() { }
        @Test
        void findAll_whenRangeFilterGiven_returnsRowsInsideRange() { }
        @Test
        void findAll_whenRelationIdGiven_returnsRowsOfThatParent() { }
        @Test
        void findAll_whenSeveralFiltersGiven_combinesThemWithAnd() { }
        @Test
        void findAll_whenNothingMatches_returnsEmptyPage() { }
    }

    @Nested
    @DisplayName("Pagination and sorting")
    class PaginationAndSorting {
        @Test
        void findAll_whenPageRequested_returnsCorrectSliceAndTotals() { }
        @Test
        void findAll_whenSortGiven_ordersResults() { }
        @Test
        void findAll_whenPageBeyondLastPage_returnsEmptyContent() { }
    }

    @Nested
    @DisplayName("Constraints and custom queries")
    class ConstraintsAndQueries {
        @Test
        void save_whenRequiredColumnIsNull_throwsException() { }
        @Test
        void save_whenUniqueColumnDuplicated_throwsException() { }
        // one test per custom query method that exists in the repository
    }
}
```

### Category rules

**1. CRUD**
- save: the row is persisted, the ID is generated, and the fields read back match after `flush()` and `clear()`.
- findById: found, and an unknown ID returns `Optional.empty()`.
- update: changed fields are persisted.
- delete: the row is gone afterward.

**2. Specification filters**
- Test `findAll(<Entity>Specification.filter(...), pageable)`, the same call the service makes.
- Cover every filter parameter the Specification has:
  - String: case-insensitive contains
  - Boolean, enum, ID: exact match
  - Numbers, dates and times: `From` only, `To` only, both, and the boundary values (inclusive)
  - Relation filters: by the related ID
- Null and blank filters are ignored.
- Several filters combine with AND.
- No match returns an empty page.

**3. Pagination and sorting**
- `totalElements`, `totalPages`, page size and `isFirst` / `isLast` are correct.
- Sorting works ascending and descending.
- A page past the end returns empty content.
- Insert enough rows to span more than one page.

**4. Constraints and custom queries**
- Null in a non-nullable column, a duplicate unique value, and a missing parent reference each throw the right exception.
- One test per custom query method (`@Query`, derived queries) that exists in the repository.

### Rules
- Test only repository methods, Specifications and constraints that exist in the code. Do not invent them.
- Test data must be created inside each test or a helper, never shared between tests.
- Always `em.flush()` and `em.clear()` before asserting on query results.
- Use AssertJ. No mocks: no `@Mock`, no Mockito in repository tests.
- Do not use H2, `Thread.sleep` or hardcoded IDs. Use the IDs returned by `persist`.
- Do not change production code or entities to make a test pass. If you find a bug, report it and ask me.
- Run `gradlew test` inside the service folder and report the results.
- If an entity's constraints or a query's behavior are unclear, ask me before writing the test.