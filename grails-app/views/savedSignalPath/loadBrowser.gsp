<table>
	<thead>
		<tr>
			<th>Id</th>
			<th>Name</th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${signalPaths}">
			<tr class="selectable" onClick="SignalPath.loadSignalPath({url: '${it.url}'})">
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
