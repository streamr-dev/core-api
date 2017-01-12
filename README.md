# unifina-core

## Purpose of this plugin

This is a Grails plugin providing core Streamr platform functionality. This includes, among other stuff:

- Basic core library of modules (packages `com.unifina.signalpath.*`)
- Controllers for instantiating, showing and running SignalPaths (canvases), creating Streams etc.
- The Build view and its resources
- Core webcomponents
- Mechanism of communicating with running SignalPaths (/live/request)

Shared functionality between different actual applications (Streamr webapp, AlgoCanvas, possible white-labeled apps) should go into the core plugin.

This allows for app-specific configuration and extensions to reside at the top level, with shared core functionality in this plugin.

This plugin is used in the app-level projects `unifina-trading` and `streamr-webapp`. Both projects contain this repository in the `plugins/unifina-core` folder as a Git submodule. This allows you to easily develop and test the app against development versions of the core plugin.

## More information

Please refer to the [Developer onboarding document](https://docs.google.com/document/d/1a14cJYjGBwe8-BXeAz08UvE-qa12KLA6OocnXEq_cKU/edit#)

## Testing

`grails test-app` will run all tests. To run the core plugin tests, give the command in the `unifina-core` directory.

There are also `mocha` tests for javascript components. Install <a href="https://nodejs.org/">node.js</a>, then do `npm install` in the `unifina-core` directory. Then you can `npm test`.

Functional tests use Geb with <a href="https://code.google.com/p/selenium/wiki/ChromeDriver">chromedriver</a> and Google Chrome. You need to place the `chromedriver` executeble in your `PATH`, and set an environment variable `CHROMEDRIVER` to point to the executable.
