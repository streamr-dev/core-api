<head>
    <title><g:layoutTitle default="Unifina" /></title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    
    <script>
<%--		project_webroot = '<g:resource absolute="false" dir="/" />'; --%>
		project_webroot = '<g:createLink uri="/" />'; 
    </script>
    
    <r:require module="jquery"/>
    <r:require module="bootstrap"/>
	<r:require module="global-error-handler"/>
	<r:require module="superfish" />
        
	<r:script>
		$(document).ready(function () {
			$('#top_banner .topnav ul').superfish({
				delay:400
			}).supposition();
		});
	</r:script>
	
	<r:layoutResources/>
    <g:layoutHead />
</head>
