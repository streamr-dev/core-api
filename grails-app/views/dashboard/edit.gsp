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
				dashboard.items = ${raw(dashboardItemsAsJson ?: "[]")}

				//console.log(runningSignalPaths)
				//console.log(dashboard)
				console.log(dashboard.items)

				var DIList = new DashboardItemList(dashboard.items)
				var sidebar = new SidebarView(runningSignalPaths, DIList)
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
		<div id="dashboard-view"></div>
	</div>
	<div id="main-menu-bg"></div>

	<script id="rsp-template" type="text/template">
		<a><span class="mm-text mmc-dropdown-delay animated fadeIn">${'<%= name %>'}</span></a>
	</script>

	<script id="uichannel-template" type="text/template">
		<input class="toggle" type="checkbox" ${"<%= checked ? 'checked' : '' %>"}>
		<a>${'<%= id %>'}</a>
	</script>

	<script id="di-template" type="text/template">
		<span>${'<%= title %>'}</span>
		<span>${'<%= uiChannel.id %>'}</span>
	</script>

</body>
</html>

