<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title>Dashboard</title>

		<r:require module="webcomponents"/>
		<r:require module="slimscroll"/>

		<!--Webcomponent-resources are required because webcomponents are imported with lightDOM=true and noDependencies=true-->
		<r:require module="webcomponent-resources" disposition="head"/>

		<link rel="import" href="${createLink(uri:"/webcomponents/index.html?lightDOM=true&noDependencies=true", plugin:"unifina-core")}">

		<r:require module="dashboard-page-webpack-bundle"/>

	</head>

	<body class="main-menu-fixed dashboard-show mmc">
		<div id="dashboardPageRoot"></div>
	</body>
</html>

