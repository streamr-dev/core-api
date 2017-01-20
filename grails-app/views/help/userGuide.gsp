<html>
<head>
	<meta name="layout" content="main" />
	<title><g:message code="help.userGuide.title" /></title>
	<g:render template="help-page-styling" model="[markdownEl: 'user-guide-markdown', wrapper: 'user-guide-wrapper']"/>
</head>

<body class="help-page">

	<ui:flashMessage/>

	<ui:breadcrumb>
		<g:render template="/help/breadcrumb"/>
	</ui:breadcrumb>

<script id="user-guide-markdown" type="text/x-markdown">
<g:render template="userGuide/introduction"/>
<!-- real_life_use_caes.gsp getting_started.gsp -->
<hr>
<g:render template="userGuide/streams"/>
<hr>
<g:render template="userGuide/modules" />
<hr>
<g:render template="userGuide/microservices" />
<hr>
<g:render template="userGuide/extensions" />
<hr>
<!-- userGuide/dashboards, userGuide/embedded_widgets, userGuide/life_outside_streamr, userGuide/sharing_and_collaboration, userGuide/streaming_data_cookbook, userGuide/glossary -->
</script>

	<div id="user-guide-wrapper" class="docs-wrapper">
	</div>

</body>
</html>
