/**
 * Triggered events (on SignalPath instance):
 * 
 * new ()
 * loading
 * loaded (saveData, signalPathData, signalPathContext)
 * saving
 * saved (saveData)
 * started
 * stopped
 * workspaceChanged (mode)
 * moduleAdded(jsonData, div)
 * 
 * Events for internal use:
 * _signalPathLoadModulesReady
 * 
 * Options:
 * 
 * canvas: id of the canvas div
 * signalPathContext: function() that returns the signalPathContext object
 * errorHandler: function(data) that shows an error message. data can be a string or object, in which case data.msg is shown.
 * notificationHandler: function(data) that shows a notification message. data can be a string or object, in which case data.msg is shown.
 * allowRuntimeChanges: boolean for whether runtime parameter changes are allowed
 * runUrl: url where to POST the run command
 * abortUrl: url where to POST the abort command
 * atmosphereUrl: url for atmosphere, the sessionId will be appended to this url
 * getModuleUrl: url for module JSON
 * uiActionUrl: url for POSTing module UI runtime changes
 * 
 */

var SignalPath = (function () { 

    var options = {
    		canvas: "canvas",
    		signalPathContext: function() {
    			return {};
    		},
    		errorHandler: function(data) {
    			alert((typeof data == "string" ? data : data.msg));
    		},
    		notificationHandler: function(data) {
    			alert((typeof data == "string" ? data : data.msg));
    		},
    		allowRuntimeChanges: true,
    		runUrl: "run",
    		abortUrl: "abort",
    		getModuleUrl: Streamr.projectWebroot+'module/jsonGetModule',
    		getModuleHelpUrl: Streamr.projectWebroot+'module/jsonGetModuleHelp',
    		uiActionUrl: Streamr.projectWebroot+"module/uiAction"
    };
    
    var connection;
	
	var modules;
	var saveData;

	var sessionId = null;
	var runnerId = null;

    var webRoot = "";
    
    var workspace = "normal";
    
    // set in init()
    var canvas;
    
	// Public
	var pub = {};
	pub.options = options;
	
    // TODO: remove if not needed anymore!
    pub.replacedIds = {};
	
	pub.init = function (opts) {
		jQuery.extend(true, options, opts);
		
		canvas = $("#"+options.canvas);
		jsPlumb.Defaults.Container = canvas;
		
		jsPlumb.bind('ready', function() {
			pub.newSignalPath();
		});
		
	    connection = new StreamrClient(options.connectionOptions)
	};
	pub.unload = function() {
		jsPlumb.unload();
	};
	pub.sendUIAction = function(hash,msg,callback) {
		if (sessionId!=null) {
			$.ajax({
				type: 'POST',
				url: options.uiActionUrl,
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
	pub.getCanvas = function() {
		return canvas;
	}
	
	function loadJSON(data) {
		// Reset signal path
		newSignalPath();

		jsPlumb.setSuspendDrawing(true);
		
		// Instantiate modules TODO: remove backwards compatibility
		$(data.signalPathData ? data.signalPathData.modules : data.modules).each(function(i,mod) {
			createModuleFromJSON(mod);
		});

		$(pub).trigger("_signalPathLoadModulesReady");

		jsPlumb.setSuspendDrawing(false,true);
	}
	pub.loadJSON = loadJSON;
	
	pub.updateModule = function(module,callback) {
		
		$.ajax({
			type: 'POST',
			url: options.getModuleUrl, 
			data: {id: module.toJSON().id, configuration: JSON.stringify(module.toJSON())}, 
			dataType: 'json',
			success: function(data) {
				if (!data.error) {
					module.updateFrom(data);
					
					var div = module.getDiv();					
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
	pub.isSaved = function() {
		return saveData.isSaved ? true : false;
	}
	pub.getSaveData = function() {
		return (saveData.isSaved ? saveData : null);
	}
	
	function replaceModule(module,id,configuration) {
		addModule(id,configuration,function(newModule) {
			var hash = newModule.getHash();
			modules[hash] = null;
			
			newModule.setLayoutData(module.getLayoutData());
			// TODO: replace connections
			module.close();
			modules[module.getHash()] = newModule;
			newModule.redraw();
		});
	}
	pub.replaceModule = replaceModule;
	
	function addModule(id,configuration,callback) { 
		// Get indicator JSON from server
		$.ajax({
			type: 'POST',
			url: options.getModuleUrl, 
			data: {id: id, configuration: JSON.stringify(configuration)}, 
			dataType: 'json',
			success: function(data) {
				if (!data.error) {
					jsPlumb.setSuspendDrawing(true);
					var module = createModuleFromJSON(data);
					jsPlumb.setSuspendDrawing(false,true);
					if (callback)
						callback(module);
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
	pub.addModule = addModule;
	
	function createModuleFromJSON(data) {
		if (data.error) {
			options.errorHandler({msg:data.message});
			return;
		}
		
		// Generate an internal index for the module and store a reference in a table
		if (data.hash==null) {
			data.hash = modules.length; //hash++;
		}
		
		var mod = eval("SignalPath."+data.jsModule+"(data,canvas,{signalPath:pub})");
		
		// Resize the modules array if necessary
		while (modules.length < data.hash+1)
			modules.push(null);
		
		modules[mod.getHash()] = mod;
		
		var div = mod.getDiv();
		
		mod.redraw(); // Just in case
		
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
	pub.createModuleFromJSON = createModuleFromJSON;
	
	function getModules() {
		var result = [];
		for (var i=0;i<modules.length;i++) {
			if (modules[i]==null)
				continue;
			else result.push(modules[i]);
		}
		return result;
	}
	pub.getModules = getModules;
	
	function getModuleById(id) {
		return modules[id];
	}
	pub.getModuleById = getModuleById;
	
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
	pub.toJSON = signalPathToJSON;

	/**
	 * SaveData should contain (optionally): url, name, (params)
	 */
	function saveSignalPath(sd, callback) {
		$(pub).trigger('saving')
		
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
					
					$(pub).trigger('saved', [saveData]);
				}
				else {
					options.errorHandler({msg:data.message});
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				$(pub).trigger('error', errorThrown);
				options.errorHandler({msg:errorThrown});
			}
		});
	}
	pub.saveSignalPath = saveSignalPath;
	
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
		
		saveData = {
				isSaved : false
		}
		
		jsPlumb.reset();		
		
		$(pub).trigger('new');
	}
	pub.newSignalPath = newSignalPath;
	
	/**
	 * Options must at least include options.url OR options.json
	 */
	function loadSignalPath(opt, callback) {
		var params = opt.params || {};

		$(pub).trigger('loading')
    
		if (opt.json)
			_load(opt.json, opt, callback)

		if (opt.url) {
			$.getJSON(opt.url, params, function(data) {
				if (data.error) {
					$(pub).trigger('error', data.error)

					options.errorHandler({msg:data.message});

					if (data.signalPathData) {
						_load(data, opt, callback);
					}
				} else {
					_load(data, opt, callback);
				}
			});
		}
	}
	pub.loadSignalPath = loadSignalPath;
	
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
		
		$(pub).trigger('loaded', [saveData, data.signalPathData ? data.signalPathData : data, data.signalPathContext]);
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
	pub.run = run;
	
	function subscribeToSession(sId,newSession) {
		if (sessionId != null)
			abort();
		
		sessionId = sId;
		connection.subscribe(sId, handleResponse)
		connection.connect(newSession)

		$(pub).trigger('started');
	}
	
	function reconnect(sId) {
		subscribeToSession(sId,false);
	}
	pub.reconnect = reconnect;
	
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
		var clear = null;
		
		// A message that has been purged from cache is the empty javascript object
		if (message.counter==null) {
			payloadCounter++;
			return null;
		}
		// Update ack counter
		if (message.counter > payloadCounter) {
			console.log("Messages SKIPPED! Counter: "+message.counter+", expected: "+payloadCounter);
		}
		else if (message.counter < payloadCounter) {
			console.log("Already received message: "+message.counter+", expecting: "+payloadCounter);
			return null; // ok?
		}
		payloadCounter = message.counter + 1;
		
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
		}
		else if (message.type=="E") {
			closeSubscription();
			options.errorHandler({msg:message.error});
		}
		else if (message.type=="N") {
			options.notificationHandler(message);
		}

		return clear;
		
	}
	
	function closeSubscription() {
		socket.unsubscribe();
		
		sessionId = null;
		$(pub).trigger('stopped');
	}
	
	function isRunning() {
		return sessionId!=null;
	}
	pub.isRunning = isRunning;
	
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
	pub.abort = abort;
	
	function setWorkspace(name) {
		var oldWorkspace = workspace;
		workspace = name;
		
		if (name=="dashboard") {
			$(pub).trigger("workspaceChanged", [name, oldWorkspace]);
		}
		else {
			$(pub).trigger("workspaceChanged", [name, oldWorkspace]);
			
			jsPlumb.repaintEverything();
		}
	}
	pub.setWorkspace = setWorkspace;
	
	function getWorkspace() {
		return workspace;
	}
	pub.getWorkspace = getWorkspace;
	
	return pub; 
}());

$(SignalPath).on("workspaceChanged", function(event, name, old) {
	
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