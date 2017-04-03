<a name="data-output"></a>
# Data Output

You can get streaming data output via our websocket-based protocol. For easy usage, we offer a [Javascript client](#js-client) that works in the browser as well as [node.js](https://nodejs.org). Clients for other languages are coming soon.

The data output API can be used to drive external applications using realtime events from Streamr. For example, you could push realtime stock prices into a mobile app, or update player positions in a multiplayer game. Or you could implement a thermostat by controlling warming or cooling based on a temperature measurement.

<g:render template="/help/api/streamr-client" />

## Data Output via HTTP

Data can also be queried via HTTP using the following endpoints. Details on these endpoints can be found in the [API Explorer](#api-explorer).

`https://www.streamr.com/api/v1/streams/:id/data/:partition/last/:count`

Use this endpoint to query the last `:count` messages from a Stream.

`https://www.streamr.com/api/v1/streams/:id/data/:partition/fromTimestamp/:timestamp`
`https://www.streamr.com/api/v1/streams/:id/data/:partition/fromTimestamp/:fromTimestamp/toTimestamp/:toTimestamp`

Use the above endpoints to query messages since a timestamp, or within a timestamp range. The timestamps are given in Java/Javascript format, ie. milliseconds since Jan 1st 1970 UTC.
