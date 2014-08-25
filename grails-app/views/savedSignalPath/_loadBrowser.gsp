<div id="${browserId}" class="loadBrowserTableWrapper signalpath-browser">
	<table class="table table-condensed table-striped table-hover">
		<thead>
			<tr>
				<g:each in="${headers}">
					<th>${it}</th>
				</g:each>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>

<script>
$(document).ready(function() {
	var browser = $("#${browserId}")

	function appendContent() {
		if (browser.data("requesting") || browser.data("complete"))
			return;
		else
			browser.data("requesting", true)

		browser.append("<div class='loading'>Loading..</div>")

		$.get('${contentUrl}', {
				max: 100,
				offset: browser.find("table tbody tr.offsetRow").length
			}, function(data) {
				var tbody = browser.find("table tbody");
				var oldLength = tbody.find("tr").length;
				tbody.append(data);
				var newLength = tbody.find("tr").length;

				if (oldLength==newLength)
					browser.data("complete", true)
				
				browser.data("requesting", false)
				browser.find(".loading").remove();

				$('tr.selectable', browser).click(function(e) {
					e.stopPropagation()
					e.preventDefault()
					$(browser).trigger('selected', $(this).data('url'))
				})

			}
		)
	}
	
	browser.scroll(function() {
	    if (browser.scrollTop() >= browser.find("table").height() - browser.height())
		    appendContent()
	})

	appendContent()
})

</script>