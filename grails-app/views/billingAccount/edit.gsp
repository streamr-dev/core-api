<html>
<head>
	<meta name="layout" content="main" />
	<title><g:message code="billingAccount.edit.label"/></title>

	<r:require module="confirm-button"/>
	<r:require module="moment-timezone"/>
	<r:require module="bootstrap-slider"/>

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

	<div class="row">
		<div class="col-sm-12 col-md-offset-2 col-md-8">
			<ui:panel title="Billing Account Key">
				<legend>Share this key with people whom you want to join to your Billing Account</legend>
				<div><pre>${user?.billingAccount.apiKey}</pre></div>
			</ui:panel>
		</div>
	</div>

	<div class="row">
		<div class="col-sm-12 col-md-offset-2 col-md-8">
			<ui:panel title="Subscription and Billing Information">
				<legend>Current subscription period ends at</legend>
				<div><pre>${subscriptions.subscription.current_period_ends_at}</pre></div>
				<legend>Credit Card information</legend>
				<form method="post" action="${'https://api.chargify.com/api/v2/subscriptions/'+subscriptions.subscription.id+'/card_update'}">
					<input type="hidden" name="secure[api_id]" 	  value="${apiId}"/>
					<input type="hidden" name="secure[timestamp]" value="${timestamp}" />
					<input type="hidden" name="secure[nonce]"     value="${nonce}" />
					<input type="hidden" name="secure[data]" 	  value="${data}"/>
					<input type="hidden" name="secure[signature]" value="${hmac}"/>
					<div class="form-group">
						<label for="creditCardFirstName" class="col-sm-2 control-label">First Name</label>
						<div class="col-sm-10">
							<input type="text" class="form-control" name="payment_profile[first_name]" id="creditCardFirstName" placeholder="" value="${subscriptions.subscription.credit_card.first_name}"/>
						</div>
						<label for="creditCardLastName" class="col-sm-2 control-label">Last Name</label>
						<div class="col-sm-10">
							<input type="text" class="form-control" name="payment_profile[last_name]" id="creditCardLastName" placeholder="" value="${subscriptions.subscription.credit_card.last_name}"/>
						</div>
						<label for="creditCardNumber" class="col-sm-2 control-label">Credit Card</label>
						<div class="col-sm-10">
							<input type="text" class="form-control" name="payment_profile[card_number]"  id="creditCardNumber" placeholder="" value="${subscriptions.subscription.credit_card.masked_card_number}"/>
						</div>
						<label for="creditCardExpirationYear" class="col-sm-2 control-label">Expiration Year</label>
						<div class="col-sm-10">
							<input type="text" class="form-control" name="payment_profile[expiration_year]" id="creditCardExpirationYear" placeholder="" value="${subscriptions.subscription.credit_card.expiration_year}"/>
						</div>
						<label for="creditCardExpirationMonth" class="col-sm-2 control-label">Expiration Month</label>
						<div class="col-sm-10">
							<input type="text" class="form-control" name="payment_profile[expiration_month]" id="creditCardExpirationMonth" placeholder="" value="${subscriptions.subscription.credit_card.expiration_month}"/>
						</div>
					</div>
					<input type="submit" class="save btn btn-lg btn-primary" value="Update"/>
				</form>
			</ui:panel>
		</div>
	</div>

	<form method="post" action="update">
		<div class="row">
			<div class="col-sm-12 col-md-offset-2 col-md-8">
				<ui:panel title="Modify Streamr Subscription">
					<legend>Select Plan</legend>
					<input type="hidden" name="signup[product][handle]" value="${subscriptions.subscription.product.handle}"/>

					<div id="product-information">
						<div id="product-description">${subscriptions.subscription.product.description}</div>
						<div>Price of product per month <span id="product-price">${subscriptions.subscription.product.price_in_cents}</span>€</div>
					</div>

					<input id="product-slider" type="text"
						   data-provide="slider"
						   data-slider-ticks="[1,2,3,4,5,6,7,8,9,10,11]"
						   data-slider-ticks-labels='["Free", "1 CU", "2 CU", "3 CU", "4 CU", "5 CU", "6 CU", "7 CU", "8 CU", "9 CU", "10 CU"]'
						   data-slider-step="1"
						   data-slider-value="1"
						   data-slider-tooltip="hide" style="width:100%" />

					<input type="submit" class="save btn btn-lg btn-primary" value="Your current plan" disabled/>
				</ui:panel>
			</div>

		</div>
	</form>
	<div class="row">
			<div class="col-sm-12 col-md-offset-2 col-md-8">
				<ui:panel title="Statements">
					<g:each in="${statements.statement.html_view}">
						${raw(it)}
					</g:each>
				</ui:panel>
			</div>
	</div>
</g:if>

