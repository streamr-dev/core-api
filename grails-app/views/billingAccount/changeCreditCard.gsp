<html>
<head>
	<meta name="layout" content="main" />
	<title>Change Credit Card</title>
	<r:require module="cardjs"/>

	<r:script>
		$("#credit-card-update-form").card({
			form: '#credit-card-update-form',
			container: '.credit-card-container',
			placeholders: {
				number: '**** **** **** ****',
				name: '${subscriptions.subscription.credit_card.first_name} ${subscriptions.subscription.credit_card.last_name}',
				expiry: '**/****',
				cvc: '***'
			},
			formSelectors: {
				numberInput: 'input[name="payment_profile[card_number]"]', // optional — default input[name="number"]
				expiryInput: 'input[name="payment_profile[expiration_month]"], input[name="payment_profile[expiration_year]"]', // optional — default input[name="expiry"]
				cvcInput: 'input[name="payment_profile[cvv]"]', // optional — default input[name="cvc"]
				nameInput: 'input[name="payment_profile[first_name]"], input[name="payment_profile[last_name]"]' // optional - defaults input[name="name"]
			}
		})
	</r:script>
</head>

<body class="billing-account-page">
	<div class="col-xs-12 col-md-6 col-md-offset-3 col-lg-4 col-lg-offset-4">
		<ui:panel title="Change credit Card">

			<div class="credit-card-container hidden-xs"></div>
			<form id="credit-card-update-form" method="post" action="${'https://api.chargify.com/api/v2/subscriptions/'+subscriptions.subscription.id+'/card_update'}">
				<input type="hidden" name="secure[api_id]" 	  value="${apiId}"/>
				<input type="hidden" name="secure[timestamp]" value="${timestamp}" />
				<input type="hidden" name="secure[nonce]"     value="${nonce}" />
				<input type="hidden" name="secure[signature]" value="${hmac}"/>
				<input type="hidden" name="secure[data]" value="${data}"/>
				<g:render template="credit_card_form" model="[
						'firstName': 'payment_profile[first_name]',
				        'lastName': 'payment_profile[last_name]',
						'cardNumber': 'payment_profile[card_number]',
						'expirationMonth': 'payment_profile[expiration_month]',
						'expirationYear': 'payment_profile[expiration_year]',
						'cvv': 'payment_profile[cvv]',
						'subscriptions': subscriptions
				]"/>
				<div class="row">
					<div class="col-xs-12">
						<g:link action="edit" class="btn">Cancel</g:link>
						<button type="submit" id="creditCardUpdate" class="btn btn-primary pull-right">Update</button>
					</div>
				</div>
			</form>
		</ui:panel>
	</div>


</body>
</html>