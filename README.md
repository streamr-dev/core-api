# unifina-core

Unfortunately this README is *very* incomplete. Please let Henri know which topics you required information for but did not find any.

## Purpose of this plugin

This is a Grails plugin providing core Streamr platform functionality. This includes, among other stuff:

- Basic core library of modules (packages `com.unifina.signalpath.*`)
- Controllers for instantiating, showing and running SignalPaths (canvases)
- The Build view and its resources
- Core webcomponents
- Mechanism of communicating with running SignalPaths (/live/request)

Shared functionality between different actual applications (Streamr webapp, AlgoCanvas, possible white-labeled apps) should go into the core plugin.

This allows for app-specific configuration and extensions to reside at the top level, with shared core functionality in this plugin.

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

