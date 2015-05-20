# unifina-core

Unfortunately this README is *very* incomplete. Please let Henri know which topics you required information for but did not find any.

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

## Setting up the development environment

- Install the Grails version required by the project (currently 2.3.7)
- Add the Grails `bin` directory to `PATH`
- You may also want to install `GGTS` (an Eclipse-based IDE for Groovy/Grails). Give it more memory by editing GGTS.ini and editing the -Xmx value!
- Set the `GRAILS_HOME` and `JAVA_HOME` environment variables
- Checkout the top-level project(s) you need (`unifina-trading` or `streamr-webapp`)
- Do a `git submodule update --init`, which will pull this core plugin as well as other submodules
- Run the app in GGTS or on the command line with `grails run-app`

## Testing

`grails test-app` will run all tests. To run the core plugin tests, give the command in the `unifina-core` directory.

There are also `mocha` tests for javascript components. Install <a href="https://nodejs.org/">node.js</a>, then do `npm install` in the `unifina-core` directory. Then you can `npm test`.

The top-level apps may contain functional tests (browser tests). Functional tests use Geb with <a href="https://code.google.com/p/selenium/wiki/ChromeDriver">chromedriver</a> and Google Chrome. You need to place the `chromedriver` executeble in your `PATH`, and set an environment variable `CHROMEDRIVER` to point to the executable.

## API methods

Some actions can be called by sending JSON requests to API endpoints. The user's API `key` and API `secret` must be provided as part of all requests. All requests must have the `Content-Type: application/json` header.

### ``POST /api/createStream``

Creates a new API stream. Example request:

```
{
	key: "", 	// User API key
	secret: "", // User API secret

	name: "Stream name",
	description: "Stream description",
	localId: "my-stream-id" // A user-defined id for the stream. If not given, the name will be used.
}
```

Example success response:

```
{
	success: true,
	stream: "", // Stream id,
	auth: "" 	// Stream auth key

	name: "Stream name",
	description: "Stream description",
	localId: "my-stream-id"
}
```

Example error response (response codes 40x):

```
{
	success: false,
	error: "error description",
	details: [] // optional error details
}
```

Example using `curl`:

`curl -i -X POST -H "Content-Type: application/json" -d "{\"key\":\"my-api-key\",\"secret\":\"my-api-secret\",\"name\":\"API generated stream\",\"description\":\"Stream description\"}" http://www.streamr.com/api/createStream`

## Webcomponents

A number of core webcomponents are available at `/webcomponents/<component-name>.html` with liberal CORS policy. An example of using them:

```html
<!-- import these in HEAD -->
<script src="<local-path>/webcomponents.js"></script>
<link rel="import" href="<server-url>/webcomponents/index.html">

<!-- use the webcomponents in BODY -->
<streamr-client server="<socketio-server-url>" autoconnect="true">
</streamr-client>

<streamr-label channel="<channel-id>"></streamr-label>
```

## Runtime requests

For request-response cycles with running SignalPaths, you use the `/live/request` endpoint. Your request will be internally redirected to whichever server the live SignalPath is actually running on.

The following request parameters should be supplied:

Name 	| Description
--- 	| ---
msg		| A stringified JSON message. The content is arbitrary but must include at least a key called `type`, which is a String that describes the request, for example `stopRequest` or `paramChange`.
id 		| Id of the RunningSignalPath the message is intended for
hash	| Hash of the module within the RunningSignalPath. Can be omitted when the message recipient is not a module but the RunningSignalPath itself.
channel | Instead of the id and hash, messages can be targeted at a module with an UI channel id.

