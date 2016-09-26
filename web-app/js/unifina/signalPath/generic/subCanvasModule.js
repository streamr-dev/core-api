/**
 * Abstraction for a module that contains subcanvases loadable at runtime
 */
SignalPath.SubCanvasModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.GenericModule(data,canvas,prot)

    function updateSubCanvasSelector() {
        if (SignalPath.isRunning()) {
            $.getJSON(pub.getRuntimeRequestURL(), {}, function(runtimeJson) {
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
        // subcanvas is adhoc if the parent is adhoc
        subJson.adhoc = parentJson.adhoc
        // subcanvas is running if the parent is running
        subJson.state = parentJson.state
        // setting the baseURL allows runtime requests to reach the subcanvas
        subJson.baseURL = baseUrl
        // subJson.id contains the wrong thing (the module domain object id)
        delete subJson.id
        SignalPath.load(subJson)
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
