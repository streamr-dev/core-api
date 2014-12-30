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
 * moduleAdded (jsonData, div)
 * error (message)
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
 * getModuleUrl: url for module JSON
 * uiActionUrl: url for POSTing module UI runtime changes
 * connectionOptions: option object passed to StreamrClient
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
		uiActionUrl: Streamr.projectWebroot+"module/uiAction",
		connectionOptions: {}
    };
    
    var connection;
	
	var modules;
	var saveData;
	var runData;

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
		
		checkJsPlumbEndpoints()
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
					handleError(data.message)
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				handleError(errorThrown)
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
	
	// Attempted workaround for CORE-283
	function checkJsPlumbEndpoints() {
		// Make sure that jsPlumb agrees with the DOM on which elements exist, otherwise the following call may fail
		// There were some random problems with this due to unknown reason
		jsPlumb.selectEndpoints().each(function(it) {
			if ($("#"+it.elementId).length==0) {
				console.log("WARN: deleting unexisting jsPlumb endpoint "+it.elementId+" to workaround jsPlumb bug")
				jsPlumb.deleteEndpoint(it, true)
			} 
		})
	}
	
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
					checkJsPlumbEndpoints()
					jsPlumb.setSuspendDrawing(false,true);
					if (callback)
						callback(module);
				}
				else {
					handleError(data.message)
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				handleError(errorThrown)
			}
		});
	}
	pub.addModule = addModule;
	
	function handleError(message) {
		$(pub).trigger("error", [message])
		options.errorHandler({msg:message})
	}
	
	function createModuleFromJSON(data) {
		if (data.error) {
			handleError(data.message)
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
					handleError(data.message)
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				handleError(errorThrown)
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
					handleError(data.message)

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
		
		saveData = {}
		
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
					handleError("Error:\n"+data.error)
				}
				else {
					subscribe(data,true);
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				handleError(textStatus+"\n"+errorThrown)
			}
		});
	}
	pub.run = run;
	
	function subscribe(rd,newSession) {
		if (runData != null)
			abort();
		
		// SignalPath channel wiring
		runData = rd;
		connection.subscribe(runData.channelMap.signalPath, processMessage)
		
		// Module channel wiring
		Object.getOwnPropertyNames(runData.channelMap.modules).forEach(function(hash) {
			connection.subscribe(runData.channelMap.modules[hash], modules[hash].receiveResponse, {resend:true})
		})
		
		// Start runner on subscription ack
		$(connection).one('subscribed', function(e, channels) {
			console.log("Subscribed, starting runner...")
			$.ajax({
				type: 'POST',
				url: runData.startURL, 
				dataType: 'json',
				error: function(jqXHR,textStatus,errorThrown) {
					handleError(textStatus+"\n"+errorThrown)
				}
			});
		})
		
		connection.connect(newSession)

		$(pub).trigger('started');
	}
	
	function reconnect() {
		subscribe(runData,false);
	}
	pub.reconnect = reconnect;
	
	function processMessage(message) {
		if (message.type=="P") {
			var hash = message.hash;
			try {
				modules[hash].receiveResponse(message.payload);
			} catch (err) {
				console.log(err.stack);
			}
			clear = hash;
		}
		else if (message.type=="MW") {
			var hash = message.hash;
			modules[hash].addWarning(message.payload);
		}
		else if (message.type=="D") {
			abort();
		}
		else if (message.type=="E") {
			disconnect();
			handleError(message.error)
		}
		else if (message.type=="N") {
			options.notificationHandler(message);
		}
	}
	
	function disconnect() {
		connection.disconnect()
		
		runData = null
		$(pub).trigger('stopped');
	}
	
	function isRunning() {
		return runData!=null;
	}
	pub.isRunning = isRunning;
	
	function abort() {

		$.ajax({
			type: 'POST',
			url: options.abortUrl, 
			data: {
				runnerId: runData.runnerId
			},
			dataType: 'json',
			success: function(data) {
				if (data.error) {
					handleError("Error:\n"+data.error)
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				handleError(textStatus+"\n"+errorThrown)
			}
		});

		disconnect();
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