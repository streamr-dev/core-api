# Streamr Engine and Editor

Web application containing and serving the Streamr Engine and Editor. Streamr Engine is an event processing system for real-time data. Streamr Editor is a visual programming environment for creating processes (called Canvases) that run on the Engine. 

Streamr Engine-and-Editor is built on top of the Java VM and Grails web framework. The current implementation runs on the centralised (cloud) version of Streamr infrastructure, detailed in the section *Dependencies* (below). 

## Dependencies

### Tools
- Grails 2.5.6
- node.js ^8.0.0
- npm
- [Chrome Driver](https://code.google.com/p/selenium/wiki/ChromeDriver) and Google Chrome.

A convenient way of installing and managing multiple versions of Grails is [SDKMAN!](http://sdkman.io/install.html). And for node.js there is [nvm](https://github.com/creationix/nvm).

### Service dependencies

Additional services are required to run this web application. The easiest way to get them running (for development purposes) is to use the [streamr-docker-dev](https://github.com/streamr-dev/streamr-docker-dev) tool we provide.

- MySQL
- Kafka
- Zookeeper
- Redis
- [Streamr Cloud Broker](https://github.com/streamr-dev/cloud-broker)

## Building and running

1. Ensure you have the dependencies listed under *Dependencies > Tools* installed.

2. You need to place the `chromedriver` executeble in your `PATH`, and set an environment variable `CHROMEDRIVER` to point to the executable.

3. Clone this repo

4. Fetch all git submodules 
```
git submodule update --init --recursive
````
5. Install front-end dependencies
```
npm install
````

6. Run `streamr-docker-dev start 1` if you are using the recommended tool streamr-docker-dev. Otherwise make sure all services dependencies are running and the the web applications is properly configured to connect to them.

7. Start the web application
```
grails run-app
```

## Publishing
A [Docker image](https://hub.docker.com/r/streamr/broker/) is automatically built and pushed to DockerHub when commits
are pushed to branch `master`.

Currently project has no CI system configured nor are any .jar artifacts published to central repositories.

## Developing

### Front-end

The UI is increasingly implemented with JavaScript libraries React and Redux. The source files must first be transpiled and compiled into bundle file(s). This happens by running `npm run build` in the root directory.

You can also run the development server with `npm run dev` which updates bundle files on-the-fly as changes are detected in source files.

### Back-end


## Testing

- To run unit, integration, and end-to-end (functional) tests: `grails test-app`
- To run front-end JavaScript tests: `npm test`

## License

This software is open source, and dual licensed under [AGPLv3](https://www.gnu.org/licenses/agpl.html) and an enterprise-friendly commercial license.
