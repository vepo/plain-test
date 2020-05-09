# Plain Test

## Rational

Testing is hard. If there is an application that executes tests from plain test files?

## Format 

```
Suite UserTest {
    Properties {
        userContent: """
                     {
                         "id": 123,
                         "firstName": "John",
                         "lastName" : "Doe"
                     }"""
    }
    HTTP CreateUser {
        url   : "http://127.0.0.1"
        method: "POST"
        body  : $userContent
        assert responseCode Equals 200
    }
}
```

# Implemented Plugins

## HTTP

Execute a HTTP Request.

### Parameters

| Name | Description | Required | Default Value |
| :--- | :---------- | :------: | :-----------: | 
| url | The complete URL for the request | true | N/A |
| method | The HTTP method that will be used on request | false | GET |
| timeout | The execution timeout, if it is provided, the request thread will be stopped after the time exceeds | N/A |
| body | The body used on POST or PUT | false | An empty string will be used on POST/PUT |

## System Process

# Ideas for Plugins

## System Backgroud Process

It can start/stop process inspect stdout, stderr or exitCode 
