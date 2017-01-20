<a name="data-output"></a>
# Data Output

You can get streaming data output via our [socket.io](http://socket.io/)-based protocol. It uses websockets if available, or various fallback methods if not. For easy usage, we offer a [JavaScript client](#js-client) that works in the browser as well as [node.js](https://nodejs.org). Ready-made clients for other platforms are in the works, please let us know which ones you need.

The data output API can be used to drive external applications using realtime events from Streamr. For example, you could push realtime stock prices into a mobile app, or update player positions in a multiplayer game. Or you could implement a thermostat by controlling warming or cooling based on a temperature measurement.

<g:render template="/help/api/streamr-client" />
