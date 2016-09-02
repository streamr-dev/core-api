SignalPath.ForEachModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.SubCanvasModule(data,canvas,prot)

    var canvasSelectorTemplate =
        '<div>'+
            '<label for="subcanvases">Sub-Canvases</label>'+
            '<select name="subcanvases" class="form-control input-sm subcanvas-select"/>'+
            '<button class="btn btn-default btn-sm btn-block view-canvas-button" type="button"><i class="fa fa-search"></i> View Canvas</button>'+
        '</div>'

    prot.createSubCanvasControls = function(runtimeJson) {
        var keys = Object.keys(runtimeJson.canvasesByKey || {})
        if (keys.length) {
            var canvasSelector = $(canvasSelectorTemplate)
            var select = canvasSelector.find('select')
            keys.forEach(function(key) {
                var option = $("<option/>", {
                    text: key,
                    value: key
                })
                select.append(option)
            })

            canvasSelector.find('button').click(function() {
                var key = canvasSelector.find('select').val()
                var subJson = runtimeJson.canvasesByKey[key]
                // We need to double-encode the key just in case it contains any slashes. Encoding it once won't work because many servers don't allow %2F in URLs
                var baseUrl = pub.getURL() + '/keys/' + encodeURIComponent(encodeURIComponent(key))
                prot.loadSubCanvas(subJson, baseUrl)
            })

            return canvasSelector
        }
        else {
            return $("<div/>")
        }
    }

    return pub;
}
