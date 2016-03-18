<%@ page import="com.unifina.feed.mongodb.MongoDbConfig" %>
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
			<g:set var="mongo" value="${com.unifina.feed.mongodb.MongoDbConfig.readFromStreamOrElseEmptyObject(stream)}"/>
			<ui:labeled class="mongo-host" label="${message(code: "stream.config.mongodb.host")}">
				${mongo.host}
			</ui:labeled>

			<ui:labeled class="mongo-port" label="${message(code: "stream.config.mongodb.port")}">
				${mongo.port}
			</ui:labeled>

			<ui:labeled class="mongo-username" label="${message(code: "stream.config.mongodb.username")}">
				${mongo.username}
			</ui:labeled>

			<ui:labeled class="mongo-password" label="${message(code: "stream.config.mongodb.password")}">
				${mongo.password}
			</ui:labeled>

			<ui:labeled class="mongo-database" label="${message(code: "stream.config.mongodb.database")}">
				${mongo.database}
			</ui:labeled>

			<ui:labeled class="mongo-collection" label="${message(code: "stream.config.mongodb.collection")}">
				${mongo.collection}
			</ui:labeled>

			<ui:labeled class="mongo-timestampKey" label="${message(code: "stream.config.mongodb.timestampKey")}">
				${mongo.timestampKey} (${mongo.timestampType?.humanReadableForm})
			</ui:labeled>

			<ui:labeled class="mongo-pollIntervalMillis" label="${message(code: "stream.config.mongodb.pollIntervalMillis")}">
				${mongo.pollIntervalMillis}
			</ui:labeled>

			<ui:labeled class="mongo-query" label="${message(code: "stream.config.mongodb.query")}">
				${mongo.query}
			</ui:labeled>
		</div>
	</div>
</div>

<div class="col-sm-6 col-md-4">
	<g:render template="fieldsPanel" model="[config:config]"/>
</div>