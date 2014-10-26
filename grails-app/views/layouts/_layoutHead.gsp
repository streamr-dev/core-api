<head>
    <title><g:layoutTitle default="Unifina" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    
	<%-- Used by Geb GrailsPage abstraction --%>
    <meta name="pageId" content="${controllerName}.${actionName}" />

	<!-- start Mixpanel -->
	<script type="text/javascript">(function(f,b){if(!b.__SV){var a,e,i,g;window.mixpanel=b;b._i=[];b.init=function(a,e,d){function f(b,h){var a=h.split(".");2==a.length&&(b=b[a[0]],h=a[1]);b[h]=function(){b.push([h].concat(Array.prototype.slice.call(arguments,0)))}}var c=b;"undefined"!==typeof d?c=b[d]=[]:d="mixpanel";c.people=c.people||[];c.toString=function(b){var a="mixpanel";"mixpanel"!==d&&(a+="."+d);b||(a+=" (stub)");return a};c.people.toString=function(){return c.toString(1)+".people (stub)"};i="disable track track_pageview track_links track_forms register register_once alias unregister identify name_tag set_config people.set people.set_once people.increment people.append people.track_charge people.clear_charges people.delete_user".split(" ");
	for(g=0;g<i.length;g++)f(c,i[g]);b._i.push([a,e,d])};b.__SV=1.2;a=f.createElement("script");a.type="text/javascript";a.async=!0;a.src="//cdn.mxpnl.com/libs/mixpanel-2.2.min.js";e=f.getElementsByTagName("script")[0];e.parentNode.insertBefore(a,e)}})(document,window.mixpanel||[]);
	mixpanel.init("6099594a7d6c4586d657c4505ca77da1");</script>
	<!-- end Mixpanel -->

	<script>
    	Streamr = {}
		Streamr.projectWebroot = '${createLink(uri:"/")}'
		Streamr.controller = '${controllerName}'
		Streamr.action = '${actionName}'
		Streamr.user = {}

		Streamr.tracking = {}
		Streamr.tracking.alias = function(id) {
			return mixpanel.alias(id)
		}
		Streamr.tracking.identify = function(id) {
			return mixpanel.identify(id)
		}
		Streamr.tracking.track = function() {
			return mixpanel.track.apply(mixpanel, arguments)
		}
		Streamr.tracking.setProfile = function(profile) {
			return mixpanel.people.set(profile);
		}

		<sec:ifLoggedIn>
			Streamr.user.id = '<sec:loggedInUserInfo field="id"/>'
			Streamr.user.username = '<sec:loggedInUserInfo field="username"/>'

			console.log('Streamr.tracking.identify', Streamr.user.username)

			Streamr.tracking.identify(Streamr.user.username)
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
