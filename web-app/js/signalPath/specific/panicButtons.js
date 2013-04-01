SignalPath.PanicButtons = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.EmptyModule(data,canvas,my)
	
	var table;
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		table = $("<table class='panic'><thead><tr><th>Symbol</th><th>Portfolio</th><th>Actions</th></tr><thead><tbody></tbody></table>");
		
		my.body.append(table);
		
		my.body.append("<h3>Long</h3>");
		
		var blockLong = $("<button>block long</button>");
		blockLong.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {blockAll:true, side:1}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(blockLong);
		
		var unblockLong = $("<button>unblock long</button>");
		unblockLong.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {unblockAll:true, side:1}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(unblockLong);
		
		var closeLong = $("<button>block&nbsp;&amp;&nbsp;close&nbsp;long</button>");
		closeLong.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {blockAll:true, closeAll:true, side:1}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(closeLong);
		
		my.body.append("<h3>Short</h3>");
		
		var blockShort = $("<button>block short</button>");
		blockShort.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {blockAll:true, side:-1}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(blockShort);
		
		var unblockShort = $("<button>unblock short</button>");
		unblockShort.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {unblockAll:true, side:-1}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(unblockShort);
		
		var closeShort = $("<button>block&nbsp;&amp;&nbsp;close&nbsp;short</button>");
		closeShort.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {blockAll:true, closeAll:true, side:-1}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(closeShort);
		
		my.body.append("<h3>All</h3>");
		
		var blockAll = $("<button>block all</button>");
		blockAll.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {blockAll:true}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(blockAll);
		
		var unblockAll = $("<button>unblock all</button>");
		unblockAll.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {unblockAll:true}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(unblockAll);
		
		var closeAll = $("<button>block&nbsp;&amp;&nbsp;close&nbsp;all</button>");
		closeAll.click(function() {
			var sent = SignalPath.sendUIAction(my.hash, {blockAll:true, closeAll:true}, function(resp) {});
			if (!sent) {
				alert("Could not connect to session.");
			}
		});
		
		my.body.append(closeAll);
	}
	my.createDiv = createDiv;
	
	function makeId(ob,pf) {
		return "panic_"+ob+"_"+pf.replace(" ","_");
	}
	
	that.receiveResponse = function(d) {
		if (d.init) {
			var tr = $("<tr id='"+makeId(d.init.ob,d.init.pf)+"'><td class='orderBook'>"+d.init.name+"</td><td class='portfolio'>"+d.init.pf+"</td></tr>");
			var buttonTd = $("<td><div class='panicButtons'></span></td>");
			tr.append(buttonTd);
			
			addBlockButton(buttonTd.find("div.panicButtons"), d.init.ob, d.init.name, d.init.pf);
			
			table.find("tbody").append(tr);
		}
		
		else if (d.update) {
			var tr = $("#"+makeId(d.update.ob, d.update.pf));
			if (d.update.blocked) {
				tr.addClass("blocked");
				addUnblockButton(tr.find("div.panicButtons"), d.update.ob, d.update.name, d.update.pf);
			}
			else {
				tr.removeClass("blocked");
				addBlockButton(tr.find("div.panicButtons"), d.update.ob, d.update.name, d.update.pf);
			}
		}
	}
	
	function addBlockButton(buttonTd, obId, obName, pf) {
		var block = $("<button>block</button>");
		block.click((function(ob, portfolio) {
			return function() {
				var sent = SignalPath.sendUIAction(my.hash, {ob: ob, pf: portfolio, block:true}, function(resp) {});
				if (!sent) {
					alert("Could not connect to session.");
				}
			};
		})(obName, pf));
		
		buttonTd.html(block);
		
		var close = $("<button>block&nbsp;&amp;&nbsp;close</button>");
		close.click((function(ob, portfolio) {
			return function() {
				var sent = SignalPath.sendUIAction(my.hash, {ob: ob, pf: portfolio, block:true, close:true}, function(resp) {});
				if (!sent) {
					alert("Could not connect to session.");
				}
			};
		})(obName, pf));
		
		buttonTd.append(close);
	}
	
	function addUnblockButton(buttonTd, obId, obName, pf) {
		var block = $("<button>unblock</button>");
		block.click((function(ob, portfolio) {
			return function() {
				var sent = SignalPath.sendUIAction(my.hash, {ob: ob, pf: portfolio, unblock:true}, function(resp) {});
				if (!sent) {
					alert("Could not connect to session.");
				}
			};
		})(obName, pf));
		
		buttonTd.html(block);
		
		var close = $("<button>close</button>");
		close.click((function(ob, portfolio) {
			return function() {
				var sent = SignalPath.sendUIAction(my.hash, {ob: ob, pf: portfolio, close:true}, function(resp) {});
				if (!sent) {
					alert("Could not connect to session.");
				}
			};
		})(obName, pf));
		
		buttonTd.append(close);
	}
	
	var superClean = that.clean;
	that.clean = function() {
		superClean();
		table.find("tbody").html("");
	}
	
	return that;
}
