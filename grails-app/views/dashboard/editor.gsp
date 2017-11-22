<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title>Dashboard</title>

		<r:require module="dashboard-page-webpack-bundle"/>

		<style>
			body, #dashboardPageRoot {
				height: 100%;
			}
		</style>

	</head>

	<body class="main-menu-fixed dashboard-show mme editing">
		<script>
			const keyId = "${key.id}"
		</script>
		<div id="dashboardPageRoot"></div>
	</body>
</html>

