<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="stream.configureMongo.label" args="[stream.name]"/></title>
</head>
<body>
<ui:breadcrumb>
    <g:render template="/stream/breadcrumbList" />
    <g:render template="/stream/breadcrumbShow" />
    <li class="active">
        <g:link controller="stream" action="configureMongo" params="[id: stream.id]">
            <g:message code="stream.configureMongo.label" args="[stream.name]"/>
        </g:link>
    </li>
</ui:breadcrumb>

<ui:flashMessage/>

<div class="col-xs-12 col-md-8 col-md-offset-2">
    <ui:panel title="${message(code:"stream.configureMongo.label", args:[stream.name])}">
        <g:form action="updateMongo">

            <g:renderErrors bean="${mongo}"/>

            <g:hiddenField name="id" value="${stream.id}"/>

            <div class="form-group">
                <label for="hostInput">${message(code:"stream.config.mongodb.host")}</label>
                <input id="hostInput" class="form-control" type="text" placeholder="Host" name="host" value="${mongo.host}">
            </div>
            <div class="form-group">
                <label for="portInput">${message(code:"stream.config.mongodb.port")}</label>
                <input id="portInput" class="form-control" type="text" placeholder="1234" name="port" value="${mongo.port}">
            </div>
            <div class="form-group">
                <label for="usernameInput">${message(code:"stream.config.mongodb.username")}</label>
                <input id="usernameInput" class="form-control" type="text" placeholder="username" name="username" value="${mongo.username}">
            </div>
            <div class="form-group">
                <label for="passwordInput">${message(code:"stream.config.mongodb.password")}</label>
                <input id="passwordInput" class="form-control" type="text" placeholder="password" name="password" value="${mongo.password}">
            </div>
            <div class="form-group">
                <label for="databaseInput">${message(code:"stream.config.mongodb.database")}</label>
                <input id="databaseInput" class="form-control" type="text" placeholder="database" name="database" value="${mongo.database}">
            </div>
            <div class="form-group">
                <label for="collectionInput">${message(code:"stream.config.mongodb.collection")}</label>
                <input id="collectionInput" class="form-control" type="text" placeholder="collection" name="collection" value="${mongo.collection}">
            </div>
            <div class="form-group">
                <label for="timestampKeyInput">${message(code:"stream.config.mongodb.timestampKey")}</label>
                <input id="timestampKeyInput" class="form-control" type="text" placeholder="timestampKey" name="timestampKey" value="${mongo.timestampKey}">
            </div>
            <div class="form-group">
                <label for="queryInput">${message(code:"stream.config.mongodb.query")} (optional)</label>
                <textarea id="queryInput" class="form-control" placeholder="" name="query">${mongo.query}</textarea>
            </div>

            <g:submitButton name="submit" class="btn btn-lg btn-primary" value="${message(code:"stream.update.label")}" />
        </g:form>
    </ui:panel>
</div>

</body>
</html>