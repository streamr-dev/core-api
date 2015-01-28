<li class="${active ? "active" : "" }">
	<g:link controller="stream" action="show" id="${stream.id}">
		<g:message code="stream.show.label" args="[stream.name]"/>
	</g:link>
</li>
