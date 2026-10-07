## Pattern: filterAndPaginate

Every "get list" endpoint uses this pattern: **PageResponse + JPA Specification + Pageable**. Method name is always `filterAndPaginate`. Every column of the table is an optional filter parameter.

### Step 0: get the real columns from the database (postgres MCP server)
Before writing any Specification, read each table's actual columns through the **postgres MCP server**. Never guess column names or types, and never take them from memory.

1. Check the MCP connection first: list the tables in the database. If the MCP server is unavailable or the table isn't found, **stop and ask me**. Do not fall back to guessing from the entity.
2. Get the exact table name from the entity's `@Table(name = "...")`.
3. Run this read-only query for each table:
```sql
   SELECT column_name, data_type, is_nullable
   FROM information_schema.columns
   WHERE table_name = '<table_name>'
   ORDER BY ordinal_position;
```
4. Compare the result with the entity's fields. Map each snake_case column to its camelCase field (`star_rating` → `starRating`, `hotel_id` → relation `hotel`, filter param `hotelId`). The Specification uses the **entity field name** in `root.get("...")`, not the column name.
5. If a column exists in the database but not in the entity, or the entity has a field with no column, **don't add or skip it silently. Report it and ask me.**
6. Pick the filter type from the column's `data_type`:
   - `character varying`, `text`, `varchar`: String, case-insensitive contains
   - `boolean`, enum types, `uuid`, ID columns (`bigint` primary and foreign keys), `sort_order`: exact match
   - `integer`, `smallint`, `numeric`, `decimal`, `double precision`: range `<field>From` / `<field>To`
   - `date`, `time`, `timestamp`, `timestamptz`: range `<field>From` / `<field>To`
7. Skip sensitive columns (password hashes, tokens, secrets). Ask me if unsure.
8. Only filter the tables the service owns. Do not build filters for another service's tables, even though they share the database.

MCP rules:
- **Read-only.** Only run `SELECT` against `information_schema` (or the table, if I ask). Never run `INSERT`, `UPDATE`, `DELETE`, `ALTER`, `DROP` or `CREATE` through MCP.
- Do not change the database schema. Schema changes live only in the Flyway scripts in `database/migration/`.
- Do not print real row data or secrets in your report.

### Step 1: create PageResponse first (once per service)
Create it before any `filterAndPaginate` method. Each service gets its own copy, since services share no code. If it already exists in the service, reuse it and do not recreate it.

**dtos/responses/`PageResponse`**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
```
The generic `T` is always the **Response DTO**, e.g. `PageResponse<HotelResponse>`. Never an entity.

### Layers (same method name in service and controller)

**repositories/`<Entity>Repository`**: add `JpaSpecificationExecutor`
```java
public interface <Entity>Repository extends JpaRepository<<Entity>, Long>, JpaSpecificationExecutor<<Entity>> {}
```

**repositories/`<Entity>Specification`**: one static method, one parameter per column
```java
public final class <Entity>Specification {
    private <Entity>Specification() {}

    public static Specification<<Entity>> filter(<Type> column1, String column2, ...) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (column1 != null) predicates.add(cb.equal(root.get("column1"), column1));
            if (column2 != null && !column2.isBlank())
                predicates.add(cb.like(cb.lower(root.get("column2")), "%" + column2.toLowerCase() + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

**services/`<Entity>Service`**: convert entities to DTOs with the mapper
```java
public PageResponse<<Entity>Response> filterAndPaginate(<Type> column1, String column2, ..., Pageable pageable) {
    Page<<Entity>> page = repository.findAll(<Entity>Specification.filter(column1, column2, ...), pageable);
    return PageResponse.from(page.map(mapper::toResponse));
}
```

**controllers/`<Entity>Controller`**
```java
@GetMapping
public PageResponse<<Entity>Response> filterAndPaginate(
        @RequestParam(required = false) <Type> column1,
        @RequestParam(required = false) String column2,
        Pageable pageable) {
    return service.filterAndPaginate(column1, column2, pageable);
}
```
Request example: `GET /api/<entity>?column2=abc&page=0&size=20&sort=column1,desc`

### Rules
- **Step 0 comes first.** Read the columns through the postgres MCP server before writing any Specification. State which tables and columns you found when you report back.
- **PageResponse comes second.** Check that `dtos/responses/PageResponse` exists in the service before writing any `filterAndPaginate`.
- **Return type is always `PageResponse<<Entity>Response>`.** Never return an entity, `Page`, `List` or `Slice` from the service or controller.
- **Entities stay inside the repository and service.** The Specification and repository work on entities. The service converts to the Response DTO with `<Entity>Mapper` before returning.
- **Parameters = the table's columns, as returned by MCP.** Use the exact field names from the model that correspond to those columns. Do not invent filters.
- **All filters are optional.** A null or blank value is skipped, and no filters returns everything, paginated.
- **Filter by type:**
  - String: case-insensitive `like` (contains)
  - Boolean, enum, ID (including cross-service IDs like `userId`), and `sortOrder`: exact match (`equal`)
  - Numbers (price, rating, capacity, quantity): range with `<field>From` / `<field>To`
  - LocalTime / LocalDate / Instant: range with `<field>From` / `<field>To`
  - JPA relations: filter by the related ID (e.g. `Long hotelId`) with exact match on `root.get("hotel").get("id")`
- `Pageable` carries `page`, `size` and `sort`. Never build pagination by hand.
- Set `spring.data.web.pageable.max-page-size: 100` and `default-page-size: 20` in `application.yml`.
- No filtering logic in controllers or services. It lives only in the Specification class.
- No new folders: `PageResponse` goes in `dtos/responses/`, the Specification class in `repositories/`.
- If a column's type or filtering behavior is unclear, or the database and the entity disagree, ask me before writing code.