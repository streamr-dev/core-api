<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title>Dashboard</title>

		<r:require module="webcomponents"/>
		<r:require module="slimscroll"/>
		<r:require module="dashboard-editor"/>

		<!--Webcomponent-resources are required because webcomponents are imported with lightDOM=true and noDependencies=true-->
		<r:require module="webcomponent-resources" disposition="head"/>

		<link rel="import" href="${createLink(uri:"/webcomponents/index.html?lightDOM=true&noDependencies=true", plugin:"unifina-core")}">

		<r:script>
			$(document).ready(function() {
				var dashboard
				var baseUrl = '${ createLink(uri: "/", absolute:true) }'

				function successHandler(dbJson) {
					window.title = dbJson.name
					$("#dashboardBreadcrumbName").text(dbJson.name)
					dashboard = new Dashboard(dbJson)
					// urlRoot needs to be set for saving to work
					dashboard.urlRoot = baseUrl + "api/v1/dashboards/"
					var dashboardView = new DashboardView({
						model: dashboard,
						el: $("#dashboard-view"),
						baseUrl: baseUrl
					})
					dashboardView.on('error', function(error, itemTitle) {
						Streamr.showError(itemTitle ? "<strong>"+itemTitle+"</strong>:<br>"+error : error)
					})

					$.getJSON(Streamr.createLink({uri: 'api/v1/canvases'}), {state:'running', adhoc:false, sort:'dateCreated', order:'desc'}, function(canvases) {
						var sidebar = new SidebarView({
							edit: ${params.edit ? "true" : "undefined"},
							dashboard: dashboard,
							canvases: canvases,
							el: $("#sidebar-view"),
							menuToggle: $("#main-menu-toggle"),
							baseUrl: baseUrl
						})
					})
					$(window).on('beforeunload', function(){
						if(!dashboard.saved)
							return 'The dashboard has changes which are not saved'
					});
				}

				function errorHandler(a, b, c) {
					$("#content-wrapper .breadcrumb").remove()
					$("#dashboard-view").append($("<div/>", {
						class: "col-xs-12 alert alert-danger",
						text: "Dashboard not found!"
					}))
				}

				$.ajax({
					url: "${createLink(uri:"/api/v1/dashboards/$id")}",
					dataType: 'json',
					success: successHandler,
					error: errorHandler
				})

				// Bind slimScroll to main menu
			    $('#main-menu-inner').slimScroll({
			      height: '100%'
			    })
			})
		</r:script>
	</head>

	<body class="main-menu-fixed dashboard-show mmc">
		<div id="main-menu" role="navigation">
			<div id="main-menu-inner">
				<div id="sidebar-view" class="scrollable"></div>
			</div>
		</div>

		<div id="content-wrapper" class="scrollable">
			<ui:breadcrumb>
				<li>
					<g:link controller="dashboard" action="list">
						<g:message code="dashboard.list.label"/>
					</g:link>
				</li>
				<li class="active">
					<g:link elementId="dashboardBreadcrumbName" controller="dashboard" action="show" id="${ id }"></g:link>
				</li>
			</ui:breadcrumb>
			<streamr-client id="client" server="${ serverUrl }" autoconnect="true" autodisconnect="false"></streamr-client>
			<ul id="dashboard-view"></ul>
		</div>
		<div id="main-menu-bg"></div>

		<g:render template="dashboard-template" />

	</body>
</html>

