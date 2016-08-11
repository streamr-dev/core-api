<form method="post" id="sign-up-form" action="https://api.chargify.com/api/v2/signups">
	<div class="row">
		<div class="col-md-8 col-md-offset-2">
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
		<div class="col-sm-12 col-md-4 col-md-offset-2">
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
		<div class="col-sm-12 col-md-4">
			<ui:panel title="Payment Profile">
				<div class="form-group">
					<label for="signup_payment_profile_first_name">First Name on Card</label><br/>
					<input class="form-control" type="text" name="signup[payment_profile][first_name]" id="signup_payment_profile_first_name" value="" />
				</div>
				<div class="form-group">
					<label for="signup_payment_profile_first_name">Last Name on Card</label><br/>
					<input class="form-control" type="text" name="signup[payment_profile][last_name]" id="signup_payment_profile_last_name" value="" />
				</div>
				<div class="form-group">
					<label for="signup_payment_profile_card_number">Card Number</label><br/>
					<input class="form-control" type="text" name="signup[payment_profile][card_number]" id="signup_payment_profile_card_number" value="" />
				</div>
				<div class="form-group form-inline">
					Expiration <label for="signup_payment_profile_expiration_month">Month</label> / <label for="signup_payment_profile_expiration_year">Year</label><br/>
					<select class="form-control" name="signup[payment_profile][expiration_month]" id="signup_payment_profile_expiration_month">
						<g:each in="${1..12}">
							<option value="${it}">${ it < 10 ? '0' + it : it }</option>
						</g:each>
					</select>
					/
					<select class="form-control" name="signup[payment_profile][expiration_year]" id="signup_payment_profile_expiration_year">
						<g:set var="currentYear" value="${ Calendar.getInstance().get(Calendar.YEAR) }"/>
						<g:each in="${ currentYear..currentYear + 10 }">
							<option value="${it}">${it}</option>
						</g:each>
					</select>
				</div>
			</div>
		</ui:panel>
	</div>
	<div class="col-md-8 col-md-offset-2">
		<button type="submit" class="save btn btn-primary pull-right">Sign Up</button>
	</div>
</form>
