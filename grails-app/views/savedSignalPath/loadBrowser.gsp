<table>
	<thead>
		<tr>
			<th>Id</th>
			<th>Name</th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${signalPaths}">
			<tr class="selectable" onClick="var url='${it.url}'; ${it.command}">
				<td>
					${it.id}
				</td>
				<td>
					${it.name}
				</td>
			</tr>
		</g:each>
	</tbody>
</table>
