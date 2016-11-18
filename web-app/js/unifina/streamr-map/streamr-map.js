
(function(exports) {

    var TRACE_REDRAW_BATCH_SIZE = 10000

    // default non-directional marker icon
    var DEFAULT_MARKER_ICON = "fa fa-map-marker fa-4x"

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

    function StreamrMap(parent, options) {

        var _this = this

        this.parent = $(parent)

        this.untouched = true

        this.markers = {}
        this.pendingMarkerUpdates = {}
        this.pendingLineUpdates = []
        this.allLineUpdates = []

        this.animationFrameRequested = false

        // Default options
        this.options = $.extend({}, {
            centerLat: 35,
            centerLng: 15,
            zoom: 2,
            minZoom: 2,
            maxZoom: 18,
            traceRadius: 2,
            drawTrace: false,
            skin: "default",
            markerIcon: DEFAULT_MARKER_ICON
        }, options || {})

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

        if (!this.parent.attr("id"))
            this.parent.attr("id", "map-"+Date.now())

        this.skin = skins[this.options.skin] || skins.default

        this.baseLayer = L.tileLayer(
            this.skin.layerUrl, {
                attribution: this.skin.layerAttribution,
                minZoom: _this.options.minZoom,
                maxZoom: _this.options.maxZoom
            }
        )

        this.map = new L.Map(this.parent[0], {
            center: new L.LatLng(this.options.centerLat, this.options.centerLng),
            zoom: this.options.zoom,
            minZoom: this.options.minZoom,
            maxZoom: this.options.maxZoom,
            layers: [this.baseLayer]
        })

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

        if(this.options.drawTrace)
            this.lineLayer = this.createLinePointLayer()
    }

    StreamrMap.prototype.createLinePointLayer = function() {
        var _this = this
        this.circles = []
        var LinePointLayer = L.CanvasLayer.extend({
            renderCircle: function(ctx, point, radius, color) {
                color = color || 'rgba(255,0,0,1)'
                ctx.fillStyle = color;
                ctx.strokeStyle = color;
                ctx.beginPath();
                ctx.arc(point.x, point.y, radius, 0, Math.PI * 2.0, true, true);
                ctx.closePath();
                ctx.fill();
                ctx.stroke();
                _this.circles.push(ctx)
            },

            render: function(changesOnly) {
                var bigPointLayer = this
                var canvas = this.getCanvas();
                var ctx = canvas.getContext('2d');

                var updates
                if (changesOnly) {
                    updates = _this.pendingLineUpdates
                }
                else {
                    updates = _this.allLineUpdates
                    // clear canvas
                    ctx.clearRect(0, 0, canvas.width, canvas.height);
                }

                if(!changesOnly)
                    clearTimeout(_this.traceRedrawTimeout)

                var i = 0
                function redrawTrace() {
                    _this.traceRedrawTimeout = setTimeout(function() {
                        var count = i + TRACE_REDRAW_BATCH_SIZE
                        while (i < count && i < updates.length) {
                            var point = bigPointLayer._map.latLngToContainerPoint(updates[i].latlng);
                            bigPointLayer.renderCircle(ctx, point, _this.options.traceRadius, updates[i].color)
                            i++
                        }
                        if (i < updates.length)
                            redrawTrace()
                    })
                }
                redrawTrace()

                _this.pendingLineUpdates = []
            }
        })

        var layer = new LinePointLayer().addTo(this.map)
        return layer
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
        // Needed for linePoints
        var color = attr.color
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
        if(this.options.drawTrace)
            this.addLinePoint(id, lat, lng, color)

        return marker
    }
    
    StreamrMap.prototype.setAutoZoom = function(lat, lng) {
        var _this = this

        this.autoZoomBounds.lat.min = Math.min(lat, _this.autoZoomBounds.lat.min)
        this.autoZoomBounds.lat.max = Math.max(lat, _this.autoZoomBounds.lat.max)
        this.autoZoomBounds.lng.min = Math.min(lng, _this.autoZoomBounds.lng.min)
        this.autoZoomBounds.lng.max = Math.max(lng, _this.autoZoomBounds.lng.max)

        this.lastEvent = [
            [_this.autoZoomBounds.lat.max, _this.autoZoomBounds.lng.min],
            [_this.autoZoomBounds.lat.min, _this.autoZoomBounds.lng.max]
        ]

        if (this.autoZoomTimeout === undefined) {
            this.autoZoomTimeout = setTimeout(function() {
                _this.autoZoomTimeout = undefined
                _this.map.fitBounds(_this.lastEvent)
            }, 1000)
        }
    }

    StreamrMap.prototype.createMarker = function(id, label, latlng, rotation) {
        this.latlng = latlng
        var marker = this.options.directionalMarkers ? L.marker(latlng, {
            icon: L.divIcon({
                iconSize:     [19, 48], // size of the icon
                iconAnchor:   [10, 10], // point of the icon which will correspond to marker's location
                popupAnchor:  [0, -41], // point from which the popup should open relative to the iconAnchor,
                className: 'streamr-map-icon ' + this.options.markerIcon
            })
        }) : L.marker(latlng, {
            icon: L.divIcon({
                iconSize:     [19, 48], // size of the icon
                iconAnchor:   [13.5, 43], // point of the icon which will correspond to marker's location
                popupAnchor:  [0, -41], // point from which the popup should open relative to the iconAnchor,
                className: 'streamr-map-icon fa fa-map-marker fa-4x'
            })
        })
        marker.bindPopup("<span>"+label+"</span>", {
            closeButton: false,
        })
        marker.on("mouseover", function() {
            marker.openPopup()
        })
        marker.on("mouseout", function() {
            marker.closePopup()
        })
        marker.addTo(this.map)
        return marker
    }

    StreamrMap.prototype.moveMarker = function(id, lat, lng, rotation) {
        this.oldLatlng = this.latlng
        this.latlng = L.latLng(lat, lng)
        var update = { latlng: this.latlng }
        if (this.options.directionalMarkers) {
            if (rotation) {
                update.rotation = rotation
            } else if (this.oldLatlng) {
                // if "heading" input isn't connected,
                //   rotate marker so that it's "coming from" the previous latlng
                var oldP = this.map.project(this.oldLatlng)
                var newP = this.map.project(this.latlng)
                var dx = newP.x - oldP.x
                var dy = newP.y - oldP.y
                if (Math.abs(dx) > 0.0001 || Math.abs(dy) > 0.0001) {
                    update.rotation = Math.atan2(dx, -dy) / Math.PI * 180;
                }
            }
        }

        this.pendingMarkerUpdates[id] = update
        this.requestUpdate()
    }

    StreamrMap.prototype.requestUpdate = function() {
        if (!this.animationFrameRequested) {
            L.Util.requestAnimFrame(this.animate, this, true);
            this.animationFrameRequested = true;
        }
    }

    StreamrMap.prototype.animate = function() {
        var _this = this
        Object.keys(this.pendingMarkerUpdates).forEach(function(id) {
            var update = _this.pendingMarkerUpdates[id]

            // Update marker position
            var marker = _this.markers[id]
            marker.setLatLng(update.latlng)
            if (update.rotation) {
                marker.setRotationAngle(update.rotation)
            }
        })

        if(this.lineLayer)
            this.lineLayer.render(true)

        this.pendingMarkerUpdates = {}
        this.animationFrameRequested = false
    }

    StreamrMap.prototype.addLinePoint = function(id, lat, lng, color) {
        var latlng = L.latLng(lat,lng)
        var update = {
            latlng: latlng,
            color: color
        }
        this.pendingLineUpdates.push(update)
        this.allLineUpdates.push(update)
    }

    StreamrMap.prototype.handleMessage = function(d) {
        if(d.t && d.t == "p") {
            this.addMarker(d)
        }
    }

    StreamrMap.prototype.resize = function(width, height) {
        this.parent.css("width", width+"px")
        this.parent.css("height", height+"px")
        this.map.invalidateSize()
        if(this.options.drawTrace)
            this.lineLayer.redraw()
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
        if(this.circles && this.circles.length) {
            var ctx = this.circles[0]
            ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
            this.circles = []
        }

        this.autoZoomBounds = this.defaultAutoZoomBounds

        this.markers = {}
        this.pendingMarkerUpdates = {}
        this.pendingLineUpdates = []
        this.allLineUpdates = []
    }

    exports.StreamrMap = StreamrMap

})(typeof(exports) !== 'undefined' ? exports : window)
