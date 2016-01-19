<g:each in="${signalPaths}">
	<tr class="selectable has-offset" data-id="${it.id}" data-offset="${it.offset}">
		<td>
			${it.id}
		</td>
		<td>
			${it.name}
		</td>
	</tr>
</g:each>