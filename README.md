# Streamr Engine and Editor 

[![Build Status](https://travis-ci.org/streamr-dev/engine-and-editor.svg?branch=master)](https://travis-ci.org/streamr-dev/engine-and-editor)

Web application containing and serving the Streamr Engine, Marketplace and Core. Streamr Engine is an event processing system for real-time data. Streamr Core is a visual programming environment for creating processes (called Canvases) that run on the Engine. 

Streamr Engine and Core is built on top of the Java VM and Grails web framework. The current implementation runs on the centralised (cloud) version of Streamr infrastructure. 

## Dependencies

### Tools
- Grails 2.5.6
- Java 8

A convenient way of installing and managing multiple versions of Grails is [SDKMAN!](http://sdkman.io/install.html).

### Service dependencies

Additional services are required to run this web application. The easiest way to get them up and running (for development purposes) is to use the [streamr-docker-dev](https://github.com/streamr-dev/streamr-docker-dev) tool we provide.

- MySQL
- Kafka
- Zookeeper
- Redis
- [Streamr Cloud Broker](https://github.com/streamr-dev/cloud-broker)

## Building and running

1. Ensure you have the dependencies listed under *Dependencies > Tools* installed.

2. Clone this repo

3. Run `streamr-docker-dev start 1` if you are using the recommended tool streamr-docker-dev. Otherwise make sure all services dependencies are running and the the web applications is properly configured to connect to them.

4. Start the web application
```
grails run-app
```

## Publishing
A [Docker image](https://hub.docker.com/r/streamr/broker/) is automatically built and pushed to DockerHub when commits
are pushed to branch `master`.

Currently project doesn't publish any .jar artifacts to central repositories.

## Developing

We provide sensible default configurations for IntelliJ IDEA but project can be developed with other IDEs as well.

### Testing

- To run unit, integration, and end-to-end (functional) use `grails test-app`
- To run back-end unit tests only, use `grails test-app -unit`
- To run back-end integration tests only, use `grails test-app -integration`

These are also available as pre-shared run configurations if you use IntelliJ IDEA.

### Back-end

The back-end consists of two logical parts. The Engine is written mostly in Java and is responsible for executing arbitrary user-defined Canvases that process, analyze and act upon real-time event data. The Editor, on the other hand, is responsible for API(s). It is mostly written in Groovy and utilizes facilities provided by the Grails framework.

When you run the Engine+Editor web app with `grails run-app` or `grails test run-app`, most changes to source code files are automatically hot reloaded into the running JVM process.


#### Useful resources
- [Grails 2.5.6 Framework Reference Documentation (single page)](https://grails.github.io/grails2-doc/2.5.6/guide/single.html)
- [Spock Framework Reference Documentation (single page)](http://spockframework.org/spock/docs/1.1/all_in_one.html)
- [Grails Database Migration Plugin Documentation (single page)](http://grails-plugins.github.io/grails-database-migration/1.4.0/guide/single.html)
- [Spock Concurrency Tools](http://spockframework.org/spock/javadoc/1.1/spock/util/concurrent/package-summary.html)

### Coding your own modules

There are already modules that allow running code on canvas, e.g. JavaModule and Javascript module. They are, however, "last resort" patches for cases where the existing modules don't offer the required functionality, but the functionality also isn't reusable enough to warrant developing a custom module.

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
