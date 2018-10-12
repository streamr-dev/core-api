# Introduction to Streamr APIs

Streamr provides a set of APIs to enable easy integration with other systems. The APIs cover [data input](#data-input), [data output](#data-output), and managing various [resources](#resources) within Streamr (such as Canvases, Dashboards, and Streams).

There are RESTful HTTP endpoints that can be accessed using any HTTP library with ease. Streaming data output is available over websockets. For easy usage, we offer a [Javascript client](#js-client) that works in the browser as well as [node.js](https://nodejs.org). The client is available on [npm](https://www.npmjs.com/package/streamr-client). Clients for other languages are coming soon.

<a name="authentication"></a>
# Authentication

You authenticate to all the RESTful API endpoints using your user API key, found on your [profile page](${createLink(controller:'profile', action:'edit')}). Include the following header on your HTTP request:

`Authorization: token your-api-key`

When reading from or writing to Streams, you can use a Stream-specific anonymous key instead of your user key to avoid exposing it. Anonymous keys can be managed on the Stream edit page.

## Authenticating with Ethereum

If you own an Ethereum account, you can use it to authenticate by signing a challenge with your private key and providing your Ethereum public address for verification.

The endpoint at `/api/v1/login/challenge` will generate a random text called a challenge. Alongside this challenge you will receive its expiration, you must provide the challenge response before it expires to be authenticated.

You can provide your response at `/api/v1/login/response`. It must contain the challenge, the signature and the Ethereum address. If the signature is correct, you will receive a session token with an expiration date and time.

You can now use this session token to be authenticated by including the following HTTP header in every request:

`Authorization: Bearer your-session-token`

The session token's expiration will be reset at every request so that you don't get logged out while using the API.