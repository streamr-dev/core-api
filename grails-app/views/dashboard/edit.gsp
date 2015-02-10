<html>
	<head>
		<meta name="layout" content="sidemenu" />

		<title><g:message code="dashboard.edit.label" args="[dashboard.name]" /></title>

		<r:require module="slimscroll"/>
		<r:require module="webcomponents"/>
		<link rel="import" href="${createLink(uri:"/webcomponents/index.html", plugin:"unifina-core")}">

		<r:script>
			$(document).ready(function() {
				// Bind slimScroll to main menu
			    $('#main-menu-inner').slimScroll({
			      height: '100%'
			    })
			})
		</r:script>
</head>

<body class="main-menu-fixed">
	<div id="main-menu" role="navigation">
		<div id="main-menu-inner">
			<g:form action="update">
				<g:hiddenField name="id" value="${dashboard.id}"/>
				<div class="form-group menu-content">
					<label for="name" class="control-label label-md">
						<g:message code="dashboard.name.label"/>
					</label>
					
					<g:textField name="name" placeholder="Name" value="${dashboard?.name}" class="form-control input-lg" />
				</div>
				<g:set var="checked" value="${dashboard?.items?.collect {it.uiChannel.id} as Set}"/>
				<g:each in="${runningSignalPaths}" var="rsp">
					<div class="menu-content">
						<div class="menu-content-header">
							<g:if test="${rsp.name}">
								<label>${rsp.name}</label>
							</g:if>
							<g:else>
								<label>id: ${rsp.id}</label>
							</g:else>
						</div>		
						<g:each in="${rsp.uiChannels}" var="uiChannel">
							<g:if test="${uiChannel.hash!=null && uiChannel.module}">
								<div class="form-group">
									<div class="col-md-6">
										<label class="control-label">
											<g:checkBox name="uiChannels" value="${uiChannel.id}" checked="${checked?.contains(uiChannel.id)}"/> 
											${uiChannel.name ?: uiChannel.id}<br>
										</label>
										
										<input type="text" class="typeahead form-control input-sm" name="title_${uiChannel.id}" placeholder="Title" value="${dashboard?.items?.find{uiChannel.id==it.uiChannel.id}?.title}"></input>
									</div>
								</div>
							</g:if>
						</g:each>
					</div>
				</g:each>
				<div class="menu-content">
					<g:submitButton class="btn btn-primary" name="submit" value="${message(code:"dashboard.update.button")}"/>
				</div>
			</g:form>
		</div> <!-- / #main-menu-inner -->
	</div> <!-- / #main-menu -->

	<div id="content-wrapper">
			<g:render template="dashboard-content" />
	</div>
	
	<div id="main-menu-bg"></div>

</body>
</html>

