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
    +   	'<div class="modal-content" style="width:600px">'
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
	
	
	function createCodeWindow() {
		if (dialog==null) {
			dialog = $(codeWindow);
			
			prot.div.parent().append(dialog)

			dialog.draggable({
				cancel: ".modal-body",
				containment: "none"
			})

			dialog.resizable()

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
		} else dialog.show()
	}
	
	var debugWindow = ''
    + 		'<div style="width:400px">'
    +   		'<div class="modal-content">'
    +     			'<div class="modal-header">'
    +					'<button type="button" class="close close-btn"><span aria-hidden="true">&times;</span></button>'
    +         			'<h4 class="modal-title">Debug messages</h4>'
    +     			'</div>'
    +     			'<div class="modal-body">'
    +					'<div class="debugText" style="width:100%; height:95%; background-color:white; overflow:auto"></div>'
    +				'</div>'
    +     			'<div class="modal-footer">'
    +					'<button class="clear-btn btn btn-default">Clear</button>'
    +					'<button class="close-btn btn btn-default">Close</button>'
    +				'</div>'
    +   		'</div>'
    + 		'</div>'
	
	function createDebugWindow() {
		if (debug==null) {
			debug = $(debugWindow);
			debugTextArea = debug.find(".debugText")
			
			prot.div.parent().append(debug)

			debug.draggable()
			
			debug.find(".clear-btn").click(function() {
				debugTextArea.html("");
			})
			debug.find(".close-btn").click(function(){
				debug.hide()
			})
		} else debug.show()
	}
	
	function addStuffToDiv() {
		var editButton = $("<button>Edit code</button>");
		editButton.click(createCodeWindow);
		
		module.find(".modulefooter").prepend(editButton);
		editButton.button();
	}
	
	function updateJson() {
		prot.jsonData.code = editor.getValue();
//		if (prot.jsonData.inputs!=null)
//			delete prot.jsonData.inputs;
//		if (prot.jsonData.outputs!=null)
//			delete prot.jsonData.outputs;
//		if (prot.jsonData.params!=null)
//			delete prot.jsonData.params;
	}
	
	var superReceiveResponse = pub.receiveResponse;
	
	pub.receiveResponse = function(payload) {
		superReceiveResponse(payload);
		
		if (payload.type=="debug" && debug != null) {
			debugTextArea.append(payload.t+" - "+payload.msg+"<br>");
//			debugTextArea.scrollTop(
//					debugTextArea[0].scrollHeight - debugTextArea.height()
//	        );
		}
		else if (payload.type=="compilationErrors") {
			for (var i=0;i<payload.errors.length;i++) {
//				editor.addLineClass(payload.errors[i].line, "text", "cm-error");
				editor.setGutterMarker(payload.errors[i].line-1, "breakpoints", makeMarker());
			}
		}
	}
	
	function makeMarker() {
		var marker = document.createElement("div");
		marker.style.color = "#822";
		marker.innerHTML = "���";
		return marker;
	}
	
	var super_onDelete = pub.onDelete;
	pub.onDelete = function() {
		if (super_onDelete)
			super_onDelete();
		
		if (dialog!=null) {
			$(dialog).modal("close");
			$(dialog).modal("destroy");
			dialog = null;
		}
		if (debug!=null) {
			$(debug).modal("close");
			$(debug).modal("destroy");
			debug = null;
		}
	}
	
	return pub;
}