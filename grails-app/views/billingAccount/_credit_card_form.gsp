
<div class="form-group">
	<label>First Name</label>
	<input type="text" class="form-control" name="${firstName}" id="creditCardFirstName" placeholder="" value="${subscriptions?.subscription?.credit_card?.first_name}"/>
</div>
<div class="form-group">
	<label>Last Name</label>
	<input type="text" class="form-control" name="${lastName}" id="creditCardLastName" placeholder="" value="${subscriptions?.subscription?.credit_card?.last_name}"/>
</div>
<div class="form-group">
	<label>Card Number</label>
	<input type="text" class="form-control" name="${cardNumber}"  id="creditCardNumber" placeholder=""/>
</div>
<div class="form-inline form-group">
	<div class="form-group">
		Expiration <label>Month</label> / <label>Year</label><br/>
		<input type="number" name="${expirationMonth}" min="1" max="12" value="1" class="form-control">
		/
		<input type="number" name="${expirationYear}" value="${Calendar.getInstance().get(Calendar.YEAR)}" min="${Calendar.getInstance().get(Calendar.YEAR)}" max="${Calendar.getInstance().get(Calendar.YEAR)} + 10" class="form-control">
	</div>
</div>
<div class="form-group">
	<label for="payment_profile_cvv">CVV</label><br/>
	<input class="form-control" type="text" style="width:100px;" name="${cvv}" id="payment_profile_cvv"/>
</div>

