# simpleWS

Author: ant1mcl

## Overview

This project provides a simple launcher to execute Groovy scripts.
It allows you to easily run Groovy scripts from the command line via a single executable JAR.

## Features
- Launch and execute Groovy scripts from a single executable JAR
- Dynamic loading of additional dependencies at runtime via an external JARs folder
- By default, all .jar files in the `lib` folder are added to the classpath if no folder is specified


## Requirements
- Java 8 or higher
- Maven
- Internet connection for dependency resolution during build

## Build Instructions

1. Clone the repository:
   ```sh
   git clone <repo-url>
   cd simplews
   ```
2. Build the project with Maven:
   ```sh
   mvn clean package
   ```
   The generated JAR will be in the `target` folder (e.g., `simplews-1.0-jar-with-dependencies.jar`).

## Usage

### Simple execution of a Groovy script

```sh
java -jar target/simplews-1.0-jar-with-dependencies.jar path/to/script.groovy [externalJarsFolder] [args...]
```

- If `[externalJarsFolder]` is provided, all .jar files in this folder will be added to the Groovy classpath.
- If not provided, all .jar files in a `lib` folder (located next to the JAR) will be loaded automatically.
- Any additional arguments (`[args...]`) are passed to the Groovy script.

#### Examples

**Use JARs in the default lib folder:**
```sh
java -jar .\simplews.jar .\script.groovy
```

**Specify a folder for additional JARs:**
```sh
java -jar .\simplews.jar .\script.groovy lib http://google.com
```

If no script is provided, a help message will be displayed:
```
Usage: java -jar target/simplews-1.0-jar-with-dependencies.jar <script.groovy> [externalJarsFolder] [args...]
```

## Example Script

Minimal example of a `script.groovy` performing an HTTP (GET) request with http-builder-ng:

```groovy
@Grab('io.github.http-builder-ng:http-builder-ng-apache:1.0.4')
import static groovyx.net.http.HttpBuilder.configure

def http = configure {
    request.uri = 'https://httpbin.org'
}.get {
    request.uri.path = '/get'
}

println http
```

## License

MIT License

---

Maintained by ant1mcl.