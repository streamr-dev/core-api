<html>
<head>
	<meta name="layout" content="main" />
	<title><g:message code="help.userGuide.title" /></title>

	<g:render template="help-page-styling" model="[wrapper: 'user-guide-wrapper']"/>

	<r:script>
		$(document).ready(function() {
			var offset = 80

			// Fix offset of anchor links.
			$("#module-help-tree a").each(function() {
				var href = $(this).attr("href")
				if (href && href.startsWith("#")) {
					console.log("href", href)
					$(this).click(function(event) {
						event.preventDefault()
						if($($(this).attr('href'))[0]){
							$($(this).attr('href'))[0].scrollIntoView()
							scrollBy(0, -(offset-30))
						}
					})
				}
			})
		});
	</r:script>

</head>

<body class="help-page">

	<ui:flashMessage/>

	<ui:breadcrumb>
		<g:render template="/help/breadcrumb"/>
	</ui:breadcrumb>

	<div id="user-guide-wrapper">

		<markdown:renderHtml template="userGuide/introduction" />
		<hr>

		<!-- <markdown:renderHtml template="userGuide/real_life_use_cases" /> -->

		<markdown:renderHtml template="userGuide/getting_started" />
		<hr>

		<markdown:renderHtml template="userGuide/streams" />
		<hr>

		<markdown:renderHtml template="userGuide/modules" />
		<hr>

		<markdown:renderHtml template="userGuide/services" />
		<hr>

		<markdown:renderHtml template="userGuide/extensions" />
		<hr>

		<markdown:renderHtml template="userGuide/dashboards" />
		<hr>

		<markdown:renderHtml template="userGuide/embedded_widgets" />
		<hr>

		<markdown:renderHtml template="userGuide/life_outside_streamr" />
		<hr>

		<!-- <markdown:renderHtml template="userGuide/sharing_and_collaboration" /> -->
		<!-- <markdown:renderHtml template="userGuide/streaming_data_cookbook" /> -->
		<!-- <markdown:renderHtml template="userGuide/glossary" /> -->

	</div>

</body>
</html>
