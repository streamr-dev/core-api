<g:if test="${feedFiles?.size()>0}">
	<p>This stream has the following history available:</p>
	
	<g:form role="form">
		<g:hiddenField name="streamId" value="${stream.id}" />
		<table class="table file-table table-striped table-bordered table-hover table-condensed table-responsive">
			<thead>
				<tr>
					<td>Begin Date</td>
					<td>End Date</td>
					<td class="text-center">
						<div class="checkbox text-center">
						  <label>
						    <input type="checkbox" name="selectAll" class="px selectAll"><span class="lbl" title="Select All"></span>
						  </label>
						</div>
					</td>
				</tr>
			</thead>
			<tbody>
				<g:each in="${feedFiles}">
					<tr >
						<td><g:formatDate date="${it.beginDate}" timeZone="UTC" format="${message(code:'default.dateOnly.format')}"/></td>
						<td><g:formatDate date="${it.endDate}" timeZone="UTC" format="${message(code:'default.dateOnly.format')}"/></td>
						<td class="text-center">
							<div class="checkbox text-center">
							  	<label>
						  			<input type="checkbox" name="selectedFeedFiles" value="${ it.id }" class="px select"><span class="lbl"></span></input>
							  	</label>
							</div>
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
		<div id="toolbar">
			<button id="deleteButton" data-action="${ createLink(action:'deleteSelectedFeedFiles') }" class="btn btn-danger confirm" data-confirm="${message(code:'stream.feedfile.delete.label')}" style="float: right;"><span class="fa fa-trash-o"></span> Delete Selected</button>
		</div>
	</g:form>
</g:if>
<g:else>
	<p>This stream has no history.</p>
</g:else>
<script>
	$(document).ready(function() {
	 	new Toolbar($("#toolbar"))
	 	$(".selectAll").change(function(){
			if($(".selectAll").prop("checked")){
				$(".px").prop("checked", true)
			} else {
				$(".px").prop("checked", false)
			}
	 	})
	 })
</script>
