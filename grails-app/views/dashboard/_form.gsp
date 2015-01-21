<div><span>Dashboard name</span></span></div><g:textField name="name" placeholder="Name" value="${dashboard?.name}"></g:textField></div>

<div class="*info-box"><span>Select the channels you want to have in your dashboard:</span></div>

<g:set var="checked" value="${dashboard?.items?.collect {it.uiChannel.id} as Set}"/>

<g:each in="${runningSignalPaths}" var="rsp">
	<h1>id ${rsp.id}</h1>
	<g:each in="${rsp.uiChannels}" var="uiChannel">
		<g:if test="${uiChannel.hash!=null}">
			<div><g:checkBox name="uiChannels" value="${uiChannel.id}" checked="${checked?.contains(uiChannel.id)}"/> ${uiChannel.id}<br><g:textField name="title_${uiChannel.id}"/></div>
		</g:if>
	</g:each>
</g:each>