<g:else>
	<form method="post" action="addBillingAccountKey">
		<div class="row">
			<div class="col-sm-12 col-md-offset-2 col-md-8">
				<ui:panel title="Add Billing Account Key">
					<legend>Billing Account Key</legend>
					<div class="form-group">

						<input type="text" class="form-control" placeholder="" name="billingAccountKey" value=""/>
					</div>
					<input type="submit" class="save btn btn-lg btn-primary" value="Join to Billing Account" />
				</ui:panel>
			</div>
		</div>
	</form>
	<form method="post" action="https://api.chargify.com/api/v2/signups">
		<div class="row">
			<div class="col-sm-12 col-md-offset-2 col-md-8">
				<ui:panel title="Streamr Subscription">
					<input type="hidden" name="secure[api_id]" 	  value="${apiId}"/>
					<input type="hidden" name="secure[timestamp]" value="${timestamp}" />
					<input type="hidden" name="secure[nonce]"     value="${nonce}" />
					<input type="hidden" name="secure[data]" 	  value="${data}"/>
					<input type="hidden" name="secure[signature]" value="${hmac}"/>
					<input type="hidden" name="signup[product][handle]" value="free"/>
					<div class="form-group">
						<legend>Select Plan</legend>

						<div id="product-information">
							<div id="product-description">${products.first().product.description}</div>
							<div>Price of product per month <span id="product-price">${products.first().product.price_in_cents}</span>€</div>
						</div>

						<input id="product-slider" type="text"
							   data-provide="slider"
							   data-slider-ticks="[1,2,3,4,5,6,7,8,9,10,11]"
							   data-slider-ticks-labels='["Free", "1 CU", "2 CU", "3 CU", "4 CU", "5 CU", "6 CU", "7 CU", "8 CU", "9 CU", "10 CU"]'
							   data-slider-step="1"
							   data-slider-value="1"
							   data-slider-tooltip="hide" style="width:100%" />
					</div>

					<div class="form-group">
						<legend>About You</legend>

						<p>
							<label for="signup_customer_first_name">First Name</label><br/>
							<input type="text" name="signup[customer][first_name]" id="signup_customer_first_name" value="" />
						</p>
						<p>
							<label for="signup_customer_last_name">Last Name</label><br/>
							<input type="text" name="signup[customer][last_name]" id="signup_customer_last_name" value=""/>
						</p>
						<p>
							<label for="signup_customer_email">Email</label><br/>
							<input type="text" name="signup[customer][email]" id="signup_customer_email" value=""/>
						</p>
						%{--TODO reference should be billing account reference, it might not be user reference..unless there's no billing account yet--}%
						<input type="hidden" name="signup[customer][reference]" id="" value="${user.username}"/>
					</div>

					<fieldset>
						<legend>Payment Profile</legend>

						<p>
							<label for="signup_payment_profile_first_name">First Name on Card</label><br/>
							<input type="text" name="signup[payment_profile][first_name]" id="signup_payment_profile_first_name" value="" />
						</p>

						<p>
							<label for="signup_payment_profile_first_name">Last Name on Card</label><br/>
							<input type="text" name="signup[payment_profile][last_name]" id="signup_payment_profile_last_name" value="" />
						</p>

						<p>
							<label for="signup_payment_profile_card_number">Card Number</label><br/>
							<input type="text" name="signup[payment_profile][card_number]" id="signup_payment_profile_card_number" value="" />
						</p>

						<p>
							Expiration <label for="signup_payment_profile_expiration_month">Month</label> / <label for="signup_payment_profile_expiration_year">Year</label><br/>
							<select name="signup[payment_profile][expiration_month]" id="signup_payment_profile_expiration_month">
								<option value="1">01</option>
								<option value="2">02</option>
								<option value="3">03</option>
								<option value="4">04</option>
								<option value="5">05</option>
								<option value="6">06</option>
								<option value="7">07</option>
								<option value="8">08</option>
								<option value="9">09</option>
								<option value="10">10</option>
								<option value="11">11</option>
								<option value="12">12</option>
							</select>
							/
							<select name="signup[payment_profile][expiration_year]" id="signup_payment_profile_expiration_year">
								<option value="2016">2016</option>
								<option value="2017">2017</option>
								<option value="2018">2018</option>
								<option value="2019">2019</option>
								<option value="2020">2020</option>
								<option value="2021">2021</option>
								<option value="2022">2022</option>
								<option value="2023">2023</option>
								<option value="2024">2024</option>
								<option value="2025">2025</option>
								<option value="2026">2026</option>
							</select>
						</p>
					</fieldset>
					<input type="submit" class="save btn btn-lg btn-primary" value="Sign Up" />
				</ui:panel>
			</div>
		</div>
	</form>
</g:else>


<g:javascript>
	var products = JSON.parse("${products}");
	var productIndex = _.findIndex(products,{product:{handle:"${subscriptions.subscription.product.handle}"}})
	$(function() {
		var newVal = $('#product-slider').data('slider').setValue(productIndex+1);



		$('#product-slider').slider().on('slideStop', function(ev){
			var newVal = $('#product-slider').data('slider').getValue();
			console.log(products[newVal-1].product.handle);
			var productVal = products[newVal-1].product.handle;
			$('input[name="signup[product][handle]"]').attr('value',productVal);

			$('#product-description').text(products[newVal-1].product.description)
			$('#product-price').text(products[newVal-1].product.price_in_cents/100)

			if (productIndex < (newVal-1)){
				$('input[type="submit"]').prop('disabled',false);
				$('input[type="submit"]').val('Upgrade');
			} else if (productIndex > (newVal -1)) {
				$('input[type="submit"]').prop('disabled',false);
				$('input[type="submit"]').val('Downgrade');
			}
			else {
				$('input[type="submit"]').prop('disabled',true);
				$('input[type="submit"]').val('Your current plan');
			}

		});
	});

</g:javascript>

</body>
</html>
