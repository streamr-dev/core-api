<g:if test="${feedFiles?.size()>0}">
	<div class="history">
		<ui:labeled label="Range">
  			This stream has archived history from 
  			<span class="history-start-date"><g:formatDate date="${feedFiles[0].beginDate}" timeZone="UTC" format="${message(code:'default.dateOnly.format')}"/></span>
  			 to 
  			<span class="history-end-date"><g:formatDate date="${feedFiles[feedFiles.size() -1].endDate}" timeZone="UTC" format="${message(code:'default.dateOnly.format')}"/></span>.
		</ui:labeled>
		<ui:labeled label="Delete data up to and including">
			<form id="history-delete-form" class="form-inline">
				<g:hiddenField name="id" value="${ stream.id }" />
				<ui:datePicker id="history-delete-date" name="date" value="${feedFiles[0].beginDate}" startDate="${feedFiles[0].beginDate}" endDate="${feedFiles[feedFiles.size() -1].endDate}" class="form-control input-sm"/>
				<button id="history-delete-button" data-action="${ createLink(action:'deleteFeedFilesUpTo') }" class="btn btn-danger confirm" data-confirm="Are you sure?">Delete</button>
			</form>
		</ui:labeled>
  	</div>
</g:if>
<g:else>
	<p id="no-history-message">This stream has no history.</p>
</g:else>
<script>
	$(document).ready(function() {
	 	new Toolbar($("#history-delete-form"))
	 })
</script>
