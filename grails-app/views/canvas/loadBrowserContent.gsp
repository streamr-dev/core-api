<%@ page import="com.unifina.domain.signalpath.Canvas" %>
<g:each in="${canvases}">
	<tr class="selectable has-offset" data-id="${it.id}" data-offset="${it.offset}">
		<td>
			${it.name}
		</td>
		<td>
			<span class="label ${it.state == com.unifina.domain.signalpath.Canvas.State.RUNNING ? "label-primary" : "label-default"}">${it.state.id.toLowerCase()}</span>
		</td>
	</tr>
</g:each>