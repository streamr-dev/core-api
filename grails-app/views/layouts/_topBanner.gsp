<div id="main-navbar" class="navbar navbar-inverse" role="navigation">

	<!-- Main menu toggle -->
	<button type="button" id="main-menu-toggle"><i class="navbar-icon fa fa-bars icon"></i><span class="hide-menu-text">HIDE MENU</span></button>

	<div class="navbar-inner">
			<!-- Main navbar header -->
			<div class="navbar-header">
			
				<!-- Logo -->
				<a href="${createLink(uri: '/')}" class="navbar-brand">
					<g:img dir="images" file="unifina_logo_black.png" plugin="unifina-core"/>
				</a>

				<!-- Main navbar toggle -->
				<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#main-navbar-collapse"><i class="navbar-icon fa fa-bars"></i></button>

			</div> <!-- / .navbar-header -->
		
			<div class="topnav">
				<g:render template="/layouts/topnav"/>
			</div>
		
	</div>
</div>