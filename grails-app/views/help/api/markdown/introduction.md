# Introduction to Streamr APIs

Streamr provides a set of APIs to enable easy integration with other systems. The APIs cover [data input](#data-input), [data output](#data-output), and managing various [resources](#resources) within Streamr (such as Canvases, Dashboards, and Streams).

There are RESTful HTTP endpoints that can be accessed using any HTTP library with ease. Streaming data output is available over websockets. For easy usage, we offer a [Javascript client](#js-client) that works in the browser as well as [node.js](https://nodejs.org). The client is available on [npm](https://www.npmjs.com/package/streamr-client). Clients for other languages are coming soon.

<a name="authentication"></a>
# Authentication

You authenticate to all the RESTful API endpoints using your user API key, found on your [profile page](${createLink(controller:'profile', action:'edit')}). Include the following header on your HTTP request:

`Authorization: token your-api-key`

When reading from or writing to Streams, you can use a Stream-specific anonymous key instead of your user key to avoid exposing it. Anonymous keys can be managed on the Stream edit page.
