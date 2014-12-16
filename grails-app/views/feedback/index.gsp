<html>
<head>
<meta name="layout" content="main" />
<title><g:message code="feedback.label" /></title>
</head>
<body>

	<div class="row">
		<div class="col-sm-12 col-md-8 col-md-offset-2">
			<ui:flashMessage />
		</div>
	</div>

	<g:form action="send">
		<div class="row">
			<div class="col-sm-12 col-md-8 col-md-offset-2">
				<ui:panel title="${message(code:"feedback.label")}">
					<div class="form-group">
						<label for="exampleInputEmail1">Please send your feedback or question to us! We will respond via email if necessary.</label> 
						<textarea class="form-control" placeholder="Feedback" name="feedback"></textarea>
					</div>
					<g:submitButton name="submit" class="save btn btn-lg btn-primary"
					value="${message(code: "feedback.button.send.label")}" />
				</ui:panel>
			</div>
		</div>
	</g:form>
</body>
</html>
