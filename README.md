# Streamr Core API

![CI & CD](https://github.com/streamr-dev/core-api/workflows/CI%20&%20CD/badge.svg)

RESTful API containing the backend for Streamr Core. With the Streamr Network now fully decentralized, the only remaining features in this centralized API are related to the Marketplace:

- Creating and listing products on the Streamr Marketplace
- Managing the mapping between products and streams (what streams are in what products)

The Marketplace will become fully decentralized, so this API is being **phased out** in the near future.

The application uses the Grails web framework and runs on Java VM.

## Dependencies

### Tools

- Java 8
- Grails 2.5.6
- node.js ^16.0.0 (for e2e tests)
- npm

A convenient way of installing and managing multiple versions of Grails is [SDKMAN!](https://sdkman.io/install).

### Service dependencies

Additional services are required to run this web application. The easiest way to get them all up and running (for
development purposes) is to use the [streamr-docker-dev](https://github.com/streamr-dev/streamr-docker-dev) tool we
provide.

- MySQL
- Redis
- A Streamr Network consisting of [broker nodes](https://github.com/streamr-dev/broker)

You might also want to run the [Core frontend](https://github.com/streamr-dev/core-frontend).

## Building and running

1. Ensure you have the dependencies listed under *Dependencies > Tools* installed.

2. Clone this repo

3. Run `make start` if you are using the recommended tool streamr-docker-dev.
   Otherwise make sure all services dependencies are running and the the web applications is properly configured to
   connect to them.

4. Start the backend application

```
make run
```

## CI

The project uses [GitHub Actions](https://github.com/streamr-dev/core-api/actions) to automatically run tests for each
commit to `main` and pull requests.

## Docker

A [Docker image](https://hub.docker.com/r/streamr/core-api/) is automatically built and pushed to DockerHub
when commits are pushed to branch `main`.

## IDE

We provide sensible default configurations for IntelliJ IDEA but project can be developed with other IDEs as well.

### Testing

- To run unit tests only, use `make test-unit`
- To run integration tests only, use `make test-integration`
- To run end-to-end REST API tests, do `make test-e2e`
- To run unit, integration, and e2e tests use `make test`

These are also available as pre-shared run configurations if you use IntelliJ IDEA.

### Core API

This codebase comprises two logical parts:

- API which allows users to create and manage streams, products, and other Streamr resources. The API
  controllers and services are mainly written in Groovy and use the Grails web framework.

When you run the app with `grails run-app`, most changes to source code files are automatically hot reloaded into the
running JVM process.

#### Useful resources

- [Grails 2.5.6 Framework Reference Documentation (single page)](https://grails.github.io/grails2-doc/2.5.6/guide/single.html)
- [Spock Framework Reference Documentation (single page)](http://spockframework.org/spock/docs/1.1/all_in_one.html)
- [Grails Database Migration Plugin Documentation](https://web.archive.org/web/20210119030814/https://grails-plugins.github.io/grails-database-migration/1.4.0/)
- [Spock Concurrency Tools](http://spockframework.org/spock/javadoc/1.1/spock/util/concurrent/package-summary.html)

## License

This software is open source, and dual licensed under [AGPLv3](https://www.gnu.org/licenses/agpl.html) and an
enterprise-friendly commercial license.
