<g:applyLayout name="ui">
	<head>
		<g:layoutHead />
	</head>

	<body class="no-main-menu main-navbar-fixed ${pageProperty(name: 'body.theme') ?: 'selected-theme'} ${pageProperty(name: 'body.class')}">
		<div id="content-wrapper">
			<g:layoutBody />
		</div>
	</body>
</g:applyLayout>
