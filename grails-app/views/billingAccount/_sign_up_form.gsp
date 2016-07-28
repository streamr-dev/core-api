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
						   data-slider-tooltip="hide" style="width:100%"/>
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