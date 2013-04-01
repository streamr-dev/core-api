SignalPath.CommentModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.EmptyModule(data,canvas,my)

	var textarea;
	
	function resizeTextarea(moduleWidth, moduleHeight) {
		textarea.css("width",moduleWidth - 10);
		textarea.css("height",moduleHeight - 30);
	}
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		textarea = $("<textarea class='comment'></textarea>");
		textarea.html(my.jsonData.text || "");
		
		my.body.append(textarea);
		
		my.div.resizable({
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
		result.layout.width = $(my.div).css('width');
		result.layout.height = $(my.div).css('height');		
		return result;
	}
	that.toJSON = toJSON;
	
	return that;
}
