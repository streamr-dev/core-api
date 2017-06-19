SignalPath.CommentModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.EmptyModule(data,canvas,prot)
    var textarea
 
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
        prot.body.addClass("drag-exclude")
		textarea = $("<textarea class='comment'></textarea>");
		textarea.html(prot.jsonData.text || "");
        
        prot.initResizable({
            minWidth: 100,
            minHeight: 50
        })
        
		if (prot.jsonData.layout && prot.jsonData.layout.width) {
            prot.body.width(prot.jsonData.layout.width)
        }
        
		if (prot.jsonData.layout && prot.jsonData.layout.height) {
		    prot.body.height(prot.jsonData.layout.height)
        }
		
		prot.body.append(textarea);
		
	}
	prot.createDiv = createDiv;
 
	var superToJSON = pub.toJSON;
	function toJSON() {
		var result = superToJSON();
		result.text = textarea.val();
		result.layout = result.layout || {}
		result.layout.width = textarea.width() + 'px'
		result.layout.height = textarea.height() + 'px'
		return result;
	}
	pub.toJSON = toJSON;
	
	return pub;
}
