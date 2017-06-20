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

				function streamrDropDownSetEnabled(buttons) {
					var buttonReference = {
						'share': '.share-button',
						'rename': '.rename-dashboard-button',
						'delete': '.delete-dashboard-button'
					}
					var list = Array.isArray(buttons) ? buttons : buttons.split(/[ ,]/)
					for (var btn in buttonReference) {
						if (list.indexOf(btn) < 0) {
							$(buttonReference[btn]).addClass('disabled').addClass('forbidden').attr('disabled', 'disabled')
						} else {
							$(buttonReference[btn]).removeClass('disabled').removeClass('forbidden').removeAttr('disabled')
						}
					}
				}

				var nameEditor

				var shareUrl
				var shareName
				$(".share-button").click(function(e) {
					e.preventDefault()
					var shareUrl = $(this).data('url')
					var shareName = $(this).data('name')
					if (shareUrl) {
						sharePopup(shareUrl, shareName)
					}
				})

				function createDashboard(dbJson) {
				    streamrDropDownSetEnabled('rename')
					document.title = dbJson.name
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
						// Have to remove the id's so we don't confuse Backbone
					    canvases = _.map(canvases, function(canvas) {
						    canvas.modules = _.map(canvas.modules, function(module) {
								delete module.id
								return module
						    })
						    return canvas
						})
					    var sidebar = new SidebarView({
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
						if (!dashboard.saved)
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
					nameEditor = new StreamrNameEditor({
						el: $(".name-editor"),
						opener: $(".rename-dashboard-button")
					}).on("changed", function(name) {
						if (dashboard) {
							dashboard.set("name", name)
						}
					})

					nameEditor.setName(dbJson.name)
				}

				function checkPermissions() {
					if (dashboard && dashboard.get("id")) {
						$.getJSON("${createLink(uri:"/api/v1/dashboards/")}" + dashboard.get("id") + "/permissions/me", function(permissions) {
						    var dbUrl = Streamr.createLink({uri: "api/v1/dashboards/" + dashboard.get("id")})
							permissions = _.map(permissions, function(p) {
								return p.operation
							})
							var enabled = ['rename']
							if (_.contains(permissions, "share")) {
								shareUrl = dbUrl
				    			shareName = dashboard && dashboard.get('name')
								enabled.push('share')
							}
							if (_.contains(permissions, "write")) {
								enabled.push('delete')
						    }
						    streamrDropDownSetEnabled(enabled)
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
						dashboard && dashboard.save()
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
						<li class="share-dashboard-button share-button">
							<a id="share-dashboard-button">
								<i class="fa fa-user"></i> Share
							</a>
						</li>
						<li class="rename-dashboard-button">
							<a id="rename-dashboard-button">
								<i class="fa fa-pencil"></i> Rename
							</a>
						</li>
						<li class="delete-dashboard-button">
							<a id="delete-dashboard-button">
								<i class="fa fa-trash-o"></i> Delete
							</a>
						</li>
					</ul>
				</div>
			</ui:breadcrumb>
			<streamr-client
					id="client"
					url="${config.streamr.ui.server}"
					autoconnect="true"
					autodisconnect="false"
					authkey="${key.id}">
			</streamr-client>
			<ul id="dashboard-view"></ul>
		</div>

		<g:render template="dashboard-template" />

	</body>
</html>

