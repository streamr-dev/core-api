var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().defaultView);
var StreamrMap = require('../../streamr-map/streamr-map').StreamrMap

describe('streamr-map', function() {
	var map
	var $parent

	beforeEach(function() {
		global.$ = $
		global.window = {
			requestAnimationFrame: function(cb) {
				setTimeout(cb,0)
			}
		}
		global.L = {
			tileLayer: function(){},
			Map: function() {
				var _this = $('<div></div>')
				_this._container = _this[0]
				_this.once = _this.one
				return _this
			},
			LatLng: function(){},
			latLng: function(){},
			CanvasLayer: {
				extend: function () {
					this.addTo = function () {
					}
					return this.extend
				}
			},
			Util: {}
		}
	})

	beforeEach(function() {
		$parent = $('<div></div>')
	})

	afterEach(function() {
		map = undefined
	})

	describe('constructor', function() {
		it('must set the default values correctly', function() {
			map = new StreamrMap($parent)
			function compare(el1, el2) {
				assert.equal(JSON.stringify(el1), JSON.stringify(el2))
			}
			compare(map.markers, {})
			compare(map.pendingMarkerUpdates, {})
			compare(map.pendingLineUpdates, [])
			compare(map.allLineUpdates, [])
			compare(map.animationFrameRequested, false)
		})

		it('must give the parent correct id', function() {
			map = new StreamrMap($parent)
			assert($parent.is("[id*='map-']"))
		})

		it('must set the default options correctly', function() {
			map = new StreamrMap($parent)
			assert.equal(map.options.centerLat, 35)
			assert.equal(map.options.centerLng, 15)
			assert.equal(map.options.zoom, 2)
			assert.equal(map.options.minZoom, 2)
			assert.equal(map.options.maxZoom, 18)
			assert.equal(map.options.traceRadius, 2)
			assert.equal(map.options.drawTrace, false)
		})

		it('must set the given options correctly', function() {
			map = new StreamrMap($parent, {
				centerLat: -45,
				zoom: -45,
				traceRadius: -45,
				drawTrace: true,
			})
			assert.equal(map.options.centerLat, -45)
			assert.equal(map.options.centerLng, 15)
			assert.equal(map.options.zoom, -45)
			assert.equal(map.options.minZoom, 2)
			assert.equal(map.options.maxZoom, 18)
			assert.equal(map.options.traceRadius, -45)
			assert.equal(map.options.drawTrace, true)
		})

		it('must create the baselayer correctly', function(done) {
			global.L.tileLayer = function(url, opt) {
				assert.equal(url, 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
				assert.equal(opt.attribution, '© OpenStreetMap contributors, Streamr')
				assert.equal(opt.minZoom, 0)
				assert.equal(opt.maxZoom, 0)
				done()
			}
			map = new StreamrMap($parent, {
				minZoom: 0,
				maxZoom: 0
			})
		})

		it('must set the bounds correctly', function() {
			map = new StreamrMap($parent)
			assert.deepEqual(map.autoZoomBounds, {
				lat: {
					min: Infinity,
					max: -Infinity
				},
				lng: {
					min: Infinity,
					max: -Infinity
				}
			})
			assert.equal(map.autoZoomBounds.lat.min, Infinity)
			assert.equal(map.autoZoomBounds.lat.max, -Infinity)
			assert.equal(map.autoZoomBounds.lng.min, Infinity)
			assert.equal(map.autoZoomBounds.lng.max, -Infinity)
		})

		it('must create the map correctly', function(done) {
			global.L.tileLayer = function() {
				return "test"
			}
			global.L.Map = function(parent, opt) {
				assert.equal(parent, $parent[0])
				assert.equal(opt.zoom, 0)
				assert.equal(opt.minZoom, 0)
				assert.equal(opt.maxZoom, 0)
				assert.equal(opt.layers, "test")
				done()
				return {
					once: function(){},
					on: function(){},
					_container: {
						addEventListener: function(){}
					}
				}
			}
			map = new StreamrMap($parent, {
				zoom: 0,
				minZoom: 0,
				maxZoom: 0
			})
		})

		it('must set untouched to false on correct events', function() {
			map = new StreamrMap($parent)
			assert(map.untouched)
			map.map.trigger("dragstart")
			assert(!map.untouched)
			map.untouched = true
			map.map.trigger("click")
			assert(!map.untouched)
			map.untouched = true
		})

		it('must call getCenterAndZoom and fire "move" event when moved', function(done) {
			map = new StreamrMap($parent)
			map.getCenterAndZoom = function(){
				return "test"
			}
			$(map).on("move", function(e, data) {
				assert.equal(data, "test")
				done()
			})
			map.map.trigger('moveend')
		})

		it('must not call createLinePointLayer if !options.drawTrace', function(done) {
			var superCreateLinePointLayer = StreamrMap.prototype.createLinePointLayer
			StreamrMap.prototype.createLinePointLayer = function() {
				
			}
			map = new StreamrMap($parent)
			StreamrMap.prototype.createLinePointLayer = function() {
				StreamrMap.prototype.createLinePointLayer = superCreateLinePointLayer
				done()
			}
			map = new StreamrMap($parent, {
				drawTrace: true
			})
		})
	})

	describe('createLinePointLayer', function() {
		beforeEach(function() {
			global.L.CanvasLayer = {
				extend: function(d) {
					var _this = this
					this.getCanvas = function(){
						this.getContext = function() {
							return {
								clearRect: function(){}
							}
						}
						return this
					}
					this._map = {
						latLngToContainerPoint: function(){}
					}
					return function() {
						$.extend(_this, d)
						this.addTo = function() {
							return _this
						}
					}
				}
			}
		})
		it('must call L.CanvasLayer.extend', function(done) {
			global.L.CanvasLayer = {
				extend: function(d) {
					done()
					var _this = this
					return function() {
						$.extend(_this, d)
						this.addTo = function() {
							return _this
						}
					}
				}
			}
			map = new StreamrMap($parent)
			var layer = map.createLinePointLayer()
		})

		it('must clear pendingLineUpdates after layer.render', function() {
			map = new StreamrMap($parent)
			var layer = map.createLinePointLayer()
			map.pendingLineUpdates.push("test", "test")
			layer.render()
			assert.equal(map.pendingLineUpdates.length, 0)
		})

		it('must call latLngToContainerPoint and renderCircle for all the updates', function(done) {
			var i = 0
			var list = [{
				latlng: 0
			},{
				latlng: 1
			},{
				latlng: 2
			},{
				latlng: 3
			}]
			global.L.CanvasLayer = {
				extend: function(d) {
					var _this = this
					this.getCanvas = function(){
						this.getContext = function() {
							return {
								clearRect: function(){},
								beginPath: function(){},
								arc: function(){},
								closePath: function(){},
								fill: function(){},
								stroke: function(){}
							}
						}
						return this
					}
					this._map = {
						latLngToContainerPoint: function(latlng) {
							return latlng
						}
					}

					return function() {
						$.extend(_this, d)
						_this.renderCircle = function(a, point) {
							assert.equal(point, i)
							i++
							if(point == 3)
								done()
						}
						this.addTo = function() {
							return _this
						}
					}
				}
			}
			map = new StreamrMap($parent)
			map.pendingLineUpdates = list
			var layer = map.createLinePointLayer()
			layer.render(true)
		})
	})

	describe('setCenter', function() {
		it('must call map.setView with new L.LatLng', function(done) {
			map = new StreamrMap($parent)
			map.map.setView = function(attr){
				assert.equal(attr.value, "test3")
				done()
			}
			global.L.LatLng = function(lat, lng) {
				assert.equal(lat, "test")
				assert.equal(lng, "test2")
				return {
					value: "test3"
				}
			}
			map.setCenter("test", "test2")
		})
	})

	describe('addMarker', function() {
		beforeEach(function() {
			map               = new StreamrMap($parent)
			map.createMarker  = function(){}
			map.moveMarker    = function(){}
			map.addTracePoint = function(){}
		})
		it('must call setAutoZoom if options.autoZoom == true && untouched == true', function(done) {
			map.setAutoZoom = function(lat, lng) {
				assert.equal(lat, 1)
				assert.equal(lng, 2)
				done()
			}
			map.options.autoZoom = true
			map.addMarker({
				lat: 1,
				lng: 2
			})
		})
		it('must call moveMarker if the marker already exists', function(done) {
			map.moveMarker = function(id) {
				assert.equal(id, "test")
				done()
			}
			map.markers["test"] = "moi"
			map.addMarker({
				id: "test",
				lat: 1,
				lng: 2
			})
		})
		it('must create a new marker if the id does not exist', function(done) {
			map.createMarker = function(id) {
				assert.equal(id, "test")
				setTimeout(function() {
					assert.equal(map.markers["test"], "moi")
					done()
				})
				return "moi"
			}
			map.addMarker({
				id: "test",
				lat: 1,
				lng: 2
			})
		})
		it('must call addTracePoint if options.drawTrace == true', function() {
			map.addTracePoint = function() {
				done()
			}
			map.addMarker({
				id: "test",
				lat: 1,
				lng: 2
			})
			map.options.drawtrace = true
			map.addMarker({
				id: "test",
				lat: 1,
				lng: 2
			})
		})
	})

	describe('setAutoZoom', function() {
		beforeEach(function() {
			map = new StreamrMap($parent)
		})
		it('must set the bounds correctly', function() {
			// Prevent from creating new timeout
			map.autoZoomTimeout = true
			var checkBounds = function(latMin, latMax, lngMin, lngMax) {
				assert.deepEqual(map.autoZoomBounds, {
					lat: {
						min: latMin,
						max: latMax
					},
					lng: {
						min: lngMin,
						max: lngMax
					}
				})
			}
			map.setAutoZoom(10, 10)
			checkBounds(10, 10, 10, 10)
			map.setAutoZoom(20, 20)
			checkBounds(10, 20, 10, 20)
			map.setAutoZoom(30, 0)
			checkBounds(10, 30, 0, 20)
			map.setAutoZoom(-10, -100)
			checkBounds(-10, 30, -100, 20)
		})
		it('must set the lastEvent correctly', function() {
			// Prevent from creating new timeout
			map.autoZoomTimeout = true
			var latMin = 0
			var latMax = 10
			var lngMin = 10
			var lngMax = 20
			map.setAutoZoom(latMin, lngMax)
			map.setAutoZoom(latMax, lngMin)
			assert.equal(map.lastEvent[0][0], latMax)
			assert.equal(map.lastEvent[0][1], lngMin)
			assert.equal(map.lastEvent[1][0], latMin)
			assert.equal(map.lastEvent[1][1], lngMax)
		})
		it('must call map.fitBounds and set autoZoomTimeout to undefined after timeout', function(done) {
			// Takes 1000ms to call
			map.map.fitBounds = function() {
				assert.equal(map.autoZoomTimeout, undefined)
				done()
			}
			map.setAutoZoom(0, 0)
		})
		it('must not set timeout if the last one is still on', function(done) {
			// Takes 1000ms to call
			map.map.fitBounds = function() {
				map.map.fitBounds = function() {
					assert(false)
				}
				assert.equal(map.autoZoomTimeout, undefined)
				done()
			}
			map.setAutoZoom(0, 0)
			map.setAutoZoom(0, 0)
			map.setAutoZoom(0, 0)
			map.setAutoZoom(0, 0)
		})
	})

	describe('createMarker', function() {
		beforeEach(function() {
			global.L.divIcon = function() {
				return $("<div></div>")
			}
			global.L.marker = function(latlng, attr) {
				var _this = attr.icon
				_this.bindPopup = function() {}
				_this.addTo = function(){}
				return _this
			}
			map = new StreamrMap($parent)
		})
		it('must create marker with the right data', function(done) {
			global.L.divIcon = function(attr) {
				assert.deepEqual(attr, {
					iconSize:     [19, 48],
					iconAnchor:   [13.5, 43],
					popupAnchor:  [0, -41],
					className: 'streamr-map-icon fa fa-map-marker fa-4x'
				})
				done()
				return $("<div></div>")
			}
			map.createMarker()
		})
		it('must bind popup to marker with right data', function(done) {
			global.L.marker = function(latlng, attr) {
				var _this = attr.icon
				_this.bindPopup = function(content, opt) {
					assert.equal(content, "<span style='text-align:center;width:100%'><span>test</span></span>")
					assert.equal(opt.closeButton, false)
					done()
				}
				_this.addTo = function(){}
				return _this
			}
			map.createMarker(0, "test")
		})
		it('must call openPopup on "mouseover"', function(done) {
			global.L.marker = function(latlng, attr) {
				var _this = attr.icon
				_this.bindPopup = function() {}
				_this.addTo = function(){}
				_this.openPopup = function() {
					done()
				}
				return _this
			}
			var marker = map.createMarker("test")
			marker.trigger("mouseover")
		})
		it('must call closePopup on "mouseout"', function(done) {
			global.L.marker = function(latlng, attr) {
				var _this = attr.icon
				_this.bindPopup = function() {}
				_this.addTo = function(){}
				_this.closePopup = function() {
					done()
				}
				return _this
			}
			var marker = map.createMarker("test")
			marker.trigger("mouseout")
		})
		it('must call add marker to map', function(done) {
			map.map = "test"
			global.L.marker = function(latlng, attr) {
				var _this = attr.icon
				_this.bindPopup = function() {}
				_this.addTo = function(map){
					assert.equal(map, "test")
					done()
				}
				return _this
			}
			map.createMarker()
		})
	})

	describe('moveMarker', function() {
		beforeEach(function() {
			map = new StreamrMap($parent)
			map.requestUpdate = function(){}
		})
		it('must create new latLng', function(done) {
			global.L.latLng = function(a, b) {
				assert.equal(a, 0)
				assert.equal(b, 1)
				done()
			}
			map.moveMarker("a", 0, 1)
		})
		it('must add the latLng to pendingMarkerUpdates', function() {
			global.L.latLng = function(a, b) {
				return {
					value: "test"
				}
			}
			map.moveMarker("a", 0, 1)
			assert.equal(map.pendingMarkerUpdates.a.value, "test")
		})
		it('must requestUpdate', function(done) {
			map.requestUpdate = function() {
				done()
			}
			map.moveMarker()
		})
	})

	describe('requestUpdate', function() {
		beforeEach(function() {
			map = new StreamrMap($parent)
			map.animate = function(){}
		})
		it('must do nothing if animationFrameRequested == true', function() {
			map.animationFrameRequested = true
			global.L.Util.requestAnimFrame = function() {
				assert(false)
			}
			map.requestUpdate()
		})
		describe('if animationFrameRequested == false', function() {
			it('must call L.util.requestAnimFrame', function(done) {
				global.L.Util.requestAnimFrame = function() {
					done()
				}
				map.requestUpdate()
			})
			it('must set animationFrameRequested to true', function() {
				global.L.Util.requestAnimFrame = function() {}
				map.requestUpdate()
				assert.equal(map.animationFrameRequested, true)
			})
		})
	})

	describe('animate', function() {
		beforeEach(function() {
			map = new StreamrMap($parent)
		})
		it('must call marker.setLatLng for all the pendingMarkerUpdates', function(done) {
			map.pendingMarkerUpdates = {
				a: 0,
				b: 1,
				d: 3
			}
			var called = 0
			function marker(id) {
				this.id = id
				this.setLatLng = function(latlng) {
					if(latlng == 2)
						assert(false)
					else
						called++
					if(called == 3)
						done()
				}
			}
			map.markers = {
				a: new marker(0),
				b: new marker(1),
				c: new marker(2),
				d: new marker(3)
			}
			map.animate()
		})
		it('must call lineLayer.render if lineLayer != undefined', function(done) {
			map.lineLayer = {
				render: function() {
					done()
				}
			}
			map.animate()
		})
		it('must set pendingMarkerUpdates to {}', function() {
			map.markers = {
				a: {
					setLatLng: function(){}
				}
			}
			map.pendingMarkerUpdates = {
				a: 0
			}
			map.animate()
			assert.deepEqual(map.pendingMarkerUpdates, {})
		})
		it('must set animationFrameRequested to false', function() {
			map.animationFrameRequested = true
			map.animate()
			assert.equal(map.animationFrameRequested, false)
		})
	})

	describe('addTracePoint', function() {
		beforeEach(function() {
			map = new StreamrMap($parent)
		})
		it('must create new latLng', function(done) {
			global.L.latLng = function(a, b) {
				assert.equal(a, 0)
				assert.equal(b, 1)
				done()
			}
			map.addTracePoint("a", 0, 1, "b")
		})
		it('must push correct update object to pendingLineUpdates and allLineUpdates', function() {
			global.L.latLng = function() {
				return "test"
			}
			map.addTracePoint("a", 0, 1, "b")
			assert.equal(map.pendingLineUpdates[0].latlng, "test")
			assert.equal(map.pendingLineUpdates[0].color, "b")
			assert.equal(map.allLineUpdates[0].latlng, "test")
			assert.equal(map.allLineUpdates[0].color, "b")
		})
	})

	describe('handleMessage', function() {
		beforeEach(function() {
			map = new StreamrMap($parent)
		})
		it('must call addMarker(d) if d.t == "p"', function(done) {
			map.addMarker = function(d) {
				assert.equal(d.a, "test")
				done()
			}
			map.handleMessage({
				a: "test",
				t: "p"
			})
		})
		it('must do nothing if d.t != "p"', function() {
			map.addMarker = function() {
				assert(false)
			}
			map.handleMessage({
				a: "test"
			})
		})
	})

	describe('redraw', function() {
		it('must call map.invalidateSize', function(done) {
			map = new StreamrMap($parent)
			map.map.invalidateSize = function() {
				done()
			}
			map.redraw()
		})
		it('must call lineLayer.redraw()', function (done) {
			map = new StreamrMap($parent)
			map.map.invalidateSize = function(){}
			map.lineLayer = {}
			map.lineLayer.redraw = function() {
				done()
			}
			map.redraw()
		})
	})

	describe('toJSON', function() {
		it('must return getCenterAndZoom', function() {
			map = new StreamrMap($parent)
			map.getCenterAndZoom = function() {
				return "test"
			}
			assert.equal(map.toJSON(), "test")
		})
	})

	describe('getCenterAndZoom', function() {
		it('must return right kind of object', function() {
			map = new StreamrMap($parent)
			map.map.getCenter = function() {
				return {
					lat: "lat",
					lng: "lng"
				}
			}
			map.map.getZoom = function() {
				return 0
			}
			var json = map.getCenterAndZoom()
			assert.equal(json.centerLat, "lat")
			assert.equal(json.centerLng, "lng")
			assert.equal(json.zoom, 0)
		})
	})

	describe('clear', function() {
		beforeEach(function() {
			map = new StreamrMap($parent)
			map.map.removeLayer = function(){}
		})
		it('must call removeLayer for all the markers', function(done) {
			var i = 0
			map.markers = {
				a: 0,
				b: 1,
				c: 2,
				d: 3
			}
			map.map.removeLayer = function(v) {
				assert.equal(v, i)
				if(i == 3)
					done()
				else
					i++
			}
			map.clear()
		})
		it('must empty circles (if exists)', function() {
			map.circles = [{
				clearRect: function(){},
				canvas: {}
			}]
			map.clear()
			assert.deepEqual(map.circles, [])
		})
		it('must call clearRect for circles[0]', function(done) {
			map.circles = [{
				clearRect: function(){
					done()
				},
				canvas: {}
			}, {
				clearRect: function(){
					assert(false)
				},
				canvas: {}
			}]
			map.clear()
		})
		it('must set bounds back to original', function() {
			map.autoZoomBounds = "adfasdfasdf"
			map.clear()
			assert.deepEqual(map.autoZoomBounds, {
				lat: {
					min: Infinity,
					max: -Infinity
				},
				lng: {
					min: Infinity,
					max: -Infinity
				}
			})
		})
		it('must set all the other stuff back to empty', function() {
			map.markers.a = "stuff"
			map.pendingMarkerUpdates.a = "stuff"
			map.pendingLineUpdates.push("stuff")
			map.allLineUpdates.push("stuff")
			map.clear()
			assert.deepEqual(map.markers, {})
			assert.deepEqual(map.pendingMarkerUpdates, {})
			assert.deepEqual(map.pendingLineUpdates, [])
			assert.deepEqual(map.allLineUpdates, [])
		})
	})
})