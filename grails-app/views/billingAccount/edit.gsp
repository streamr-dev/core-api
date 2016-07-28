<html>
<head>
	<meta name="layout" content="main" />
	<title><g:message code="billingAccount.edit.label"/></title>

	<r:require module="confirm-button"/>
	<r:require module="moment-timezone"/>
	<r:require module="bootstrap-slider"/>
	<r:require module="bootstrap-credit-card"/>

	<r:script>
		$(document).ready(function() {
			var tzOpts = moment.tz.names().map(function(tz) {
				return $('<option '+(tz === "${user.timezone}" ? 'selected': '')+' value="'+tz+'">'+tz+'</option>')
			})
			$("#timezone").append(tzOpts)
		})
	</r:script>

</head>
<body>

<div class="row">
	<div class="col-sm-12 col-md-offset-2 col-md-8">
		<ui:flashMessage/>
	</div>
</div>

<g:if test="${user?.billingAccount}">

	<g:render template="email_invite_form" model="${pageScope.variables}"/>

	<g:if test="${subscriptions}">
		<div class="row">
			<div class="col-sm-12 col-md-offset-2 col-md-8">
				<ui:panel title="Subscription and Billing Information">
					<legend>Current subscription period ends at</legend>
					<div><pre>${Date.parse("yyyy-MM-dd'T'HH:mm:ss",subscriptions.subscription.current_period_ends_at).format("YYYY-MM-dd")}</pre></div>
					<legend>Credit Card information</legend>

					<div class="my-cc">
							<div class="row">
								<div class="col-xs-12">
									<!-- Card -->
									<div class="item item-black item-image">
										<!-- Transparent Image -->
										<img src="${resource(dir:'images',file:'transparent.png')}" alt="" class="img-responsive" />
										<!-- Heading -->
										<div class="item-heading clearfix">
											<!-- Heading -->
											<h3>Your Card</h3>
											<!-- Bank Name -->
											<h4></h4>
										</div>
										<!-- Account -->
										<div class="item-account">
											<!-- Value -->
											<g:each in="${subscriptions.subscription.credit_card.masked_card_number.split('-')}">
												<span>${it}</span>
											</g:each>
										</div>
										<!-- Validity Starts -->
										<div class="item-validity">
											<div class="row">
												<div class="col-md-6 col-sm-6 col-xs-6">
													<!-- Item -->
													%{--<div class="item-valid clearfix">
														<!-- Valid From -->
														<h5>Valid From</h5>
														<!-- Date -->
														<span>12/05</span>
													</div>--}%
												</div>
												<div class="col-md-6 col-sm-6 col-xs-6">
													<!-- Item -->
													<div class="item-valid clearfix">
														<!-- Valid Thru -->
														<h5>Valid Thru</h5>
														<!-- Date -->
														<span>${subscriptions.subscription.credit_card.expiration_month}/${subscriptions.subscription.credit_card.expiration_year.toString()[-2..-1]}</span>
													</div>
												</div>
											</div>
										</div>
										<!-- Validity Ends -->

										<!-- Card Type Starts -->
										<div class="item-cc-type clearfix">
											<!-- Type -->
											<h6>${subscriptions.subscription.credit_card.first_name + " " + subscriptions.subscription.credit_card.last_name}</h6>
											<!-- Icon -->
											<i class="fa fa-cc-visa"></i>
										</div>
										<!-- Card Type Ends -->
									</div>
								</div>
							</div>
					</div>

					<button type="button" id="credit-card-form-button" class="btn btn-info">Change</button>

					<g:render template="credit_card_update_form" model="${pageScope.variables}"/>

				</ui:panel>
			</div>
		</div>

		<g:render template="subs_slider_form" model="${pageScope.variables}"/>

		<g:if test="${statements}">
			<div class="row">
					<div class="col-sm-12 col-md-offset-2 col-md-8">
						<ui:panel title="Statements">
							<table class="table">
								<thead>
									<tr>
										<td>Date</td>
										<td>Sum</td>
										<td>Link</td>
									</tr>
								</thead>
								<tbody>
									<g:each in="${statements?.content.statement}">
										<tr>
											<td>${Date.parse("yyyy-MM-dd'T'HH:mm:ss",it.created_at).format("YYYY-MM-dd")}</td>
											<td>${it.total_in_cents.toInteger()/100} €</td>
											<td><g:link controller="billingAccount" action="statement" params="[statementId:it.id]">Statement ${it.id}</g:link></td>
										</tr>
									</g:each>
								</tbody>
							</table>
						</ui:panel>
					</div>
			</div
		</g:if>
	</g:if>
</g:if>

<g:else>
	<g:render template="sign_up_form" model="${pageScope.variables}"/>
</g:else>

<g:javascript>
	var products = JSON.parse("${products}");
	var productIndex = _.findIndex(products,{product:{handle:"${subscriptions.subscription.product.handle}"}})
	$(function() {
		if ($('#product-slider').length){
			var newVal = $('#product-slider').data('slider').setValue(productIndex+1);
			$('#product-slider').slider().on('slideStop', function(ev){
				var newVal = $('#product-slider').data('slider').getValue();
				/*console.log(products[newVal-1].product.handle);*/
				var productVal = products[newVal-1].product.handle;
				$('input[name="signup[product][handle]"]').attr('value',productVal);

				$('#product-description').text(products[newVal-1].product.description)
				$('#product-price').text(products[newVal-1].product.price_in_cents/100)

				if (productIndex < (newVal-1)){
					$('#planUpdate').prop('disabled',false);
					$('#planUpdate').val('Upgrade');
				} else if (productIndex > (newVal -1)) {
					$('#planUpdate').prop('disabled',false);
					$('#planUpdate').val('Downgrade');
				}
				else {
					$('#planUpdate').prop('disabled',true);
					$('#planUpdate').val('Your current plan');
				}
			});
		}
	});


	$('#credit-card-form-button').on('click', function(e) {
		$('#credit-card-update-form').toggle('slow');
	})

</g:javascript>

</body>
</html>
