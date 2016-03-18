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
	SignalPathBrowser.contentAppender($("#${browserId}"), '${raw(contentUrl)}')
})
</script>
