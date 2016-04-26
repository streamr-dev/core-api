var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().defaultView);
var StreamrMap = require('../../streamr-map/streamr-map').StreamrMap

describe('streamr-map', function() {
	var map
	var $parent

	before(function() {
		global.$ = $
		global.window = {
			requestAnimationFrame: function(cb) {
				setTimeout(cb,0)
			}
		}
		global.L = {
			tileLayer: function(){},
			Map: function() {
				return {
					one: function(){},
					on: function(){}
				}
			},
			LatLng: function(){},
			CanvasLayer: {
				extend: function () {
					this.addTo = function () {
					}
					return this.extend
				}
			}
		}
	})

	beforeEach(function() {
		$parent = $('<div></div>')
	})

	afterEach(function() {
		map = undefined
	})

	describe('constructor', function() {
		it('should set the default values correctly', function() {
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

		it('should give the parent correct id', function() {
			map = new StreamrMap($parent)
			assert($parent.is("[id*='map-']"))
		})

		it('should set the default options correctly', function() {
			map = new StreamrMap($parent)
			assert.equal(map.options.centerLat, 35)
			assert.equal(map.options.centerLng, 15)
			assert.equal(map.options.zoom, 2)
			assert.equal(map.options.minZoom, 2)
			assert.equal(map.options.maxZoom, 18)
			assert.equal(map.options.traceRadius, 2)
			assert.equal(map.options.drawTrace, false)
		})

		it('should set the given options correctly', function() {
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

		it('should create the baselayer correctly', function(done) {
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

		it('should create the map correctly', function(done) {
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
					one: function(){},
					on: function(){}
				}
			}
			map = new StreamrMap($parent, {
				zoom: 0,
				minZoom: 0,
				maxZoom: 0
			})
		})

		it('should set the event handlers to map correctly', function(done) {
			var superGetCenterAndZoom = StreamrMap.prototype.getCenterAndZoom
			StreamrMap.prototype.getCenterAndZoom = function(){
				return "test"
			}
			global.L.Map = function(parent, opt) {
				return $('<div></div>')
			}
			map = new StreamrMap($parent)
			$(map).on("move", function(e, data) {
				assert.equal(map.untouched, false)
				assert.equal(data, "test")
				StreamrMap.prototype.getCenterAndZoom = superGetCenterAndZoom
				done()
			})
			map.map.trigger('zoomstart')
			map.map.trigger('moveend')
		})

		it('should not call createLinePointLayer if !options.drawTrace', function(done) {
			var superCreateLinePointLayer = StreamrMap.prototype.createLinePointLayer
			StreamrMap.prototype.createLinePointLayer = function() {
				assert(false)
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

		it('must set updates to either pendingLineUpdates or allLineUpdates correctly', function() {
			assert(false)
		})

		it('must call renderCircle for all the updates', function() {
			assert(false)
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
		describe('if options.autoZoom === true && untouched === true', function() {
			it('must call setCenter', function() {
				assert(false)
			})
			it('must call map.setZoom', function() {
				assert(false)
			})
			it('must set untouched to false', function() {
				assert(false)
			})
		})
		it('must call moveMarker if the marker already exists', function() {
			assert(false)
		})
		it('must create a new marker if the id does not exist', function() {
			assert(false)
		})
		it('must call addLinePoint if options.drawTrace === true', function() {
			assert(false)
		})
	})

	describe('createMarker', function() {
		beforeEach(function() {
			global.L.divIcon = function(attr) {
				assert.equal(attr.iconSize[0], 19)
				assert.equal(attr.iconSize[1], 48)
				assert.equal(attr.iconAnchor[0], 14)
				assert.equal(attr.iconAnchor[1], 53)
				assert.equal(attr.popupAnchor[0], 0)
				assert.equal(attr.popupAnchor[1], -41)
				assert.equal(attr.className, 'streamr-map-icon fa fa-map-marker fa-4x')
				return $("<div></div>")
			}
			global.L.marker = function(latlng, attr) {
				this.bindPopup = function(content, opt) {
					assert.equal(content, "<span style='text-align:center;width:100%'><span>"+id+"</span></span>")
					assert.equal(opt.closeButton, false)
				}
				return attr.icon
			}
		})
		it('must create marker with the right data', function() {

		})
		it('must bind popup to marker', function() {

		})
		it('must call openPopup on "mouseover"', function() {

		})
		it('must call closePopup on "mouseout"', function() {

		})
		it('must call add marker to map', function() {

		})
	})

})