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

Use the `POST` endpoint at `/api/v1/login/challenge` to generate a random text called a challenge in the following format: 

```
{
    "id": "challenge-id"
    "challenge": "challenge-text-to-be-signed"
    "expires": "2018-10-22T08:38:59Z"
}
```

You must provide the challenge response before it expires to be authenticated. You can do it with a `POST` at `/api/v1/login/response`. It must contain the challenge, the signature and the Ethereum address in the following format:

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

```
{
    "token": "your-session-token"
    "expires": "2018-10-22T11:38:59Z"
}
```

You can now use this session token to be authenticated by including the following HTTP header in every request:

`Authorization: Bearer your-session-token`

The session token's expiration will be reset at every request so that you don't get logged out while using the API.
