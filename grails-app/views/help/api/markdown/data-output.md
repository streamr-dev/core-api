<a name="data-output"></a>
# Data Output

There are two APIs for requesting data from Streamr into external applications: our websocket-based streaming API, and our RESTful HTTP API.

The streaming API can be used to control external applications using realtime events from Streamr. For example, you could push realtime stock prices into a mobile app, or update player positions in a multiplayer game. Or you could implement a thermostat by controlling warming or cooling based on a temperature measurement. The streaming API pushes new events to subscribed clients immediately when they become available. For easy usage of the streaming API, we offer a [Javascript client](#js-client) that works in the browser as well as [node.js](https://nodejs.org). Clients for other languages are coming soon.

<g:render template="/help/api/streamr-client" />

## Data Output via HTTP

Events in streams can be queried via HTTP. Details on the endpoints can be found in the [API Explorer](#api-explorer) under the endpoints related to streams.

For example, the following endpoint would return the 5 most recent messages in a stream partition:

`https://www.streamr.com/api/v1/streams/{id}/data/partitions/{partition}/last?count=5`
