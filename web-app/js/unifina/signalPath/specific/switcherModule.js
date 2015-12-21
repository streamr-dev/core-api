SignalPath.SwitcherModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var switcher;
	
	var super_createDiv = prot.createDiv;
	prot.createDiv = function() {
		super_createDiv();
		switcher = new StreamrSwitcher(prot.body, {
			checked: prot.jsonData.switcherValue
		})
		prot.body.addClass("switcher-container")
	}

	var super_toJSON = pub.toJSON
	pub.toJSON = function () {
		var json = super_toJSON()
		json.switcherValue = switcher.getValue()
		return json
	}
	
	pub.receiveResponse = function(p) {
		switcher.receiveResponse(p)
	}
	
	return pub;
}
