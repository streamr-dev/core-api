<g:render template="/layouts/head" />

<r:require module="streamr" />
<r:require module="jquery" />
<r:require module="main-theme" />
<r:require module="global-error-handler" />
<r:require module="moment" />

<g:render template="/tours/tours" />
<r:require module="tour" />

<g:render template="/layouts/tracking" />

<sec:ifLoggedIn>
	<r:script>
		<!--Start of Tawk.to Script-->
		var Tawk_API=Tawk_API||{}, Tawk_LoadStart=new Date();
		(function(){
			var s1=document.createElement("script"),s0=document.getElementsByTagName("script")[0];
			s1.async=true;
			s1.src='https://embed.tawk.to/5730b2fceec1bc57567cb850/default';
			s1.charset='UTF-8';
			s1.setAttribute('crossorigin','*');
			s0.parentNode.insertBefore(s1,s0);
		})();
		Tawk_API.visitor = {
			name  : '<sec:username/>',
			email : '<sec:username/>'
		};
	</r:script>
</sec:ifLoggedIn>
