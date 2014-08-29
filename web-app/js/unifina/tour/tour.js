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

Tour.playTour = function(tourNumber) {
	console.log('Tour.playTour', tourNumber)

	hopscotch.endTour()

	Tour.loadTour(tourNumber, function(tour) {
		tour.start(0)
	})
}

Tour.loadTour = function(tourNumber, cb) {
	var url = tourUrlRoot + tourNumber + '.js'
	$.get(url, function(tourCode) {
		var tour = eval(tourCode)
		tour.setTourNumber(tourNumber)
		if (cb)
			cb(tour)
	})
}

Tour.list = function(cb) {
	$.get(tourUrlRoot + 'index.json', function(resp) {
		cb(JSON.parse(resp))
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
	$.post(Streamr.createLink({ controller: 'TourUser', action: 'completed' }),
		{ tourNumber: this._tourNumber })
}

Tour.prototype.start = function(step) {
	var that = this

	if (step === 0 && this._controller && this._controller !== Streamr.controller) {
		window.location = Streamr.createLink(this._controller, this._action) +
			'?playTour=' + this._tourNumber
		return false;
	}

	hopscotch.startTour({
		steps: this._steps,
		id: this._tourNumber,
		onEnd: function() {
			that._completed()
		},
		showPrevButton: true
	}, step)
}

Tour.prototype.step = function(content, target, opts, onShow) {
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
		onShow: onShow || null
	}, options))

	return this
}

Tour.prototype.bindModule = function(moduleName, div) {
	if (!this._modules[moduleName])
		this._modules[moduleName] = 1

	div.addClass('tour' + moduleName + this._modules[moduleName]++)
}

Tour.prototype.waitForModuleAdded = function(moduleName) {
	var that = this

	return function() {
		function listener(e, jsonData, div) {
			if (jsonData.name === moduleName) {
				that.bindModule(moduleName, div)
				$(SignalPath).off('moduleAdded', listener)
				that.next()
			}
		}

		$(SignalPath).on('moduleAdded', listener)
	}
}

Tour.prototype.waitForConnections = function(conns) {
	var that = this

	var connsMade = 0

	function checkConnections() {
		conns.forEach(function(conn) {
			var from = conn[0]
			var to = conn[1]
			var fromModule = from.split('.')[0]
			var fromEndpoint = from.split('.')[1]
			var toModule = to.split('.')[0]
			var toEndpoint = to.split('.')[1]

			var endpoint = $('.'+toModule).data('spObject')
				.getEndpointByName(toEndpoint)

			endpoint.getConnectedEndpoints().forEach(function(ep) {
				if (ep.module.div.hasClass(fromModule)
					&& ep.json.name === fromEndpoint)
				{
					connsMade++

					if (connsMade === conns.length) {
						setListeners(false)
						that.next()
					}
				}
			})
		})
	}

	function setListeners(onOff) {
		conns.forEach(function(conn) {
			var from = conn[0]
			var to = conn[1]
			var fromModule = from.split('.')[0]
			var toModule = to.split('.')[0]

			$('.'+toModule)[onOff ? 'on' : 'off']('spConnect', checkConnections)
		})
	}

	return function() {
		setListeners(true)
	}
}

exports.Tour = Tour

})(window)

