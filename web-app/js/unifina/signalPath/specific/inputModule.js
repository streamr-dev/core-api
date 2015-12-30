SignalPath.InputModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var module;
	
	var super_createDiv = prot.createDiv;
	prot.createDiv = function() {
		super_createDiv();
		var options = prot.jsonData.moduleOptions
		if(!prot.jsonData.module)
			throw "prot.jsonData.module must be defined!"
		var moduleName = eval(prot.jsonData.module)
		module = new moduleName(prot.body, options)

		$(module).on("update", function() {
			prot.redraw()
		})

		$(module).on("input", function(e, value) {
			prot.sendValue(value)
		})
	}

	var super_toJSON = pub.toJSON
	pub.toJSON = function () {
		var json = super_toJSON()
		if(module.getData)
			json.data = module.getData()
		return json
	}
	
	pub.receiveResponse = function(p) {
		module.receiveResponse(p)
	}

	prot.sendValue = function(value) {
		SignalPath.sendRequest(pub.getHash(), {
			type: "uiEvent",
			value: value
		}, function(resp) {});
	}
	
	return pub;
}
