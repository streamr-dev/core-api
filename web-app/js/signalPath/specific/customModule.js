SignalPath.CustomModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.GenericModule(data,canvas,my)

	var module = that.getDiv();
	
	var dialog = null;
	var debug = null;
	var debugTextArea = null;
	var editor = null;
	
	addStuffToDiv();
	
	function addStuffToDiv() {
		if (dialog==null) {
			dialog = $("<div class='codeWindow' style='display:none'></div>");
			var textArea = $("<textarea style='width:100%; height:100%;'>"+my.jsonData.code+"</textarea>");
			dialog.append(textArea);
			
			$(dialog).dialog({
				autoOpen:false,
				title:"Code editor",
				width: 600,
				height: 400,
				buttons: {
					"Show debug": function() {
						$(debug).dialog("open");
					},
					"Apply": function() {
						updateJson();
						SignalPath.updateModule(that, function() {
							module = that.getDiv();
							addStuffToDiv();
						});
					},
					Cancel: function() {
						$( this ).dialog( "close" );
					}
				}
			}).dialog("widget").draggable("option","containment","none");
			
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

		var editButton = $("<button>Edit code</button>");
		editButton.click(function() {
			$(dialog).dialog("open");
			if (editor==null) {
				var textArea = $(dialog).find("textarea").get(0);
				editor = CodeMirror.fromTextArea(textArea, {
					lineNumbers: true,
					matchBrackets: true,
					mode: "text/x-groovy"
				});
			}
			setTimeout(function(){editor.refresh(); editor.focus();}, 20);
		});
		
		module.append(editButton);
	
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