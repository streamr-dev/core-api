# Streamr Engine and Editor 

[![Build Status](https://travis-ci.org/streamr-dev/engine-and-editor.svg?branch=master)](https://travis-ci.org/streamr-dev/engine-and-editor)

Web application containing the backend for Streamr Core, facilitating things like:
- Creating streams
- Creating and running canvases
- Creating dashboards
- Creating products for the Streamr Marketplace 

The application uses the Grails web framework and runs on Java VM. 

## Dependencies

### Tools
- Java 8
- Grails 2.5.6
- node.js ^8.0.0
- npm

A convenient way of installing and managing multiple versions of Grails is [SDKMAN!](http://sdkman.io/install.html).

### Service dependencies

Additional services are required to run this web application. The easiest way to get them all up and running (for development purposes) is to use the [streamr-docker-dev](https://github.com/streamr-dev/streamr-docker-dev) tool we provide.

- MySQL
- Redis
- Cassandra
- A Streamr Network consisting of [broker nodes](https://github.com/streamr-dev/broker)

You might also want to run the [Core UI](https://github.com/streamr-dev/streamr-platform). 

## Building and running

1. Ensure you have the dependencies listed under *Dependencies > Tools* installed.

2. Clone this repo

3. Run `streamr-docker-dev start 1` if you are using the recommended tool streamr-docker-dev. Otherwise make sure all services dependencies are running and the the web applications is properly configured to connect to them.

4. Start the backend application
```
grails run-app
```

5. (Optional) Start the [Core frontend](https://github.com/streamr-dev/streamr-platform) if you need it.

## CI

The project uses [Travis CI](https://travis-ci.org/streamr-dev/engine-and-editor) to automatically run tests for each commit to `master` and pull requests.

## Docker

A [Docker image](https://hub.docker.com/r/streamr/engine-and-editor/) is automatically built and pushed to DockerHub when commits are pushed to branch `master`.

## IDE

We provide sensible default configurations for IntelliJ IDEA but project can be developed with other IDEs as well.

### Testing

- To run unit and integration tests use `grails test-app`
- To run unit tests only, use `grails test-app -unit`
- To run integration tests only, use `grails test-app -integration`
- To run end-to-end REST API tests, do `cd rest-e2e-tests && npm install && npm test`

These are also available as pre-shared run configurations if you use IntelliJ IDEA.

### Core API & Engine

This codebase comprises two logical parts:

- API which allows users to create and manage streams, canvases, products, and other Streamr resources. The API controllers and services are mainly written in Groovy and use the Grails web framework.
- The Engine is written mostly in Java and is responsible for executing canvases (user-defined processes which process, analyze and act upon real-time event data. The APIs, on the other hand, is responsible for API(s), rendered web pages and other front-facing functionality.

When you run the app with `grails run-app`, most changes to source code files are automatically hot reloaded into the running JVM process.

#### Useful resources
- [Grails 2.5.6 Framework Reference Documentation (single page)](https://grails.github.io/grails2-doc/2.5.6/guide/single.html)
- [Spock Framework Reference Documentation (single page)](http://spockframework.org/spock/docs/1.1/all_in_one.html)
- [Grails Database Migration Plugin Documentation (single page)](http://grails-plugins.github.io/grails-database-migration/1.4.0/guide/single.html)
- [Spock Concurrency Tools](http://spockframework.org/spock/javadoc/1.1/spock/util/concurrent/package-summary.html)

### Coding your own modules

There are already modules that allow running code on canvas, e.g. JavaModule. They are, however, "last resort" patches for cases where the existing modules don't offer the required functionality, but the functionality also isn't reusable enough to warrant developing a custom module.

If the required functionality is complex, or if it depends on libraries or external code, you'll need to set up the development environment following the above steps. Upside is of course, after it's done, you also get IDE support (we use [IntelliJ IDEA](https://www.jetbrains.com/idea/)), and you can write unit tests etc.

A module is as simple as a Java class that extends `AbstractSignalPathModule` and implements the critical module-specific functionality (`sendOutput` and `clearState`). The code is going to look the same as in a JavaModule, only wrapped in a Java class. Please take a look at the [XOR module](https://github.com/streamr-dev/engine-and-editor/blob/master/src/java/com/unifina/signalpath/bool/Xor.java) for a simple example / boilerplate / starting point. For unit testing a module, see the [XorSpec unit test class](https://github.com/streamr-dev/engine-and-editor/blob/master/test/unit/com/unifina/signalpath/bool/XorSpec.groovy).

You also need to add your module to the `module` table in the MySQL database so that Editor finds your module, and you can add it on the canvas.

We want to integrate quality module contributions to the project. To get your custom module to the master, the following is required:
* the module code <small>(*MyModule* extends AbstractSignalPathModule)</small>
* unit tests <small>(*MyModuleSpec* extends spock.lang.Specification)</small>
* database migration <small>(under grails-app/migrations, [see XOR module migration for example](https://github.com/streamr-dev/engine-and-editor/blob/master/grails-app/migrations/core/2017-01-17-xor-module.groovy))</small>
* all this in a neat git branch with no conflicts with master, and a [pull request in github](https://github.com/streamr-dev/engine-and-editor/pull/229)

We'll be happy to help with completing these steps. Best ways to reach the Streamr developer community are the [official Telegram group](https://t.me/streamrdata) and the community-run [developer forum](http://forum.streamr.dev/).

## License

This software is open source, and dual licensed under [AGPLv3](https://www.gnu.org/licenses/agpl.html) and an enterprise-friendly commercial license.
