<head>
    <title><g:layoutTitle default="Unifina" /></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, minimum-scale=1.0, maximum-scale=1.0">
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    
	<%-- Used by Geb GrailsPage abstraction --%>
    <meta name="pageId" content="${controllerName}.${actionName}" />
    
    <script>
		project_webroot = '<g:createLink uri="/" />'; 
    </script>

	<!-- start Mixpanel -->
	<script type="text/javascript">
		(function(f,b){if(!b.__SV){var a,e,i,g;window.mixpanel=b;b._i=[];b.init=function(a,e,d){function f(b,h){var a=h.split(".");2==a.length&&(b=b[a[0]],h=a[1]);b[h]=function(){b.push([h].concat(Array.prototype.slice.call(arguments,0)))}}var c=b;"undefined"!==typeof d?c=b[d]=[]:d="mixpanel";c.people=c.people||[];c.toString=function(b){var a="mixpanel";"mixpanel"!==d&&(a+="."+d);b||(a+=" (stub)");return a};c.people.toString=function(){return c.toString(1)+".people (stub)"};i="disable track track_pageview track_links track_forms register register_once alias unregister identify name_tag set_config people.set people.set_once people.increment people.append people.track_charge people.clear_charges people.delete_user".split(" ");
for(g=0;g<i.length;g++)f(c,i[g]);b._i.push([a,e,d])};b.__SV=1.2;a=f.createElement("script");a.type="text/javascript";a.async=!0;a.src="//cdn.mxpnl.com/libs/mixpanel-2.2.min.js";e=f.getElementsByTagName("script")[0];e.parentNode.insertBefore(a,e)}})(document,window.mixpanel||[]);

	mixpanel.init("0c87281db078cadecb4c8fbdbed82daf");
	</script>
	<!-- end Mixpanel -->

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
