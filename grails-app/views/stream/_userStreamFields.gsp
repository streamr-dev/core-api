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