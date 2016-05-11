<r:require module="scrollspy-helper"/>
<r:require module="codemirror"/>

<r:script>
	$(function() {
		var wrapper = $("#${wrapper}")
		var padding = $("<div class='col-xs-0 col-lg-1'/>")
		var sidebar = $("<div class='col-xs-0 col-sm-0 col-md-3 col-lg-3' />")
		wrapper.addClass("scrollspy-wrapper col-md-9 col-lg-offset-2 col-lg-6")
		wrapper.wrap($("<div class='row' />"))
		wrapper.parent(".row").append(padding)
		wrapper.parent(".row").append(sidebar)

		// Draws sidebar with scrollspy. If h1 -> first level title. If h2 -> second level title.
		// Scrollspy uses only titles to track scrolling. div.help-text elements are not significant for the scrollspy.
		new ScrollSpyHelper(wrapper, sidebar)

		// style vanilla elements rendered from markdown with bootstrap styles
		$("table").addClass("table table-striped table-hover")

		// style code blocks with codemirror
		var codeBlocks = document.querySelectorAll("pre code");
		for (var i=0; i < codeBlocks.length; ++i) {
			var codeBlock = codeBlocks[i]
			var myCodeMirror = CodeMirror(function(elt) {
				codeBlock.parentNode.replaceChild(elt, codeBlock);
			}, {value: codeBlocks[i].innerHTML.trim()});
		}
	})
</r:script>
