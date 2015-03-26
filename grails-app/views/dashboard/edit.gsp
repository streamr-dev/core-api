<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title><g:message code="dashboard.edit.label" args="[dashboard.name]" /></title>

		<r:require module="dashboard-editor"/>
		<r:require module="slimscroll"/>
		<r:require module="webcomponents"/>
		<link rel="import" href="${createLink(uri:"/webcomponents/index.html", plugin:"unifina-core")}">

		<r:script>
			$(document).ready(function() {
				var runningSignalPaths = ${raw(runningSignalPathsAsJson ?: "[]")}
				var dashboard = ${raw(dashboardAsJson ?: "{}")}

				//console.log(runningSignalPaths)
				//console.log(dashboard)
				//console.log(dashboard.items)

				var DIList = new DashboardItemList(dashboard.items)
				var sidebar = new SidebarView(dashboard.name, runningSignalPaths, DIList)
				var dashboard = new DashboardView(DIList)

				// Bind slimScroll to main menu
			    $('#main-menu-inner').slimScroll({
			      height: '100%'
			    })
			})
		</r:script>
</head>

<body class="main-menu-fixed dashboard-edit">
	<div id="main-menu" role="navigation">
		<div id="main-menu-inner">
			<div id="sidebar-view">

			</div>
		</div> 
	</div>

	<div id="content-wrapper">
		<streamr-client server="${serverUrl}"></streamr-client>
		<div id="dashboard-view"></div>

		</div>
	<div id="main-menu-bg"></div>

	<g:render template="dashboard-template" />


	

</body>
</html>

