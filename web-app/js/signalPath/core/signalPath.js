/**
 * Triggered events (on SignalPath instance):
 * 
 * signalPathNew ()
 * signalPathLoad (saveData, signalPathData, signalPathContext)
 * signalPathSave (saveData)
 * signalPathStart
 * signalPathStop
 * signalPathWorkspaceChange (mode)
 * 
 * Events for internal use:
 * _signalPathLoadModulesReady
 * 
 * Options:
 * 
 * canvas: id of the canvas div
 * signalPathContext: function() that returns the signalPathContext object
 * runUrl: url where to POST the run command
 * abortUrl: url where to POST the abort command
 * atmosphereUrl: url for atmosphere, the sessionId will be appended to this url
 */

var SignalPath = (function () { 

    var detectedTransport = null;
    var socket = $.atmosphere;
    var subSocket;
	
	// Private
	var modules;
//	var hash;
	var saveData;

	var sessionId = null;
	var runnerId = null;

	var payloadCounter = 0;	

    var atmosphereEndTag = "<!-- EOD -->";
	
    var webRoot = "";
    
    var workspace = "normal";
    
    // set in init()
    var canvas;
    var options = {
    		canvas: "canvas",
    		signalPathContext: function() {
    			return {};
    		},
    		errorHandler: function(data) {
    			alert(data.msg);
    		},
    		notificationHandler: function(data) {
    			alert(data.msg);
    		},
    		runUrl: "run",
    		abortUrl: "abort",
    		atmosphereUrl: project_webroot+"atmosphere/",
    		allowRuntimeChanges: true,
    };
    
	// Public
	var my = {};
	my.options = options;
	
    // TODO: remove if not needed anymore!
    my.replacedIds = {};
	
	my.init = function (opts) {
		jQuery.extend(true, options, opts);
		
		canvas = $("#"+options.canvas);
		jsPlumb.Defaults.Container = canvas;
		
		newSignalPath();

		jsPlumb.init();
		jQuery.atmosphere.logLevel = 'error';
	};
	my.unload = function() {
		jsPlumb.unload();
	};
	my.sendUIAction = function(hash,msg,callback) {
		if (sessionId!=null) {
			$.ajax({
				type: 'POST',
				url: project_webroot+"module/uiAction",
				data: {
					sessionId: sessionId,
					hash: hash,
					msg: JSON.stringify(msg)
				},
				success: callback,
				dataType: 'json'
			});
			return true;
		}
		else return false;
	}
	my.getCanvas = function() {
		return canvas;
	}
	
	function loadJSON(data) {
		// Reset signal path
		newSignalPath();
		
//		var connectedInputs = [];

		jsPlumb.setSuspendDrawing(true);
		
		// Backwards compatibility
//		if (!data.signalPathData) {
//			$(data.orderBooks).each(function(i,ob) {
//				createModuleFromJSON(ob);
//				$(ob.params).each(function(i,conn) {
//					if (conn.connected)
//						connectedInputs.push(conn);
//				});
//				$(ob.inputs).each(function(i,conn) {
//					if (conn.connected)
//						connectedInputs.push(conn);
//				});
//			});
//			$(data.charts).each(function(i,c) {
//				createModuleFromJSON(c);
//				$(c.params).each(function(i,conn) {
//					if (conn.connected)
//						connectedInputs.push(conn);
//				});
//				$(c.inputs).each(function(i,conn) {
//					if (conn.connected)
//						connectedInputs.push(conn);
//				});
//			});
//
//		}
		
		// Instantiate modules TODO: remove backwards compatibility
		$(data.signalPathData ? data.signalPathData.modules : data.modules).each(function(i,mod) {
			createModuleFromJSON(mod);
//			$(mod.params).each(function(i,conn) {
//				if (conn.connected)
//					connectedInputs.push(conn);
//			});
//			$(mod.inputs).each(function(i,conn) {
//				if (conn.connected)
//					connectedInputs.push(conn);
//			});
		});

		$(my).trigger("_signalPathLoadModulesReady");
		
		// Programmatically connect all connected inputs and outputs
//		$(connectedInputs).each(function(i,input) {
//			// TODO: remove fix when not needed anymore
//			var endpointId = (my.replacedIds[input.endpointId] == null ? input.endpointId : my.replacedIds[input.endpointId]);
//			var sourceId = (my.replacedIds[input.sourceId] == null ? input.sourceId : my.replacedIds[input.sourceId]);
//			
//			var inputEndpoint = $("#"+endpointId).data("endpoint");
//			if (inputEndpoint==null)
//				console.log("Warning: input endpoint "+endpointId+" was null!");
//			
//			var outputEndpoint = $("#"+sourceId).data("endpoint");
//			if (outputEndpoint==null)
//				console.log("Warning: output endpoint "+endpointId+" was null!");
//			
//			if (inputEndpoint!=null && outputEndpoint!=null)
//				jsPlumb.connect({source:inputEndpoint, target:outputEndpoint});
//		});

		jsPlumb.setSuspendDrawing(false,true);
	}
	my.loadJSON = loadJSON;
	
	my.updateModule = function(module,callback) {
		
		$.ajax({
			type: 'POST',
			url: project_webroot+'module/jsonGetModule', 
			data: {id: module.toJSON().id, configuration: JSON.stringify(module.toJSON())}, 
			dataType: 'json',
			success: function(data) {
				if (!data.error) {
					module.updateFrom(data);
					
					var div = module.getDiv();
//					$('#canvas').append(div);
//					$(div).draggable(module.getDragOptions());
					
					modules[module.getHash()] = module;
					module.redraw(); // Just in case
					if (callback)
						callback();
				}
				else {
					// TODO: generalize
					if (data.moduleErrors) {
						for (var i=0;i<data.moduleErrors.length;i++) {
							modules[data.moduleErrors[i].hash].receiveResponse(data.moduleErrors[i].payload);
						}
					}
					options.errorHandler({msg: data.message});
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				options.errorHandler({msg:errorThrown});
			}
		});
	}
	my.isSaved = function() {
		return saveData.isSaved ? true : false;
	}
	my.getSaveData = function() {
		return (saveData.isSaved ? saveData : null);
	}
	
	function addModule(id,configuration) { 
		// Get indicator JSON from server
		$.ajax({
			type: 'POST',
			url: project_webroot+'module/jsonGetModule', 
			data: {id: id, configuration: JSON.stringify(configuration)}, 
			dataType: 'json',
			success: function(data) {
				if (!data.error) {
					jsPlumb.setSuspendDrawing(true);
					createModuleFromJSON(data);
					jsPlumb.setSuspendDrawing(false,true);
				}
				else {
					options.errorHandler({msg:data.message});
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				options.errorHandler({msg:errorThrown});
			}
		});
		
		
//		$.getJSON(project_webroot+"module/jsonGetModule", {id: id, configuration: JSON.stringify(configuration)}, 
//			function(data) {
//				createModuleFromJSON(data);
//			});
	}
	my.addModule = addModule;
	
	function createModuleFromJSON(data) {
		if (data.error) {
			options.errorHandler({msg:data.message});
			return;
		}
		
		// Generate an internal index for the module and store a reference in a table
		if (data.hash==null) {
			data.hash = modules.length; //hash++;
		}
		
		var mod = eval("SignalPath."+data.jsModule+"(data,canvas)");
		
		// Resize the modules array if necessary
		while (modules.length < data.hash+1)
			modules.push(null);
		
		modules[mod.getHash()] = mod;
		
		var div = mod.getDiv();
		
		// Add the module to the canvas UPDATE: added in mod.createDiv()
//		$('#canvas').append(div);
		mod.redraw(); // Just in case
		
		// Made draggable in mod.createDiv()
//		$(div).draggable(mod.getDragOptions());
		
		var oldClose = mod.onClose;
		
		mod.onClose = function() {
			// Remove hash entry
			var hash = mod.getHash();
			modules[hash] = null;
			if (oldClose)
				oldClose();
		}
		
		return mod;
	}
	my.createModuleFromJSON = createModuleFromJSON;
	
	function getModules() {
		var result = [];
		for (var i=0;i<modules.length;i++) {
			if (modules[i]==null)
				continue;
			else result.push(modules[i]);
		}
		return result;
	}
	my.getModules = getModules;
	
	function getModuleById(id) {
		return modules[id];
	}
	my.getModuleById = getModuleById;
	
	function signalPathToJSON(signalPathContext) {
		var result = {
				workspace: getWorkspace(),
				signalPathContext: (signalPathContext ? signalPathContext : options.signalPathContext()),
				signalPathData: {
					modules: []
				}
		}
		
		if (saveData && saveData.name)
			result.signalPathData.name = saveData.name;
		
		for (var i=0;i<modules.length;i++) {
			if (modules[i]==null)
				continue;
			
			var json = modules[i].toJSON();
			result.signalPathData.modules.push(json);
		}
		
		return result;
	}
	my.toJSON = signalPathToJSON;

	/**
	 * SaveData should contain (optionally): url, name, (params)
	 */
	function saveSignalPath(sd,callback) {
		
		var signalPathContext = options.signalPathContext();
		
		if (sd==null)
			sd = saveData;
		else 
			saveData = sd;
		
		var result = signalPathToJSON(signalPathContext);
		var params = {
			name: sd.name,
			json: JSON.stringify(result)	
		};
		
		if (sd.params)
			$.extend(params,sd.params);
		
		$.ajax({
			type: 'POST',
			url: sd.url, 
			data: params,
			dataType: 'json',
			success: function(data) {
				if (!data.error) {
					$.extend(saveData, data);
					saveData.isSaved = true;

					if (callback)
						callback(saveData);
					
					$(my).trigger('signalPathSave', [saveData]);
				}
				else {
					options.errorHandler({msg:data.message});
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				options.errorHandler({msg:errorThrown});
			}
		});
	}
	my.saveSignalPath = saveSignalPath;
	
	function newSignalPath() {
		if (isRunning())
			abort();
		
		if (modules) {
			$(modules).each(function(i,mod) {
				if (mod!=null)
					mod.close();
			});
		}
		
		modules = [];
//		hash = 0;
		
		saveData = {
				isSaved : false
		}
		
		jsPlumb.reset();
		
		// Bind connection and disconnection events
		jsPlumb.bind("connection",function(connection) {
			$(connection.source).trigger("spConnect", $(connection.target).data("spObject"));
			$(connection.target).trigger("spConnect", $(connection.source).data("spObject"));
		});
		jsPlumb.bind("connectionDetached",function(connection) {
			if (!connection.connection.pending) {
				$(connection.source).trigger("spDisconnect", $(connection.target).data("spObject"));
				$(connection.target).trigger("spDisconnect", $(connection.source).data("spObject"));
			}
		});
		
		$(my).trigger('signalPathNew');
	}
	my.newSignalPath = newSignalPath;
	
	/**
	 * Options must at least include options.url OR options.json
	 */
	function loadSignalPath(opt,callback) {
		var params = opt.params || {};
		
		if (opt.json) {
			_load(opt.json,opt,callback);
		}
		if (opt.url) {
			$.getJSON(opt.url, params, function(data) {
				if (data.error) {
					options.errorHandler({msg:data.message});
					if (data.signalPathData) {
						_load(data,opt,callback);
					}
				}
				else _load(data,opt,callback);
			});
		}
	}
	my.loadSignalPath = loadSignalPath;
	
	function _load(data,options,callback) {
		loadJSON(data);
		
		saveData = {
			isSaved : true,
			name: (data.signalPathData && data.signalPathData.name ? data.signalPathData.name : data.name)
		}
		
		$.extend(saveData,options.saveData);
		$.extend(saveData,data.saveData);
		
		// TODO: remove backwards compatibility
		if (callback) callback(saveData, data.signalPathData ? data.signalPathData : data, data.signalPathContext);
		
		if (data.workspace!=null && data.workspace!="normal")
			setWorkspace(data.workspace);
		
		$(my).trigger('signalPathLoad', [saveData, data.signalPathData ? data.signalPathData : data, data.signalPathContext]);
	}
	
	function run(additionalContext) {
		var signalPathContext = options.signalPathContext();
		jQuery.extend(true,signalPathContext,additionalContext);
		
		// Abort first if this signalpath is currently running
		if (isRunning())
			abort();
		
		var result = signalPathToJSON(signalPathContext);

		// TODO: move to module clean()
		$(".warning").remove();

		// Clean modules before running
		for (var i=0;i<modules.length;i++) {
			if (modules[i]==null)
				continue;
			else modules[i].clean();
		}
		
		$.ajax({
			type: 'POST',
			url: options.runUrl, 
			data: {
				signalPathContext: JSON.stringify(result.signalPathContext),
				signalPathData: JSON.stringify(result.signalPathData)
			},
			dataType: 'json',
			success: function(data) {
				if (data.error) {
					options.errorHandler({msg:"Error:\n"+data.error});
				}
				else {
					runnerId = data.runnerId;
					subscribeToSession(data.sessionId,true);
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				options.errorHandler({msg:textStatus+"\n"+errorThrown});
			}
		});
	}
	my.run = run;
	
	function subscribeToSession(sId,newSession) {
		if (sessionId != null)
			abort();
		
		if (newSession)
			payloadCounter = 0;
		
//		$("#spinner").show();
		
		sessionId = sId;
		
		var request = { 
				url : options.atmosphereUrl+sId,
				transport: 'long-polling',
				fallbackTransport: 'long-polling',
				executeCallbackBeforeReconnect : true,
				maxRequest: Number.MAX_VALUE,
				headers: {
					"X-C": function() {
						return payloadCounter;
					}
				}
		};

		request.onMessage = handleResponse;
		subSocket = socket.subscribe(request);
		
		$(my).trigger('signalPathStart');
	}
//	my.subscribeToSession = subscribeToSession;
	
	function reconnect(sId) {
//		if (rerequest(sId,0,-1))
		subscribeToSession(sId,false);
	}
	my.reconnect = reconnect;
	
//	function rerequest(sId,start,end) {
//		var returnVal;
//		
//		// Synchronous request
//		jQuery.ajax({
//			url:    'resend',
//			data: {sessionId:sId, runnerId:runnerId, start:start, end:end},
//			dataType: 'json',
//			success: function(result) {
//				if (result.success) {
//					processMessageBatch(result.messages);
//					returnVal = true; 
//				}
//				else returnVal = false;
//			},
//			async:   false
//		});
//		
//		return returnVal;
//	}
	
	function handleResponse(response) {
		detectedTransport = response.transport;
		
		if (response.status == 200) {
            var data = response.responseBody;
            
            // Check for the non-js comments that atmosphere may write

            var end = data.indexOf(atmosphereEndTag);
            if (end != -1) {
            	data = data.substring(end+atmosphereEndTag.length, data.length);
            }
            
            if (data.length > 0) {
            	var jsonString = '{"messages":['+(data.substring(0,data.length-1))+']}';
            	
        		var clear = [];
        		var msgObj = null;

        		try {
        			msgObj = jQuery.parseJSON(jsonString);
        		} catch (e) {
        			// Atmosphere sends commented out data to WebKit based browsers
        			// Atmosphere bug sometimes allows incomplete responses to reach the client
        			console.log(e);
        		}
        		if (msgObj != null)
        			processMessageBatch(msgObj.messages);
        		
//            	var messages = data.split("##");
            	
//            	for (var i=0;i<messages.length;i++) {
//            		var msgObj = null;
//
//            		try {
//            			var msgObj = jQuery.parseJSON(messages[i]);
//            		} catch (e) {
//            			// Atmosphere sends commented out data to WebKit based browsers
//            			// Atmosphere bug sometimes allows incomplete responses to reach the client
//            			console.log(e);
//            		}
//            		if (msgObj != null) {
//            			var hash = processMessage(msgObj);
//        				if (hash != null && $.inArray(hash, clear)==-1)
//        					clear.push(hash);
//            		}
//            	}
//            	
//        		for (var i=0;i<clear.length;i++) {
//        			modules[clear[i]].endOfResponses();
//        		}
            }
        }
	}
	
	function processMessageBatch(messages) {
		var clear = [];
		
		for (var i=0;i<messages.length;i++) {
			var hash = processMessage(messages[i]);
			if (hash != null && $.inArray(hash, clear)==-1)
				clear.push(hash);
		}
		
		for (var i=0;i<clear.length;i++) {
			try {
				modules[clear[i]].endOfResponses();
			} catch (err) {
				console.log(err.stack);
			}
		}
	}
	
	function processMessage(message) {
//		try {

		var clear = null;
		
		// A message that has been purged from cache is the empty javascript object
		if (message.counter==null) {
			payloadCounter++;
			return null;
		}
		// Update ack counter
		if (message.counter > payloadCounter) {
//			abort();
//			options.errorHandler("Counter: "+message.counter+", expected: "+payloadCounter);
			console.log("Messages SKIPPED! Counter: "+message.counter+", expected: "+payloadCounter);
//			if (!rerequest(sessionId,payloadCounter,message.counter-1)) {
//				console.log("WARN: Rerequest failed. Skipping messages!");
//				payloadCounter = message.counter;
//			}
		}
		else if (message.counter < payloadCounter) {
			console.log("Already received message: "+message.counter+", expecting: "+payloadCounter);
			return null; // ok?
		}
//		console.log("Counter: "+message.counter);
		payloadCounter = message.counter + 1;
		
//		for (var i=0;i<messages.length;i++) {
			if (message.type=="P") {
				var hash = message.hash;
				try {
					modules[hash].receiveResponse(message.payload);
				} catch (err) {
					console.log(err.stack);
				}
				clear = hash;
			}
			else if (message.type=="D") {
				abort();
//				closeSubscription();
			}
			else if (message.type=="E") {
				closeSubscription();
				options.errorHandler({msg:message.error});
			}
			else if (message.type=="N") {
				options.notificationHandler(message);
			}
//		}

		return clear;
		
//		} 
//		catch (e) {
//		options.errorHandler(e);
//		}
	}
	
	function closeSubscription() {
		socket.unsubscribe();
		
//		if ($.atmosphere.unsubscribe)
//			$.atmosphere.unsubscribe();
//		else $.atmosphere.close();
		
		sessionId = null;
		$(my).trigger('signalPathStop');
//		$("#spinner").hide();
	}
	
	function isRunning() {
		return sessionId!=null;
	}
	my.isRunning = isRunning;
	
	function abort() {

		$.ajax({
			type: 'POST',
			url: options.abortUrl, 
			data: {
				sessionId: sessionId,
				runnerId: runnerId
			},
			dataType: 'json',
			success: function(data) {
				if (data.error) {
					options.errorHandler({msg:"Error:\n"+data.error});
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				options.errorHandler({msg:textStatus+"\n"+errorThrown});
			}
		});

		closeSubscription();
	}
	my.abort = abort;
	
	function setWorkspace(name) {
		var oldWorkspace = workspace;
		workspace = name;
		
		if (name=="dashboard") {
			$(my).trigger("signalPathWorkspaceChange", [name, oldWorkspace]);
		}
		else {
			$(my).trigger("signalPathWorkspaceChange", [name, oldWorkspace]);
			
			jsPlumb.repaintEverything();
		}
	}
	my.setWorkspace = setWorkspace;
	
	function getWorkspace() {
		return workspace;
	}
	my.getWorkspace = getWorkspace;
	
	return my; 
}());

$(SignalPath).on("signalPathWorkspaceChange", function(event, name, old) {
	
	if (name=="dashboard") {
		$(jsPlumb.getAllConnections()).each(function(i,o) {
			o.setVisible(false); 
		});
		jsPlumb.selectEndpoints({scope:"*"}).each(function(ep) { 
			ep.setVisible(false); 
		});
	}
	else {
		jsPlumb.selectEndpoints({scope:"*"}).each(function(ep) { 
			ep.setVisible(true); 
		});
		
		$(jsPlumb.getAllConnections()).each(function(i,o) {
			o.setVisible(true); 
		});
	}
});