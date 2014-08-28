SignalPath.LabelModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var label;
	
	var super_createDiv = prot.createDiv;
	prot.createDiv = function() {
		super_createDiv();
		label = $("<div class='modulelabel' style='"+prot.jsonData.options.style.value+"'>"+prot.jsonData.params[0].value+"</div>");
		prot.body.append(label);
		
		prot.div.find("input").keyup(function() {
			label.html($(this).val());
		});
		
		prot.initResizable();
	}
	
	var super_receiveResponse = prot.receiveResponse
	prot.receiveResponse = function(payload) {
		super_receiveResponse(payload);
		if (payload.type=="u") {
			label.html(payload.val);
		}
	}
	
	return pub;
}
