
<form id="credit-card-update-form" style="display:none;" method="post" action="${'https://api.chargify.com/api/v2/subscriptions/'+subscriptions.subscription.id+'/card_update'}">
	<div class="col-xs-12">
		<input type="hidden" name="secure[api_id]" 	  value="${apiId}"/>
		<input type="hidden" name="secure[timestamp]" value="${timestamp}" />
		<input type="hidden" name="secure[nonce]"     value="${nonce}" />
		<input type="hidden" name="secure[data]" 	  value="${data}"/>
		<input type="hidden" name="secure[signature]" value="${hmac}"/>
		<div class="form-group">
			<label>First Name</label>
			<input type="text" class="form-control" name="payment_profile[first_name]" id="creditCardFirstName" placeholder="" value="${subscriptions.subscription.credit_card.first_name}"/>
		</div>
		<div class="form-group">
			<label>Last Name</label>
			<input type="text" class="form-control" name="payment_profile[last_name]" id="creditCardLastName" placeholder="" value="${subscriptions.subscription.credit_card.last_name}"/>
		</div>
		<div class="form-group">
			<label>Credit Card</label>
			<input type="text" class="form-control" name="payment_profile[card_number]"  id="creditCardNumber" placeholder="" value="${subscriptions.subscription.credit_card.masked_card_number}"/>
		</div>
		<div class="form-group">
			<div class="form-group form-inline">
				Expiration <label>Month</label> / <label>Year</label><br/>
				<select class="form-control" name="payment_profile[expiration_month]" id="creditCardExpirationMonth">
					<g:each in="${1..12}">
						<option value="${it}">${ it < 10 ? '0' + it : it }</option>
					</g:each>
				</select>
				/
				<select class="form-control" name="payment_profile[expiration_year]" id="creditCardExpirationYear">
					<g:set var="currentYear" value="${ Calendar.getInstance().get(Calendar.YEAR) }"/>
					<g:each in="${ currentYear..currentYear + 10 }">
						<option value="${it}">${it}</option>
					</g:each>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-12">
				<button type="submit" id="creditCardUpdate" class="save btn btn-primary pull-right">Update</button>
			</div>
		</div>
	</div>
</form>