<!DOCTYPE html>
<html>
	<g:render template="/layouts/layoutHead"/>
	
    <body class="${pageProperty( name:'body.class' )}">

		<g:render template="/layouts/topBanner"/>

		<g:if env="lunda">
			<g:render template="/layouts/productionWarning"/>
		</g:if>

        <g:layoutBody />        
		<r:layoutResources/>
    </body>
</html>
