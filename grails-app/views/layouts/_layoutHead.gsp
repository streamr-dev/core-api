<head>
    <title><g:layoutTitle default="Unifina" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    
	<%-- Used by Geb GrailsPage abstraction --%>
    <meta name="pageId" content="${controllerName}.${actionName}" />

    <mixpanel:head/>

	<script>
    	Streamr = {}
		Streamr.projectWebroot = '<g:createLink uri="/" />'
		Streamr.controller = '${controllerName}'
		Streamr.action = '${actionName}'
		Streamr.user = {}

		var _mixpanelTrack = mixpanel.track
		mixpanel.track = function() {
			console.log('mixpanel.track', arguments)
			return _mixpanelTrack.apply(mixpanel, arguments)
		}

		<sec:ifLoggedIn>
			Streamr.user.id = '<sec:loggedInUserInfo field="id"/>'
			Streamr.user.username = '<sec:loggedInUserInfo field="username"/>'

			console.log('mixpanel.identify', Streamr.user.id)

			mixpanel.identify(Streamr.user.id)
		</sec:ifLoggedIn>
    </script>

    <r:require module="streamr"/>
    <r:require module="jquery"/>
    <r:require module="main-theme"/>
	<r:require module="global-error-handler"/>

	<r:require module='tour'/>

	<r:layoutResources/>
	<g:layoutHead />
</head>
