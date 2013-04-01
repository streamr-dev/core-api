SignalPath.SignalAnalysis = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.GenericModule(data,canvas,my);
	
	var superReceiveResponse = that.receiveResponse;
	that.receiveResponse = function(d) {
		superReceiveResponse(d);
		
		// Copied from chartModule.js
		// TODO: make it DRY
		if (d.type=="csv") {
			var div = $("<span class='csvDownload'></span>");
			var link = $("<a href='"+d.link+"'></a>");
			link.append("<img src='../images/download.png'/>&nbsp;"+d.filename);
			div.append(link);
			my.body.append(div);
			div.effect("highlight",{},2000);
			
			link.click(function(event) {
				event.preventDefault();
				$.getJSON("existsCsv", {filename:d.filename}, (function(div) {
					return function(resp) {
						if (resp.success) {
							$(div).remove();
							var elemIF = document.createElement("iframe"); 
							elemIF.src = "downloadCsv?filename="+resp.filename; 
							elemIF.style.display = "none"; 
							document.body.appendChild(elemIF);
						}
						else alert("The file is already gone from the server. Please re-run your signal path!")
					}})(div));
			});
		}
	}
	
	return that;
}
