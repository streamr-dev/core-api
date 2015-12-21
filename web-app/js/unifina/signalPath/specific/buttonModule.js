SignalPath.ButtonModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var button;
	
	var super_createDiv = prot.createDiv;
	prot.createDiv = function() {
		super_createDiv();
		button = new StreamrButton(prot.body)

		$(button).on("nameChange", function() {
			prot.redraw()
		})
	}
	
	pub.receiveResponse = function(p) {
		button.receiveResponse(p)
	}
	
	return pub;
}
