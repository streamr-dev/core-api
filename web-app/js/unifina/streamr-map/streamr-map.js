// TODO: Remove this!
var MAP

(function(exports) {

    function StreamrMap(parent, options) {
        // TODO: Remove this!
        MAP = this

        var _this = this
        this.parent = $(parent)

        this.untouched = true

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

        this.markers = {}

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

        this.extLayers = []
        //this.extLayers.push(this.createTraceLayer())
    }

    //StreamrMap.prototype.createTraceLayer = function(){
    //    var _this = this
    //
    //    var paths = {};
    //    this.pathsLayer = L.featureGroup().addTo(this.map);
    //
    //    this.realtime.on('update', function(e) {
    //        for(var key in e.enter) {
    //            var pl = L.polyline([this.getLayer(key).getLatLng()])
    //            paths[key] = pl
    //            paths[key].addTo(_this.pathsLayer)
    //            if(_this.untouched) {
    //                _this.map.fitBounds(pl.getBounds(), {
    //                    maxZoom: 13
    //                })
    //                _this.untouched = false
    //            }
    //        }
    //        for(var key in e.update) {
    //            // FIXME this currently stores the pixel-level positions
    //            paths[key].addLatLng(this.getLayer(key).getLatLng());
    //        }
    //    })
    //
    //    return this.pathsLayer
    //}

    StreamrMap.prototype.setCenter = function(coords) {
        this.map.setView(new L.LatLng(coords[0],coords[1]))
    }

    StreamrMap.prototype.addMarker = function(attr) {
        var _this = this

        var id = attr.id
        var latlng = L.latLng(attr.lat, attr.long)
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
            this.markers[id] = marker
            this.map.addLayer(marker)
        } else {
            marker.setLatLng(latlng)
        }

        $(this).trigger("addPoint", id, latlng)

        return marker
    }

    StreamrMap.prototype.handleMessage = function(d) {
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
        this.markers = {}
    }

    exports.StreamrMap = StreamrMap

})(typeof(exports) !== 'undefined' ? exports : window)