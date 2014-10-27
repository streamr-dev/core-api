
var assert = require('assert')

global.window = {
	location: {
		search: ''
	}
}

global.Streamr = {
	user: "test",
	
	createLink: function(opt) {
		if (opt.uri)
			return '/'+opt.uri

		var ctrl = arguments[0]

		if (opt.controller)
			ctrl = opt.controller[0].toLowerCase() + opt.controller.slice(1)

		return '/'+ctrl+'/'+opt.action
	}
}

function Eventor() {
	var listeners = []
	return {
		on: function(en, cb) {
			listeners.push(cb)
		},
		off: function() {},
		trigger: function(e, a1, a2) {
			listeners.forEach(function(cb) {
				cb.apply(cb, [e, a1, a2])
			})
		}
	}
}

var Tour = require('../tour/tour').Tour

describe('Tour', function() {
	var tour

	beforeEach(function() {
		tour = new Tour()

		var $objects = {}

		global.SignalPath = new Eventor()

		global.$ = function(o) {
			if (o === global.SignalPath)
				return global.SignalPath

			if (!$objects[o])
				$objects[o] = new Eventor()

			return $objects[o]
		}
		global.$.extend = function(o) {
			return o
		}
		global.$.get = function(_url, cb) {
			cb([])
		}

		global.hopscotch = {
			endTour: function() {},
			getState: function() {}
		}
		
		Tour.startableTours([])
		Tour.continuableTours([])
	})

	it('should load the tour defined on page', function(done) {
		Tour.loadTour = function(tn) {
			assert.equal(tn, 123)
			done()
		}

		Tour.startableTours([123])
		Tour.autoStart()
	})

	it('should continue the tour set in the cookie if continuable on this page', function(done) {
		Tour.loadTour = function(tn, cb) {
			assert.equal(tn, 12)
			cb({
				start: function(step) {
					assert.equal(step, 34)
					done()
				}
			})
		}

		global.hopscotch.getState = function() {
			return Streamr.user+'#12:34'
		}
		
		Tour.startableTours([123])
		Tour.continuableTours([12])
		Tour.autoStart()
	})
	
	it('should not continue a non-continuable tour', function(done) {
		Tour.loadTour = function(tn, cb) {
			assert.equal(tn, 123)
			cb({
				start: function(step) {
					assert.equal(step, 0)
					done()
				}
			})
		}

		global.hopscotch.getState = function() {
			return Streamr.user+'#12:34'
		}
		
		Tour.startableTours([123])
		Tour.continuableTours([])
		Tour.autoStart()
	})
	
	it('should not continue another users tour', function(done) {
		global.hopscotch.getState = function() {
			return 'anotheruser#12:34'
		}
		
		Tour.loadTour = function(tn, cb) {
			assert.equal(tn, 123)
			cb({
				start: function(step) {
					assert.equal(step, 0)
					done()
				}
			})
		}

		Tour.startableTours([123])
		Tour.continuableTours([12])
		Tour.autoStart()
	})

	it('should play the tour given in the url', function(done) {
		global.window.location.search = '?playTour=34'
		Tour.loadTour = function(tn) {
			assert.equal(tn, 34)
			done()
		}
		
		Tour.startableTours([34])
		Tour.autoStart()
	})
	
	it('should play the tour even if its not startable from page', function(done) {
		global.window.location.search = '?playTour=34'
			Tour.loadTour = function(tn) {
			assert.equal(tn, 34)
			done()
		}
		
		Tour.startableTours([12])
		Tour.autoStart()
	})

	describe('waiting for modules', function() {
		it('should wait for one module added', function(done) {
			tour.next = done
			var div = { addClass: function() {} }
			tour.waitForModuleAdded('Boo')()
			global.SignalPath.trigger('moduleAdded', { name: 'Boo' }, div)
		})

		it('should wait for one module added and call back', function(done) {
			var div = { addClass: function() {} }
			tour.waitForModuleAdded('Boo')(done)
			global.SignalPath.trigger('moduleAdded', { name: 'Boo' }, div)
		})

		it('should wait for multiple modules added', function(done) {
			var count = 0
			tour.next = function() {
				assert.equal(count, 3)
				done()
			}
			var div = { addClass: function() { count++ } }
			tour.waitForModulesAdded(['Foo', 'Bar', 'Baz'])()
			global.SignalPath.trigger('moduleAdded', { name: 'Foo' }, div)
			global.SignalPath.trigger('moduleAdded', { name: 'Bar' }, div)
			global.SignalPath.trigger('moduleAdded', { name: 'Baz' }, div)
		})
	})

	describe('multiple waiters', function() {
		it('should wait for multiple triggers', function(done) {
			tour.next = done
			var waiters = []
			tour.waitForMultiple([
				function(cb) { waiters.push(cb) },
				function(cb) { waiters.push(cb) },
				function(cb) { waiters.push(cb) }
			])()
			setTimeout(function() {
				waiters.forEach(function(cb) {
					cb()
				})
			})
		})
	})

	describe('waiting for connections', function() {
		var filterEndpoints, speakerEndpoints, audioEndpoints

		function mockModule(name, endpoints) {
			return { getInputs: function() { return [] },
					getInput: function() { return { getConnectedEndpoints: function() {
					return endpoints
			}}}}
		}

		function mockEndpoint(name, moduleName) {
			return { json: { name: name }, module: { div: { hasClass: function(className) {
					return className === moduleName
			}}}}
		}

		beforeEach(function() {
			filterEndpoints = []
			speakerEndpoints = []
			audioEndpoints = []
			var _spObjects = {
				'audio': mockModule('audio', audioEndpoints),
				'filter': mockModule('filter', filterEndpoints),
				'speaker': mockModule('speaker', speakerEndpoints)
			}

			tour._getSpObject = function(name) {
				return _spObjects[name]
			}
		})

		it('should turn off listeners when connection is made', function(done) {
			var offCounter = 0
			function counter() {
				offCounter++
			}
			tour.next = function() {
				assert.equal(2, offCounter)
				done()
			}
			tour.waitForConnections([
				['audio.out', 'filter.in'],
				['filter.out', 'speaker.in']
			])()
			$('.filter').off = counter;
			$('.speaker').off = counter;
			filterEndpoints.push(mockEndpoint('out', 'audio'))
			speakerEndpoints.push(mockEndpoint('out', 'filter'))
			$('.speaker').trigger('spConnect')
			$('.filter').trigger('spConnect')
		})

		it('should wait for connections with all specified endpoints', function(done) {
			tour.next = done
			tour.waitForConnections([
				['audio.out', 'filter.in'],
				['filter.out', 'speaker.in']
			])()
			filterEndpoints.push(mockEndpoint('out', 'audio'))
			$('.filter').trigger('spConnect')
			$('.audio').trigger('spConnect')

			speakerEndpoints.push(mockEndpoint('out', 'filter'))
			$('.speaker').trigger('spConnect')
			$('.filter').trigger('spConnect')
		})

		it('should wait for connections with no specified endpoints', function(done) {
			tour.next = done
			tour.waitForConnections([
				['audio', 'filter'],
				['filter', 'speaker']
			])()
			tour._getSpObject('filter').getInputs = function() {
				return [{ getConnectedEndpoints: function() {
					return [ mockEndpoint('out', 'audio') ]
				}}]
			}
			tour._getSpObject('speaker').getInputs = function() {
				return [{ getConnectedEndpoints: function() {
					return [ mockEndpoint('out', 'filter') ]
				}}]
			}
			$('.speaker').trigger('spConnect')
			$('.filter').trigger('spConnect')
		})

		it('should wait for connections with left side endpoints', function(done) {
			tour.next = done
			tour.waitForConnections([
				['audio.out', 'filter'],
				['filter.out', 'speaker']
			])()
			tour._getSpObject('filter').getInputs = function() {
				return [{ getConnectedEndpoints: function() {
					return [ mockEndpoint('out', 'audio') ]
				}}]
			}
			tour._getSpObject('speaker').getInputs = function() {
				return [{ getConnectedEndpoints: function() {
					return [ mockEndpoint('out', 'filter') ]
				}}]
			}
			$('.speaker').trigger('spConnect')
			$('.filter').trigger('spConnect')
		})

		it('should wait for connections with right side endpoints', function(done) {
			tour.next = done
			tour.waitForConnections([
				['audio', 'filter.in'],
				['filter', 'speaker.in']
			])()
			speakerEndpoints.push(mockEndpoint('out', 'filter'))
			filterEndpoints.push(mockEndpoint('out', 'audio'))
			$('.speaker').trigger('spConnect')
			$('.filter').trigger('spConnect')
		})
	})

})

