# Plain Test

## Rational

Testing is hard. If there is an application that executes tests from plain test files?

## Format 

```
Suite UserTest {
    HTTP CreateUser {
        url   : "http://127.0.0.1"
        method: "POST"
        body  : """
                   {
                       "id": 123,
                       "firstName": "John",
                       "lastName" : "Doe"
                   }
                """
        assert responseCode: 200
    }
}
```
