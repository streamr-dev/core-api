SignalPath.CanvasModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.SubCanvasModule(data,canvas,prot)

    var canvasSelectorTemplate = '<button class="btn btn-default btn-sm btn-block view-canvas-button" type="button"><i class="fa fa-search"></i> View Canvas</button>'

    prot.createSubCanvasControls = function(runtimeJson) {
        var button = $(canvasSelectorTemplate)

        button.click(function() {
            prot.loadSubCanvas(pub.getURL())
        })

        return button
    }

    return pub;
}
