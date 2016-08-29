<html>
<head>
	<meta name="layout" content="main"/>
	<title><g:message code="stream.config.twitter.title" args="[stream.name]"/></title>
</head>

<body>
<ui:breadcrumb>
	<g:render template="/stream/breadcrumbList"/>
	<g:render template="/stream/breadcrumbShow"/>
	<li class="active">
		<g:link controller="stream" action="configureTwitterStream" params="[id: stream.id]">
			<g:message code="stream.config.twitter.title" args="[stream.name]"/>
		</g:link>
	</li>
</ui:breadcrumb>

<ui:flashMessage/>

<r:require module="streamr"/>
<r:require module="list-editor"/>

<r:script>
    $(function() {
    	<g:applyCodec encodeAs="none">
		var streamData = ${stream.toMap() as grails.converters.JSON}
		</g:applyCodec>
		var twitterConfig = streamData.config.twitter || {}
		var keywordCollection = new ListEditor.ValueList(twitterConfig.keywords || [])
		var editor = new ListEditor({
			el: $("#keywordList"),
			collection: keywordCollection
		})

		$("#configure-twitter-form").submit(function(e) {
			twitterConfig.keywords = keywordCollection.toJSON()
			$("#configure-twitter-form input#keywords").val(twitterConfig.keywords)
		})
	})
</r:script>

<div class="col-xs-12 col-md-8 col-md-offset-2">
	<ui:panel title="${message(code: "stream.config.twitter.title", args: [stream.name])}">
		<g:renderErrors bean="${twitter}"/>

		<div class="form-group">
			<label for="keywordList">${message(code: "stream.config.twitter.keywords")}</label>
			<div id="keywordList">
				Keyword list
			</div>
		</div>

		<g:form name="configure-twitter-form" action="saveTwitterStream">
			<g:hiddenField name="keywords" />
			<g:hiddenField name="id" value="${stream.id}" />
			<g:submitButton name="submit" class="btn btn-lg btn-primary"
							value="${message(code: "stream.update.label")}"/>
		</g:form>
	</ui:panel>
</div>

</body>
</html>