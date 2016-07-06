<%@ page import="com.unifina.feed.mongodb.MongoDbConfig" %>
<div class="col-sm-6 col-md-4">
	<div class="panel">
		<g:set var="twitter" value="${com.unifina.feed.twitter.TwitterStreamConfig.fromStreamOrEmpty(stream)}"/>

		<div class="panel-heading">
			<span class="panel-title">Twitter Stream Settings</span>

			<g:if test="${twitter.accessToken}">
				<div class="panel-heading-controls">
					<g:link action="configureTwitterStream" id="${stream.id}">
						<span class="btn btn-sm" id="edit-mongodb-button">Edit</span>
					</g:link>
				</div>
			</g:if>
		</div>

		<div class="panel-body">

			<g:if test="${twitter.accessToken}">
				<ui:labeled class="twitter-keywords" label="${message(code: "stream.config.twitter.keywords")}">
					${twitter.keywords}
				</ui:labeled>
			</g:if>
			<g:else>
				<p>Must sign in with Twitter before the stream is available!</p>
			</g:else>
		</div>
	</div>
</div>
