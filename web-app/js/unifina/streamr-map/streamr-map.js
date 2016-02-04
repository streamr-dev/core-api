// TODO: Remove this!
var MAP

(function(exports) {

    function StreamrMap(parent, options) {
        // TODO: Remove this!
        MAP = this

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
            min: 0,
            max: 20,
            center: [35,15],
            zoom: 2,
            minZoom: 2,
            maxZoom: 18
        }, options || {})

        if (!this.parent.attr("id"))
            this.parent.attr("id", "map-"+Date.now())

        this.baseLayer = L.tileLayer(
            'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
                attribution: '© OpenStreetMap contributors, Streamr',
                minZoom: this.options.minZoom,
                maxZoom: this.options.maxZoom
            }
        )

        this.map = new L.Map(this.parent[0], {
            center: new L.LatLng(this.options.center[0], this.options.center[1]),
            zoom: this.options.zoom,
            layers: [this.baseLayer]
        })

        this.map.once("zoomstart dragstart", function() {
            _this.untouched = false
        })

        this.createBigPointLayer()


        //for (var i=0;i<1;i++) {
        //    _this.addMarker({
        //        id: i.toString(),
        //        lat: Math.random()*90,
        //        long: Math.random()*90
        //    })
        //}
        //
        //setInterval(function() {
        //    for (var i=0;i<1;i++) {
        //        var current = _this.markers[i.toString()].getLatLng()
        //        _this.handleMessage({
        //            t: "p",
        //            id: i.toString(),
        //            lat: current.lat + (Math.random() - 0.5)/1000,
        //            long: current.lng + (Math.random() - 0.5)/1000,
        //            color: 'rgb('+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+')'
        //        })
        //    }
        //})

    }

    StreamrMap.prototype.createBigPointLayer = function() {
        var _this = this
        this.circles = []
        var BigPointLayer = L.CanvasLayer.extend({
            //initialize: function() {
            //    this.cC = []
            //},
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
                // TODO: remove this
                console.log("Render called")
                var start = Date.now()
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
                    bigPointLayer.renderCircle(ctx, point, 3, update.color)
                })

                console.log("Render took "+ (Date.now()-start)+"ms, pendingLineUpdates.length: "+_this.pendingLineUpdates.length)
                _this.pendingLineUpdates = []
            }
        })

        this.lineLayer = new BigPointLayer().addTo(this.map)
        return this.lineLayer
    }

    StreamrMap.prototype.setCenter = function(coords) {
        this.map.setView(new L.LatLng(coords[0],coords[1]))
    }

    StreamrMap.prototype.addMarker = function(attr) {
        var _this = this

        var id = attr.id
        var lat = attr.lat
        var lng = attr.long
        var color = attr.color
        var latlng = new L.LatLng(lat, lng)

        if(this.untouched) {
            this.setCenter([lat,lng])
            this.untouched = false
        }

        var marker = this.markers[id]
        if(marker === undefined) {
            var marker = L.marker(latlng, {
                'icon': L.divIcon({
                    iconSize:     [19, 48], // size of the icon
                    iconAnchor:   [11, 47], // point of the icon which will correspond to marker's location
                    popupAnchor:  [-4, -38], // point from which the popup should open relative to the iconAnchor,
                    className: "streamr-map-icon fa fa-map-marker fa-4x",
                })
            })
            var popupContent = "<span style='text-align:center;width:100%'><span>"+id+"</span></span>"
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
            this.markers[id] = marker
            this.addLinePoint(id, lat, lng, color)
        } else {
            this.moveMarker(id, lat, lng, color)
        }

        return marker
    }

    StreamrMap.prototype.moveMarker = function(id, lat, lng, color) {
        var latlng = L.latLng(lat,lng)
        this.pendingMarkerUpdates[id] = latlng
        this.addLinePoint(id, lat, lng, color)
        this.requestUpdate()
    }

    StreamrMap.prototype.requestUpdate = function() {
        if (!this.animationFrameRequested) {
            this.requestUpdateStart = Date.now()
            console.log("RequestUpdate called")
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

        this.lineLayer.render(true)

        this.pendingMarkerUpdates = {}
        this.animationFrameRequested = false
        console.log("Animate, took "+(Date.now() - this.requestUpdateStart))
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
    var counter = 0
    setInterval(function() {
        console.log(counter+ " msg/sec")
        counter = 0
    },1000)
    StreamrMap.prototype.handleMessage = function(d) {
        counter++

        if(d.t && d.t == "p") {
            this.addMarker({
                id: d.id,
                lat: d.lat,
                long: d.long
            })
        }
    }

    StreamrMap.prototype.resize = function(width, height) {
        this.parent.css("width", width+"px")
        this.parent.css("height", height+"px")
        this.map.invalidateSize()
    }

    StreamrMap.prototype.clear = function() {
        var _this = this
        $.each(this.markers, function(k, v) {
            _this.map.removeLayer(v)
        })
        if(this.circles.length) {
            var ctx = this.circles[0]
            ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
        }
        this.circles = []
        this.markers = {}
    }

    exports.StreamrMap = StreamrMap

})(typeof(exports) !== 'undefined' ? exports : window)