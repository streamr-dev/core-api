<!DOCTYPE html>
<html>
	<g:render template="/layouts/layoutHead"/>
	
    <body class="${pageProperty( name:'body.class' )}">

		<g:render template="/layouts/topBanner" model="[centered:true]"/>

<%--        <div id="grailsLogo"><a href="http://grails.org"><img src="${resource(dir:'images',file:'grails_logo.png')}" alt="Grails" border="0" /></a></div>--%>
        <g:layoutBody />
        
		<r:layoutResources/>
    </body>
</html>
