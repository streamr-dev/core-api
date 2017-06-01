
(function(exports) {

    var TRACE_REDRAW_BATCH_SIZE = 10000

    var skins = {
        default: {
            layerUrl: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
            layerAttribution: "© OpenStreetMap contributors, Streamr"
        },
        cartoDark: {
            layerUrl: "http://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png",
            layerAttribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, &copy; <a href="https://cartodb.com/attributions">CartoDB</a>, Streamr'
        },
        esriDark: {
            layerUrl: "{s}.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Reference/MapServer/tile/{z}/{y}/{x}",
            layerAttribution: "Esri, HERE, DeLorme, MapmyIndia, © OpenStreetMap contributors, Streamr"
        }
    }

    var directionalStyles = {
        "arrowhead": "fa fa-2x fa-location-arrow position-middle",
        "arrow": "fa fa-2x fa-arrow-up position-middle",
        "longArrow": "fa fa-2x fa-long-arrow-up position-middle",
    }

    var nonDirectionalStyles = {
        "pin": "fa fa-map-marker fa-4x position-top",
        "circle": "fa fa-circle fa-2x position-middle"
    }

	var getMarkerIcon = function (isDirectional, directionalStyle, nonDirectionalStyle) {
		if (isDirectional) {
			return directionalStyles[directionalStyle] || directionalStyles['arrow']
		} else {
			return nonDirectionalStyles[nonDirectionalStyle] || directionalStyles['pin']
		}
	}
	
	function StreamrMap(parent, options) {
		
		var _this = this
		
		this.parent = $(parent)
		
		this.untouched = true
		
		this.markers              = {}
        this.queuedMessages       = []
		this.pendingMarkerUpdates = {}
		this.pendingLineUpdates   = []
		
		this.animationFrameRequested = false
		
		// Default options
		this.options = $.extend({}, {
			centerLat: 35,
			centerLng: 15,
			zoom: 2,
			minZoom: 2,
			maxZoom: 18,
			traceWidth: 2,
			drawTrace: false,
			skin: "default",
            directionalMarkers: false,
            directionalMarkerIcon: 'arrow',
            markerIcon: 'pin'
		}, options || {})

        this.iconClass = getMarkerIcon(this.options.directionalMarkers, this.options.directionalMarkerIcon, this.options.markerIcon)

        this.defaultAutoZoomBounds = {
            lat: {
                min: Infinity,
                max: -Infinity
            },
            lng: {
                min: Infinity,
                max: -Infinity
            }
        }
        this.autoZoomBounds = this.defaultAutoZoomBounds

        if (!this.parent.attr("id")) {
            this.parent.attr("id", "map-" + Date.now())
        }

        this.skin = skins[this.options.skin] || skins.default

        this.map = new L.Map(this.parent[0], {
            center: new L.LatLng(this.options.centerLat, this.options.centerLng),
            zoom: this.options.zoom,
            minZoom: this.options.minZoom,
            maxZoom: this.options.maxZoom
        })
        
        if (!this.options.customImageUrl) {
            this.createMapLayer()
        } else {
            this.createCustomImageLayer()
        }

        var mouseEventHandler = function() {
            _this.untouched = false
        }

        this.map.once("dragstart click", mouseEventHandler)
        // For some reason, listening to 'wheel' event didn't work
        this.map._container.addEventListener("mousewheel", mouseEventHandler)
        // 'mousewheel' doesn't work in Firefox but 'DOMMouseScroll' does
        this.map._container.addEventListener("DOMMouseScroll", mouseEventHandler)

        this.map.on("moveend", function() {
            $(_this).trigger("move", {
                centerLat: _this.getCenter().lat,
                centerLng: _this.getCenter().lng,
                zoom: _this.getZoom()
            })
        })
    
        this.parent.on('resize', function() {
            _this.redraw()
        })

        if (this.options.drawTrace) {
            this.lineLayer = this.createTraceLayer()
        }
    }
    
    StreamrMap.prototype.getParent = function() {
		return this.parent
	}
    
    StreamrMap.prototype.createMapLayer = function() {
        L.tileLayer(
            this.skin.layerUrl, {
                attribution: this.skin.layerAttribution,
                minZoom: this.options.minZoom,
                maxZoom: this.options.maxZoom
            }
        ).addTo(this.map)
    }

    StreamrMap.prototype.createCustomImageLayer = function() {
        // Load image with jQuery to get width/height
        var _this = this
        $("<img/>", {
            src: this.options.customImageUrl,
            load: function() {
                _this.customImageWidth = this.width;
                _this.customImageHeight = this.height;
                console.info("Read", _this.options.customImageUrl, "with dimensions", this.width, "x", this.height)

                _this.map.options.crs = L.CRS.Simple
                _this.map.setView(L.latLng(_this.options.centerLat, _this.options.centerLng), _this.options.zoom, {
                    animate: false
                })
                var bounds = L.latLngBounds(L.latLng(_this.customImageHeight, 0), L.latLng(0, _this.customImageWidth))

                // ImageOverlay is added to overlayPane (hard-coded). This can lead to problems when using it as as map base,
                // because overlayPane (z-index 4) is drawn above the tilePane (z-index 2). See comments in createTraceLayer().
                L.imageOverlay(_this.options.customImageUrl, bounds).addTo(_this.map)

                _this.queuedMessages.forEach(function(msg) {
                    _this.handleMessage(msg)
                })
                _this.queuedMessages = []
            }
        })
    }

    StreamrMap.prototype.createTraceLayer = function() {
        var _this = this
        this.traceUpdates = {}
        this.lastLatLngs = {}
        function isInsideCanvas(point, canvas) {
            return point.x >= 0 && point.x <= canvas.width && point.y >= 0 && point.y <= canvas.height
        }
        var TraceLayer = L.CanvasLayer.extend({
            renderLines: function(canvas, ctx, updates) {
                ctx.lineWidth = _this.options.traceWidth
                ctx.strokeStyle = updates[0] && updates[0].color || 'rgba(255,0,0,1)'
                ctx.beginPath()

                updates.forEach(function(update) {
                    var from = _this.map.latLngToContainerPoint(update.fromLatLng)
                    var to = _this.map.latLngToContainerPoint(update.toLatLng)
                    if (from.x != to.x || from.y != to.y || isInsideCanvas(from, canvas) || isInsideCanvas(to, canvas) ) {
                        if (ctx.strokeStyle !== update.color) {
                            ctx.stroke()
                            ctx.beginPath()
                            ctx.strokeStyle = update.color || 'rgba(255,0,0,1)'
                        }
                        ctx.moveTo(from.x, from.y)
                        ctx.lineTo(to.x, to.y)
                    }
                })

                ctx.stroke()
            },
            
            render: function(changesOnly) {
                var linePointLayer = this
                var canvas = this.getCanvas()
                var ctx = canvas.getContext('2d')

                var updates = []
                if (changesOnly) {
                    updates = _this.pendingLineUpdates
                } else {
                    Object.keys(_this.traceUpdates).forEach(function(key) {
                        updates.push(_this.traceUpdates[key])
                    })

                    // clear canvas
                    ctx.clearRect(0, 0, canvas.width, canvas.height)
                }

                if (!changesOnly && _this.requestedAnimationFrame) {
                    L.Util.cancelAnimFrame(_this.requestedAnimationFrame)
                }
                
                function redrawTrace(i) {
                    var count = i + TRACE_REDRAW_BATCH_SIZE
                    if (updates.length) {
                        _this.requestedAnimationFrame = L.Util.requestAnimFrame(function() {
                            redrawTrace(count)
                        })
                    }
                    linePointLayer.renderLines(canvas, ctx, updates.splice(0, count))
                }
                redrawTrace(0)
    
    
                _this.pendingLineUpdates = []
            }
        })

        var traceLayer = new TraceLayer({
            zIndexOffset: 10 // the trace layer should be above other low-z-index layers in its pane (tile layers in the tilePane)
        })
        traceLayer.addTo(this.map)

        /**
         * The zoom animation doesn't really seem to work. Let's hack it so that it will hide
         * the trace layer when animation starts, and re-display it when it ends.
         * (See leaflet_canvas_layer.js)
         */
        this.map.off('zoomanim', traceLayer._animateZoom, traceLayer)
        this.map.off('zoomend', traceLayer._endZoomAnim, traceLayer)
        this.map.on('zoomanim', function(e) {
            traceLayer._canvas.style.display = 'none'
        }, traceLayer)
        this.map.on('zoomend', function() {
            traceLayer._canvas.style.display = 'block';
        }, traceLayer)

        /**
         * CanvasLayer adds the canvas to tilePane, and unfortunately that's hard-coded.
         * This leads to ordering problems, because at the same time the ImageOverlay
         * used for ImageMap base image is hard-coded to overlayPane, which is on top
         * the tilePane. We need a little hack to lift tilePane over the overlayPane.
         * Below is a summary of what is drawn in which pane, bottom-up.
         *
         * Geo map:
         * overlayPane: empty (zIndex 4 by default - don't draw anything here, it will be hidden below geo tiles)
         * tilePane: tiles and traces (now zIndex 5, default is 2)
         * markerPane: markers (zIndex 6 by default)
         *
         * Image map:
         * overlayPane: holds the image layer (zIndex 4 by default)
         * tilePane: traces (now zIndex 5, default is 2)
         * markerPane: markers (zIndex 6 by default)
         */
        this.map.getPanes().tilePane.style.zIndex = 5
        return traceLayer
    }
    
    StreamrMap.prototype.getZoom = function() {
        return this.map.getZoom()
    }

    StreamrMap.prototype.getCenter = function() {
        return this.map.getCenter()
    }

    StreamrMap.prototype.setCenter = function(lat, lng) {
        this.map.setView(new L.LatLng(lat, lng))
    }

    StreamrMap.prototype.addMarker = function(attr) {
        var id = attr.id
        var label = attr.label || attr.id
        var lat = attr.lat
        var lng = attr.lng
        var rotation = attr.dir
        var color = attr.color

        if (this.options.customImageUrl) {
            lng *= this.customImageWidth
            lat *= this.customImageHeight
        }

        var latlng = new L.LatLng(lat, lng)

        if(this.options.autoZoom && this.untouched) {
            this.setAutoZoom(lat, lng)
        }

        var marker = this.markers[id]
        if (marker === undefined) {
            this.markers[id] = this.createMarker(id, label, latlng, rotation)
        } else {
            this.moveMarker(id, lat, lng, rotation)
        }
        if(this.options.drawTrace) {
            var tracePointId = attr.tracePointId
            this.addTracePoint(id, lat, lng, color, tracePointId)
        }

        return marker
    }
    
    StreamrMap.prototype.removeMarkerById = function(id) {
        var marker = this.markers[id]
        if (marker) {
            delete this.markers[id]
            delete this.pendingMarkerUpdates[id]
            if (this.lastLatLngs) {
                delete this.lastLatLngs[id]
            }
            this.map.removeLayer(marker)
        }
    }
    
    StreamrMap.prototype.removeMarkersByIds = function(idList) {
        for (var i in idList) {
            this.removeMarkerById(idList[i])
        }
    }
    
    StreamrMap.prototype.removeTracePoints = function(tracePointIds) {
        for (var i=0; i < tracePointIds.length; ++i) {
            delete this.traceUpdates[tracePointIds[i]]
        }
        this.lineLayer.render()
    }
    
    StreamrMap.prototype.setAutoZoom = function(lat, lng) {
        var _this = this

        this.autoZoomBounds.lat.min = Math.min(lat, this.autoZoomBounds.lat.min)
        this.autoZoomBounds.lat.max = Math.max(lat, this.autoZoomBounds.lat.max)
        this.autoZoomBounds.lng.min = Math.min(lng, this.autoZoomBounds.lng.min)
        this.autoZoomBounds.lng.max = Math.max(lng, this.autoZoomBounds.lng.max)

        this.lastEvent = [
            [this.autoZoomBounds.lat.max, this.autoZoomBounds.lng.min],
            [this.autoZoomBounds.lat.min, this.autoZoomBounds.lng.max]
        ]

        if (this.autoZoomTimeout === undefined) {
            this.autoZoomTimeout = setTimeout(function() {
                _this.autoZoomTimeout = undefined
                _this.map.fitBounds(_this.lastEvent)
            }, 1000)
        }
    }

    StreamrMap.prototype.createMarker = function(id, label, latlng, rotation) {
        var marker = L.marker(latlng, {
            icon: L.divIcon({
                iconSize:     [0, 0],
                iconAnchor:   [0, 0],
                popupAnchor:  [0, -41],
				className: 'invisible-container',
				html: $('<span/>', {
					class: this.iconClass + ' streamr-map-icon',
					style: 'color:' + this.options.markerColor
				})[0].outerHTML
            })
        })
        marker.bindPopup("<span>" + label + "</span>", {
            closeButton: false,
        })
        marker.on("mouseover", function() {
            marker.openPopup()
        })
        marker.on("mouseout", function() {
            marker.closePopup()
        })
        if (rotation) {
            marker.setRotationAngle(rotation)
        }
        marker.addTo(this.map)
        return marker
    }

    StreamrMap.prototype.moveMarker = function(id, lat, lng, rotation) {
        var newLatLng = L.latLng(lat, lng)
        var update = { latlng: newLatLng }
        if (this.options.directionalMarkers) {
            if (rotation) {
                update.rotation = rotation
            } else if (this.markers[id] !== undefined) {
                // if "heading" input isn't connected,
                //   rotate marker so that it's "coming from" the previous latlng
                var oldLatLng = this.markers[id].getLatLng()
                var oldP = this.map.project(oldLatLng)
                var newP = this.map.project(newLatLng)
                var dx = newP.x - oldP.x
                var dy = newP.y - oldP.y
                if (Math.abs(dx) > 0.000001 || Math.abs(dy) > 0.000001) {
                    update.rotation = Math.atan2(dx, -dy) / Math.PI * 180;
                }
            }
        }

        this.pendingMarkerUpdates[id] = update
        this.requestUpdate()
    }

    StreamrMap.prototype.requestUpdate = function() {
        if (!this.animationFrameRequested) {
            this.animationFrameRequested = true;
            L.Util.requestAnimFrame(this.animate, this, true);
        }
    }

    StreamrMap.prototype.animate = function() {
        var _this = this
        Object.keys(this.pendingMarkerUpdates).forEach(function(id) {
            var update = _this.pendingMarkerUpdates[id]

            // Update marker position
            var marker = _this.markers[id]
            marker.setLatLng(update.latlng)
            if (update.hasOwnProperty("rotation")) {
                marker.setRotationAngle(update.rotation)
            }
        })

        if (this.lineLayer) {
            this.lineLayer.render(true)
        }

        this.pendingMarkerUpdates = {}
        this.animationFrameRequested = false
    }

    StreamrMap.prototype.addTracePoint = function(id, lat, lng, color, tracePointId) {
        var latlng = L.latLng(lat,lng)
        var lastLatLng = this.lastLatLngs[id]
        if (lastLatLng) {
            var update = {
                id: id,
                fromLatLng: latlng,
                toLatLng: lastLatLng,
                color: color,
                tracePointId: tracePointId
            }
            this.pendingLineUpdates.push(update)
            this.traceUpdates[tracePointId] = update
        }
        
        this.lastLatLngs[id] = latlng
    }

    StreamrMap.prototype.handleMessage = function(d) {
        // If the image has not yet loaded, we are not ready to handle any messages. Queue them instead
        if (this.options.customImageUrl && !(this.customImageHeight && this.customImageWidth)) {
            this.queuedMessages.push(d)
        } else if (d.t) {
            if (d.t === "p") {
                this.addMarker(d)
            } else if (d.t === "d") {
                if (d.markerList && d.markerList.length) {
                    this.removeMarkersByIds(d.markerList)
                }
                if (d.pointList && d.pointList.length) {
                    this.removeTracePoints(d.pointList)
                }
            }
        }
    }

    StreamrMap.prototype.redraw = function() {
        this.map.invalidateSize()

        if (this.options.drawTrace) {
            // map.invalidateSize() clears lineLayer, so redraw
            this.lineLayer.render()
        }
    }

    StreamrMap.prototype.toJSON = function() {
        return this.getCenterAndZoom();
    }

    StreamrMap.prototype.getCenterAndZoom = function() {
        return {
            centerLat: this.map.getCenter().lat,
            centerLng: this.map.getCenter().lng,
            zoom: this.map.getZoom()
        }
    }

    StreamrMap.prototype.clear = function() {
        var _this = this
        $.each(this.markers, function(k, v) {
            _this.map.removeLayer(v)
        })
        if(this.lineLayer) {
            var ctx = this.lineLayer.getCanvas().getContext('2d')
            ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
            this.traceUpdates = {}
            this.lastLatLngs = {}
        }

        this.autoZoomBounds = this.defaultAutoZoomBounds

        this.markers = {}
        this.pendingMarkerUpdates = {}
        this.pendingLineUpdates = []
    }

    exports.StreamrMap = StreamrMap

})(typeof(exports) !== 'undefined' ? exports : window)
