<form id="credit-card-update-form" style="display:none;" method="post" action="${'https://api.chargify.com/api/v2/subscriptions/'+subscriptions.subscription.id+'/card_update'}">
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

		<label for="creditCardNumber" class="col-sm-2 control-label">Credit Card Expiration</label>
		<div class="col-sm-10">
			<label for="signup_payment_profile_expiration_month">Month</label> / <label for="signup_payment_profile_expiration_year">Year</label><br/>
			<select name="payment_profile[expiration_month]" id="creditCardExpirationYear">
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
			<select name="payment_profile[expiration_year]" id="creditCardExpirationMonth">
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
		</div>
	</div>
	<input type="submit" id="creditCardUpdate" class="save btn btn-lg btn-primary" value="Update"/>
</form>