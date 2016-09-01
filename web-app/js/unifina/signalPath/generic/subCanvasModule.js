/**
 * Abstraction for a module that contains subcanvases loadable at runtime
 */
SignalPath.SubCanvasModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.GenericModule(data,canvas,prot)

    function updateSubCanvasSelector() {
        if (SignalPath.isRunning()) {
            $.getJSON(Streamr.createLink({uri: 'api/v1/canvases/' + SignalPath.getId() + '/modules/' + prot.getHash()}), {runtime: true}, function(runtimeJson) {
                var controls = prot.createSubCanvasControls(runtimeJson)
                controls.addClass("subcanvas-controls")
                prot.body.find(".subcanvas-controls").remove()
                prot.body.append(controls)
            })
        }
        else {
            prot.body.find(".subcanvas-controls").remove()
        }
    }

    prot.loadSubCanvas = function(subJson, baseUrl) {
        var parentJson = SignalPath.toJSON()
        var json = $.extend({}, parentJson, subJson)
        delete json.id
        json.state = 'running'
        json.baseURL = baseUrl
        SignalPath.load(json)
    }

    prot.createSubCanvasControls = function(runtimeJson) {
        throw "Implement me!"
    }

    var super_createDiv = prot.createDiv
    prot.createDiv = function() {
        super_createDiv()
        setTimeout(updateSubCanvasSelector)
    }

    $(SignalPath).on('started stopped', updateSubCanvasSelector)
    $(prot).on('closed', function() {
        $(SignalPath).off('started stopped', updateSubCanvasSelector)
    })

    return pub;
}
