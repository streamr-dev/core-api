<g:if test="${dataRange?.beginDate && dataRange?.endDate}">
	<div class="history">
		<ui:labeled label="Range">
  			This stream has archived history from 
  			<span class="history-start-date"><g:formatDate date="${dataRange.beginDate}" timeZone="UTC" format="${message(code:'default.dateOnly.format')}"/></span>
  			 to 
  			<span class="history-end-date"><g:formatDate date="${dataRange.endDate}" timeZone="UTC" format="${message(code:'default.dateOnly.format')}"/></span>.
		</ui:labeled>
		<ui:labeled label="Delete data up to and including">
			<form action="${ createLink(action: 'deleteDataUpTo') }" id="history-delete-form" class="form-inline">
				<g:hiddenField name="id" value="${ stream.id }" />
				<ui:datePicker id="history-delete-date" name="date" value="${dataRange.beginDate}" startDate="${dataRange.beginDate}" endDate="${dataRange.endDate}" class="form-control input-sm"/>
				<button type="button" id="history-delete-button" class="btn btn-danger"><g:message code="default.button.delete.label"/></button>
			</form>
		</ui:labeled>
  	</div>
</g:if>
<g:else>
	<p id="no-history-message">This stream has no history.</p>
</g:else>
<script>
	$(function() {
		new ConfirmButton($("#history-delete-button"), {
			message: "${ message(code:'stream.feedfile.delete.label' )}",
		}, function(result) {
			if (result) {
				$("#history-delete-form").submit()
			}
		})
 	})
</script>
