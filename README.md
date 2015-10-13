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

Please refer to the [Developer onboarding document](https://docs.google.com/document/d/1a14cJYjGBwe8-BXeAz08UvE-qa12KLA6OocnXEq_cKU/edit#)

## Testing

`grails test-app` will run all tests. To run the core plugin tests, give the command in the `unifina-core` directory.

There are also `mocha` tests for javascript components. Install <a href="https://nodejs.org/">node.js</a>, then do `npm install` in the `unifina-core` directory. Then you can `npm test`.

The top-level apps may contain functional tests (browser tests). Functional tests use Geb with <a href="https://code.google.com/p/selenium/wiki/ChromeDriver">chromedriver</a> and Google Chrome. You need to place the `chromedriver` executeble in your `PATH`, and set an environment variable `CHROMEDRIVER` to point to the executable.

## API methods

Some actions can be called by sending JSON requests to API endpoints. The user's API `key` and API `secret` must be provided as part of all requests. All requests must have the `Content-Type: application/json` header.

### ``POST /api/stream/create``

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

`curl -i -X POST -H "Content-Type: application/json" -d "{\"key\":\"my-api-key\",\"secret\":\"my-api-secret\",\"name\":\"API generated stream\",\"description\":\"Stream description\"}" http://www.streamr.com/api/stream/create`


### ``POST /api/stream/lookup``

Queries the stream id based on your localId:

```
{
	key: "", 	// User API key
	secret: "", // User API secret

	localId: "my-stream-id"
}
```

Example success response:

```
{
	stream: "" // stream id
}
```

Example error response (response code 404):

```
{
	success: false,
	error: "stream not found"
}
```

Example using `curl`:

`curl -i -X POST -H "Content-Type: application/json" -d "{\"key\":\"my-api-key\",\"secret\":\"my-api-secret\",\"localId\":\"my-stream-local-id\"}" http://www.streamr.com/api/stream/lookup`


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

For request-response cycles with running SignalPaths, you use the `/api/live/request` endpoint. Your request will be internally redirected to whichever server the live SignalPath is actually running on. This is not really part of the public api.

The request content should be the following:

```
{
	key: "", 		// User API key
	secret: "", 	// User API secret
	id: 0,			// Id of the RunningSignalPath
	hash: undefined,// Hash of the module within the RunningSignalPath. Can be omitted when the message recipient is not a module but the RunningSignalPath itself.
	channel: ""		// Instead of the id and hash, messages can be targeted at a module with an UI channel id.
}
```
