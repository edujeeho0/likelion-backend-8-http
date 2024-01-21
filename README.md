# HTTP 다루기

HTTP는 서버 클라이언트 구조에서 서로 주고받는 데이터가 어떤 형식으로 작성되어야 하는지가 정의된
OSI 7계층 중 응용계층에 해당하는 통신 규약이다.

### 요청 예시

- Request Line: HTTP Method, URL의 Path, HTTP 버전이 명시되는 곳.
- Request Headers: 요청에 대한 부수적인 정보가 추가되는 곳. 요청 자체를 설명하는 내용이 Key - Value 형태로 작성된다.
- Request Body: 실제로 처리하고 싶은 데이터를 추가하는 곳. 요청에 따라 생략 가능. (GET 요청 등)

```text
GET /example HTTP/1.1

Host: www.example.com
Accept: text/html,application/xhtml+xml
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36
```

### 응답 예시

- Status Line: 응답의 처리 상태를 나타내는 곳. Status Code와 메시지가 담긴다.
- Response Headers: Request Headers와 비슷하게 응답에 대한 부수적인 정보가 추가되는 곳.
- Response Body: 실제 응답의 내용이 추가되는 곳.

```text
HTTP/1.1 200 OK

Content-Type: text/html
Content-Length: 1024
<html>
  // ... 생략
```

## `@RequestMapping`

`@Controller` 객체 내부에는 `@RequestMapping` 어노테이션이 붙은 메서드를 통해
HTTP 요청을 처리한다. 이때 `@RequestMapping`에 인자를 전달하여 어떤 조건을 만족하는
요청에 대하여 실행되는 지를 결정할 수 있다.

```java
@RequestMapping(
        value = "/example/{pathVar}",
        method = { RequestMethod.GET, RequestMethod.POST },
        consumes = MediaType.APPLICATION_JSON_VALUE,
        headers = "x-likelion=hello",
        params = "likelion=hello"
)
public String example(@PathVariable("pathVar") String pathVar) {
    log.info("GET or POST /example/" + pathVar);
    return "index";
}
```

- `path` / `value` : 요청 URL의 경로를 설정. 할당된 경로 외의 요청에 대해서는 메소드가 실행되지 않는다.
- `method` : 요청의 HTTP Method를 설정합니다. 할당된 HTTP Method에 대해서만 실행됩니다.
- `consumes` : 요청의 `Content-Type` Header.
- `produces` : 응답의 `Content-Type` Header.
- `headers` : 포함 또는 포함하지 않아야 하는 HTTP Header를 설정.
- `params` : 포함 또는 포함하지 않아야 하는 Query Parameter를 설정.

## `@RequestHeader`

요청에 포함된 헤더를 메서드 매개변수에 `@RequestHeader`를 첨부하여 가져올 수 있다.

특정 헤더의 값을 확인하는 경우

```java
@PostMapping("/header-one")
public String getHeader(
        @RequestHeader("x-likelion-some-header") String targetHeader
) {
    log.info("POST /header-one header: " + targetHeader);
    return "index";
}
```

반드시 포함해야 하는지를 설정하고 싶다면 `required` 활용.

```java
@RequestHeader(value = "x-likelion-string", required = false) String targetStrHeader
```

어떤 자료형이 들어올지를 알고 있다면 그 자료형으로 받을 수 있다. 호환이 되지 않을경우 400 에러 발생.

```java
@RequestHeader(value = "x-likelion-integer", required = false) Integer targetIntHeader
```

요청에 포함된 모든 헤더를 가져올 수도 있다.
```java
@PostMapping(
        "/headers"
)
public String getHeaders(
        @RequestHeader HttpHeaders headers,
        @RequestHeader Map<String, String> headerMap,
        @RequestHeader MultiValueMap<String, String> headerMvMap
) {
    headers.forEach((key, value) 
        -> log.warn(String.format("%s: %s, %s", key, value, value.getClass())));
    headerMap.forEach((key, value) 
        -> log.warn(String.format("%s: %s, %s", key, value, value.getClass())));
    headerMvMap.forEach((key, value) 
        -> log.warn(String.format("%s: %s, %s", key, value, value.getClass())));
    log.info("POST /headers");
    return "index";
}
```
단, `Map`의 경우 같은 이름의 헤더가 여럿이라면 제일 첫 값만 받는다. `MultiValueMap`은 `List` 처럼 활용 가능.

## `@RequestBody`, `@ResponseBody`

HTTP 요청 / 응답의 Body를 지정하기 위한 어노테이션.

```java
@Controller
public class ArticleController {
    @PostMapping(
            "/dto"
    )
    @ResponseBody
    public ArticleDto body(@RequestBody ArticleDto dto) {
        log.info("POST /dto, body: " + dto.toString());
        LocalDateTime now = LocalDateTime.now();
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        return dto;
    }
}
```

- `@RequestBody`: 사용자가 첨부한 Body 데이터를 Java 객체 형태로 번역해서 매개변수에 할당해준다.
- `@ResponseBody`: 반환하는 Java 객체를 직렬화된 형태의 데이터(JSON 등)로 응답한다.

만약 `@Controller` 대신 `@RestController`를 사용하는 경우, 모든 메서드의 반환형에 `@ResponseBody`가
덧붙여진다.

## `ResponseEntity`

`@ResponseBody`는 응답 Body의 형태를 지정한다. 좀더 정교하게 응답을 조정하고 싶다면 `ResponseEntity`활용 가능.

```java
@PostMapping(
        value = "entity"
)
public ResponseEntity<ArticleDto> entity(@RequestBody ArticleDto dto) {
    log.info("POST /entity, body: " + dto.toString());
    LocalDateTime now = LocalDateTime.now();
    dto.setCreatedAt(now);
    dto.setUpdatedAt(now);
    return new ResponseEntity<>(dto, HttpStatus.CREATED);
}
```

빌더 패턴의 형태로도 활용이 가능하다.

```java
@PostMapping(
        value = "entity"
)
public ResponseEntity<ArticleDto> entity(@RequestBody ArticleDto dto) {
    log.info("POST /entity, body: " + dto.toString());
    LocalDateTime now = LocalDateTime.now();
    dto.setCreatedAt(now);
    dto.setUpdatedAt(now);

    return ResponseEntity.ok()
            .header("x-likelion-response", "foo")
            .body(dto);
}
```

## `HttpServletRequest`, `HttpServletResponse`

본래 Java EE에서 사용하는 `HttpServletRequest`, `HttpServletResponse` 객체를 매개변수로 받을 수 있다.
그 외에 사용할 수 있는 여러 종류의 인자가 있으므로, [공식 문서](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html)를 확인하자.

