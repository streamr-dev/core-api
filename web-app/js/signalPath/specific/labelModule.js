SignalPath.LabelModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.GenericModule(data,canvas,my)

	var label;
	
	var super_createDiv = my.createDiv;
	my.createDiv = function() {
		super_createDiv();
		label = $("<div class='modulelabel' style='"+my.jsonData.options.style.value+"'>"+my.jsonData.params[0].value+"</div>");
		my.body.append(label);
		
		my.div.find("input").keyup(function() {
			label.html($(this).val());
		});
		
		my.initResizable();
	}
	
	var super_receiveResponse = my.receiveResponse
	my.receiveResponse = function(payload) {
		super_receiveResponse(payload);
		if (payload.type=="u") {
			label.html(payload.val);
		}
	}
	
	return that;
}
