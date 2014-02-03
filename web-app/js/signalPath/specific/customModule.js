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

SignalPath.CustomModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.GenericModule(data,canvas,my)

	var module = that.getDiv();
	
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
						SignalPath.updateModule(that, function() {
							module = that.getDiv();
							addStuffToDiv();
						});
					},
					Cancel: function() {
						$( this ).dialog( "close" );
					}
				},
				open: function() {
					editor = CodeMirror(dialog[0], $.extend({},SignalPath.CustomModuleOptions.codeMirrorOptions,{
						value: my.jsonData.code,
						mode:  "groovy"
					}));
				},
				close: function() {
					$(dialog).dialog("destroy");
					$(dialog).remove();
					dialog = null;
				}
			}).dialog("widget").draggable("option","containment","none");
		}
	}
	
	function addStuffToDiv() {
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

		var editButton = $("<button>Edit code</button>");
		editButton.click(createCodeWindow);
		
		module.find(".modulefooter").prepend(editButton);
		editButton.button();
	}
	
	function updateJson() {
		my.jsonData.code = editor.getValue();
//		if (my.jsonData.inputs!=null)
//			delete my.jsonData.inputs;
//		if (my.jsonData.outputs!=null)
//			delete my.jsonData.outputs;
//		if (my.jsonData.params!=null)
//			delete my.jsonData.params;
	}
	
	var superReceiveResponse = that.receiveResponse;
	
	that.receiveResponse = function(payload) {
		superReceiveResponse(payload);
		
		if (payload.type=="debug") {
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
		marker.innerHTML = "●";
		return marker;
	}
	
	that.endOfResponses = function() {
		
	}
	
	var thatDeleteOld = that.onDelete;
	that.onDelete = function() {
		if (thatDeleteOld)
			thatDeleteOld();
		
		if (dialog!=null) {
			$(dialog).dialog("close");
			$(dialog).dialog("destroy");
			$(dialog).remove();
		}
		if (debug!=null) {
			$(debug).dialog("close");
			$(debug).dialog("destroy");
			$(debug).remove();
		}
	}
	
	return that;
}