<head>
    <title><g:layoutTitle default="Streamr" /></title>
	<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.png')}" type="image/x-icon" />

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

	<g:if test="${!login}">
		<g:render template="/tours/tours"/>
		<r:require module='tour'/>
	</g:if>
	
	<g:render template="/layouts/tracking"/>

	<sec:ifLoggedIn>
		<r:script>
			<!--Start of Tawk.to Script-->
			var Tawk_API=Tawk_API||{}, Tawk_LoadStart=new Date();
			(function(){
				var s1=document.createElement("script"),s0=document.getElementsByTagName("script")[0];
				s1.async=true;
				s1.src='https://embed.tawk.to/5730b2fceec1bc57567cb850/default';
				s1.charset='UTF-8';
				s1.setAttribute('crossorigin','*');
				s0.parentNode.insertBefore(s1,s0);
			})();
			Tawk_API.visitor = {
				name  : '<sec:username/>',
				email : '<sec:username/>'
			};
		</r:script>
	</sec:ifLoggedIn>
	
	<r:layoutResources/>
    <g:layoutHead />
</head>
