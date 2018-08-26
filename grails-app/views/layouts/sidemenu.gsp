<!DOCTYPE html>
<ui:html>
	<head>
		<g:render template="/layouts/headUi" />
		<g:layoutHead />
		<r:layoutResources />
	</head>
	<body class="main-navbar-fixed ${pageProperty(name: 'body.theme') ?: 'selected-theme'} ${pageProperty(name:'body.class')}">
		<g:applyLayout name="mainWrapper">
			<g:layoutBody />
			<div id="main-menu-bg"></div>
		</g:applyLayout>
		<r:layoutResources />
	</body>
</ui:html>
