# Plain Test

## Rational

Testing is hard. If there is an application that executes tests from plain test files?

## Format 

```
TestStuite T1 {
    send HTTP POST /user
        body """{
            "id": 123,
            "firstName": "John",
	    "lastName" : "Doe"
	}"""
}
```
