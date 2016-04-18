<html>
<head>
	<meta name="layout" content="main" />
	<title>User guide</title>

	<r:require module="user-guide"/>
	<r:require module="codemirror"/>

	<r:script>
		// Draws sidebar with scrollspy. If h1 -> first level title. If h2 -> second level title.
		// Scrollspy uses only titles to track scrolling. div.help-text elements are not significant for the scrollspy.
		new UserGuide("#module-help-tree", "#sidebar")
	</r:script>

	<r:script>
		$(document).ready(function() {
			var textAreaElements = document.querySelectorAll("textarea");
			for (var i=0; i < textAreaElements.length; ++i) {
				CodeMirror.fromTextArea(textAreaElements[i]);
			}

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
<body class="user-guide">

<ui:flashMessage/>

<ui:breadcrumb>
	<g:render template="/help/breadcrumb"/>
</ui:breadcrumb>

<div class="row">
	<div class="col-sm-12">
		<div class="scrollspy-wrapper col-md-9" id="module-help-tree">

			<markdown:renderHtml template="userGuide/what_is_streamr" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/real_life_use_cases" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/getting_started" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/what_is_a_stream" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/what_is_a_service" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/working_with_streams" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/creating_services" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/using_modules" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/live_services" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/dashboards" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/embedded_widgets" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/extensions" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/life_outside_streamr" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/sharing_and_collaboration" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/streaming_data_cookbook" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/module_reference" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/api_reference" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

			<markdown:renderHtml template="userGuide/glossary" />
			<hr style="width: 70%; border-top: #E9570F solid 2px;  margin-top: 30px; margin-bottom: 30px">

		</div>


		<!-- Don't remove this div -->
		<div class="col-xs-0 col-sm-0 col-md-3" id="sidebar"></div>
	</div>
</div>
</body>
</html>
