<div class="col-sm-4">
	<ui:panel title="Fields">
		<g:if test="${!config.fields || config.fields.size()==0}">
			<div class='alert alert-danger'>
				<i class='fa fa-exclamation-triangle'></i>
				This stream is not properly configured. Click <g:link action="edit" id="${stream.id}">here</g:link> to configure it.
			</div>
		</g:if>
		<g:else>
			<g:render template="userStreamFields" model="[config:config]"/>
			<g:link action="edit" id="${stream.id}"><span class="btn btn-default">Edit</span></g:link>
		</g:else>
	</ui:panel>
</div>

<div class="col-sm-4">
	<ui:panel title="HTTP API credentials">
		<g:render template="userStreamCredentials" model="[stream:stream]"/>
	</ui:panel>
</div>