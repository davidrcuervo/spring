# IntelliJ IDEA
## Run Junit test
```bash
-Djasypt.encryptor.password=secret -Dspring.config.additional-location=C:\Users\username\DevSpace\webapp\API\,C:\Users\username\DevSpace\webapp\
```
## Shortcuts

+ Abbreviation: ```sapi```
+ Description: ```Api description in API\application.yml```
+ Applicable: ```Other```
```yml
# METHOD: $METHOD$ | BODY: $BODY_DESCRIPTION$ | CODE: $CODE$ | RESP: $RESP$
```
+ Abbreviation: ```sapilong```
+ Description: ```Api description in API\application.yml```
+ Applicable: ```Other```
```yaml
# ACTION: $ACTION_DESCRIPTION$
# METHOD: $METHOD$
# PATH VARIABLES:
# - $PATH_VARIABLE_1$: $PATH_VARIABLE_DESCRIPTION_1$
# - $PATH_VARIABLE_2$: $PATH_VARIABLE_DESCRIPTION_2$ 
# QUERY PARAMETERS:
# - $PARAMETER$: $PARAMETER_DESCRIPTION$
# REQUEST BODY: $BODY_DESCRIPTION$
# ON SUCCESS:
# - Code: $CODE$
# - Body: $RETURN_BODY$
# ERRORS:
# - 400: $ERROR_400_DESCRIPTION$ 
# - 401: $ERROR_401_DESCRIPTION$
# - 403: $ERROR_403_DESCRIPTION$
# - 404: $ERROR_404_DESCRIPTION$
# - 500: $ERROR_500_DESCRIPTION$
```

+ Abbreviation: ```slog```
+ Description: ```insert log```
+ Applicable: ```Java -> Statement```
```java
log.$LEVERL$("$TIER$_$CLASS$::$method$ | $mess$");
```

+ Abbreviation: ```slogd```
+ Description: ```insert debug log```
+ Applicable: ```Java -> Statement```
```java
log.debug("$TIER$_$CLASS$::$method$ | $mess$");
```

+ Abbreviation: ```smvc```
+ Description: ```create mvc to test end point```
+ Applicable: ```Java -> Statement```
```java
mvc.perform($METHOD$($ADDRESS$)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString($OBJECT$))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + $USER_TOKEN$))
        .andExpect(status().$EXPECTED$())
```

+ Abbreviation: ```stest```
+ Description: ```It works for create task of pending implementation```
+ Applicable: ```Java -> Declaration```
```java
@Test
public void $NAME_OF_TEST$() throws Exception {
    fail();
}
```

+ Abbreviation: ```svalue```
+ Description: ```Add spring boot value shortcut```
+ Applicable: ```Java -> Declaration```
```java
@Value("${$VALUE$}")
private String
```
