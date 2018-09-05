<!DOCTYPE html>
<ui:html>
	<head>
		<g:render template="/layouts/head" />
		<g:layoutHead />
		<r:layoutResources />
	</head>
	<body class="${pageProperty(name: 'body.class')}">
		<g:layoutBody />
		<r:layoutResources />
	</body>
</ui:html>
