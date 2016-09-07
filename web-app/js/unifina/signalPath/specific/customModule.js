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
	var pub = SignalPath.UIChannelModule(data,canvas,prot)

	var dialog = null;
	var debug = null;
	var debugTextArea = null;
	var editor = null;
	
	createEditCodeButton();
	$(prot).on('updated', createEditCodeButton)
	
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

			dialog.css('top',canvas.scrollTop() + 10);
			dialog.css('left',canvas.scrollLeft() + 10);

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
				SignalPath.updateModule(pub);
			})
			dialog.find(".close-btn").click(function(){
				dialog.hide()
			})
			
			$(SignalPath).on("new", pub.onDelete);
			$(SignalPath).on("loaded", pub.onDelete);

			$(SignalPath).on("started", clearDebug);
			
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

			debug.css('top',canvas.scrollTop() + 10);
			debug.css('left',canvas.scrollLeft() + 10);

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
				clearDebug()
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
	
	function createEditCodeButton() {
		var editButton = $("<button class='btn btn-primary btn-sm'>Edit code</button>");
		editButton.click(createCodeWindow);
		pub.getDiv().find(".modulefooter").prepend(editButton);
	}
	
	function updateJson() {
		prot.jsonData.code = editor.getValue();
	}

	prot.receiveResponse = function(payload) {
		if (payload.type=="debug" && debug != null) {
			debugTextArea.append(payload.t+" - "+payload.msg+"<br>");
		}
	}

	pub.handleError = prot.handleError = function(error) {
		if (error.type=="compilationErrors") {
			for (var i=0;i<error.errors.length;i++) {
				editor.setGutterMarker(error.errors[i].line-1, "breakpoints", makeMarker());
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
		clearModule()
	}

	var super_onClose = pub.onClose;
	pub.onClose = function() {
		if(super_onClose)
			super_onClose()
		clearModule()
	}

	function clearDebug() {
		if(debugTextArea)
			debugTextArea.html("");
	}

	var clearModule = function(){
		if (dialog!=null) {
			$(dialog).remove()
			dialog = null
		}
		if (debug!=null) {
			$(debug).remove()
			debug = null
		}
	}
	
	return pub;
}