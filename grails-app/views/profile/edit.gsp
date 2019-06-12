<html>
<head>
    <meta name="layout" content="main" />
    <title>
		<g:message code="profile.edit.label"/>
	</title>

	<r:require module="streamr-credentials-control"/>

	<webpack:cssBundle name="commons"/>
	<webpack:cssBundle name="profilePage"/>

</head>
<body>

	<div class="row">
		<div class="container">
			<ui:flashMessage/>
		</div>
	</div>
	<div class="row">
		<div class="container">
			<div id="profilePageRoot"></div>
		</div>
	</div>

	<webpack:jsBundle name="commons"/>
	<webpack:jsBundle name="profilePage"/>

</body>
</html>
