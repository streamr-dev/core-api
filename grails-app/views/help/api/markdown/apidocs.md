# Introduction to Streamr APIs

Streamr provides a set of APIs for easy integration with other systems. The APIs cover [authentication](#authentication), [data input](#data-input), [data output](#data-output), and managing various [resources](#resources) within Streamr (such as Streams, Canvases, Products, and Dashboards).

There are RESTful HTTP endpoints that can be accessed using any HTTP library with ease. You can explore these endpoints using the [API explorer](#api-explorer).

For efficiently publishing and subscribing to data in realtime, using the websocket API is recommended. 

## Client libraries

For easy usage of both the HTTP and websocket APIs, there are official and community-maintained client libraries available for various languages:
  
<a name="libraries"></a>
- [JavaScript (official)](https://github.com/streamr-dev/streamr-client-javascript) - client works in the browser as well as node.js. The package is available on [npm](https://www.npmjs.com/package/streamr-client).
- [Java (official)](https://github.com/streamr-dev/streamr-client-java) - work-in-progress but already implements the most important set of functionality.
- Python (community) - coming soon

If you'd like to contribute a client library and get it listed here, please get in touch on [Telegram](https://t.me/streamrdata), [Reddit](https://www.reddit.com/r/streamr/) or [email](mailto:contact@streamr.com)!

<a name="authentication"></a>
# Authentication

A session token is required to make requests to the REST API endpoints or over the websocket protocol. You can obtain a session token by authenticating either using an API key or by signing a cryptographic challenge using an Ethereum private key.

Once you get a session token using one of the below methods, see the section on [using it](#using-the-session-token).

## Authenticating with an API key

Any number of API keys can be attached to your user. You can manage your API keys on your [profile page](${createLink(controller:'profile', action:'edit')}).

When reading from or writing to Streams, you can use a Stream-specific anonymous key instead of your user key to avoid exposing it. Anonymous keys can be managed on the details page of a Stream.

To obtain a session token using an API key, send a `POST` request to the `/api/v1/login/apikey` endpoint with a JSON body like the one below:

```
{
    "apiKey": "YOUR-API-KEY-HERE"
}
``` 

## Authenticating with Ethereum

You can use an Ethereum private key to authenticate by signing a challenge with it and providing your Ethereum public address for verification.

Use the `POST` endpoint at `/api/v1/login/challenge/YOUR-PUBLIC-ADDRESS` to generate a random text called a challenge, which looks like the following: 

```
{
    "id": "challenge-id"
    "challenge": "challenge-text-to-be-signed"
    "expires": "2018-10-22T08:38:59Z"
}
```

To authenticate, you must provide a response before the challenge expires. You can do it with a `POST` to `/api/v1/login/response`. It must contain the challenge, the signature and the Ethereum address in the following format:

```
{
    "challenge": {
	    "id": "challenge-id",
	    "challenge": "challenge-text-that-you-signed"
    },
    "signature": "signature-of-the-challenge",
    "address": "your-public-ethereum-address"
}
```

The signature must follow the convention described [here](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-712.md). The secp256k1 ECDSA algorithm is applied on the keccak256 hash of a string derived from the challenge text:

`sign(keccak256("\x19Ethereum Signed Message:\n" + len(challengeText) + challengeText)))`

If the signature is correct, you will receive a session token with an expiration date and time in the following format:

<a name="using-the-session-token"></a>
## Using the session token

By using one of the above authentication methods, you will obtain a session token response, which looks like this: 

```
{
    "token": "YOUR-SESSION-TOKEN"
    "expires": "2018-10-22T11:38:59Z"
}
```

You can now use this session token to make authenticated requests by including an `Authorization` header on every HTTP request with content as follows:

`Authorization: Bearer YOUR-SESSION-TOKEN`

The session token's expiration will be reset on every request to prevent you from getting logged out while using the API. If the token expires, you can obtain a new one exactly as before.

<a name="data-input"></a>
# Data input

## Data input over websocket

The websocket protocol is easiest to use with one of the available [client libraries](#libraries). 

Below is an example of publishing a message using the JS client:

```
// Create the client and authenticate using an API key:
const client = new StreamrClient({
    apiKey: 'MY-API-KEY'
})

// Here is the event we'll be sending
const msg = {
    hello: 'world',
    random: Math.random()
}

// Publish the event to the Stream
client.publish('MY-STREAM-ID', msg)
    .then(() => console.log('Sent successfully: ', msg))
    .catch((err) => console.error(err))
```

If there isn't a client library available for your language, you can dive into the details of the [websocket protocol](https://github.com/streamr-dev/streamr-client-protocol-js/blob/master/PROTOCOL.md).

## Data input over HTTP

You can write events to streams by POSTing JSON objects to the below API endpoint. Note that the stream id is part of the URL:

`https://www.streamr.com/api/v1/streams/:id/data`

The body of the request should be a JSON object, encoded in UTF-8, containing the key-value pairs representing your data.

Example using node.js + restler:

```javascript
var restler = require('restler');

var msg = {
	foo: "hello",
	bar: 24.5
}

restler.post('https://www.streamr.com/api/v1/streams/MY-STREAM-ID/data', {
    headers: {
        Authorization: "Bearer MY-SESSION-TOKEN"
    },
	data: JSON.stringify(msg)
})
```

Example using python + requests:

```python
import requests

msg = {
	'foo': 'hello',
	'bar': 24.5
}

requests.post('https://www.streamr.com/api/v1/streams/MY-STREAM-ID/data', json=msg, headers={'Authorization': 'Bearer MY-SESSION-TOKEN'})
```

Example using `curl`:

```
curl -i -X POST -H "Authorization: Bearer MY-SESSION-TOKEN" -d "{\"foo\":\"hello\",\"bar\":24.5}" https://www.streamr.com/api/v1/streams/MY-STREAM-ID/data
```

### Response codes

code | description
---- | -----------
200  | Success (the response is empty)
400  | Invalid request
401  | Permission denied
403  | Authentication failed
404  | Stream not found
500  | Unexpected error

<a name="data-output"></a>
# Data output

There are two APIs for requesting data from Streamr into external applications: the websocket-based streaming API, and the HTTP API.

The streaming API can be used to control external applications using realtime events from Streamr. For example, you could push realtime stock prices into a mobile app, or update player positions in a multiplayer game. Or you could implement a thermostat by controlling warming or cooling based on a temperature measurement. The streaming API pushes new events to subscribed clients immediately when they become available. 

## Data output over websocket

The websocket protocol is easiest to use with one of the available [client libraries](#libraries). 

Below is an example of subscribing to a Stream using the JS client:

```javascript
// Create the client and authenticate using an API key:
const client = new StreamrClient({
    apiKey: 'MY-API-KEY'
})

// Subscribe to a stream
const subscription = client.subscribe(
    {
        stream: 'MY-STREAM-ID',
    },
    function(message) {
        // This function will be called when new messages occur
        console.log(JSON.stringify(message))
    }
)
```

If there isn't a client library available for your language, you can dive into the details of the [websocket protocol](https://github.com/streamr-dev/streamr-client-protocol-js/blob/master/PROTOCOL.md).

## Data output over HTTP

Events in streams can be queried via HTTP. Details on the endpoints can be found in the [API Explorer](#api-explorer) under the endpoints related to streams.

For example, the following endpoint would return the 5 most recent messages in a stream (or to be more precise, the default partition 0 of a stream):

`https://www.streamr.com/api/v1/streams/{id}/data/partitions/0/last?count=5`

<a name="api-explorer"></a>
# API explorer

Base URL: `https://www.streamr.com/api/v1`

The HTTP API covers session management, data input, data output, and managing Streamr resources such as Canvases, Streams, and Dashboards. The endpoints allow you to list, create, read, update and delete the resources, as well as execute resource-specific actions such as start and stop Canvases.

Use the below tool to explore and test the API.

