<a name="resources"></a>
# Managing Streamr Resources

Base URL: `https://www.streamr.com/api/v1`

Streamr resources such as Canvases, Streams, and Dashboards can be managed via our RESTful API. The endpoints allow you to list, create, read, update and delete the resources, as well as execute resource-specific actions such as start and stop Canvases.

## Authentication

You authenticate to the resource API using your API key, found on your [profile page](${createLink(controller:'profile', action:'edit')}). Include the following header on your HTTP request:

`Authorization: token your-api-key`

## API Explorer

Use the below tool to explore and test the resource API.