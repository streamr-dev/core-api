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
	
	var dialogTemplate = '<div class="codeWindow" style="width:600px; height:400px">'+
							'<div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">'+
							  '<div class="modal-dialog">'+
							    '<div class="modal-content">'+
							      '<div class="modal-header">'+
							        '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
							        '<h4 class="modal-title" id="myModalLabel">Code editor</h4>'+
							      '</div>'+
							      '<div class="modal-body">'+
							      '</div>'+
							      '<div class="modal-footer">'+
							        '<button type="button" class="debug-btn btn btn-default">Show debug</button>'+
							        '<button type="button" class="apply-btn btn btn-default">Apply</button>'+
							        '<button type="button" class="close-btn btn btn-default" data-dismiss="modal">Close</button>'+
							      '</div>'+
							    '</div>'+
							  '</div>'+
							'</div>'+
						'</div>'
		
	var debugTemplate = '<div class="debugWindow" style="display:none; width:400px; height:300px">'+
							'<div class="modal" id="code-modal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">'+
							  '<div class="modal-dialog">'+
							    '<div class="modal-content">'+
							      '<div class="modal-header">'+
							        '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+
							        '<h4 class="modal-title" id="myModalLabel">Debug messages</h4>'+
							      '</div>'+
							      '<div class="modal-body">'+
								      '<div id="debugText" style="width:100%; height:95%; background-color:white; overflow:auto">'+
										'</div>'+
							      '</div>'+
							      '<div class="modal-footer">'+
							        '<button type="button" class="clear-btn btn btn-default">Clear</button>'+
							        '<button type="button" class="close-btn btn btn-default" data-dismiss="modal">Close</button>'+
							      '</div>'+
							    '</div>'+
							  '</div>'+
							'</div>'+
						'</div>'
	
	function createCodeWindow() {
		if (dialog==null) {
			dialog = $(dialogTemplate)
			
//			prot.div.parent().append(dialog)
			dialog.find(".modal").modal({
				backdrop:true
			})
			dialog.find('.debug-btn').click(function() {
				createDebugWindow()
			})
			
			dialog.find('.apply-btn').click(function() {
				editor.clearGutter("breakpoints");
				updateJson();
				SignalPath.updateModule(pub, function() {
					module = pub.getDiv();
					addStuffToDiv();
				});
			})
			
			dialog.find('.close-btn').click(function() {
				dialog.hide()
				pub.onDelete()
			})
			
			editor = CodeMirror(dialog.find('.modal-body')[0], $.extend({},SignalPath.CustomModuleOptions.codeMirrorOptions,{
					value: prot.jsonData.code,
					mode:  "groovy"
				}));
			
			dialog.draggable({
				containment: "none",
				cancel: ".modal-body"
			});
			
			$(SignalPath).on("new", pub.onDelete);
			$(SignalPath).on("loaded", pub.onDelete);
		}
		else dialog.show()
	}
		
	function createDebugWindow() {
		
		if (debug==null) {
			debug = $(debugTemplate)
			
			prot.div.parent().append(debug)
	
			debug.find(".clear-btn").click(function() {
					debugTextArea.html("");
			})
			debug.find(".close-btn").click(function() {
					debug.hide()
			})
		} else debug.show()

	}
	
	function addStuffToDiv() {
		var editButton = $("<button class='btn btn-primary btn-sm'>Edit code</button>");
		editButton.click(function () {
			createCodeWindow()
		});
		
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
		marker.innerHTML = "���";
		return marker;
	}
	
	var super_onDelete = pub.onDelete;
	pub.onDelete = function() {
		if (super_onDelete)
			super_onDelete();
		
		if (dialog!=null) {
			$(dialog).remove();
			dialog = null;
		}
		if (debug!=null) {
			$(debug).remove();
			debug = null;
			debugTextArea = null;
		}
	}
	
	return pub;
}