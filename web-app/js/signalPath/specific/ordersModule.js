SignalPath.OrdersModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.EmptyModule(data,canvas,my)

	var area = null;

	var tableContainer;
	var table;
	var tableHeader;
	var tableBody;
	var rowCount = Number.MAX_VALUE;
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		if (table!=null)
			$(table).remove();
		
		tableContainer = $("<div></div>");
		table = $("<table></table>");
		
		tableHeader = $("<thead></thead>");
		
//		tableHeader.append("<th></th>");
		tableHeader.append("<th>SP</th>");
		tableHeader.append("<th>PF</th>");
		tableHeader.append("<th>Symbol</th>");
		tableHeader.append("<th>Side</th>");
		tableHeader.append("<th>Status</th>");
		tableHeader.append("<th>Price</th>");
		tableHeader.append("<th>Qty</th>");
		tableHeader.append("<th>ExcQty</th>");
		tableHeader.append("<th>rPnl</th>");
		tableHeader.append("<th>Time</th>");
		tableHeader.append("<th>Reason</th>");
		
		table.append(tableHeader);

		tableBody = $("<tbody></tbody>");
		table.append(tableBody);
		
		tableContainer.append(table);
		
		$(tableContainer).css("width",600);
		$(table).css("width","100%");
		
		$(tableContainer).css("overflow","hidden");
//		$(tableHeader).css("width",d.width);
		$(table).css("display","block");
		$(table).css("width","100%");
		$(table).css("height",400);
		$(table).css("overflow","auto");
		
		my.body.append(tableContainer);
		
//		my.div.resizable();
	}
	my.createDiv = createDiv;
	
	that.receiveResponse = function(d) {
		// Status update message
		if (d.u!=null) {
			var row = $("#order_"+d.u);
			if (row.length==0)
				throw "Order update received but no order was found: orderId "+d.u;
			
			var status = row.find(".status")
			status.html(d.s);
			
			// If cancellable, draw a cancel button
			if (d.c==1) {
				var button = $('<span class="delete modulebutton ui-corner-all ui-icon ui-icon-closethick"></span>');
				button.click((function(oid,mid) {
					return function() {
						var sent = SignalPath.sendUIAction(my.hash, {cancel: oid, marketId:mid}, function(resp) {});
						if (!sent) {
							alert("Could not connect to session.");
						}
					}
				})(d.cu,d.mId));
				status.append(button);
			}
			// Else remove it
			else {
				status.find(".delete").remove();
			}
			
			row.find(".time").html(d.t);
			
			if (d.rsn)
				row.find(".rsn").html(d.rsn);
		}
		// New order message
		else if (d.n!=null) {
			// Remove last row if table full
			var rows = $(tableBody).children();
			if (rows.length==rowCount)
				$(rows[rows.length-1]).remove();
			
			// The order row might already exist if the same order is reported twice
			// In this case replace the row with a new one
			
			var newRow;
			if ($("#order_"+d.n).length>0) {
				newRow = $("#order_"+d.n);
				newRow.empty();
			}
			else newRow = $("<tr id='order_"+d.n+"'></tr>");
			
//			newRow.append("<td></td>"); //TODO: cancel button
			newRow.append("<td>"+d.sp+"</td>");
			newRow.append("<td>"+d.pf+"</td>");
			newRow.append("<td>"+d.symbol+"</td>");
			newRow.append("<td class='side'>"+d.side+"</td>");
			newRow.append("<td class='status'>"+d.status+"</td>");
			newRow.append("<td class='price'>"+d.price+"</td>");
			newRow.append("<td class='qty'>"+d.qty+"</td>");
			newRow.append("<td class='excQty'>"+d.excQty+"</td>");
			newRow.append("<td class='rpnl'></td>");
			newRow.append("<td class='time'>"+d.t+"</td>");
			newRow.append("<td class='rsn'>"+(d.rsn || "")+"</td>");
			
			if (d.side=="BUY")
				newRow.find(".side").addClass("positive");
			else newRow.find(".side").addClass("negative");
			
			tableBody.prepend(newRow);
			
//			newRow.effect("highlight", {queue:false}, 200);
		}
		// Execution message 
		else if (d.e!=null) {
			var row = $("#order_"+d.e);
			row.find(".price").html(d.p);
			row.find(".excQty").html(d.ex);
			row.find(".time").html(d.t);
			row.find(".qty").html(d.q);
			
			if (d.rpnl != 0) {
				var rpnl = row.find(".rpnl");
				rpnl.html(d.rpnl);
				if (d.rpnl > 0) {
					rpnl.addClass("positive");
					rpnl.removeClass("negative");
				}
				else {
					rpnl.addClass("negative");
					rpnl.removeClass("positive");					
				}
			}

			if (d.q==0) row.find(".status").addClass("executed");
			
//			row.effect("highlight", {queue:false}, 200);
		}
	}
	
	var superClean = that.clean;
	that.clean = function() {
		superClean();
		// Remove all rows
		tableBody.find("tr").remove();
	}
	
	return that;
}
