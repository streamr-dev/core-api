SignalPath.CommentModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.EmptyModule(data,canvas,my)

	var textarea;
	
	function resizeTextarea(moduleWidth, moduleHeight) {
		textarea.css("width",moduleWidth - 20);
		textarea.css("height",moduleHeight - 40);
	}
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		textarea = $("<textarea class='comment'></textarea>");
		textarea.html(my.jsonData.text || "");
		
		my.body.append(textarea);
		
		my.initResizable({
			resize: function(event,ui) {
				resizeTextarea(ui.size.width, ui.size.height);
			}
		});
		
		resizeTextarea(my.div.width(),my.div.height());
	}
	my.createDiv = createDiv;
	
	
	
	var superToJSON = that.toJSON;
	function toJSON() {
		var result = superToJSON();
		result.text = textarea.val();	
		return result;
	}
	that.toJSON = toJSON;
	
	return that;
}
