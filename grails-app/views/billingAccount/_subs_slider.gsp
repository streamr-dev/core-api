
<div id="product-information" class="col-xs-12">
	<table class="table table-condensed plan-table">
		%{-- If you change the amount of fields, also change the width of .plan-table th in billing-tables.less --}%
		<tr>
			<th>Plan</th>
			<th>A</th>
			<th>B</th>
			<th>C</th>
			<th>Price per month</th>
		</tr>
		<tr>
			<td>
				<span id="product-name">
					${subscriptions.subscription.product.name}
				</span>
			</td>
			<td>X amount of A</td>
			<td>Y amount of B</td>
			<td>Z amount of C</td>
			<td>
				<span id="product-price">
					${subscriptions.subscription.product.price_in_cents / 100}
				</span>
				€
			</td>
		</tr>
	</table>
	</div>
	<div class="col-xs-12">
	<div class="col-xs-12">
	<input id="product-slider" type="text"
		   data-slider-ticks="[1,2,3,4,5,6,7,8,9,10,11]"
		   data-slider-ticks-labels='["Free", "1 CU", "2 CU", "3 CU", "4 CU", "5 CU", "6 CU", "7 CU", "8 CU", "9 CU", "10 CU"]'
		   data-slider-step="1"
		   data-slider-value="1"
		   data-slider-tooltip="hide"
		   style="width:100%"/>
	</div>
</div>
