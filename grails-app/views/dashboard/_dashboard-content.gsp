<streamr-client server="${serverUrl}"></streamr-client>
	<div class="row">
		<g:each in="${dashboard.items}" var="item">
			<g:if test="${item.uiChannel.module?.id == 67}">
				<g:render template="/dashboard/streamr-chart" model="[title:"${item.title}", channel:"${item.uiChannel.id}"]"></g:render>
			</g:if>
			<g:if test="${item.uiChannel.module?.id == 145}">
				<g:render template="/dashboard/streamr-label" model="[title:"${item.title}", channel:"${item.uiChannel.id}"]"></g:render>
			</g:if>
		</g:each>
    </div>