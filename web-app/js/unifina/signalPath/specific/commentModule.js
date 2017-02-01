SignalPath.CommentModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.EmptyModule(data,canvas,prot)

	var textarea;
	
	//function resizeTextarea(moduleWidth, moduleHeight) {
	//	textarea.css("width",moduleWidth - 20);
	//	textarea.css("height",moduleHeight - 40);
	//}
    
    
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
        prot.body.addClass(".drag-exclude")
		textarea = $("<textarea class='comment'></textarea>");
		textarea.html(prot.jsonData.text || "");
		
		prot.body.append(textarea);
		
		//prot.initResizable({
		//	resize: function(event,ui) {
		//		resizeTextarea(ui.size.width, ui.size.height);
		//	}
		//});
		//
		//resizeTextarea(prot.div.width(),prot.div.height());
	}
	prot.createDiv = createDiv;
	
	
	
	var superToJSON = pub.toJSON;
	function toJSON() {
		var result = superToJSON();
		result.text = textarea.val();	
		return result;
	}
	pub.toJSON = toJSON;
	
	return pub;
}
