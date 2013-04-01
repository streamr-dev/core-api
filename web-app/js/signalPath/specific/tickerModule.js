SignalPath.TickerModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.EmptyModule(data,canvas,my)

	var area = null;

	var tableContainer;
	var table;
	var tableHeader;
	var tableBody;
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		if (table!=null)
			$(table).remove();
		
		tableContainer = $("<div></div>");
		table = $("<table></table>");
		
		tableHeader = $("<thead></thead>");
		
		tableHeader.append("<th>Portfolio</th>");
		tableHeader.append("<th>Symbol</th>");
		tableHeader.append("<th>Position</th>");
		tableHeader.append("<th>PurchaseVal</th>");
		tableHeader.append("<th>AvgPrice</th>");
		tableHeader.append("<th>uPnl</th>");
		tableHeader.append("<th>rPnl</th>");
		tableHeader.append("<th>Bid</th>");
		tableHeader.append("<th>Ask</th>");
		tableHeader.append("<th>Last</th>");
		tableHeader.append("<th>ChgOpen</th>");
		
		table.append(tableHeader);
		
		tableBody = $("<tbody></tbody>");
		table.append(tableBody);
		
		tableContainer.append(table);
		my.body.append(tableContainer);
	}
	my.createDiv = createDiv;
	
	that.receiveResponse = function(d) {
		
		// Header message
		if (d.ob) {
			initAggregateRow(d.ob);
		}
		else if (d.pf) {
			// Message may contain aggregate row update
			var agg = $("#agg_"+d.pf.pf.ob);
			if (agg.length==0) {
				agg = initAggregateRow((d.pf.agg ? d.pf.agg : d.pf.pf));
				// Update it to get colors etc. right
				updatePortfolioRow(agg, d.pf.agg);
			}
			else if (d.pf.agg) 
				updatePortfolioRow(agg, d.pf.agg);
			
			// Update pf row
			var row = $("#pf_"+d.pf.pf.id+"_"+d.pf.pf.ob);
			if (row.length==0) {
				row = initPortfolioRow(d.pf.pf, agg);
				updatePortfolioRow(row, d.pf.pf);
			}
			else updatePortfolioRow(row, d.pf.pf);
		}
		// Value message
		else if (d.tu) {
			var tu = d.tu;
			
			// Find aggregate row
			var row = $("#agg_"+tu.ob);
			
			if (tu.bid!=null) row.find("td.bid").html(tu.bid);
			if (tu.ask!=null) row.find("td.ask").html(tu.ask);
			if (tu.last!=null) row.find("td.last").html(tu.last);
			if (tu.chgOpen!=null) {
				var td = row.find("td.chgOpen"); 
				td.html(tu.chgOpen.toFixed(2)+" %");
				updateColors(td,tu.chgOpen);
			}

			if (tu.upnl!=null && tu.upnl.length>0) {
				var totalUpnl = 0;
				
				// Update pf rows
				for (var i=0;i<tu.upnl.length;i++) {
					var td = $("#pf_"+tu.upnl[i].pf+"_"+tu.ob+" td.upnl");
					td.html(tu.upnl[i].upnl.toFixed(2));
					updateColors(td,tu.upnl[i].upnl);
					totalUpnl += tu.upnl[i].upnl;
				}
				
				// Update aggregate row
				var aggTd = row.find("td.upnl");
				aggTd.html(totalUpnl.toFixed(2));
				updateColors(aggTd,totalUpnl);
			}
		}
	}
	
	function initAggregateRow(msg) {
		var newRow = $("<tr id='agg_"+msg.ob+"' class='ob_"+msg.ob+"'></tr>");
		
		var expandButton = $("<span class='expand'>+</span>");
		var pfTd = $("<td class='portfolio'></td>");
		
		pfTd.append(expandButton);
		newRow.append(pfTd);
		newRow.append("<td class='symbol'>"+msg.symbol+"</td>");
		newRow.append("<td class='position'>"+(msg.position || "")+"</td>");
		newRow.append("<td class='purchaseVal'>"+(msg.purchaseVal || "")+"</td>");
		newRow.append("<td class='avgPrice'>"+(msg.avgPrice || "")+"</td>");
		newRow.append("<td class='upnl'>"+(msg.upnl || "")+"</td>");
		newRow.append("<td class='rpnl'>"+(msg.rpnl || "")+"</td>");
		newRow.append("<td class='bid'>"+(msg.bid || "")+"</td>");
		newRow.append("<td class='ask'>"+(msg.ask || "")+"</td>");
		newRow.append("<td class='last'>"+(msg.last || "")+"</td>");
		newRow.append("<td class='chgOpen'>"+(msg.chgOpen || "")+"</td>");
		tableBody.append(newRow);
		
		expandButton.hover(function() {$(this).css('cursor','pointer');}, function() {$(this).css('cursor','default');});
		expandButton.click((function(ob,btn) {
				return function() {
					tableBody.find("tr.pf.ob_"+ob).toggle();
					if (btn.html()=="+")
						btn.html("-");
					else btn.html("+");
				};
		})(msg.ob,expandButton));
		
		return newRow;
	}
	
	function initPortfolioRow(msg, after) {
		var newRow = $("<tr id='pf_"+msg.id+"_"+msg.ob+"' class='pf ob_"+msg.ob+"' style='display:none'></tr>");
		newRow.append("<td class='portfolio'>"+msg.name+"</td>");
		newRow.append("<td class='symbol'>"+msg.symbol+"</td>");
		newRow.append("<td class='position'>"+(msg.position || "")+"</td>");
		newRow.append("<td class='purchaseVal'>"+(msg.purchaseVal || "")+"</td>");
		newRow.append("<td class='avgPrice'>"+(msg.avgPrice || "")+"</td>");
		newRow.append("<td class='upnl'>"+(msg.upnl || "")+"</td>");
		newRow.append("<td class='rpnl'>"+(msg.rpnl || "")+"</td>");
		newRow.append("<td class='bid'>"+(msg.bid || "")+"</td>");
		newRow.append("<td class='ask'>"+(msg.ask || "")+"</td>");
		newRow.append("<td class='last'>"+(msg.last || "")+"</td>");
		newRow.append("<td class='chgOpen'>"+(msg.chgOpen || "")+"</td>");
		newRow.insertAfter(after);
		
		return newRow;
	}
	
	function updatePortfolioRow(row, d) {
		var td
		
		td = row.find("td.position");
		td.html(td.html()=="" && !d.position ? "" : d.position);
		updateColors(td,d.position);
		
		td = row.find("td.avgPrice");
		td.html(td.html()=="" && !d.avgPrice ? "" : d.avgPrice.toFixed(2));
//		updateColors(td,d.avgPrice);
		
		td = row.find("td.purchaseVal");
		td.html(td.html()=="" && !d.purchaseVal ? "" : d.purchaseVal.toFixed(2));
		updateColors(td,d.position);
		
		td = row.find("td.rpnl");
		td.html(td.html()=="" && !d.rpnl ? "" : d.rpnl.toFixed(2));
		updateColors(td,d.rpnl);
		
		
		td = row.find("td.upnl");
		td.html(td.html()=="" && !d.upnl ? "" : d.upnl.toFixed(2));
		updateColors(td,d.upnl);
	}
	
	function updateColors(td,val) {
		if (val>0) {
			td.addClass("positive");
			td.removeClass("negative");
		}
		else if (val<0) {
			td.addClass("negative");
			td.removeClass("positive");
		}
		else {
			td.removeClass("negative");
			td.removeClass("positive");
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
