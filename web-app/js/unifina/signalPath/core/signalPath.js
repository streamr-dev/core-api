/**
 * Triggered events (on SignalPath instance):
 * 
 * new ()
 * loading
 * loaded (saveData, signalPathData, settings)
 * saving
 * saved (saveData)
 * starting
 * started
 * stopping
 * stopped
 * moduleAdded (jsonData, div)
 * done
 * error (message)
 * 
 * Events for internal use:
 * _signalPathLoadModulesReady
 * 
 * Options:
 * 
 * parentElement: the parent DOM element to draw the SignalPath on
 * settings: function() that returns the settings object
 * errorHandler: function(data) that shows an error message. data can be a string or object, in which case data.msg is shown.
 * notificationHandler: function(data) that shows a notification message. data can be a string or object, in which case data.msg is shown.
 * allowRuntimeChanges: boolean for whether runtime parameter changes are allowed
 * apiUrl: base url of Streamr api
 * getModuleUrl: url for module JSON
 * requestUrl: url for POSTing runtime requests
 * connectionOptions: option object passed to StreamrClient
 */

var SignalPath = (function () { 

    var options = {
		parentElement: "parentElement",
		settings: function() {
			return {};
		},
		errorHandler: function(data) {
			alert((typeof data == "string" ? data : data.msg));
		},
		notificationHandler: function(data) {
			alert((typeof data == "string" ? data : data.msg));
		},
		allowRuntimeChanges: true,
		apiUrl: Streamr.createLink({"uri": "api/v1"}),
		getModuleUrl: Streamr.createLink("module", "jsonGetModule"),
		getModuleHelpUrl: Streamr.createLink("module", "jsonGetModuleHelp"),
		connectionOptions: {},
		resendOptions: {resend_all:true},
		zoom: 1
    };
    
    var connection;
	
	var modules = {}
	var moduleHashGenerator = 0

	// My representation that is currently saved
	var savedJson = null
	// My representation that is currently running
	var runningJson = null

    // set in init()
    var parentElement;
    var name;
    
	// Public
	var pub = {};
	pub.options = options;
	
    // TODO: remove if not needed anymore!
    pub.replacedIds = {};
	
	pub.init = function (opts) {
		jQuery.extend(true, options, opts);
		
		parentElement = $(options.parentElement);
		jsPlumb.Defaults.Container = parentElement;
		parentElement.data("spObject",pub)
		
		jsPlumb.bind('ready', function() {
			pub.clear();
		});
		
	    connection = new StreamrClient(options.connectionOptions)
	    connection.bind('disconnected', function() {
	    	pub.disconnect()
	    })
		pub.setZoom(opts.zoom)
		pub.jsPlumb = jsPlumb
	};
	pub.unload = function() {
		jsPlumb.reset();
		if (connection && connection.isConnected()) {
			connection.disconnect()
		}
	};
	pub.sendRequest = function(hash,msg,callback) {
		if (runData) {
			
			// Include UI channel if exists
			var channel
			for (var i=0;i<runData.uiChannels.length;i++) {
				// using == on purpose
				if (runData.uiChannels[i].hash==hash) {
					channel = runData.uiChannels[i].id
					break
				}
			}

			$.ajax({
				type: 'POST',
				url: options.requestUrl,
				data: JSON.stringify({
					id: runData.id,
					hash: hash,
					channel: channel,
					msg: msg
				}),
				dataType: 'json',
				contentType: 'application/json; charset=utf-8',
				success: function(data) {
					if (!data.success) {
						handleError(data.error || data.response.error)
					}
					else if (callback)
						callback(data.response)
				}
			});
			return true;
		}
		
		return false;
	}
	pub.getParentElement = function() {
		return parentElement;
	}
	pub.getConnection = function() {
		return connection;
	}
	function loadJSON(data) {
		// Reset signal path
		clear();

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
					setModuleById(module.getHash(), module);
					module.redraw(); // Just in case
					if (callback)
						callback();
				}
				else {
					// TODO: generalize
					if (data.moduleErrors) {
						for (var i=0;i<data.moduleErrors.length;i++) {
							getModuleById(data.moduleErrors[i].hash).receiveResponse(data.moduleErrors[i].payload);
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

	function isSaved() {
		return savedJson != null && savedJson.id != null && savedJson.type !== 'example'
	}
	pub.isSaved = isSaved
	
	function replaceModule(module,id,configuration) {
		addModule(id,configuration,function(newModule) {
			deleteModuleById(newModule.getHash())
			
			newModule.setLayoutData(module.getLayoutData());
			// TODO: replace connections
			module.close();
			setModuleById(module.getHash(), newModule)
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
			data.hash = moduleHashGenerator++
		}
		// Else check that the moduleHashGenerator keeps up
		else if (data.hash >= moduleHashGenerator) {
			moduleHashGenerator = data.hash + 1
		}
		
		var mod = eval("SignalPath."+data.jsModule+"(data,parentElement,{signalPath:pub})");
		
		setModuleById(mod.getHash(), mod)
		
		var div = mod.getDiv();
		
		mod.redraw(); // Just in case
		
		var oldClose = mod.onClose;
		
		mod.onClose = function() {
			// Remove hash entry
			var hash = mod.getHash();
			deleteModuleById(hash)
			if (oldClose)
				oldClose();
		}
		
		return mod;
	}
	pub.createModuleFromJSON = createModuleFromJSON;
	
	function redraw() {
		pub.getModules().forEach(function(module) {
			module.redraw()
		})
		jsPlumb.repaintEverything()
	}
	pub.redraw = redraw
	
	function getModules() {
		var result = []
		Object.keys(modules).forEach(function(key) {
			result.push(modules[key])
		})
		return result
	}
	pub.getModules = getModules;
	
	function getModuleById(id) {
		return modules[id.toString()];
	}
	pub.getModuleById = getModuleById;
	
	function setModuleById(id, module) {
		modules[id.toString()] = module
	}
	
	function deleteModuleById(id) {
		delete modules[id.toString()]
	}
	
	function toJSON(settings) {
		var result = {
			name: getName(),
			settings: (settings ? settings : options.settings()),
			modules: []
		}
		
		getModules().forEach(function(module) {
			var json = module.toJSON()
			result.modules.push(json);
		})

		result = $.extend({}, savedJson, result)

		return result;
	}
	pub.toJSON = toJSON;

	function getName() {
		return name;
	}
	pub.getName = getName
	
	function setName(n) {
		name = n
	}
	pub.setName = setName

	/**
	 * Checks if this SignalPath has unsaved changes
	 */
	function isDirty() {
		return _.isEqual(toJSON(), savedJson)

	}
	pub.isDirty = isDirty

	function saveAs(name, callback) {
		setName(name)
		save(callback, true)
	}
	pub.saveAs = saveAs

	function save(callback, forceCreateNew) {
		$(pub).trigger('saving')

		function onSuccess(json) {
			savedJson = json

			if (callback)
				callback(json);

			$(pub).trigger('saved', [json]);
		}

		var json = toJSON()

		// If already saved, update
		if (isSaved() && !forceCreateNew) {
			_update(json, onSuccess)
		}
		// Otherwise create new (template)
		else {
			json.type = 'template'
			_create(json, onSuccess)
		}
	}
	pub.save = save;

	function _create(json, callback) {
		$.ajax({
			type: 'POST',
			url: options.apiUrl + "/canvases",
			data: JSON.stringify(json),
			contentType: 'application/json',
			dataType: 'json',
			success: function(response) {
				if (!response.error) {
					if (callback)
						callback(response)
				}
				else {
					handleError(response.error)
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				handleError(errorThrown)
			}
		})
	}

	function _update(json, callback) {
		$.ajax({
			type: 'PUT',
			url: options.apiUrl + "/canvases/"+json.id,
			data: JSON.stringify(json),
			contentType: 'application/json',
			dataType: 'json',
			success: function(response) {
				if (!response.error) {
					callback(json)
				}
				else {
					handleError(response.error)
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				handleError(errorThrown)
			}
		})
	}
	
	function clear() {
		if (isRunning())
			stop();
		
		getModules().forEach(function(module) {
			module.close()
		})
		
		modules = {};
		moduleHashGenerator = 0;
		
		parentElement.empty()
		
		savedJson = {}
		
		jsPlumb.reset();		
		
		$(pub).trigger('new');
	}
	pub.clear = clear;
	
	/**
	 * idOrObject can be either an id to fetch from the api, or json to apply as-is
	 */
	function load(idOrObject, callback) {
		$(pub).trigger('loading')

		function doLoad(json) {
			loadJSON(json);

			savedJson = json
			setName(json.name)

			if (json.state === 'running')
				runningJson = json

			if (callback)
				callback(json);

			// Trigger loaded on pub and parentElement
			$(pub).add(parentElement).trigger('loaded', [json]);
		}

		// Fetch from api by id
		if (typeof idOrObject === 'string') {
			$.getJSON(options.apiUrl + '/canvases/' + idOrObject, function(response) {
				if (response.error) {
					handleError(response.error)
				} else {
					doLoad(response);
				}
			});
		}
		else {
			doLoad(idOrObject)
		}
	}
	pub.load = load;
	
	function start(startRequest, callback) {
		startRequest = startRequest || {}

		if (isRunning()) {
			handleError("Canvas is already running.")
			return
		}
		if (isDirty()) {
			handleError("Canvas has unsaved changes. Please save it before starting.")
			return
		}

		$(pub).trigger('starting')

		if (runningJson == null)
			runningJson = savedJson

		// Clean modules before running
		getModules().forEach(function(module) {
			module.clean()
		})
		
		$.ajax({
			type: 'POST',
			url: options.apiUrl + '/canvases/' + runningJson.id + '/start',
			data: JSON.stringify(startRequest),
			contentType: 'application/json',
			dataType: 'json',
			success: function(response) {
				if (response.error) {
					handleError("Error:\n"+response.error)
				}

				runningJson = response

				if (callback)
					callback(response)

				$(pub).trigger('started', [response])
			},
			error: function(jqXHR,textStatus,errorThrown) {
				handleError(textStatus+"\n"+errorThrown)
			}
		});
	}
	pub.start = start;

	function startAdhoc(callback) {
		var json = toJSON()
		json.adhoc = true
		_create(json, function(createdJson) {
			runningJson = createdJson
			start({loadState:false}, callback)
		})
	}
	pub.startAdhoc = startAdhoc
	
	function subscribe() {
		if (!runningJson)
			throw "No runningJson, nothing to subscribe to!"

		if (!connection.isConnected())
			connection.connect()

		// SignalPath uiChannel
		if (runningJson.uiChannel) {
			connection.subscribe(runningJson.uiChannel.id, processMessage, options.resendOptions)
		}
		
		// Module uiChannels
		runningJson.modules.forEach(function(moduleJson) {
			if (moduleJson.uiChannel) {
				// Module channels reference the module by hash
				var module = getModuleById(moduleJson.hash)
				connection.subscribe(moduleJson.uiChannel.id, module.receiveResponse, module.getUIChannelOptions())
			}
		})
	}
	pub.subscribe = subscribe
	
	function reconnect() {
		subscribe();
	}
	pub.reconnect = reconnect;
	
	function processMessage(message) {
		if (message.type=="P") {
			var hash = message.hash;
			try {
				getModuleById(hash).receiveResponse(message.payload);
			} catch (err) {
				console.log(err.stack);
			}
		}
		else if (message.type=="MW") {
			var hash = message.hash;
			getModuleById(hash).addWarning(message.msg);
		}
		else if (message.type=="D") {
			$(pub).trigger("done")
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
	}
	pub.disconnect = disconnect;
	
	function isRunning() {
		return runningJson!=null && runningJson.state === 'started'
	}
	pub.isRunning = isRunning;
	
	function stop() {
		if (isRunning()) {
			$(pub).trigger('stopping')

			$.ajax({
				type: 'POST',
				url: options.apiUrl + '/canvases/' + runningJson.id + '/stop',
				dataType: 'json',
				success: function(data) {
					// Ignore errors on stop
					if (data.error) {}

					runningJson = null
					$(pub).trigger('stopped');
				},
				error: function(jqXHR,textStatus,errorThrown) {
					handleError(textStatus+"\n"+errorThrown)
				}
			});
		}
		disconnect();
	}
	pub.stop = stop;
	
	function getZoom() {
		return parentElement.css("zoom") != null ? parseFloat(parentElement.css("zoom")) : 1
	}
	pub.getZoom = getZoom

	function setZoom(zoom, animate) {
		if (animate===undefined)
			animate = true
		
		if (animate)
			parentElement.animate({ zoom: zoom }, 300);
		else (parentElement.css("zoom", zoom))
	}
	pub.setZoom = setZoom

	return pub; 
}());
