<g:each in="${signalPaths}">
	<tr class="selectable offsetRow" onClick="var url='${it.url}'; ${it.command}">
		<td>
			${it.id}
		</td>
		<td>
			${it.name}
		</td>
	</tr>
</g:each>