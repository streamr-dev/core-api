<form method="post" id="subsSliderForm" action="update">
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

				<input type="submit" id="planUpdate" class="save btn btn-lg btn-primary" value="Your current plan" disabled/>
			</ui:panel>
		</div>
	</div>
</form>