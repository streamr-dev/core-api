<%@ page import="com.unifina.feed.twitter.TwitterStreamConfig" %>
<div class="col-sm-6 col-md-4">
	<div class="panel">
		<g:set var="twitter" value="${com.unifina.feed.twitter.TwitterStreamConfig.forStream(stream, session)}"/>

		<div class="panel-heading">
			<span class="panel-title">Twitter Stream Settings</span>

			<g:if test="${twitter.accessToken}">
				<div class="panel-heading-controls">
					<g:link action="configureTwitterStream" id="${stream.id}">
						<span class="btn btn-sm" id="edit-twitter-stream-button">Edit</span>
					</g:link>
				</div>
			</g:if>
		</div>

		<div class="panel-body">

			<g:if test="${twitter.accessToken}">
				<ui:labeled class="twitter-keywords" label="${message(code: "stream.config.twitter.keywords")}">
					<ul>
						<g:each in="${twitter.keywords}">
							<li>${it}</li>
						</g:each>
					</ul>
				</ui:labeled>
			</g:if>
			<g:else>
				<p>Must sign in with Twitter before the stream can be used!</p>
				<a href="${twitter.signInURL}" id="twitter-sign-in-button">
					<g:img dir="images/twitter" file="sign-in-with-twitter-gray.png" />
				</a>
			</g:else>
		</div>
	</div>
</div>
