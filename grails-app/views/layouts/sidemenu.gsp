<g:applyLayout name="ui">
	<head>
		<g:layoutHead />
	</head>

	<body class="main-navbar-fixed ${pageProperty(name: 'body.theme') ?: 'selected-theme'} ${pageProperty(name:'body.class')}">
		<g:layoutBody />
		<div id="main-menu-bg"></div>
	</body>
</g:applyLayout>
