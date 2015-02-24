SignalPath.LabelModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var label;
	
	var super_createDiv = prot.createDiv;
	prot.createDiv = function() {
		super_createDiv();
		label = $("<div class='modulelabel'></div>");
		prot.body.append(label);
	}
	
	pub.receiveResponse = function(payload) {
		label.html(payload.value);
	}

	pub.getUIChannelOptions = function() {
		// Force resending of only last value
		return { resend_last: 1 }
	}
	
	return pub;
}
