SignalPath.CustomModuleOptions = {
	codeMirrorOptions: {
		lineNumbers: true,
		matchBrackets: true,
		mode: "text/x-groovy",
		theme: "default",
		viewportMargin: Infinity,
		gutters: ["CodeMirror-linenumbers", "breakpoints"]
	}
};

SignalPath.CustomModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var module = pub.getDiv();
	
	var dialog = null;
	var debug = null;
	var debugTextArea = null;
	var editor = null;
	
	addStuffToDiv();
	
	var codeWindow = ''
    +   '<div class="code-editor-dialog" style="width:600px; height:400px">'
    +   	'<div class="modal-content flexing">'
    +    		'<div class="modal-header">'
    +			'<button type="button" class="close close-btn"><span aria-hidden="true">&times;</span></button>'
    +        		'<h4 class="modal-title">Code editor</h4>'
    +  	 		'</div>'
    +   	 	'<div class="modal-body"></div>'
    +   		'<div class="modal-footer">'
    +				'<button class="debug-btn btn btn-default">Show debug</button>'
    +				'<button class="apply-btn btn btn-default">Apply</button>'
    +				'<button class="close-btn btn btn-default">Close</button>'
    +			'</div>'
    +   	'</div>'
    +	'</div>'
	
	
	function createCodeWindow() {
		if (dialog==null) {
			dialog = $(codeWindow);
			
			prot.div.parent().append(dialog)

			dialog.draggable({
				cancel: ".modal-body",
				containment: "none",
				drag: function(e, ui) {
					var cpos = canvas.offset()
					var x = ui.offset.left + canvas.scrollLeft()
					var y = ui.offset.top + canvas.scrollTop()
					
					if (x < cpos.left-100 || y < cpos.top-50) {
						return false
					}
				}
			})

			dialog.find(".debug-btn").click(function() {
				createDebugWindow()
			})
			
			dialog.find(".apply-btn").click(function() {
				editor.clearGutter("breakpoints");
				updateJson();
				SignalPath.updateModule(pub, function() {
					module = pub.getDiv();
					addStuffToDiv();
				});
			})
			dialog.find(".close-btn").click(function(){
				dialog.hide()
			})
						
			
			$(SignalPath).on("new", pub.onDelete);
			$(SignalPath).on("loaded", pub.onDelete);
			
			editor = CodeMirror(dialog.find(".modal-body")[0], $.extend({},SignalPath.CustomModuleOptions.codeMirrorOptions,{
				value: prot.jsonData.code,
				mode:  "groovy"
			}));

			dialog.resizable({
				minHeight:320,
				minWidth:400,
				resize: function(){
					editor.refresh()
				}
			})
		} else dialog.show()
	}
	
	var debugWindow = ''
    +   	'<div class="debug-dialog modal-content flexing" style="width:400px; height:300px">'
    +     		'<div class="modal-header">'
    +				'<button type="button" class="close close-btn"><span aria-hidden="true">&times;</span></button>'
    +         		'<h4 class="modal-title">Debug messages</h4>'
    +     		'</div>'
    +     		'<div class="modal-body">'
    +				'<div class="debugText" style="width:100%; height:95%; background-color:white; overflow:auto"></div>'
    +			'</div>'
    +     		'<div class="modal-footer">'
    +				'<button class="clear-btn btn btn-default">Clear</button>'
    +				'<button class="close-btn btn btn-default">Close</button>'
    +			'</div>'
    +   	'</div>'
	
	function createDebugWindow() {
		if (debug==null) {
			debug = $(debugWindow);
			debugTextArea = debug.find(".debugText")
			
			prot.div.parent().append(debug)

			debug.draggable({
				cancel: ".modal-body",
				containment: "none",
				drag: function(e, ui) {
					var cpos = canvas.offset()
					var x = ui.offset.left + canvas.scrollLeft()
					var y = ui.offset.top + canvas.scrollTop()
					
					if (x < cpos.left-100 || y < cpos.top-50) {
						return false
					}
				}
			})
			debug.find(".clear-btn").click(function() {
				debugTextArea.html("");
			})
			debug.find(".close-btn").click(function(){
				debug.hide()
			})
			debug.resizable({
				minHeight:200,
				minWidth:200
			})
		} else debug.show()
	}
	
	function addStuffToDiv() {
		var editButton = $("<button class='btn btn-primary btn-sm'>Edit code</button>");
		editButton.click(createCodeWindow);
		
		module.find(".modulefooter").prepend(editButton);
		editButton.button();
	}
	
	function updateJson() {
		prot.jsonData.code = editor.getValue();
	}
	
	var superReceiveResponse = pub.receiveResponse;
	
	pub.receiveResponse = function(payload) {
		superReceiveResponse(payload);
		
		if (payload.type=="debug" && debug != null) {
			debugTextArea.append(payload.t+" - "+payload.msg+"<br>");
		}
		else if (payload.type=="compilationErrors") {
			for (var i=0;i<payload.errors.length;i++) {
				editor.setGutterMarker(payload.errors[i].line-1, "breakpoints", makeMarker());
			}
		}
	}
	
	function makeMarker() {
		var marker = document.createElement("div");
		marker.style.color = "#822";
		marker.innerHTML = "&#9679;";
		return marker;
	}
	
	var super_onDelete = pub.onDelete;
	pub.onDelete = function() {
		if (super_onDelete)
			super_onDelete();
		
		if (dialog!=null) {
			$(dialog).remove()
			dialog = null
		}
		if (debug!=null) {
			$(debug).remove()
			debug = null;
		}
	}
	
	return pub;
}