<head>
    <title><g:layoutTitle default="Unifina" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
    
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    
    <script>
		project_webroot = '<g:createLink uri="/" />'; 
    </script>
    
    <r:require module="jquery"/>
    <r:require module="main-theme"/>
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
