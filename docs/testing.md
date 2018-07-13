Testing with Spock in Grails
============================
This document outlines our testing strategy and some helpful technical tips on how to write expressive, efficient and
isolated unit tests in Spock and Grails.

## Theory

### Unit tests
Should be short, to-the-point, fast to run, isolated, and not touch external systems such as the file system,
database, HTTP, and so forth.

You should be able to continually run them while developing without having to wait. They server to catch unexpected
changes of behavior, i.e., regression errors.

### Integration tests
Used to verify that compositions of objects and subsystems work as intended. May access external systems such as
databases or the web or 3rd party APIs. Configured to run with full environment. More brittle and higher-level than unit
tests and when errors occur, localization of error typically harder.

Writing an integration test suite against a 3rd party API can be useful in encoding our assumptions about the behavior
of said API, and in the case of an error, localizing whether the error is caused by our internal logic or because of the
3rd party API.

### Functional tests (aka end-to-end tests)
Exercises the application through the user interface and verifies that visible behavior occurs as expected. The behavior
is typically verified by inspecting visual elements on a HTML page or by detecting page transitions. The application is
fully initialized and run.

Functional tests tend to be quite brittle, and figuring out why they break, can be challenging. Nevertheless, they are
necessary in order to verify that the application really works from the viewpoint of the entire system and end-user.

## Testing in Grails with Spock

### Unit tests
Reside in folder [test/unit](../test/unit). Classes extend `import spock.lang.Specification`. When testing a controller
or service class-level annotation `@TestFor(FooService)` can be used to automatically inject an instance into the test
as variable `controller` or `service`.

Class-level annotation `@Mock([DomainObject1, DomainObject2])` can be used to mock the database interactions of domain
objects.

### Integration tests
Reside in folder [test/integration](../test/integration). Classes extend `grails.test.spock.IntegrationSpec`. Database
of environment `test` is used during test execution, but changes are carried out in a transaction and rolled back at the
end. Thus no permanent changes are made to database state even in the presence of errors.

Service dependencies are automatically injected into integration tests by using corresponding field names.

### Tips

#### Injecting services and beans

At least in unit tests, you can inject services (and other beans) in test setup using `defineBeans(Closure)`:
```groovy
class FooSpec extends spock.lang.Specification {
    def barService
    def userService

    def setup() {
        defineBeans {
            barService(HashMapBackedBarService)
            userService(UserService) { it.autowire = true }
        }
    }
    ...
}
```

The optional closure `{ it.autowire = true }` automatically injects the services used by `UserService` itself.

#### Injecting mock/stub or instances of services and beans

```groovy
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.springframework.context.ApplicationContext

class FooSpec extends spock.lang.Specification {
    KafkaService kafkaService = Mock(KafkaService)
	ExampleService exampleService = Mock(ExampleService)

    def setup() {
        // Setup application context
		def applicationContext = Stub(ApplicationContext) {
			getBean(KafkaService) >> kafkaService
			getBean(ExampleService) >> exampleService
		}
        
        // Setup grailsApplication
		def grailsApplication = new DefaultGrailsApplication()
		grailsApplication.setMainContext(applicationContext)
    }
    ...
}
```

#### Testing for exceptions

Use `thrown()`, e.g.,
```groovy
def "it throws exception on foo"() {
    when:
    bar.foo()

    then:
    thrown(RuntimeException)
}
```

To further assert the exception assign the  result of thrown() to a local variable:
```groovy
def "it throws exception on foo"() {
    when:
    bar.foo()

    then:
    RuntimeException ex = thrown()
    ex.message == "Unexpected foo in bar"
}
```

#### Testing threaded code

You might want to checkout [Spock's concurrency helper](http://spockframework.github.io/spock/javadoc/1.0/spock/util/concurrent/package-frame.html).

~~Use `sleep` (not optimal) or some synchronization mechanics as discussed in
<http://www.alexecollins.com/5-tips-unit-testing-threaded-code/>.~~

### Gotchas and troubleshooting

- Mixins such as `@TestFor` and `@Mock` should only be used in unit tests. If you attempt to use these annotations in an
integration test, for example, it will cause weird `ServletContext`-related errors in other, seemingly unrelated
integration test.
[source](http://stackoverflow.com/questions/25327072/grails-2-3-integration-test-behaving-badly-servletcontext-must-not-null)
