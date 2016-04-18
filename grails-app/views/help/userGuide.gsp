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
			var codeBlocks = document.querySelectorAll("pre code");
			for (var i=0; i < codeBlocks.length; ++i) {
				var codeBlock = codeBlocks[i]
				var myCodeMirror = CodeMirror(function(elt) {
					codeBlock.parentNode.replaceChild(elt, codeBlock);
				}, {value: codeBlocks[i].innerHTML.trim()});
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


		<!-- Don't remove this div -->
		<div class="col-xs-0 col-sm-0 col-md-3" id="sidebar"></div>
	</div>
</div>
</body>
</html>
