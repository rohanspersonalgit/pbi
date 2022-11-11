# phone-number-parser
## Problem Statement
https://docs.google.com/document/d/1JH8UD2U6is-lJ_ogadIjWASj9ZJw6z0sOIENk_zsomg/edit?usp=sharing
## First-time Setup
1. Clone this repository to some directory on your machine like `~/dev/phone_number_parser`
1. Install IntelliJ IDEA Community (free - available via https://www.jetbrains.com/idea/download/)
1. Install OpenJDK 11 (64-bit). We recommend AdoptOpenJDK, which can be installed like so:
   - OSX: 
     - Install `brew` first by visiting https://brew.sh and following the instructions.
     - Run these commands: 
       ```
       $ brew tap AdoptOpenJDK/openjdk
       $ brew install --cask adoptopenjdk11
       ```
   - Windows: https://adoptopenjdk.net/installation.html#x64_win-jdk
   - Linux: https://adoptopenjdk.net/installation.html#linux-pkg
1. Install Docker Community Edition: https://docs.docker.com/get-docker/
1. `cd` to the directory you cloned this repository in and bring up the docker container for the DB:
   ```
   $ cd ~/dev/phone_number_parser
   $ docker-compose up
   ```
   If this worked, you'll see a message that looks something like this:
   ```
   db_1  | 2020-12-22 05:57:02.006 UTC [1] LOG:  database system is ready to accept connections
   ```
1. Open IntelliJ, and open the `phone_number_parser` directory.
1. Click Intellij IDEA | Preferences | Build, Execution, Deployment | Compiler | Annotation Processors and ensure Enable annotation processing is checked. Click OK to apply the changes.
1. Click Build -> Rebuild Project and wait for the build to complete successfully
1. Right click on `Application.kt` and click `Run`
1. You should see a log message at the bottom of the console that looks like this:
   ```
   2020-12-21 21:57:08,995 main DEBUG LoggerContext[name=3d4eac69, org.apache.logging.log4j.core.LoggerContext@626abbd0] started OK.
   ```
1. In your browser, navigate to http://localhost:8080/health which should return `{"status": "UP"}`. This means the project is running successfully!

## Common Tasks
- To add a new database migration, just add a new .sql file to resources/db/migration/ with file format `V2__AddSomething.sql`. Migrations get run automatically on application initialization.
- To see which migrations have been applied, visit the following URL after you've started the application: http://localhost:8080/flyway
## Implementation notes
- Micronaut (https://micronaut.io) was chosen as the web framework because that's what we use at DoorDash.
- PostgreSQL, JDBC (connection pooler: hikari), and flyway (database migrations, see docs below) have been set up for you to save you a bit of time. Feel free to swap these components out if you prefer a different database.
- Testcontainers has been set up for you - with testcontainers, the database is automatically cleared after all the tests in the same file complete. Make sure to have your tests in the same file use different data/inputs, otherwise writes could overlap unexpectedly and cause test failures!
## Helpful 3rd Party Library Documentation
### http-client

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

### sql-jdbi

- [Micronaut Jdbi documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbi)

### testcontainers

- [https://www.testcontainers.org/](https://www.testcontainers.org/)

### flyway

- [Micronaut Flyway Database Migration documentation](https://micronaut-projects.github.io/micronaut-flyway/latest/guide/index.html)

- [https://flywaydb.org/](https://flywaydb.org/)

### jdbc-hikari

- [Micronaut Hikari JDBC Connection Pool documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbc)

