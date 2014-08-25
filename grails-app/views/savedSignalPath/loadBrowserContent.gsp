<g:each in="${signalPaths}">
	<tr class="selectable offsetRow" data-url="${it.url}">
		<td>
			${it.id}
		</td>
		<td>
			${it.name}
		</td>
	</tr>
</g:each>