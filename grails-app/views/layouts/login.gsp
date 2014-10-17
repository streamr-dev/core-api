<!DOCTYPE html>
<!--[if IE 8]>         <html class="ie8"> <![endif]-->
<!--[if IE 9]>         <html class="ie9 gt-ie8"> <![endif]-->
<!--[if gt IE 9]><!--> <html class="gt-ie8 gt-ie9 not-ie"> <!--<![endif]-->

	<g:render template="/layouts/layoutHead"/>
	
    <body class="no-main-menu page-signin-alt ${pageProperty( name:'body.theme' ) ?: 'selected-theme'} ${pageProperty( name:'body.class' )}">
		<div class="signin-header">
			<r:img dir="images" file="unifina_logo_black.png" alt="" style="margin-top: -4px;"/>
			<g:link controller="register" action="signup" class="btn btn-primary btn-sign-up">Sign Up</g:link>
			<g:link controller="register" action="signup" class="btn btn-primary btn-sign-in">Sign In</g:link>
		</div> <!-- / .header -->
		<g:layoutBody />
		<r:layoutResources/>	
    </body>
</html>
