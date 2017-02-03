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
			$(function() {
				var dashboard
				var baseUrl = '${ createLink(uri: "/", absolute:true) }'
				var sidebar
				var nameEditor = new StreamrNameEditor({
					el: $(".name-editor"),
					opener: $(".rename-dashboard-button")
				}).on("changed", function(name) {
					if (dashboard)
						dashboard.set("name", name)
				})

				function createDashboard(dbJson) {
					document.title = dbJson.name
					nameEditor.setName(dbJson.name)
					dashboard = new Dashboard(dbJson)
					// urlRoot needs to be set for saving to work
					dashboard.urlRoot = baseUrl + "api/v1/dashboards/"
					var dashboardView = new DashboardView({
						model: dashboard,
						el: $("#dashboard-view"),
						baseUrl: baseUrl
					})
					dashboard.on("saved", function(name) {
						nameEditor.setName(name)
						checkPermissions()
					})
					dashboardView.on('error', function(error, itemTitle) {
						Streamr.showError(itemTitle ? "<strong>"+itemTitle+"</strong>:<br>"+error : error)
					})

					$.getJSON(Streamr.createLink({uri: 'api/v1/canvases'}), {state:'running', adhoc:false, sort:'dateCreated', order:'desc'}, function(canvases) {
						sidebar = new SidebarView({
							edit: ${params.edit ? "true" : "undefined"},
							dashboard: dashboard,
							canvases: canvases,
							el: $("#sidebar-view"),
							menuToggle: $("#main-menu-toggle"),
							baseUrl: baseUrl
						})
						checkPermissions()
					})
					$(window).on('beforeunload', function(){
						if(!dashboard.saved)
							return 'The dashboard has changes which are not saved'
					})
					new ConfirmButton("#delete-dashboard-button", {
						message: "${ message(code: 'dashboard.delete.confirm') }"
					}, function(result) {
						if (result) {
							$.ajax("${ createLink(uri:"/api/v1/dashboards", absolute: true)}" + '/' + dashboard.get("id"), {
								method: 'DELETE',
								success: function() {
									$(window).off("beforeunload")
									Streamr.showSuccess("Dashboard deleted succesfully!", "", 2000)
									setTimeout(function() {
										window.location = "${ createLink(controller: 'dashboard', action:'list')}"
									}, 2000)
								},
								error: function(e, t, msg) {
									Streamr.showError("${ message(code: 'dashboard.delete.error') }", "${ message(code: 'dashboard.delete.error.title') }")
								}
							})
						}
					})
				}
				var checkPermissions = function() {
					if (dashboard && dashboard.get("id")) {
						$.getJSON("${createLink(uri:"/api/v1/dashboards/")}" + dashboard.get("id") + "/permissions/me", function(permissions) {
							permissions = _.map(permissions, function(p) {
								return p.operation
							})
							setTimeout(function() {
								if (_.contains(permissions, "share")) {
									$(".share-button").data("url", Streamr.createLink({uri: "api/v1/dashboards/" + dashboard.get("id")}))
									$(".share-button").attr("name", dashboard.get("name"))
									$(".share-button").removeAttr("disabled")
									$("li.share-dashboard-button").removeClass("disabled")
								} else {
									$(".share-button").addClass("forbidden")
								}
								if (_.contains(permissions, "write")) {
									$(".delete-dashboard-button").removeClass("disabled")
									$("#saveButton").removeAttr("disabled")
								} else {
									$(".delete-dashboard-button").addClass("forbidden")
									$("#saveButton").addClass("forbidden")
								}
							})
						})
					}
				}
				<g:if test="${ id }">
					function errorHandler(a, b, c) {
						Streamr.showError("Dashboard with if $id not found!")
						createDashboard({
							name: "Untitled Dashboard",
							items: []
						})
					}
					$.getJSON("${createLink(uri:"/api/v1/dashboards/$id")}", createDashboard).fail(errorHandler)
				</g:if>
				<g:else>
					createDashboard({
						name: "Untitled Dashboard",
						items: []
					})
				</g:else>
				// Bind slimScroll to main menu
			    $('#main-menu-inner').slimScroll({
			      height: '100%'
			    })

			    $("body").keydown(function(e) {
					// ctrl + s || cmd + s
					if ((e.ctrlKey || e.metaKey) && String.fromCharCode(e.which).toLowerCase() == 's') {
						e.preventDefault()
						sidebar.save()
					}
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
					<span class="name-editor"></span>
				</li>
				<div class="streamr-dropdown">
					<button class="dashboard-menu-toggle dropdown-toggle btn btn-xs btn-outline" data-toggle="dropdown">
						<i class="fa fa-cog"></i> <i class="navbar-icon fa fa-caret-down"></i>
					</button>
					<ul class="dropdown-menu">
						<li class="disabled share-dashboard-button">
							<ui:shareButton type="link" getName='dashboard.get(\"name\")'>Share</ui:shareButton>
						</li>
						<li class="rename-dashboard-button">
							<a>
								<i class="fa fa-pencil"></i> Rename
							</a>
						</li>
						<li class="delete-dashboard-button disabled">
							<a id="delete-dashboard-button">
								<i class="fa fa-trash-o"></i> Delete
							</a>
						</li>
					</ul>
				</div>
			</ui:breadcrumb>
			<streamr-client id="client" server="${ serverUrl }" autoconnect="true" autodisconnect="false"></streamr-client>
			<ul id="dashboard-view"></ul>
		</div>

		<g:render template="dashboard-template" />

	</body>
</html>

