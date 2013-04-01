SignalPath.ManualOrderEntryModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.EmptyModule(data,canvas,my)
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		var symTable = $("<table><tr><td>Symbol:</td><td></td></tr><tr><td>Portfolio:</td><td></td></tr><tr><td>Quantity:</td><td></td></tr><tr><td>Price:</td><td></td></tr></table>");
		
		var ob = $("<input class='manualOrderBook' type='text' value='SYMBOL'>");
		var pf = $("<input class='manualPortfolio' type='text' value='MANUAL'>");
		var quantity = $("<input class='manualQuantity' type='text' value='100'>");
		var price = $("<input class='manualPrice' type='text' value='PRICE'>");
		
		
		$(symTable.find("tr td")[1]).append(ob);
		$(symTable.find("tr td")[3]).append(pf);
		$(symTable.find("tr td")[5]).append(quantity);
		$(symTable.find("tr td")[7]).append(price);
		my.body.append(symTable);
		
		var types = $("<select class='manualOrderType'></select>");
		types.append("<option>LMT</option>");
//		types.append("<option>MKT</option>");
		my.body.append(types);
		
		/*
		var side = $("<select class='manualSide'></select>");
		side.append("<option>BUY</option>");
		side.append("<option>SELL</option>");
		my.body.append(side);
		*/
		
		var tif = $("<select class='manualTif'></select>");
		tif.append("<option>DAY</option>");
		tif.append("<option>IOC</option>");
		tif.append("<option>FOK</option>");
		tif.append("<option>AUCTION</option>");
		my.body.append(tif);

		var buy = $("<button>BUY</button>");
		buy.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {
				type: types.val(),
				side: "BUY",
				orderBook: ob.val(),
				portfolio: pf.val(),
				quantity: parseInt(quantity.val()),
				price: parseFloat(price.val()),
				tif: tif.val()
			},
			function(resp) {
				
			});
			
			if (!sent) {
				alert("Not running.");
			}
		});
		my.body.append(buy);
		
		var sell = $("<button style='float:right'>SELL</button>");
		sell.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {
				type: types.val(),
				side: "SELL",
				orderBook: ob.val(),
				portfolio: pf.val(),
				quantity: parseInt(quantity.val()),
				price: parseFloat(price.val()),
				tif: tif.val()
			},
			function(resp) {
				
			});
			
			if (!sent) {
				alert("Not running.");
			}
		});
		my.body.append(sell);
		
		/*
		var cancel = $("<input class='manualCancel' type='text' value='orderId'>");
		my.body.append(cancel);

		var cancelButton = $("<button>Cancel</button>");
		cancelButton.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {
				cancel: parseInt(cancel.val()),
			},
			function(resp) {
				
			});
			
			if (!sent) {
				alert("Not running.");
			}
		});
		my.body.append(cancelButton);
		*/
	}
	my.createDiv = createDiv;
	
	that.receiveResponse = function(d) {

	}
	
	var superClean = that.clean;
	that.clean = function() {
		superClean();
	}
	
	return that;
}
