# Streamr Engine and Editor

Web application containing and serving the Streamr Engine and Editor. Streamr Engine is an event processing system for real-time data. Streamr Editor is a visual programming environment for creating processes (called Canvases) that run on the Engine. 

The application is built on top of the Java VM and Grails web framework. The current implementation runs on the centralised (cloud) version of Streamr infrastructure, detailed in the section Dependencies (below). 

## Dependencies

### Tools
- Grails 2.5.6
- node.js ^8.0.0

A convenient way of installing and managing multiple versions of Grails is [SDKMAN!](http://sdkman.io/install.html). And for node.js there is [nvm](https://github.com/creationix/nvm).

### Service dependencies

Additional services are required to run this web application. The easiest way to get them running (for development purposes) is to use the [streamr-docker-dev](https://github.com/streamr-dev/streamr-docker-dev) tool we provide.

- MySQL
- Kafka
- Zookeeper
- Redis
- [Streamr Cloud Broker](https://github.com/streamr-dev/cloud-broker)

## Building
1. Ensure you have Grails 2.5.6 and node.js version 8.0.0 or newer installed.
2. Clone this repo
3. Fetch all git submodules `git submodule update --init --recursive`
4. Install front-end dependencies with `npm install`
5. If you are using the recommended tool `streamr-docker-dev` then run `streamr-docker-dev start 1`. Otherwise make sure all 3rd party services are running and the the web applications is properly configured with correct hostnames and username/password-combinations.
6. Start the web applicatio nwith `grails run-app`

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
