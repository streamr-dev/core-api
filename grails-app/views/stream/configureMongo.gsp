<html>
<head>
	<meta name="layout" content="main"/>
	<title><g:message code="stream.configureMongo.label" args="[stream.name]"/></title>
</head>

<body>
<ui:breadcrumb>
	<g:render template="/stream/breadcrumbList"/>
	<g:render template="/stream/breadcrumbShow"/>
	<li class="active">
		<g:link controller="stream" action="configureMongo" params="[id: stream.id]">
			<g:message code="stream.configureMongo.label" args="[stream.name]"/>
		</g:link>
	</li>
</ui:breadcrumb>

<ui:flashMessage/>

<r:require module="streamr"/>

<r:script>
    $(function() {
        var showStreamUrl = "${createLink(controller: "stream", action: "show", params: [id: stream.id])}"
        var streamApiUrl = "${createLink(uri: "/api/v1/streams/${stream.id}")}"

        var $form = $("#configure-mongo-form")

        $form.submit(function(e) {
            e.preventDefault()

            $.getJSON(streamApiUrl, function(streamData) {

                // (flat) form to JSON
                var mongoConfig = {}
                $form.serializeArray().forEach(function(x) {
                	if (x.value != null && x.value !== "") {
                		mongoConfig[x.name] = x.value
                	}
                })
                if (mongoConfig.port) {
                    mongoConfig.port = parseInt(mongoConfig.port, 10)
                }
                if (mongoConfig.pollIntervalMillis) {
                	mongoConfig.pollIntervalMillis = parseInt(mongoConfig.pollIntervalMillis, 10)
                }

                streamData.config.mongodb = mongoConfig

                // Redirect to stream page
                $.ajax({
                    url: streamApiUrl,
                    data: JSON.stringify(streamData),
                    type: "PUT",
                    success: function(data) {
                        window.location.href = showStreamUrl
                        Streamr.showSuccess("MongoDB settings saved", "Success")
                    },
                    error: function(xhr, ajaxOptions, thrownError) {
                        Streamr.showError(xhr.responseJSON.message, "Error")
                    }
                })
            })
        })
    })
</r:script>

<div class="col-xs-12 col-md-8 col-md-offset-2">
	<ui:panel title="${message(code: "stream.configureMongo.label", args: [stream.name])}">
		<g:form name="configure-mongo-form">

			<g:renderErrors bean="${mongo}"/>

			<div class="form-group">
				<label for="host-input">${message(code: "stream.config.mongodb.host")}</label>
				<input id="host-input" class="form-control" type="text" name="host"
					   value="${mongo.host}" required>
			</div>

			<div class="form-group">
				<label for="port-input">${message(code: "stream.config.mongodb.port")}</label>
				<input id="port-input" class="form-control" type="text" name="port"
					   value="${mongo.port}">
			</div>

			<div class="form-group">
				<label for="username-input">${message(code: "stream.config.mongodb.username")} (optional)</label>
				<input id="username-input" class="form-control" type="text" name="username"
					   value="${mongo.username}">
			</div>

			<div class="form-group">
				<label for="password-input">${message(code: "stream.config.mongodb.password")} (optional)</label>
				<input id="password-input" class="form-control" type="text" name="password"
					   value="${mongo.password}">
			</div>

			<div class="form-group">
				<label for="database-input">${message(code: "stream.config.mongodb.database")}</label>
				<input id="database-input" class="form-control" type="text" name="database"
					   value="${mongo.database}" required>
			</div>

			<div class="form-group">
				<label for="collection-input">${message(code: "stream.config.mongodb.collection")}</label>
				<input id="collection-input" class="form-control" type="text" name="collection"
					   value="${mongo.collection}" required>
			</div>

			<div class="form-group">
				<label for="timestampKey-input">${message(code: "stream.config.mongodb.timestampKey")}</label>
				<input id="timestampKey-input" class="form-control" type="text"
					   name="timestampKey" value="${mongo.timestampKey}" required>
			</div>

			<div class="form-group">
				<label for="timestampType">${message(code: "stream.config.mongodb.timestampType")}</label>
				<g:select
						class="form-control input-lg"
						name="timestampType"
						optionValue="humanReadableForm"
						from="${com.unifina.feed.mongodb.MongoDbConfig.TimestampType.values()}"
						value="${mongo.timestampType}"
				/>
			</div>

			<div class="form-group">
				<label for="pollIntervalMillis-input">${message(code: "stream.config.mongodb.pollIntervalMillis")}</label>
				<input id="pollIntervalMillis-input" class="form-control" type="text" placeholder="1000"
					   name="pollIntervalMillis" value="${mongo.pollIntervalMillis}" required>
			</div>

			<div class="form-group">
				<label for="query-input">${message(code: "stream.config.mongodb.query")} (optional)</label>
				<textarea id="query-input" class="form-control" name="query">${mongo.query ?: "{}"}</textarea>
			</div>

			<g:submitButton name="submit" class="btn btn-lg btn-primary"
							value="${message(code: "stream.update.label")}"/>
		</g:form>
	</ui:panel>
</div>

</body>
</html>