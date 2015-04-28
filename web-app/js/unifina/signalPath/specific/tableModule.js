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
		pub.table = new StreamrTable(prot.body, options)
		if (prot.jsonData.options && prot.jsonData.options.maxRows)
			options = prot.jsonData.options

		if (prot.jsonData.tableConfig && prot.jsonData.tableConfig.headers)
			headers = prot.jsonData.tableConfig.headers
		
		pub.table.initTable(headers)
	}

	pub.receiveResponse = function(d) {
		pub.table.receiveResponse(d)
	}
	
	var superClean = pub.clean;
	pub.clean = function() {
		superClean();
		pub.table.clean()
	}
	
	return pub;
}
