<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="help.api.title" /></title>

    <r:require module="swagger"/>
	<g:render template="help-page-styling" model="[wrapper: 'api-docs-wrapper']"/>

</head>

<body class="help-page">

	<ui:flashMessage/>

	<ui:breadcrumb>
		<g:render template="/help/apiBreadcrumb"/>
	</ui:breadcrumb>

	<div id="api-docs-wrapper" class="docs-wrapper">
		<markdown:renderHtml template="api/introduction" />
		<hr>
		<markdown:renderHtml template="api/data-input" />

		<hr>
		<markdown:renderHtml template="api/data-output" />
		<hr>
		<markdown:renderHtml template="api/resources" />
	</div>
</body>
</html>
