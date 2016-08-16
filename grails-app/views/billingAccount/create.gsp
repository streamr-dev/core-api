<html>
<head>
	<meta name="layout" content="main" />
	<title><g:message code="billingAccount.label"/></title>

	<r:require module="bootstrap-slider"/>
	<r:require module="cardjs"/>

</head>
<body class="billing-account-page billing-account-create-page">
	<div class="col-md-8 col-md-offset-2">
		<div class="row">
			<div class="col-xs-12">
				<ui:flashMessage/>
			</div>
		</div>

		<form method="post" id="signup-form" action="https://api.chargify.com/api/v2/signups">
			<div class="row">
				<div class="col-xs-12">
					<ui:panel title="Select Subscription Plan">
						<input type="hidden" name="secure[api_id]" 	  value="${apiId}"/>
						<input type="hidden" name="secure[timestamp]" value="${timestamp}" />
						<input type="hidden" name="secure[nonce]"     value="${nonce}" />
						<input type="hidden" name="secure[data]" 	  value="${data}"/>
						<input type="hidden" name="secure[signature]" value="${hmac}"/>
						<input type="hidden" name="signup[product][handle]" value="free"/>
						<g:render template="subs_slider"/>
					</ui:panel>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12 col-md-6">
					<ui:panel title="Payment Profile">
						<div class="credit-card-container"></div>
						<g:render template="credit_card_form" model="[
								'firstName': 'signup[payment_profile][first_name]',
								'lastName': 'signup[payment_profile][last_name]',
								'cardNumber': 'signup[payment_profile][card_number]',
								'expirationMonth': 'signup[payment_profile][expiration_month]',
								'expirationYear': 'signup[payment_profile][expiration_year]',
								'cvv': 'signup[payment_profile][cvv]'
						]"/>
					</ui:panel>
				</div>
				<div class="col-xs-12 col-md-6">
					<ui:panel title="About You">
						<div class="form-group">
							<label for="signup_customer_first_name">First Name</label><br/>
							<input class="form-control" type="text" name="signup[customer][first_name]" id="signup_customer_first_name" value="" />
						</div>
						<div class="form-group">
							<label for="signup_customer_last_name">Last Name</label><br/>
							<input class="form-control" type="text" name="signup[customer][last_name]" id="signup_customer_last_name" value=""/>
						</div>
						<div class="form-group">
							<label for="signup_customer_email">Email</label><br/>
							<input class="form-control" type="text" name="signup[customer][email]" id="signup_customer_email" value=""/>
						</div>
					%{--TODO reference should be billing account reference, it might not be user reference..unless there's no billing account yet--}%
						<input type="hidden" name="signup[customer][reference]" id="" value="${user.username}"/>
					</ui:panel>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<button type="submit" class="save btn btn-primary">Sign Up</button>
				</div>
			</div>
		</form>
	</div>


	<g:javascript>
		var products = JSON.parse("${products}");
		$(function() {
			if ($('#product-slider').length){
				var slider = $('#product-slider').slider()
				slider.on('change', function() {
					var value = slider.slider("getValue") - 1
					$('#product-name').text(products[value].product.name)
					$('#product-price').text(products[value].product.price_in_cents/100)
					$("input[name='signup[product][handle]']").val(products[value].product.handle)
				})
				slider.slider("setValue", 6)
				slider.trigger("change")
			}
		});


		$('#credit-card-form-button').on('click', function(e) {
			$('#credit-card-update-form').slideDown()
			$(".panel.billing-information").addClass("open")
		})

		$("#signup-form").card({
			form: '#signup-form',
			container: '.credit-card-container',
			placeholders: {
				number: '**** **** **** ****',
				name: '****** *******',
				expiry: '**/****',
				cvc: '***'
			},
			formSelectors: {
				numberInput: 'input[name="signup[payment_profile][card_number]"]', // optional — default input[name="number"]
				expiryInput: 'input[name="signup[payment_profile][expiration_month]"], input[name="signup[payment_profile][expiration_year]"]', // optional — default input[name="expiry"]
				cvcInput: 'input[name="signup[payment_profile][cvv]"]', // optional — default input[name="cvc"]
				nameInput: 'input[name="signup[payment_profile][first_name]"], input[name="signup[payment_profile][last_name]"]' // optional - defaults input[name="name"]
			}
		})

	</g:javascript>

</body>
</html>
