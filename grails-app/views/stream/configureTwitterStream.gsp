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
			var keywordCollection = new ListEditor.ValueList(${twitter.keywords})
		</g:applyCodec>
		var editor = new ListEditor({
			el: $("#keywordList"),
			collection: keywordCollection
		})

		var showStreamUrl = "${createLink(controller: "stream", action: "show", params: [id: stream.id])}"
		var streamApiUrl = "${createLink(uri: "/api/v1/streams/${stream.id}")}"

		$("#configure-twitter-form").submit(function(e) {
			e.preventDefault()

			$.getJSON(streamApiUrl, function(streamData) {

				var twitterConfig = streamData.config.twitter || {}

				twitterConfig.keywords = keywordCollection.toJSON()

				streamData.config.twitter = twitterConfig

				// Save using API call and redirect to stream page
				$.ajax({
					url: streamApiUrl,
					data: JSON.stringify(streamData),
					type: "PUT",
					success: function(data) {
						window.location.href = showStreamUrl
						Streamr.showSuccess("Twitter stream settings saved", "Success")
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
	<ui:panel title="${message(code: "stream.config.twitter.title", args: [stream.name])}">
		<g:renderErrors bean="${twitter}"/>

		<div class="form-group">
			<label for="keywordList">${message(code: "stream.config.twitter.keywords")}</label>
			<div id="keywordList">
				Keyword list
			</div>
		</div>

		<g:form name="configure-twitter-form">
			<g:submitButton name="submit" class="btn btn-lg btn-primary"
							value="${message(code: "stream.update.label")}"/>
		</g:form>
	</ui:panel>
</div>

</body>
</html>