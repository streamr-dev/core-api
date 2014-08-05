<!DOCTYPE html>
<html>
	<g:render template="/layouts/layoutHead"/>
	
    <body class="${pageProperty( name:'body.class' )}">

		<g:render template="/layouts/topBanner"/>

        <g:layoutBody />        
		<r:layoutResources/>
    </body>
</html>
