<div class="form-group">
	<label for="name" class="control-label label-lg">
		<g:message code="dashboard.name.label"/>
	</label>
	
	<g:textField name="name" placeholder="Name" value="${dashboard?.name}" class="form-control input-lg" />
</div>

<div class="*info-box"><span>Select the channels you want to have in your dashboard:</span></div>

<g:set var="checked" value="${dashboard?.items?.collect {it.uiChannel.id} as Set}"/>
	<g:each in="${runningSignalPaths}" var="rsp">
		<g:if test="${rsp.name}">
			<h3>${rsp.name}</h3>
		</g:if>
		<g:else>
			<h1>id: ${rsp.id}</h1>
		</g:else>
			
		<g:each in="${rsp.uiChannels}" var="uiChannel">
			<g:if test="${uiChannel.hash!=null && uiChannel.module}">
				<div class="form-group">
					<div class="col-md-6">
						<label class="control-label">
							<g:checkBox name="uiChannels" value="${uiChannel.id}" checked="${checked?.contains(uiChannel.id)}"/> 
							${uiChannel.name ?: uiChannel.id}<br>
						</label>
						
						<g:textField class="form-control input-md"name="title_${uiChannel.id}" placeholder="Title" value="${dashboard?.items?.find{uiChannel.id==it.uiChannel.id}?.title}"/>
					</div>
				</div>
			</g:if>
		</g:each>
	</g:each>
