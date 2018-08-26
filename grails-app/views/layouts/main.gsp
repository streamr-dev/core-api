<!DOCTYPE html>
<ui:html>
	<head>
		<g:render template="/layouts/headUi" />
		<g:layoutHead />
		<r:layoutResources />
	</head>
	<body class="no-main-menu main-navbar-fixed ${pageProperty(name: 'body.theme') ?: 'selected-theme'} ${pageProperty(name: 'body.class')}">
		<g:applyLayout name="mainWrapper">
			<div id="content-wrapper">
				<g:layoutBody />
			</div>
		</g:applyLayout>
		<r:layoutResources />
	</body>
</ui:html>
