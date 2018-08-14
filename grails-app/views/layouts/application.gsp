<!DOCTYPE html>
<ui:html>
	<head>
		<title><g:layoutTitle default="Streamr" /></title>
		<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.png')}" type="image/x-icon" />

		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
		<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">

		<%-- Used by Geb GrailsPage abstraction --%>
		<meta name="pageId" content="${controllerName}.${actionName}" />

		<r:script disposition="head">
			Streamr = typeof Streamr !== 'undefined' ? Streamr : {}
			Streamr.projectWebroot = '${createLink(uri:"/", absolute:true)}'
			Streamr.controller = '${controllerName}'
			Streamr.action = '${actionName}'
			<sec:ifLoggedIn>
			Streamr.user = "${raw(grails.util.Holders.getApplicationContext().getBean("springSecurityService").getCurrentUser().getUsername())}"
			</sec:ifLoggedIn>
		</r:script>

		<g:layoutHead />
		<r:layoutResources />
	</head>
	<body class="${pageProperty(name: 'body.class')}">
		<g:layoutBody />
		<r:layoutResources />
	</body>
</ui:html>
