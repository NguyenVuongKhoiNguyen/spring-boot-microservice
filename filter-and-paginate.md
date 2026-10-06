## Pattern: filterAndPaginate

Every "get list" endpoint uses this pattern: **PageResponse + JPA Specification + Pageable**. Method name is always `filterAndPaginate`. Every column of the table is an optional filter parameter.

### Step 0: create PageResponse first (once per service)
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
- **PageResponse comes first.** Check that `dtos/responses/PageResponse` exists in the service before writing any `filterAndPaginate`.
- **Return type is always `PageResponse<<Entity>Response>`.** Never return an entity, `Page`, `List` or `Slice` from the service or controller.
- **Entities stay inside the repository and service.** The Specification and repository work on entities. The service converts to the Response DTO with `<Entity>Mapper` before returning.
- **Parameters = the table's columns.** Use the exact field names from the model. Do not invent filters.
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
- If a column's type or filtering behavior is unclear, ask me before writing code.