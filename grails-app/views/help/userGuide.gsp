<html>
<head>
	<meta name="layout" content="main" />
	<title><g:message code="help.userGuide.title" /></title>

	<g:render template="help-page-styling" model="[wrapper: 'user-guide-wrapper']"/>

</head>

<body class="help-page">

	<ui:flashMessage/>

	<ui:breadcrumb>
		<g:render template="/help/breadcrumb"/>
	</ui:breadcrumb>

	<div id="user-guide-wrapper" class="docs-wrapper">

		<markdown:renderHtml template="userGuide/introduction" />
		<hr>

		<!-- <markdown:renderHtml template="userGuide/real_life_use_cases" /> -->
		<!-- <markdown:renderHtml template="userGuide/getting_started" /> -->

		<markdown:renderHtml template="userGuide/streams" />
		<hr>

		<markdown:renderHtml template="userGuide/modules" />
		<hr>

		<markdown:renderHtml template="userGuide/microservices" />
		<hr>

		<markdown:renderHtml template="userGuide/extensions" />
		<hr>

		<!-- <markdown:renderHtml template="userGuide/dashboards" /> -->
		<!-- <markdown:renderHtml template="userGuide/embedded_widgets" /> -->
		<!-- <markdown:renderHtml template="userGuide/life_outside_streamr" /> -->
		<!-- <markdown:renderHtml template="userGuide/sharing_and_collaboration" /> -->
		<!-- <markdown:renderHtml template="userGuide/streaming_data_cookbook" /> -->
		<!-- <markdown:renderHtml template="userGuide/glossary" /> -->

	</div>

</body>
</html>
