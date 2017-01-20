<r:require module="scrollspy-helper"/>
<r:require module="codemirror"/>
<r:require module="marked"/>

<r:script>
	$(function() {
		var markdownEl = $("#${markdownEl}")
		var wrapper = $("#${wrapper}")

		// Render markdown
		marked.setOptions({
			gfm: true
		})
		var markdown = markdownEl.text()
		wrapper.prepend(marked(markdown))

		var sidebar = $("<div class='col-xs-0 col-sm-0 col-md-3 col-lg-4' />")

		wrapper.addClass("scrollspy-wrapper col-md-9 col-lg-offset-1 col-lg-7")
		wrapper.wrap($("<div class='row' />"))
		wrapper.parent(".row").append(sidebar)

		wrapper.wrapInner("<div class='panel-body'/>")
		wrapper.wrapInner("<div class='panel panel-default'/>")

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
		// FOUC is prevented by having the content hidden on load. Now show it!
		wrapper.show()
		// Need to refresh codemirrors after showing
		$('.CodeMirror').each(function(i, el) {
			el.CodeMirror.refresh();
		});

		// Fix offset of anchor links.
		var offset = 80
		wrapper.find("a").each(function() {
			var href = $(this).attr("href")
			if (href && href.startsWith("#")) {
				var el = $("a[name="+href.substring(1)).add(href)
				if (el.length == 0) {
					console.log("Anchor "+href+" is a link target, but does not exist on page!")
				}
				else {
					$(this).click(function(event) {
						if (el[0].scrollIntoView !== undefined) {
							event.preventDefault()
							el[0].scrollIntoView()
							scrollBy(0, -(offset-30))
						}
					})
				}
			}
		})
	})
</r:script>
