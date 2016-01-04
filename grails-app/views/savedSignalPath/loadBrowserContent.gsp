<g:each in="${signalPaths}">
	<tr class="selectable has-offset" data-url="${it.url}" data-offset="${it.offset}">
		<td>
			${it.name}
		</td>
		<td>
			${ formatDate(format: "yyyy-MM-dd HH:mm:ss", date:it.dateCreated, timeZone: timezone )}
		</td>
	</tr>
</g:each>