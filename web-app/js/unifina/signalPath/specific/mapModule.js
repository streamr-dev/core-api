SignalPath.MapModule = function (data, canvas, prot) {
    prot    = prot || {};
    var pub = SignalPath.UIChannelModule(data, canvas, prot)
    var container
    var map = null
    
    prot.enableIONameChange = false;
    
    prot.getMap = function() {
        return map
    }
    
    // Dragging in the chart container or the controls must not move the module
    prot.dragOptions.cancel = ".map-container"
    
    var superCreateDiv = prot.createDiv;
    prot.createDiv     = function () {
        superCreateDiv();
        
        prot.div.addClass('map-module')
        
        if (prot.jsonData.layout) {
            if (prot.jsonData.layout.width) {
                width = prot.jsonData.layout.width
            }
            if (prot.jsonData.layout.height) {
                height = prot.jsonData.layout.height
            }
        }
        
        container = $("<div/>", {
            class: 'map-container'
        })
        prot.body.append(container)
        
        prot.createMap()
        
        prot.initResizable({
            minWidth: parseInt(prot.div.css("min-width").replace("px", "")),
            minHeight: parseInt(prot.div.css("min-height").replace("px", "")),
            stop: updateSize
        });
        
        $(SignalPath).on("loaded", updateSize)
    }
    
    prot.createMap = function () {
        var mapOptions = _.mapValues(prot.jsonData.options, function (o) {
            return o.value
        })
        map = new StreamrMap(container, mapOptions)
        
        $(map).on("move", function (e, data) {
            $.each(data, function (k, v) {
                if (prot.jsonData.options[k] && prot.jsonData.options[k].value)
                    prot.jsonData.options[k].value = v
            })
        })
    }
    
    prot.getMap = function () {
        return map
    }
    
    function updateSize() {
        if (map) {
            map.redraw()
        }
    }
    
    prot.receiveResponse = function (d) {
        map.handleMessage(d)
    }
    
    var superClean = pub.clean;
    pub.clean      = function () {
        if (map)
            map.clear()
    }
    
    var super_redraw = pub.redraw
    pub.redraw       = function () {
        super_redraw()
        updateSize()
    }
    
    var superToJSON = pub.toJSON;
    pub.toJSON      = function () {
        prot.jsonData = superToJSON();
        var json      = _.mapValues(map.toJSON(), function (v) {
            return {
                value: v
            }
        })
        $.extend(true, prot.jsonData.options, json)
        return prot.jsonData
    }
    
    pub.getMap = function () {
        return map;
    }
    
    return pub;
}
