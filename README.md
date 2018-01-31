# Streamr Engine and Editor

## Overview

This is a Grails web application containing and serving the Streamr Engine and Editor.

## Dependencies

Running this app requires some helper infrastructure to be running:

- MySQL
- Kafka
- Zookeeper
- Redis
- `streamr-broker`

The easiest way to get these running is to get them in a Docker image we provide. (TODO: add instructions).

Also:
- Grails
  - Install instructions for Ubuntu:
    - `sudo apt-get install zip unzip`
    - `curl -s "https://get.sdkman.io" | bash` (as in [SDKMAN install instructions](http://sdkman.io/install.html))
    - `source "$HOME/.sdkman/bin/sdkman-init.sh"`
    - `sdk install grails 2.3.11`

## Building and running

- You need to have Grails 2.5.6 and Node.js ^8.0.0 installed.
- Clone the repo
- `git submodule update --init --recursive`
- `npm install`
- `grails run-app`

## Developing

### React and Redux components

Part of the UI is implemented with libraries like React and Redux. Those files must first be compiled into bundle file(s). This happens by running `npm run build` in the root directory. 

You can also run the development server with `npm run dev`.

## Testing

- `grails test-app`
- `npm test`

Functional tests use Geb with <a href="https://code.google.com/p/selenium/wiki/ChromeDriver">chromedriver</a> and Google Chrome. You need to place the `chromedriver` executeble in your `PATH`, and set an environment variable `CHROMEDRIVER` to point to the executable.
