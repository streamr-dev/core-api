var MAP
(function(exports) {

    function StreamrMap(parent, options) {
        MAP = this
        var _this = this
        this.parent = $(parent)

        // Default options
        this.options = $.extend({}, {
            min: 0,
            max: 20,
            center: [35,15],
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
            center: new L.LatLng(this.options.center[0], this.options.center[1]),
            zoom: this.options.zoom,
            layers: [baseLayer]
        });

        this.realtime = L.realtime(null, {
            start: false,
            filter: function(geojson) {
                if (_this.realtime.getFeature(_this.realtime.options.getFeatureId(geojson))) {
                    _this.realtime.options.onEachFeature(geojson, _this.realtime.getLayer(_this.realtime.options.getFeatureId(geojson)));
                    return false;
                }
                return true;
            },
            updateFeature: function(f, oldLayer, newLayer) {
                var latlng = L.GeoJSON.coordsToLatLng(f.geometry.coordinates);
                // FIXME setLatLng should still be called
                // although setPosition should not
                if (! oldLayer._icon || ! L.DomUtil.getPosition(oldLayer._icon).equals(_this.map.latLngToLayerPoint(latlng).round()))
                    oldLayer.setLatLng(latlng);
                return oldLayer;
            }
        }).addTo(this.map);

    }

    StreamrMap.prototype.handleMessage = function(d) {
        this.realtime.update({
            type: "Feature",
            geometry: {
                type: "Point",
                coordinates: [d.lat, d.long]
            },
            properties: {
                id: d.id
            }
        });
    }

    StreamrMap.prototype.update = function() {

    }

    StreamrMap.prototype.resize = function() {

    }

    exports.StreamrMap = StreamrMap

})(typeof(exports) !== 'undefined' ? exports : window)