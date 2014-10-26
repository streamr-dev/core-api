(function(exports) {

var tourUrlRoot = Streamr.createLink({ uri: 'js/tours/' })
var pageTours = []

var urlRe = /[\?\&]playTour=([0-9]*)/

function loadUserCompletedTours(cb) {
	$.get(Streamr.createLink({ controller: 'TourUser', action: 'list' }), cb)
}

function Tour() {
	this._modules = {}
	this._steps = []
	this._tourNumber = 0
	this._beforeStart = function(cb) {
		cb()
	}
}

Tour.hasTour = function(tourNumber) {
	pageTours.push(tourNumber)
}

Tour.continueTour = function() {
	var urlParam = window.location.search.match(urlRe)
	if (urlParam) {
		return Tour.playTour(parseInt(urlParam[1], 10))
	}

	var state = hopscotch.getState()

	console.log('Tour state', state)

	if (state) {
		var currentTour = parseInt(state.split(':')[0], 10) || 0
		var currentStep = parseInt(state.split(':')[1], 10) || 0
		return Tour.loadTour(currentTour, function(tour) {
			// only if the previous step is multipage, should we continue the tour
			if (currentStep > 0 && !tour._steps[currentStep-1].multipage) {
				console.log('Tour resetting to step 0')
				currentStep = 0
			}

			tour.start(currentStep)
		})
	}

	loadUserCompletedTours(function(completedTours) {
		console.log('Tour.completedTours', completedTours)

		pageTours.some(function(tourNumber) {
			if (completedTours.indexOf(tourNumber) === -1) {
				Tour.playTour(tourNumber)
				return false
			}
		})
	})
}

Tour.playTour = function(tourNumber, currentStep) {
	console.log('Tour.playTour', tourNumber)

	hopscotch.endTour()

	Tour.loadTour(tourNumber, function(tour) {
		tour.start(currentStep || 0)
	})
}

Tour.loadTour = function(tourNumber, cb) {
	var url = tourUrlRoot + tourNumber + '.js'

	Tour.create = function() {
		console.log('Tour.create', tourNumber)

		var tour = new Tour()
		tour.ready = function() {
			tour.setTourNumber(tourNumber)

			if (cb)
				cb(tour)

			return this
		}

		return tour
	}

	$('<script src="'+url+'">').appendTo('head')
}

Tour.list = function(cb) {
	$.getJSON(tourUrlRoot + 'index.json', function(resp) {
		cb(resp)
	})
}

// ---

Tour.prototype.setGrailsPage = function(controller, action) {
	this._controller = controller
	this._action = action
	return this
}

Tour.prototype.setTourNumber = function(n) {
	this._tourNumber = n
}

Tour.prototype.next = function() {
	hopscotch.nextStep()
}

Tour.prototype._completed = function() {
	console.log('Tour completed', this._tourNumber)
	Streamr.tracking.track('Tour completed', {
		tour: this._tourNumber
	})
	this._markAsCompleted()
}

Tour.prototype._markAsCompleted = function() {
	$.post(Streamr.createLink({ controller: 'TourUser', action: 'completed' }),
		{ tourNumber: this._tourNumber })
}

Tour.prototype._closed = function() {
	console.log('Tour closed', this._tourNumber)
	Streamr.tracking.track('Tour closed', {
		tour: this._tourNumber,
		step: hopscotch.getCurrStepNum()
	})
	return this._markAsCompleted()
}

Tour.prototype.start = function(step) {
	var that = this

	if (step === 0 && this._controller && this._controller !== Streamr.controller) {
		window.location = Streamr.createLink(this._controller, this._action) +
			'?playTour=' + this._tourNumber
		return false;
	}

	window.tour = this // for debugging, may be removed

	if (step === 0) {
		Streamr.tracking.track('Tour start', {
			tour: this._tourNumber,
			step: step
		})
	}

	this._beforeStart(function() {
		hopscotch.startTour({
			steps: that._steps,
			id: that._tourNumber,
			onClose: function() {
				that._closed()
			},
			onEnd: function() {
				that._completed()
			},
			showPrevButton: true
		}, step)
	})
}

Tour.prototype.beforeStart = function(cb) {
	this._beforeStart = cb
	return this
}

Tour.prototype.offerNextTour = function(text) {
	var that = this

	return this.step(text, 'body', {
		ctaLabel: "Begin",
		showCTAButton: true,
		placement: 'top',
		yOffset: 300,
		xOffset: 200,
		nextOnTargetClick: false,
		showNextButton: false,
		onCTA: function() {
			Tour.playTour(that._tourNumber + 1)
		}
	})
}

Tour.prototype.step = function(content, target, opts, onShow) {
	var that = this
	var options = opts || {}

	if (typeof(opts) === 'function') {
		onShow = opts
		options = {}
	}

	if (!target && !opts) {
		// defaults for showing only text
		target = 'body'
		options = {
			placement: 'top',
			yOffset: 300,
			xOffset: 200,
			nextOnTargetClick: false
		}
	}

	this._steps.push($.extend({
		content: content,
		target: target,
		zindex: 3000,
		placement: 'right',
		showNextButton: (!onShow && !options.nextOnTargetClick),
		onShow: function() {
			Streamr.tracking.track('Tour step', {
				tour: that._tourNumber,
				step: hopscotch.getCurrStepNum()
			})

			if (!onShow)
				return;

			// call onShow in next tick, as elements may not have been drawn yet
			setTimeout(onShow, 0)
		}
	}, options))

	return this
}

Tour.prototype.bindModule = function(moduleName, div) {
	if (!this._modules[moduleName])
		this._modules[moduleName] = 1

	div.addClass('tour' + moduleName + this._modules[moduleName]++)
}

Tour.prototype._getSpObject = function(name) {
	return $('.'+name).data('spObject')
}

Tour.prototype.waitForMultiple = function(waits) {
	var _cb = this.next.bind(this)
	var count = 0

	return function(cb) {
		if (cb)
			_cb = cb

		waits.forEach(function(waitFor) {
			setTimeout(function() {
				waitFor(function() {
					if (++count === waits.length)
						_cb()	
				})
			}, 0) // setup in next tick
		})
	}
}

Tour.prototype.waitForModulesAdded = function(moduleNames) {
	var that = this
	var _cb = this.next.bind(this)

	var count = 0

	function listener() {
		count++
		if (count === moduleNames.length)
			_cb()
	}

	return function(cb) {
		if (cb)
			_cb = cb
		moduleNames.forEach(function(mn) {
			that.waitForModuleAdded(mn)(listener)
		})
	}
}

Tour.prototype.waitForModuleAdded = function(moduleName) {
	var that = this
	var _cb = this.next.bind(this)

	return function(cb) {
		if (cb)
			_cb = cb

		function listener(e, jsonData, div) {
			if (jsonData.name !== moduleName)
				return;

			that.bindModule(moduleName, div)

			$(SignalPath).off('moduleAdded', listener)

			_cb()
		}

		$(SignalPath).on('moduleAdded', listener)
	}
}

Tour.prototype.waitForInput = function(selector, content) {
	var _cb = this.next.bind(this)

	return function(cb) {
		if (cb)
			_cb = cb

		var $sel = $(selector)

		function _listener() {
			if ($sel.val() === content) {
				$sel.off('change', _listener)
				_cb()
			}
		}

		$sel.on('change', _listener)
	}
}

Tour.prototype.waitForEvent = function(selector, eventName) {
	var _cb = this.next.bind(this)

	return function(cb) {
		if (cb)
			_cb = cb

		$(selector).one(eventName, _cb)
	}
}

Tour.prototype.waitForStream = function(selector, streamName) {
	var _cb = this.next.bind(this)

	return function(cb) {
		if (cb)
			_cb = cb

		var $module = $($(selector).data('spObject'))
		$module.on('updated', function(e, data) {
			data.params.some(function(param) {
				if (!param.streamName)
					return;

				if (param.streamName === streamName) {
					_cb()
					return true
				}
			})
		})
	}
}

Tour.prototype.waitForConnections = function(conns) {
	var that = this
	var _triggered = false
	var parsedConns = []
	var _cb = this.next.bind(this)

	conns.forEach(function(conn) {
		var from = conn[0], to = conn[1]
		var fromModule = from.split('.')[0]
		var fromEndpoint = from.split('.')[1]
		var toModule = to.split('.')[0]
		var toEndpoint = to.split('.')[1]
		parsedConns.push({
			fromModule: fromModule, fromEndpoint: fromEndpoint,
			toModule: toModule, toEndpoint: toEndpoint
		})
	})

	function checkConnections() {
		var connsMade = 0

		if (_triggered)
			return;

		parsedConns.forEach(function(c) {
			var targetModule = that._getSpObject(c.toModule)
			var targetEndpoints = targetModule.getInputs()

			if (c.toEndpoint)
				targetEndpoints = [ targetModule.getInput(c.toEndpoint) ]

			targetEndpoints.forEach(function(targetEp) {
				targetEp.getConnectedEndpoints().forEach(function(xEndpoint) {
					if (!xEndpoint.module.div.hasClass(c.fromModule))
						return;

					if (c.fromEndpoint && xEndpoint.json.name !== c.fromEndpoint)
						return;

					connsMade++

					if (connsMade === conns.length) {
						setListeners(false)
						_triggered = true
						_cb()
					}
				})
			})
		})
	}

	function setListeners(onOff) {
		parsedConns.forEach(function(c) {
			$('.'+c.toModule)[onOff ? 'on' : 'off']('spConnect', checkConnections)
		})
	}

	return function(cb) {
		if (cb)
			_cb = cb
		setListeners(true)
	}
}

exports.Tour = Tour

})(typeof(exports) !== 'undefined' ? exports : window)
