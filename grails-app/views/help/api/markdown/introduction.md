# Introduction to Streamr APIs

Streamr provides a set of APIs to enable easy integration with other systems. The APIs cover [data input](#data-input), [data output](#data-output), and managing various [resources](#resources) within Streamr (such as Canvases, Dashboards, and Streams).

The data input and resource management APIs are RESTful HTTP endpoints that can be accessed using any HTTP library with ease. For streaming data output, we support the [socket.io](http://socket.io/) protocol. It uses websockets if available, or various fallback methods if not. For easy usage, we offer a [JavaScript client](#js-client) that works in the browser as well as [node.js](https://nodejs.org). The client is available on [npm](https://www.npmjs.com/package/streamr-client).