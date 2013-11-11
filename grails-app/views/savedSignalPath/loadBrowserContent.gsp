<g:each in="${signalPaths}">
	<tr class="selectable offsetRow" onClick="var url='${it.url}'; SignalPath.loadSignalPath({url:url});">
		<td>
			${it.id}
		</td>
		<td>
			${it.name}
		</td>
	</tr>
</g:each>