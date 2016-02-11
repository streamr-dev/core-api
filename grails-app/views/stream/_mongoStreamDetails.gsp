<div class="col-sm-6 col-md-4">
	<div class="panel ">
		<div class="panel-heading">
			<span class="panel-title">MongoDB Settings</span>

			<div class="panel-heading-controls">
				<g:link action="configureMongo" id="${stream.id}">
					<span class="btn btn-sm" id="edit-mongodb-button">Edit</span>
				</g:link>
			</div>
		</div>

		<div class="panel-body">
			<g:set var="mongo" value="${com.unifina.data.MongoDbConfig.readFromStream(stream)}"/>
			<ui:labeled label="${message(code: "stream.config.mongodb.host")}">
				${mongo.host}
			</ui:labeled>

			<ui:labeled label="${message(code: "stream.config.mongodb.port")}">
				${mongo.port}
			</ui:labeled>

			<ui:labeled label="${message(code: "stream.config.mongodb.username")}">
				${mongo.username}
			</ui:labeled>

			<ui:labeled label="${message(code: "stream.config.mongodb.password")}">
				${mongo.password}
			</ui:labeled>

			<ui:labeled label="${message(code: "stream.config.mongodb.database")}">
				${mongo.database}
			</ui:labeled>

			<ui:labeled label="${message(code: "stream.config.mongodb.collection")}">
				${mongo.collection}
			</ui:labeled>

			<ui:labeled label="${message(code: "stream.config.mongodb.timestampKey")}">
				${mongo.timestampKey}
			</ui:labeled>

			<ui:labeled label="${message(code: "stream.config.mongodb.pollIntervalMillis")}">
				${mongo.pollIntervalMillis}
			</ui:labeled>

			<ui:labeled label="${message(code: "stream.config.mongodb.query")}">
				${mongo.query}
			</ui:labeled>
		</div>
	</div>
</div>

<div class="col-sm-6 col-md-4">
	<div class="panel ">
		<div class="panel-heading">
			<span class="panel-title">Fields</span>

			<div class="panel-heading-controls">
				<g:link action="configure" id="${stream.id}"><span class="btn btn-sm"
																   id="configure-fields-button">Configure Fields</span></g:link>
			</div>
		</div>

		<div class="panel-body">
			<g:if test="${!config.fields || config.fields.size() == 0}">
				<div class='alert alert-info'>
					<i class='fa fa-exclamation-triangle'></i>
					The fields for this stream are not yet configured. Click the button above to configure them.
				</div>
			</g:if>
			<g:else>
				<g:render template="userStreamFields" model="[config: config]"/>
			</g:else>
		</div>
	</div>
</div>