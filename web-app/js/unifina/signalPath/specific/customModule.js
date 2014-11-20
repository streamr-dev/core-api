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
	
	function createCodeWindow() {
		if (dialog==null) {
			dialog = $("<div class='codeWindow' style='display:none'></div>");
			
			$(dialog).dialog({
				autoOpen:true,
				title:"Code editor",
				width: 600,
				height: 400,
				buttons: {
					"Show debug": function() {
						$(debug).dialog("open");
					},
					"Apply": function() {
						editor.clearGutter("breakpoints");
						updateJson();
						SignalPath.updateModule(pub, function() {
							module = pub.getDiv();
							addStuffToDiv();
						});
					},
					"Close": function() {
						$( this ).dialog( "close" );
					}
				},
				open: function() {
					editor = CodeMirror(dialog[0], $.extend({},SignalPath.CustomModuleOptions.codeMirrorOptions,{
						value: prot.jsonData.code,
						mode:  "groovy"
					}));
				},
				close: pub.onDelete
			}).dialog("widget").draggable("option","containment","none");
			
			$(SignalPath).on("new", pub.onDelete);
			$(SignalPath).on("loaded", pub.onDelete);
		}
		
		if (debug==null) {
			debug = $("<div class='debugWindow' style='display:none'></div>");
			debugTextArea = $("<div id='debugText' style='width:100%; height:95%; background-color:white; overflow:auto'></div>");
			debug.append(debugTextArea);
			
			$(debug).dialog({
				autoOpen:false,
				title:"Debug messages",
				width: 400,
				height: 300,
				buttons: {
					"Clear": function() {
						debugTextArea.html("");
					},
					"Close": function() {
						$(this).dialog("close");
					}
				}
			});
		}

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
	
	pub.endOfResponses = function() {
		
	}
	
	var super_onDelete = pub.onDelete;
	pub.onDelete = function() {
		if (super_onDelete)
			super_onDelete();
		
		if (dialog!=null) {
			$(dialog).dialog("close");
			$(dialog).dialog("destroy");
			$(dialog).remove();
			dialog = null;
		}
		if (debug!=null) {
			$(debug).dialog("close");
			$(debug).dialog("destroy");
			$(debug).remove();
			debug = null;
			debugTextArea = null;
		}
	}
	
	return pub;
}