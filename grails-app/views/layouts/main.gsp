<!DOCTYPE html>
<html>
	<g:render template="/layouts/layoutHead"/>
	
    <body class="${pageProperty( name:'body.theme' ) ?: 'selected-theme'} ${pageProperty( name:'body.class' )}">

		<div id="wrapper">

			<g:render template="/layouts/topBanner"/>
	
	        <g:layoutBody />
        
        </div>
                
		<r:layoutResources/>
    </body>
</html>
