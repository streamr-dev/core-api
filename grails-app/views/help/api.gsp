<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="help.api.title" /></title>

    <r:require module="swagger"/>
	<g:render template="help-page-styling" model="[markdownEl: 'api-docs-markdown', wrapper: 'api-docs-wrapper']" />

</head>

<body class="help-page">

	<ui:flashMessage/>

	<ui:breadcrumb>
		<g:render template="/help/apiBreadcrumb"/>
	</ui:breadcrumb>

<script id="api-docs-markdown" type="text/x-markdown">
<g:render template="api/introduction" />
<hr>
<g:render template="api/data-input" />
<hr>
<g:render template="api/data-output" />
<hr>
<g:render template="api/resources" />
</script>
	<div id="api-docs-wrapper" class="docs-wrapper">
		<g:render template="api/swagger"/>
	</div>
</body>
</html>
