<html>
<head>
	<title>Streamr</title>

	%{-- Copy paste from grails-app/views/layouts/main.gsp --}%

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">

	<r:script disposition="head">
    	Streamr = typeof Streamr !== 'undefined' ? Streamr : {}
		Streamr.projectWebroot = '${createLink(uri:"/", absolute:true)}'
		Streamr.controller = '${controllerName}'
		Streamr.action = '${actionName}'
		<sec:ifLoggedIn>
			Streamr.user = "${raw(grails.util.Holders.getApplicationContext().getBean("springSecurityService").getCurrentUser().getUsername())}"
		</sec:ifLoggedIn>
	</r:script>
	<r:require module="streamr"/>

	<webpack:cssBundle name="commons"/>
	<webpack:cssBundle name="authPage"/>
	<style>
		body {
			background-color: #0D009A;
		}
		#authPageRoot {
			height: 100%;
			width: 100%;
		}
	</style>

	<r:layoutResources/>
</head>
<body>
	<div id="authPageRoot"></div>
	<webpack:jsBundle name="commons"/>
	<webpack:jsBundle name="authPage"/>


	<r:layoutResources/>
</body>
</html>
