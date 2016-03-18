<div class="panel ">
	<div class="panel-heading">
		<span class="panel-title">Fields</span>

		<div class="panel-heading-controls">
			<g:link action="configure" id="${stream.id}">
				<span class="btn btn-sm" id="configure-fields-button">Configure Fields</span>
			</g:link>
		</div>
	</div>

	<div class="panel-body">
		<g:if test="${!config.fields || config.fields.size() == 0}">
			<div class='alert alert-info'>
				<i class='fa fa-exclamation-triangle'></i>
				The fields for this stream are not yet configured. Click the button above to configure them.
			</div>
		</g:if>
		<g:else>
			<table id="stream-fields" class="table table-striped table-condensed">
				<thead>
				<tr>
					<th>Name</th>
					<th>Type</th>
				</tr>
				</thead>
				<tbody>
				<g:each in="${config.fields}" status="i" var="field">
					<tr>
						<td>
							${field.name}
						</td>
						<td>
							${field.type}
						</td>
					</tr>
				</g:each>
				</tbody>
			</table>
		</g:else>
	</div>
</div>