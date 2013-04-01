SignalPath.DynamicInputsModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.GenericModule(data,canvas,my)

	// Ensure that the div has been created
	that.getDiv();
	
	my.addInput({
		name: "in+",
		type: {
			name: "dynamic"
		}
	});

	var superConnect = that.onConnect;
	
	that.onConnect = function(holder,div,sourceHolder,sourceDiv) {
		superConnect(holder,div,sourceHolder,sourceDiv);
		var json = div.data("json");
		
	}
	
	return that;
}