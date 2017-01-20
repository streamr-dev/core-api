(function(exports) {

	function StreamrHeatMap(parent, options) {
		var _this = this
		this.parent = $(parent)

		// Default options
		this.options = $.extend({}, {
			min: 0,
			max: 20,
			centerLat: 35,
			centerLng: 15,
			zoom: 2,
			minZoom: 2,
			maxZoom: 18,
			// radius should be small ONLY if scaleRadius is true (or small radius is intended)
			// if scaleRadius is false it will be the constant radius used in pixels
			radius: 30,
			maxOpacity: .8, 
			// scales the radius based on map zoom
			scaleRadius: false, 
			// if set to false the heatmap uses the global maximum for colorization
			// if activated: uses the data maximum within the current map boundaries 
			//   (there will always be a red spot with useLocalExtremas true)
			useLocalExtrema: false,
			// which field name in your data represents the latitude - default "lat"
			latField: 'l',
			// which field name in your data represents the longitude - default "lng"
			lngField: 'g',
			// which field name in your data represents the data value - default "value"
			valueField: 'value',

			lifeTime: 7*1000,
			fadeInTime: 500,
			fadeOutTime: 6500,

		}, options || {})

		if (!this.parent.attr("id"))
			this.parent.attr("id", "heatmap-"+Date.now())

		this.data = []

		var baseLayer = L.tileLayer(
		  'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
		    attribution: '© OpenStreetMap contributors, Streamr',
		    minZoom: this.options.minZoom,
		    maxZoom: this.options.maxZoom
		  }
		);

		this.map = new L.Map(this.parent[0], {
			center: new L.LatLng(this.options.centerLat, this.options.centerLng),
			zoom: this.options.zoom,
			layers: [baseLayer]
		});

		this.heatmapLayer = this.createHeatmapLayer()

		// Workaround for https://github.com/pa7/heatmap.js/issues/160
		this.data = [{l:0,g:0,value:0}]
		this.syncData()
		this.data = []
		this.syncData()

		this.map.on("moveend", function() {
			$(_this).trigger("move", {
				centerLat: _this.getCenter().lat,
				centerLng: _this.getCenter().lng,
				zoom: _this.getZoom()
			})
		})

		// From https://github.com/pa7/heatmap.js/issues/120
		// this.map.on("resize", function() {
		// 	_this.map.removeLayer(_this.heatmapLayer)
		// 	_this.heatmapLayer = _this.createHeatmapLayer()
		// 	_this.syncData()
		// });
	}

	StreamrHeatMap.prototype.getZoom = function() {
		return this.map.getZoom()
	}

	StreamrHeatMap.prototype.getCenter = function() {
		return this.map.getCenter()
	}

	StreamrHeatMap.prototype.setCenter = function(lat, lng) {
		this.map.setView(new L.LatLng(lat, lng))
	}

	StreamrHeatMap.prototype.requestUpdate = function() {
		var _this = this
		var draw = function() {
			var dirty = _this.updateData()
			if (dirty) {
				_this.syncData()
			}

			if (_this.data.length>0 && dirty)
				_this.animationRequest = window.requestAnimationFrame(draw)
			// Check when we need to start fading out to avoid needless requestAnimationFrames
			else if (_this.data.length>0 && !dirty) {
				var timeTillFadeOut = Math.max(_this.data[0].expiresTime - _this.options.fadeOutTime - Date.now(), 0)
				_this.animationRequest = null
				setTimeout(function() {
					_this.requestUpdate()
				}, timeTillFadeOut)
			}
			// Else don't request animation anymore
			else _this.animationRequest = null
		}
		if (!this.animationRequest) {
			this.animationRequest = window.requestAnimationFrame(draw)
		}
	}

	StreamrHeatMap.prototype.updateData = function() {
		var now = Date.now()
		// Remove already-expired points
		var dirty = this.expireData(now)

		// Update fading-out values from top of array
		for (var i=0;i<this.data.length;i++) {
			var d = this.data[i]
			// Are we in fade-out phase? Already expired points have been removed
			if (d.expiresTime - now < this.options.fadeOutTime) {
				d.value = d.v * ((d.expiresTime-now) / this.options.fadeOutTime)
				// console.log("Fading out: "+d.value+" ("+d.v+")")
				dirty = true
			}
			else {
				break
			}
		}

		// Update fading-in values from tail of array
		for (var i=this.data.length-1;i>=0;i--) {
			var d = this.data[i]
			// Are we in fade-in phase?
			if (now - d.addedTime < this.options.fadeInTime) {
				d.value = d.v * ((now - d.addedTime) / this.options.fadeInTime)
				// console.log("Fading in: "+d.value+" ("+d.v+")")
				dirty = true
			}
			// Break if at full value or fading out
			else if (d.value===d.v || d.expiresTime - now < this.options.fadeOutTime) {
				break
			}
			// Else set to full value
			else d.value = d.v
		}

		return dirty
	}

	/**
	 * Synchronizes the data in this.data to the heatmap layer
	 */
	StreamrHeatMap.prototype.syncData = function() {
		var d = {
			min: this.options.min,
			max: this.options.max,
			data: this.data
		}
		this.heatmapLayer.setData(d)

		// Workaround for https://github.com/pa7/heatmap.js/issues/141
		if (this.data.length===0)
			this.heatmapLayer._heatmap.setData(d)
	}

	StreamrHeatMap.prototype.createHeatmapLayer = function() {
		var layer = new HeatmapOverlay(this.options);
		this.map.addLayer(layer)
		return layer
	}

	StreamrHeatMap.prototype.getMap = function() {
		return this.map
	}

	StreamrHeatMap.prototype.expireData = function(t) {
		var i
		for (i=0;i<this.data.length;i++) {
			if (this.data[i].expiresTime > t)
				break;
		}

		if (i>0) {
			this.data.splice(0, i)
			return true
		}
		else return false
	}

	StreamrHeatMap.prototype.addData = function(data) {
		var _this = this
		data.addedTime = Date.now()
		data.expiresTime = Date.now() + this.options.lifeTime
		//data.radius = Math.max(this.options.radius, Math.ceil(100 * (data.v / this.options.max)))
		data.value = this.options.min
		this.data.push(data)

		if (!this.animationRequest)
			this.requestUpdate()
	}

	StreamrHeatMap.prototype.handleMessage = function(msg) {
		// Data point message
		if (msg.t==="p" && msg.v>0) {
			this.addData(msg)
		}
	}

	StreamrHeatMap.prototype.clear = function() {
		this.data = []
		this.syncData()
	}

	StreamrHeatMap.prototype.redraw = function() {
		this.map.invalidateSize()
	}

	exports.StreamrHeatMap = StreamrHeatMap

})(typeof(exports) !== 'undefined' ? exports : window)