<g:if test="${feedFiles?.size()>0}">
	<div class="history">
		<ui:labeled label="Range">
  			This stream has history data from 
  			<strong><g:formatDate date="${feedFiles[0].beginDate}" timeZone="UTC" format="${message(code:'default.dateOnly.format')}"/></strong>
  			 to 
  			<strong><g:formatDate date="${feedFiles[feedFiles.size() -1].endDate}" timeZone="UTC" format="${message(code:'default.dateOnly.format')}"/></strong>.
		</ui:labeled>
		<ui:labeled label="Delete data up to and including">
			<g:form class="form-inline feedFileDelete">
				<g:hiddenField name="id" value="${ stream.id }" />
				<ui:datePicker name="date" value="${feedFiles[0].beginDate}" startDate="${feedFiles[0].beginDate}" endDate="${feedFiles[feedFiles.size() -1].endDate}" class="form-control input-sm"/>
				<button data-action="${ createLink(action:'deleteFeedFilesUpTo') }" class="btn btn-danger delete-btn confirm" data-confirm="Are you sure?">Delete</button>
			</g:form>
		</ui:labeled>
  	</div>
</g:if>
<g:else>
	<p>This stream has no history.</p>
</g:else>
<script>
	$(document).ready(function() {
	 	new Toolbar($("form.feedFileDelete"))
	 })
</script>
