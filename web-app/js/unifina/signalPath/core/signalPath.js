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
 * moduleBeforeClose (jsonData, div)
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
		connectionOptions: {
			autoConnect: true,
			autoDisconnect: true
		},
		zoom: 1
    };
    
    var connection
	var subscription
	
	var modules = {}
	var moduleHashGenerator = 0

	// My representation that is currently saved
	var savedJson = null
	// My representation that is currently running
	var runningJson = null

    // set in init()
    var parentElement;
    var name = "Untitled canvas"
    
	// Public
	var pub = {};
	pub.options = options;

	var isBeingReloaded = false
	var debugLoopInterval = null
	
    // TODO: remove if not needed anymore!
    pub.replacedIds = {};
	
	pub.init = function (opts) {
		jQuery.extend(true, options, opts);
		
		parentElement = $(options.parentElement);
		jsPlumb.Defaults.Container = parentElement;
		parentElement.data("spObject", pub)
		
		jsPlumb.bind('ready', function() {
			pub.clear();
		});
		
	    connection = new StreamrClient(options.connectionOptions)

		pub.setZoom(opts.zoom)
		pub.jsPlumb = jsPlumb

		$(pub).on('new', pub.unsubscribe)
		$(pub).on('started', pub.subscribe)
		$(pub).on('stopped', function() {
			// Simply unsubscribe when a canvas is stopped. It is important to let adhoc canvases auto-unsubscribe when done.
			if (runningJson && !runningJson.adhoc) {
                pub.unsubscribe()
			}
			runningJson = null
		})
		$(pub).on('loaded', function() {
			if (pub.isRunning())
                pub.subscribe()
		})

        pub.floatingSearchBar = new FloatingSearchBar(parentElement, {}, function(item, position) {
            var moduleId = item.id
            var options = {
                layout: {
                    position: {
                        top: position.y,
                        left: position.x
                    }
                }
            }
            if (item.resultType == "stream") { // is stream, specifies module
                moduleId = item.feed.module
                options.params = [{
                    name: 'stream',
                    value: item.id
                }]
            }
            pub.addModule(moduleId, options)
        })
	};

	pub.unload = function() {
		jsPlumb.reset();
		if (connection && connection.isConnected()) {
			connection.unsubscribe()
		}
	};

	pub.runtimeRequest = function(url, msg, callback) {
		$.ajax({
			type: 'POST',
			url: url,
			data: JSON.stringify(msg),
			dataType: 'json',
			contentType: 'application/json; charset=utf-8',
			success: function(data) {
				if (callback)
					callback(data)
			},
			error: function(xhr) {
				var errorMsg
				var errorObject
				try {
					var response = JSON.parse(xhr.responseText)
					errorObject = response
					errorMsg = response.message
				} catch (err) {
					errorObject = {message: xhr.responseText}
					errorMsg = xhr.responseText
				}

				if (callback) {
					callback(errorObject, errorMsg)
				}
				else {
					_handleError(errorMsg)
				}
			}
		})
	}

	pub.getURL = function() {
		if (runningJson && (runningJson.id || runningJson.baseURL)) {
			return runningJson.baseURL || options.apiUrl + '/canvases/'+(runningJson.id)
		}
		else if (savedJson && (savedJson.id || savedJson.baseURL)) {
			return savedJson.baseURL || options.apiUrl + '/canvases/'+(savedJson.id)
		}
		else {
			return undefined
		}
	}

	pub.getRuntimeRequestURL = function() {
		return pub.getURL() ? pub.getURL() + '/request' : undefined
	}

	pub.getParentElement = function() {
		return parentElement;
	}

	pub.getConnection = function() {
		return connection;
	}

    pub.loadJSON = function(data) {
		// Reset signal path
        pub.clear(true);

		jsPlumb.setSuspendDrawing(true);

		$(data.modules).each(function(i,mod) {
            pub.createModuleFromJSON(mod);
		});

		$(pub).trigger("_signalPathLoadModulesReady");
		
		_checkJsPlumbEndpoints()
		jsPlumb.setSuspendDrawing(false,true);
	}
	
	pub.updateModule = function(module, callback) {
		$.ajax({
			type: 'POST',
			url: options.getModuleUrl, 
			data: {id: module.toJSON().id, configuration: JSON.stringify(module.toJSON())}, 
			dataType: 'json',
			success: function(data) {
				if (!data.error) {
					// Guard against the situation where module has been closed while the update was in flight
					if (!module.isClosed()) {
						module.updateFrom(data);

						var div = module.getDiv();
						_setModuleById(module.getHash(), module);
						module.redraw(); // Just in case
						if (callback) {
							callback(data);
						}
					}
				}
				else {
					if (data.moduleErrors) {
						for (var i=0;i<data.moduleErrors.length;i++) {
                            pub.getModuleById(data.moduleErrors[i].hash)._handleError(data.moduleErrors[i].payload);
						}
					}
					_handleError(data.message)
                    
                    if (callback) {
						callback(data, data.message)
					}
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				_handleError(errorThrown)
			}
		});
	}

    pub.isSaved = function() {
        return savedJson != null && savedJson.id != null && savedJson.type !== 'example'
    }

    pub.getId = function() {
		return savedJson ? savedJson.id : undefined
	}

    pub.replaceModule = function(module,id,configuration) {
        pub.addModule(id,configuration,function(newModule) {
			_deleteModuleById(newModule.getHash())
			
			newModule.setLayoutData(module.getLayoutData());
			// TODO: replace connections
			module.close();
			_setModuleById(module.getHash(), newModule)
			newModule.redraw();
		});
	}

    pub.addModule = function(id, configuration, callback) {
		// Get indicator JSON from server
		$.ajax({
			type: 'POST',
			url: options.getModuleUrl, 
			data: {id: id, configuration: JSON.stringify(configuration)}, 
			dataType: 'json',
			success: function(data) {
				if (!data.error) {
					jsPlumb.setSuspendDrawing(true);
					var module = pub.createModuleFromJSON(data);
					_checkJsPlumbEndpoints()
					jsPlumb.setSuspendDrawing(false,true);
					if (callback)
						callback(module);
				} else {
					_handleError(data.message)
				}
			},
			error: function(jqXHR,textStatus,errorThrown) {
				_handleError(errorThrown)
			}
		});
	}

    pub.createModuleFromJSON = function(data) {
		if (data.error) {
			_handleError(data.message)
			return
		}
		
		// Generate an internal index for the module and store a reference in a table
		if (data.hash == null) {
			data.hash = moduleHashGenerator++
		}
		// Else check that the moduleHashGenerator keeps up
		else if (data.hash >= moduleHashGenerator) {
			moduleHashGenerator = data.hash + 1
		}
		
		var mod = eval("SignalPath."+data.jsModule+"(data,parentElement,{signalPath:pub})");
		
		_setModuleById(mod.getHash(), mod)
		
		var div = mod.getDiv();
		
		mod.redraw(); // Just in case
		
		var oldClose = mod.onClose;
		
		mod.onClose = function() {
			// Remove hash entry
			var hash = mod.getHash();
			_deleteModuleById(hash)
   
			if (oldClose) {
                oldClose()
			}
		}
		
		return mod;
	}

    pub.redraw = function() {
		pub.getModules().forEach(function(module) {
			module.redraw()
		})
		jsPlumb.repaintEverything()
	}

    pub.getModules = function() {
		var result = []
		Object.keys(modules).forEach(function(key) {
			result.push(modules[key])
		})
		return result
	}

    pub.getModuleById = function(id) {
		return modules[id.toString()];
	}

    pub.toJSON = function(settings) {
		var result = {
			name: pub.getName(),
			settings: (settings ? settings : options.settings()),
			modules: []
		}

        pub.getModules().forEach(function(module) {
			var json = module.toJSON()
			result.modules.push(json);
		})

		result = $.extend({}, savedJson, result)

		return result;
	}

    pub.getName = function() {
		return name;
	}

    pub.setName = function(n) {
		name = n
	}

	/**
	 * Checks if this SignalPath has unsaved changes
	 */
    pub.isDirty = function() {
		var currentJson = $.extend(true, {}, savedJson, pub.toJSON())
		var dirty = (JSON.stringify(savedJson) !== JSON.stringify(currentJson))
		return dirty
	}

    pub.saveAs = function(name, callback) {
        pub.setName(name)
        pub.save(function(json) {
			pub.clear()
            pub.load(json, function() {
				setTimeout(callback, 0)
			})
		}, true)
	}

    pub.save = function(callback, forceCreateNew) {
		$(pub).trigger('saving')

		function onSuccess(json) {
			_setSavedJson(json)

			if (callback)
				callback(json);

			$(pub).trigger('saved', [json]);
		}

		var json = pub.toJSON()

		// If already saved, update
		if (pub.isSaved() && !forceCreateNew) {
			_update(json, onSuccess)
		}
		// Otherwise create new
		else {
			_create(json, onSuccess)
		}
	}

    pub.clear = function(isSilent) {
		pub.unsubscribe()

	function _update(json, callback, errorCallback) {
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
				if (jqXHR.responseJSON) {
					var apiError = jqXHR.responseJSON;
					if (apiError && apiError.message) {
						handleError(apiError.message)
                        errorCallback && errorCallback(apiError)
						return
					}

				}
				handleError(errorThrown)
			}
		})
	}
	
	function clear(isSilent) {
		unsubscribe()

		if (isRunning() && runningJson.adhoc)
			stop();
		
		getModules().forEach(function(module) {
			module.close()
		})
		
		modules = {};
		moduleHashGenerator = 0;
		
		parentElement.empty()

		name = "Untitled canvas"
		savedJson = {}
		runningJson = null
		
		jsPlumb.reset();		

		if (!isSilent)
			$(pub).trigger('new');
	}
	
	/**
	 * idOrObject can be either an id to fetch from the api, or json to apply as-is
	 */
    pub.load = function(idOrObject, callback) {
		SignalPath.isBeingReloaded = true
		$(pub).trigger('loading')

		function doLoad(json) {
            pub.loadJSON(json);
            pub.setName(json.name)
			_setSavedJson(json)

			if (json.state.toLowerCase() === 'running')
				runningJson = json
			else
				runningJson = null

			if (callback)
				callback(json);

			SignalPath.isBeingReloaded = false
			
			// Trigger loaded on pub and parentElement
			$(pub).add(parentElement).trigger('loaded', [savedJson]);

			// It is important that savedJson is rewritten after the loaded event has been fired, because it may affect canvas settings
			savedJson = $.extend(true, savedJson, pub.toJSON()) // deep copy
		}

		// Fetch from api by id
		if (typeof idOrObject === 'string') {
			$.getJSON(options.apiUrl + '/canvases/' + idOrObject)
				.done(doLoad)
				.fail(function(jqXHR, textStatus, errorThrown) {
					_handleError(jqXHR.responseJSON.message)
				})
		}
		else {
            doLoad(idOrObject)
		}
	}

    pub.start = function(startRequest, callback, ignoreDirty) {
		startRequest = startRequest || {}

		if (pub.isRunning()) {
			_handleError("Canvas is already running.")
			return
		}
		if (!ignoreDirty && pub.isDirty()) {
			_handleError("Canvas has unsaved changes. Please save it before starting.")
			return
		}

		$(pub).trigger('starting')

		if (runningJson == null)
			runningJson = savedJson

		// Clean modules before running
        pub.getModules().forEach(function(module) {
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
					_handleError("Error:\n"+response.error)
				}

				runningJson = response

				if (!response.adhoc)
					savedJson = runningJson

				if (callback)
					callback(response)

				$(pub).trigger('started', [response])

				response.modules.forEach(function(runningModuleJson) {
					$(pub.getModuleById(runningModuleJson.hash)).trigger('started', [runningModuleJson])
				})
			},
			error: function(jqXHR, textStatus, errorThrown) {
				if (callback) {
					callback(undefined, jqXHR.responseJSON)
				} if (jqXHR && jqXHR.responseJSON && jqXHR.responseJSON.message) {
					_handleError(jqXHR.responseJSON.message)
				} else {
					_handleError(textStatus + "\n" + errorThrown)
				}
			}
		});
	}

    pub.startAdhoc = function(startRequest, callback) {
		startRequest = startRequest || {}
		startRequest.clearState = false
		var json = pub.toJSON()
		json.adhoc = true
		_create(json, function(createdJson) {
			runningJson = createdJson
            pub.start(startRequest, callback, true)
		})
	}

    pub.subscribe = function() {
		if (pub.isRunning() && runningJson.uiChannel) {
			subscription = connection.subscribe(
 				{
					stream: runningJson.uiChannel.id,
					resend_all: (runningJson.adhoc ? true : undefined)
				},
				_processMessage
			)
		}
	}

    pub.reconnect = function() {
        pub.subscribe();
	}

    pub.unsubscribe = function () {
		if (subscription) {
			connection.unsubscribe(subscription)
			subscription = undefined
		}
	}

    pub.isRunning = function() {
		return runningJson != null && runningJson.state !== undefined && runningJson.state.toLowerCase() === 'running'
	}

	pub.isAdhoc = function() {
		return pub.isRunning() && runningJson.adhoc
	}

    pub.stop = function(callback) {
		$(pub).trigger('stopping')

		function triggerStopped() {
			$(pub).trigger('stopped');
            pub.getModules().forEach(function(module) {
				$(module).trigger('stopped');
			})
		}

		if (pub.isRunning()) {
			function onStopped(data) {
				if (data) {
					_setSavedJson(data)
				}
				triggerStopped()
			}

			$.ajax({
				type: 'POST',
				url: options.apiUrl + '/canvases/' + runningJson.id + '/stop',
				dataType: 'json',
				success: function(data) {
					onStopped(data)

					// data may be undefined if the canvas was deleted on stop
					if (callback)
						callback(data)
				},
				error: function(jqXHR,textStatus,errorThrown) {
					// Handle the case where the canvas is dead and can't be reached by the stop request
					if (jqXHR.responseJSON && jqXHR.responseJSON.code === 'CANVAS_UNREACHABLE') {
						savedJson.state = 'STOPPED'
						onStopped()
					}

					if (callback) {
						callback(undefined, jqXHR.responseJSON)
					} else {
						_handleError(textStatus + "\n" + errorThrown)
					}
				}
			});
		}
		else {
			triggerStopped()
		}
	}

    pub.getZoom = function() {
		return parentElement.css("zoom") != null ? parseFloat(parentElement.css("zoom")) : 1
	}

    pub.setZoom = function(zoom, animate) {
		if (animate===undefined)
			animate = true
		
		if (animate)
			parentElement.animate({ zoom: zoom }, 300);
		else (parentElement.css("zoom", zoom))
	}

	pub.isLoading = function() {
		return SignalPath.isBeingReloaded;
	}

	// Whether canvas represents something that cannot be edited but only viewed, e.g., a running sub-canvas.
	pub.isReadOnly = function() {
		return runningJson && runningJson.readOnly;
    }

	pub.DEBUG_STRING_MAX_LENGTH = 30;
	pub.toggleDebugMode = function(intervalInMs) {
		if (debugLoopInterval) {
			clearInterval(debugLoopInterval)
			debugLoopInterval = null
			return false
		} else {
			debugLoopInterval = setInterval(function () {
				if (pub.isRunning() && !pub.isLoading()) {
					$.ajax({
						type: 'POST',
						url: pub.getURL() + "/request",
						data: JSON.stringify({type: "json"}),
						contentType: "application/json",
						dataType: "json",
						success: function (response) {
							console.log(response)
							var outputs = _.pluck(response.json.modules, "outputs")
							var inputs = _.pluck(response.json.modules, "inputs")
							var params = _.pluck(response.json.modules, "params")
							var endpoints = _.flatten(outputs.concat(inputs, params))
							_.each(endpoints, function (endpoint) {
								var displayedValue = endpoint.value ?
									JSON.stringify(endpoint.value).slice(0, pub.DEBUG_STRING_MAX_LENGTH) : "NULL";

								var endpointObject = $("#" + endpoint.id).data("spObject")
								if (endpointObject) {
									endpointObject.updateState(displayedValue);
								}
							})

						}
					})
				}
			}, intervalInMs || 10000)
			return true
		}
	}

    // Attempted workaround for CORE-283
    function _checkJsPlumbEndpoints() {
        // Make sure that jsPlumb agrees with the DOM on which elements exist, otherwise the following call may fail
        // There were some random problems with this due to unknown reason
        jsPlumb.selectEndpoints().each(function(it) {
            if ($("#"+it.elementId).length==0) {
                console.log("WARN: deleting unexisting jsPlumb endpoint "+it.elementId+" to workaround jsPlumb bug")
                jsPlumb.deleteEndpoint(it, true)
            }
        })
    }

    function _setSavedJson(data) {
        savedJson = $.extend(true, {}, pub.toJSON(), data) // Deep copy
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
                    _handleError(response.error)
                }
            },
            error: function(jqXHR,textStatus,errorThrown) {
                if (jqXHR.responseJSON) {
                    var apiError = jqXHR.responseJSON;
                    if (apiError && apiError.message) {
                        _handleError(apiError.message)
                        return;
                    }

                }
                _handleError(errorThrown)
            }
        })
    }

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
                    _handleError(response.error)
                }
            },
            error: function(jqXHR,textStatus,errorThrown) {
                _handleError(errorThrown)
            }
        })
    }

    function _processMessage(message) {
        if (message.type == "MW") {
            var hash = message.hash;
            pub.getModuleById(hash).addWarning(message.msg);
        }
        else if (message.type == "D") {
            $(pub).trigger("done")

            if (runningJson && runningJson.adhoc)
                $(pub).trigger("stopped")
        }
        else if (message.type == "E") {
            $(pub).trigger("stopped")
            _handleError(message.error)
        }
        else if (message.type == "N") {
            options.notificationHandler(message);
        }
    }

    function _setModuleById(id, module) {
        modules[id.toString()] = module
    }

    function _deleteModuleById(id) {
        delete modules[id.toString()]
    }

    function _handleError(message) {
        $(pub).trigger("error", [message])
        options.errorHandler({msg:message})
    }
    
	return pub;
}());
