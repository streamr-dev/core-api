# Streamr Engine and Editor

## Overview

This is a web application containing and serving the Streamr Engine and Editor. The Engine is an event processing system for real-time data. The Editor is a visual programming environment for creating processes (called Canvases) that run on the Engine. 

The application runs on the Java VM and uses the Grails web framework. The current version uses the centralised (cloud) version of Streamr infrastructure, detailed below. 

## Dependencies

Running this app requires some helper infrastructure to be running:

- MySQL
- Kafka
- Zookeeper
- Redis
- [Streamr Cloud Broker](https://github.com/streamr-dev/cloud-broker)

The easiest way to get these running is to use Docker and the [streamr-docker-dev](https://github.com/streamr-dev/streamr-docker-dev) tool we provide.

## Building and running

- You need to have Grails 2.5.6 and Node.js ^8.0.0 installed. (Easiest way is to install Grails via [SDKMAN](http://sdkman.io/install.html) and Node via [nvm](https://github.com/creationix/nvm).)
- Clone the repo
- `git submodule update --init --recursive`
- `npm install`
- Start the infrastructure with `streamr-docker-dev start 1`
- `grails run-app`

## Developing

### React and Redux components

Part of the UI is implemented with libraries like React and Redux. Those files must first be compiled into bundle file(s). This happens by running `npm run build` in the root directory. 

You can also run the development server with `npm run dev`.

## Testing

- `grails test-app`
- `npm test`

Functional tests use Geb with [chromedriver](https://code.google.com/p/selenium/wiki/ChromeDriver) and Google Chrome. You need to place the `chromedriver` executeble in your `PATH`, and set an environment variable `CHROMEDRIVER` to point to the executable.

## License

This software is open source, and dual licensed under [AGPLv3](https://www.gnu.org/licenses/agpl.html) and an enterprise-friendly commercial license.
