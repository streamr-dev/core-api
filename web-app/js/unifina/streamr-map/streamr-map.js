
(function(exports) {

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
            drawTrace: false
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

        this.baseLayer = L.tileLayer(
            'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
                attribution: '© OpenStreetMap contributors, Streamr',
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

        this.map.one("dragstart click", mouseEventHandler)
        this.map._container.addEventListener("wheel", mouseEventHandler)

        this.map.on("moveend", function() {
            $(_this).trigger("move", _this.getCenterAndZoom())
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

                updates.forEach(function(update) {
                    // get center from the map (projected)
                    var point = bigPointLayer._map.latLngToContainerPoint(update.latlng);
                    bigPointLayer.renderCircle(ctx, point, _this.options.traceRadius, update.color)
                })

                _this.pendingLineUpdates = []
            }
        })

        var layer = new LinePointLayer().addTo(this.map)
        return layer
    }

    StreamrMap.prototype.setCenter = function(lat, lng) {
        this.map.setView(new L.LatLng(lat, lng))
    }

    StreamrMap.prototype.addMarker = function(attr) {
        var id = attr.id
        var label = attr.label
        var lat = attr.lat
        var lng = attr.lng
        // Needed for linePoints
        var color = attr.color
        var latlng = new L.LatLng(lat, lng)

        if(this.options.autoZoom && this.untouched) {
            this.setAutoZoom(lat, lng)
        }

        var marker = this.markers[id]
        if(marker === undefined) {
            this.markers[id] = this.createMarker(id, label, latlng)
        } else {
            this.moveMarker(id, lat, lng)
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

    StreamrMap.prototype.createMarker = function(id, label, latlng) {
        var marker = L.marker(latlng, {
            icon: L.divIcon({
                iconSize:     [19, 48], // size of the icon
                iconAnchor:   [14, 53], // point of the icon which will correspond to marker's location
                popupAnchor:  [0, -41], // point from which the popup should open relative to the iconAnchor,
                className: 'streamr-map-icon fa fa-map-marker fa-4x'
            })
        })
        var popupContent = "<span style='text-align:center;width:100%'><span>"+label+"</span></span>"
        var popupOptions = {
            closeButton: false,
        }
        marker.bindPopup(popupContent, popupOptions)
        marker.on("mouseover", function() {
            marker.openPopup()
        })
        marker.on("mouseout", function() {
            marker.closePopup()
        })
        marker.addTo(this.map)
        return marker
    }

    StreamrMap.prototype.moveMarker = function(id, lat, lng) {
        var latlng = L.latLng(lat,lng)
        this.pendingMarkerUpdates[id] = latlng
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
            var latlng = _this.pendingMarkerUpdates[id]

            // Update marker position
            _this.markers[id].setLatLng(latlng)
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