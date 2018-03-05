<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title>Dashboard</title>

		<r:require module="streamr-chart"/>
		<r:require module="streamr-map"/>
		<r:require module="streamr-heatmap"/>
		<r:require module="streamr-table"/>

		<webpack:cssBundle name="commons"/>
		<webpack:cssBundle name="dashboardPage"/>

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

		<webpack:jsBundle name="commons"/>
		<webpack:jsBundle name="dashboardPage"/>
	</body>

</html>
