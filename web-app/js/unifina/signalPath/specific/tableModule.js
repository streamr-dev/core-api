SignalPath.TableModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.UIChannelModule(data,canvas,prot)

	var area = null;
	var headers = [];
	
	/**
	 * Initialization
	 */
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
		initTable()
	}
	prot.createDiv = createDiv;

	function initTable(){
		var tableOptions = {}
		if (prot.jsonData.options) {
			Object.keys(prot.jsonData.options).forEach(function(key) {
				tableOptions[key] = prot.jsonData.options[key].value
			})
		}
        
		if (prot.jsonData.tableConfig && prot.jsonData.tableConfig.headers)
			tableOptions.headers = prot.jsonData.tableConfig.headers
        
		prot.table = new StreamrTable(prot.body, tableOptions)
		
        prot.table.tableContainer.addClass("drag-exclude")
	}

	function sendInitRequest() {
		if (SignalPath.isRunning()) {
			SignalPath.runtimeRequest(pub.getRuntimeRequestURL(), {type:'initRequest'}, function(response, err) {
				if (err)
					console.error("Failed initRequest for TableModule: %o", err)
				else
					prot.table.receiveResponse(response.initRequest)
			})
		}
	}

	prot.receiveResponse = function(d) {
		prot.table.receiveResponse(d)
	}
	
	var superClean = pub.clean;
	pub.clean = function() {
		superClean();
		prot.table.clean()
	}

	var superClose = pub.close;
	pub.close = function() {
		$(SignalPath).off("started", startFunction)
		$(SignalPath).off("loaded", sendInitRequest)
		superClose()
	}

	function startFunction (e, canvas){
		if (!canvas || !canvas.adhoc) {
			sendInitRequest()
		}
	}

	$(SignalPath).on("started", startFunction)
	$(SignalPath).on("loaded", sendInitRequest)
	
	return pub;
}


