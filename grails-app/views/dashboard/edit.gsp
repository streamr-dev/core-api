<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title><g:message code="dashboard.edit.label" args="[dashboard.name]" /></title>

		<r:require module="webcomponents"/>
		<r:require module="slimscroll"/>
		<r:require module="dashboard-editor"/>

		<link rel="import" href="${createLink(uri:"/webcomponents/index.html", plugin:"unifina-core")}">

		<style>
			#main-menu .navigation .uichannel.checked .menu-icon.fa-square {
				display:none;
			}
			#main-menu .navigation .uichannel:not(.checked) .menu-icon.fa-check-square {
				display:none;
			}
			#main-menu .mm-dropdown > ul > li > a.ui-title {
				padding-left: 30px;
			}
			.selected-theme #main-menu .navigation .uichannel.checked a {
				color:#d25213;
			}
			#dashboard-view .dashboarditem:not(.editing) .stat-panel .stat-row .stat-cell .titlebar-edit {
				display:none;
			}
			#dashboard-view .dashboarditem.editing .stat-panel .stat-row .stat-cell .titlebar {
				display:none;
			}
		</style>

		<r:script>
			$(document).ready(function() {
				var runningSignalPaths = ${raw(runningSignalPathsAsJson ?: "[]")}
				var dashboard = new Dashboard(${raw(dashboardAsJson ?: "{}")})

				var dashboardView = new DashboardView(dashboard)
				var sidebar = new SidebarView(dashboard, runningSignalPaths)
				

				// Bind slimScroll to main menu
			    $('#main-menu-inner').slimScroll({
			      height: '100%'
			    })

			    dashboard.collection.on("remove", function (model) {
					var client = document.getElementById("client")
					client.streamrClient.unsubscribe([model.get("uiChannel").id])
				})
			})
		</r:script>
</head>

<body class="main-menu-fixed dashboard-edit">
	<div id="main-menu" role="navigation">
		<div id="main-menu-inner">
			<div id="sidebar-view" class=""></div>
		</div> 
	</div>

	<div id="content-wrapper">
		<streamr-client id="client" server="${serverUrl}"></streamr-client>
		<div id="dashboard-view"></div>

		</div>
	<div id="main-menu-bg"></div>

	<g:render template="dashboard-template" />

</body>
</html>

