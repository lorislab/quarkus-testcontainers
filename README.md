# quarkus-testcontainers

[![License](https://img.shields.io/github/license/lorislab/quarkus-testcontainers?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/lorislab/quarkus-testcontainers/build/master?logo=github&style=for-the-badge)](https://github.com/lorislab/quarkus-testcontainers/actions?query=workflow%3Amaster)
[![Maven Central](https://img.shields.io/maven-central/v/org.lorislab.quarkus/quarkus-testcontainers?logo=java&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/org.lorislab.quarkus/quarkus-testcontainers)
[![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/lorislab/quarkus-testcontainers?logo=github&style=for-the-badge)](https://github.com/lorislab/quarkus-testcontainers/releases/latest)

Quarkus testcontainers extension

* [Quarkus](https://quarkus.io/)
* [Testcontainers](https://www.testcontainers.org/)

## Pipeline and tests

1. Build project, run the unit test and build native image: 
    * mvn clean package -Pnative (1) 
2. Build the docker image
    * docker build
3. Run the integration test
    * mvn failsafe:integration-test failsafe:verify
4. Push the docker image 
    * docker push
    
(1) build native image with a docker image: 
    * mvn clean package -Pnative -Dquarkus.native.container-build=true        

## How to write the tests

Add this maven test dependency to the project.
```xml
<dependency>
    <groupId>org.lorislab.quarkus</groupId>
    <artifactId>quarkus-testcontainers</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```
Create abstract test class which will set up the docker test environment. The default location of the docker compose file
is `src/test/resources/docker-compose.yaml`

```java
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.lorislab.quarkus.testcontainers.DockerComposeService;
import org.lorislab.quarkus.testcontainers.DockerService;
import org.lorislab.quarkus.testcontainers.DockerComposeTestResource;

@QuarkusTestResource(DockerComposeTestResource.class)
public abstract class AbstractTest {

    @DockerService("quarkus-test")
    protected DockerComposeService app;

    @BeforeEach
    public void init() {
        if (app != null) {
            RestAssured.port = app.getPort(8080);
        }
    }
}
```
Create a common test for unit and integration test
```java
public class DeploymentRestControllerT extends AbstractTest {

    @Test
    public void deploymentTest() {
        // ...        
    }
}
```
Unit test
```java
@QuarkusTest
public class DeploymentRestControllerTest extends DeploymentRestControllerT {

}
```
Integration test
```java
import org.lorislab.quarkus.testcontainers.DockerComposeTest;

@DockerComposeTest
public class DeploymentRestControllerTestIT extends DeploymentRestControllerT {

}
```

## Maven settings
Unit test maven plugin
```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>${surefire-plugin.version}</version>
    <configuration>
        <systemProperties>
            <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
        </systemProperties>
    </configuration>
</plugin>
```
Integration test maven plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>${surefire-plugin.version}</version>
    <executions>
        <execution>
            <id>native</id>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
            <phase>integration-test</phase>
        </execution>
    </executions>
    <configuration>
        <systemPropertyVariables>
            <test.integration>true</test.integration>
        </systemPropertyVariables>
    </configuration>                
</plugin>
```
The system property `<test.integration>true</test.integration>` activate the integration test.

## Docker labels

| label   | values | default | description |
|---|---|---|---|
| test.integration=true | `boolean` | `true` | enable the docker for the integration test |
| test.unit=true | `boolean` | `true` | enable the docker for the unit test |
| test.image.pull=DEFAULT | `string` | `DEFAULT,ALWAYS,MAX_AGE` | pull docker image before test |
| test.image.pull.max_age | `string` | `PT10` | only for the `MAX_AGE` pull docker image before test if older than duration. Default: 10s |
| test.Wait.forLogMessage.regex= | `string` | `null` | regex of the WaitStrategy for log messages |
| test.Wait.forLogMessage.times=1 | `int` | `1` | the number of times the pattern is expected in the WaitStrategy |
| test.Log=true | `boolean` | `true` | enabled log of the docker container |
| test.priority=100 | `int` | `100` | start priority |
| test.property.<name>=<value> | `string` | `null` | set the system property with <name> and <value> in the tests |
| test.env.<name>=<value> | `string` | `null` | set the environment variable with <name> and <value> in the docker container |

The value of the test.property.* or test.env.* supported this syntax:
* simple value: `123` result: 123
* host of the service: `$${host:<service>}` the host of the service `<service>`
* port of the service: `$${port:<service>:<port>}` the port number of the `<port>` of the `<service>` service
* url of the service: `$${url:<service>:<port>}` the url of the service `http://<service>:<port>`
* system property: `$${prop:<name>`}
* environment variable: `${env:<name>`}
 
 Example:
 ```bash
test.property.quarkus.datasource.url=jdbc:postgresql://$${host:postgres}:$${port:postgres:5432}/p6?sslmode=disable
```
The system property `quarkus.datasource.url` will be set to 
`jdbc:postgresql://localhost:125432/p6?sslmode=disable` if the docker image host of the 
postgres is `localhost` and tet containers dynamic port ot the container port `5432` is set to
`125432` value.

## Docker compose example

```yaml
version: "2"
services:
  quarkus-postgres:
    container_name: quarkus-postgres
    image: postgres:10.5
    environment:
      POSTGRES_DB: "test"
      POSTGRES_USER: "test"
      POSTGRES_PASSWORD: "test"
    labels:
      - "test.Wait.forLogMessage.regex=.*database system is ready to accept connections.*\\s"
      - "test.Wait.forLogMessage.times=2"
      - "test.log=true"
      - "test.property.quarkus.datasource.url=jdbc:postgresql://$${host:quarkus-postgres}:$${port:quarkus-postgres:5432}/p6?sslmode=disable"
    ports:
      - "5432:5432"
    networks:
      - test
  quarkus-test:
    container_name: quarkus-test
    image: quarkus-test:latest
    ports:
      - "8080:8080"
    labels:
      - "test.unit=false"
      - "test.priority=101"
      - "test.image.pull=DEFAULT"
      - "test.env.QUARKUS_DATASOURCE_URL=jdbc:postgresql://quarkus-postgres:5432/test?sslmode=disable"
    networks:
      - test
networks:
  test:
```

## How to release this project

Create new release run
```bash
mvn semver-release:release-create
```

Create new patch branch run
```bash
mvn semver-release:patch-create -DpatchVersion=X.X.0
```
