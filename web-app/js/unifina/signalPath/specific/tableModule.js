SignalPath.TableModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var area = null;
	var options = {};
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
		prot.table = new StreamrTable(prot.body, options)
		if (prot.jsonData.options && prot.jsonData.options.maxRows)
			options = prot.jsonData.options

		if (prot.jsonData.tableConfig && prot.jsonData.tableConfig.headers)
			headers = prot.jsonData.tableConfig.headers
		
		prot.table.initTable(headers)
	}

	pub.receiveResponse = function(d) {
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
		superClose()
	}

	function startFunction (e, runData){
		if (!runData || !runData.adhoc) {
			SignalPath.sendRequest(prot.hash, {type:'initRequest'}, function(response) {
				prot.table.receiveResponse(response.initRequest)
			})
		}
	}

	$(SignalPath).on("started", startFunction)
	
	return pub;
}


