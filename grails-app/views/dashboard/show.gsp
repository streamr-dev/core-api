<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title><g:message code="dashboard.edit.label" args="[dashboard.name]" /></title>

		<r:require module="webcomponents"/>
		<r:require module="slimscroll"/>
		<r:require module="dashboard-editor"/>

		<!--If ~/index.html?noDependencies=true-->
		<r:require module="webcomponent-resources" disposition="head"/>

		<link rel="import" href="${createLink(uri:"/webcomponents/index.html?lightDOM=true&noDependencies=true", plugin:"unifina-core")}">

		<r:script>
			$(document).ready(function() {
				var dashboard


				$.getJSON("${createLink(controller:'dashboard', action:'getJson', id:dashboard.id)}", {}, function(dbJson) {
					dashboard = new Dashboard(dbJson)
					var dashboardView = new DashboardView({
						model: dashboard,
						el: $("#dashboard-view")
					})

					dashboard.urlRoot = "${createLink(controller:'dashboard', action:'update')}"

				    dashboard.get("items").on("remove", function (model) {
						var client = document.getElementById("client")
						client.streamrClient.unsubscribe([model.get("uiChannel").id])
					})
					
					$.getJSON("${createLink(controller:'live', action:'getListJson')}", {}, function(rspJson) {
						var sidebar = new SidebarView({
							edit: "${params.edit}",
							dashboard: dashboard, 
							RSPs: rspJson,
							el: $("#sidebar-view")
						})
					})
				})

				// Bind slimScroll to main menu
			    $('#main-menu-inner').slimScroll({
			      height: '100%'
			    })

			})
		</r:script>
</head>

<body class="main-menu-fixed dashboard-edit mmc">
	<div id="main-menu" role="navigation">
		<div id="main-menu-inner">
			<div id="sidebar-view" class=""></div>
		</div> 
	</div>

	<div id="content-wrapper" class="scrollable">
		<streamr-client id="client" server="${ serverUrl }" autoconnect="true" autodisconnect="false"></streamr-client>
		<ul id="dashboard-view"></ul>
	</div>
	<div id="main-menu-bg"></div>

	<g:render template="dashboard-template" />

</body>
</html>

