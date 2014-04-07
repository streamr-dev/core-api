//SignalPath.RapidModelModule = function(data,canvas,my) {
//	my = my || {};
//	var that = SignalPath.GenericModule(data,canvas,my)
//
//	if (!data.model) {
//		var module = my.createDivWindow(data,my.id);
//		my.div = module;
//
//		var sDiv = $("<div class='rapidModelSelect'></div>");
//		var select = $("<select></select>");
//
//		$.getJSON("jsonGetRapidModels",{},function(data) {
//			$(data.models).each(function(i,model) {
//				select.append("<option value='"+model.id+"'>"+model.name+"</option>")
//			});
//		});
//
//		sDiv.append(select);
//
//		var button = $("<button>Load</button>");
//
//		button.click(function() {
//			// Get indicator JSON from server
//			$.getJSON(project_webroot+"module/jsonGetModule", {id:my.jsonData.id, rapidModelId: select.val(), type: my.type}, function(data) {
//				var oldDiv = my.div;
//				my.div = my.createDiv(data,my.id);
//				my.jsonData.model = data.model;
//				$(oldDiv).replaceWith(my.div);
//				that.redraw();
//			});
//		});
//		sDiv.append(button);
//		module.append(sDiv);
//
//	}
//
//	return that;
//}