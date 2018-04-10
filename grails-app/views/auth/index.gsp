<html>
<head>
	<title>Streamr</title>

	%{-- Copy paste from grails-app/views/layouts/main.gsp --}%
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
