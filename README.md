# dynamic-operations-with-quarkus-reactive

Simple application showcasing dynamic execution of operations using quarkus reactive.

## Usage

- Build and run the application using maven:

````shell
mvn quarkus:dev
````

- Access http://localhost:8080/swagger
- Operation names must be passed as query parameters
- Available operations are "a", "b" and "c"
- All operations emit their results on a delay:
    - a -> 3 seconds
    - b -> 2 seconds
    - c -> 1 second
- Operations may be executed sequentially or concurrently

