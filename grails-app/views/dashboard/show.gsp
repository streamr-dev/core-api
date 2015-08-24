<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title>${ dashboard.name }</title>

		<r:require module="webcomponents"/>
		<r:require module="slimscroll"/>
		<r:require module="dashboard-editor"/>

		<!--Webcomponent-resources are required because webcomponents are imported with lightDOM=true and noDependencies=true-->
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
					dashboardView.on('error', function(error, itemTitle) {
						$.pnotify({
							type: 'error',
			        		title: 'Error',
				        	text: itemTitle ? "<strong>"+itemTitle+"</strong>:<br>"+error : error,
				        	delay: 4000
			    		});
					})

					dashboard.urlRoot = "${createLink(controller:'dashboard', action:'update')}"

				    dashboard.get("items").on("remove", function (model) {
						var client = document.getElementById("client")
						client.streamrClient.unsubscribe(model.get("uiChannel").id)
					})
					
					$.getJSON("${createLink(controller:'live', action:'getListJson')}", {}, function(rspJson) {
						var sidebar = new SidebarView({
							edit: "${params.edit}",
							dashboard: dashboard, 
							RSPs: rspJson,
							el: $("#sidebar-view"),
							menuToggle: $("#main-menu-toggle")
						})
					})
					$(window).bind('beforeunload', function(){
						if(!dashboard.saved)
							return 'The dashboard has changes which are not saved'
					});
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
			<g:render template="/dashboard/breadcrumbList" model="[dashboard:dashboard]"/>
			<g:render template="/dashboard/breadcrumbShow" model="[dashboard:dashboard, active:true]"/>
		</ui:breadcrumb>
		<streamr-client id="client" server="${ serverUrl }" autoconnect="true" autodisconnect="false"></streamr-client>
		<ul id="dashboard-view"></ul>
	</div>
	<div id="main-menu-bg"></div>

	<g:render template="dashboard-template" />

</body>
</html>

