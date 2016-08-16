<html>
<head>
	<meta name="layout" content="main" />
	<title><g:message code="billingAccount.label"/></title>

	<r:require module="bootstrap-slider"/>
	<r:require module="cardjs"/>

</head>
<body class="billing-account-page billing-account-edit-page">
	<div class="col-md-8 col-md-offset-2">

		<div class="row">
			<div class="col-xs-12">
				<ui:flashMessage/>
			</div>
		</div>

		<g:if test="${subscriptions}">
		<div class="row">
			<div class="col-xs-12">
				<ui:panel title="Modify Streamr Subscription">
					<form method="post" id="subsSliderForm" action="update">
						<input type="hidden" name="signup[product][handle]" value="${subscriptions.subscription.product.handle}"/>
						<g:render template="subs_slider" model="${pageScope.variables}"/>
					</form>
				</ui:panel>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-12 col-md-6">
				<ui:panel title="Subscription and Billing Information" class="billing-information">
					<div class="form-group">
						<label>Current subscription period ends at</label>
						<div>${Date.parse("yyyy-MM-dd'T'HH:mm:ss",subscriptions.subscription.current_period_ends_at).format("YYYY-MM-dd")}</div>
					</div>
					<label>Credit Card information</label>
					<form id="credit-card-dummy-form" class="hidden-xs">
						<div class="credit-card-container"></div>
					</form>
					<div class="hidden-sm hidden-md hidden-lg">
						<div class="form-group">
							<label>Name In Card</label>
							<div>${subscriptions.subscription.credit_card.first_name} ${subscriptions.subscription.credit_card.first_name}</div>
						</div>
						<div class="form-group">
							<label>Card number</label>
							<div>${subscriptions.subscription.credit_card.masked_card_number}</div>
						</div>
						<div class="form-group">
							<label>Valid Until</label>
							<div>${subscriptions.subscription.credit_card.expiration_month} / ${subscriptions.subscription.credit_card.expiration_year}</div>
						</div>
					</div>
					<div class="col-xs-12 form-group">
						<g:link class="btn" action="changeCreditCard">Change</g:link>
					</div>
				</ui:panel>
			</div>
			<g:if test="${statements}">
			<div class="col-xs-12 col-md-6">
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
			</g:if>
		</div>
		</g:if>
		<div class="row">
			<g:render template="email_invite_form" model="${pageScope.variables}"/>
		</div>
		<div class="row">
			<div class="col-xs-12">
				<button type="submit" class="save btn btn-primary pull-right" id="planUpdate" form="subsSliderForm"></button>
			</div>
		</div>
	</div>
</div>


<g:javascript>
	$(function() {
		var products = JSON.parse("${products}")
		var productIndex = _.findIndex(products, {
			product: {
				handle: "${subscriptions.subscription.product.handle}"
			}
		})
		if ($('#product-slider').length){
			var slider = $('#product-slider').slider()
			slider.on('change', function() {
			    var value = slider.slider("getValue") - 1
				$('#product-name').text(products[value].product.name)
				$('#product-price').text(products[value].product.price_in_cents/100)
			})
			slider.on('slideStop', function(ev){
			    var value = slider.slider("getValue") - 1
				var productVal = products[value].product.handle;
				$('input[name="signup[product][handle]"]').attr('value', productVal);
				if (productIndex < value){
					$('#planUpdate').prop('disabled',false);
					$('#planUpdate').text('Upgrade');
				} else if (productIndex > value) {
					$('#planUpdate').prop('disabled',false);
					$('#planUpdate').text('Downgrade');
				} else {
					$('#planUpdate').prop('disabled',true);
					$('#planUpdate').text('Your current plan');
				}
			})
			slider.slider("setValue", productIndex + 1)
			slider.trigger("change")
			slider.trigger("slideStop")
		}

		$('#credit-card-form-button').on('click', function(e) {
			$('#credit-card-update-form').slideDown()
			$(".panel.billing-information").addClass("open")
		})

		$("#credit-card-dummy-form").card({
			form: "#credit-card-update-form",
			container: ".credit-card-container",
			placeholders: {
				number: '${ subscriptions.subscription.credit_card.masked_card_number }',
				name: '${ subscriptions.subscription.credit_card.first_name } ${ subscriptions.subscription.credit_card.last_name }',
				expiry: '${ subscriptions.subscription.credit_card.expiration_month }/${ subscriptions.subscription.credit_card.expiration_year }',
				cvc: '***'
			}
		})
		var cardType = "${ subscriptions.subscription.credit_card.card_type }"
		var typeClassesByType = {
		    american_express: 'jp-card-amex',
		    dankort: 'jp-card-dankort',
		    diners_club: 'jp-card-dinersclub',
		    discover: 'jp-card-discover',
		    laser: 'jp-card-laser',
		    maestro: 'jp-card-maestro',
		    master: 'jp-card-mastercard',
		    visa: 'jp-card-visa'
		}
		$("#credit-card-dummy-form .jp-card-container .jp-card").addClass("jp-card-identified").addClass(typeClassesByType[cardType] ? typeClassesByType[cardType] : "")
	})
</g:javascript>

</body>
</html>
