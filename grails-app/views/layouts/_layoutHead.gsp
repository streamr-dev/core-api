<head>
    <title><g:layoutTitle default="Unifina" /></title>
<%--    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />--%>

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    
	<%-- Used by Geb GrailsPage abstraction --%>
    <meta name="pageId" content="${controllerName}.${actionName}" />
    
    <script>
    	Streamr = {}
		Streamr.projectWebroot = '${createLink(uri:"/", absolute:true)}'
		Streamr.controller = '${controllerName}'
		Streamr.action = '${actionName}'
		<sec:ifLoggedIn>
		Streamr.user = "<sec:username />"
		</sec:ifLoggedIn>
    </script>
    
    <r:require module="streamr"/>
    <r:require module="jquery"/>
    <r:require module="main-theme"/>
	<r:require module="global-error-handler"/>
	<r:require module="clickable-table"/>

	<g:if test="${!login}">
		<r:require module='tour'/>
	</g:if>
	
	<g:render template="/layouts/tracking"/>
	
	<r:layoutResources/>
    <g:layoutHead />
</head>
