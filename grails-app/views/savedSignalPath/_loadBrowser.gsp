<div id="${browserId}" class="loadBrowserTableWrapper signalpath-browser">
	<table class="table table-condensed table-striped table-hover">
		<thead>
			<tr>
				<th>Name</th>
				<th class="dateCreated">Created</th>
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
