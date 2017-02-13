
SignalPath.ImageMapModule = function(data, canvas, prot) {
    prot = prot || {};
    var pub = SignalPath.MapModule(data, canvas, prot)
    
    var superReceiveResponse = prot.receiveResponse
    prot.receiveResponse = function(d) {
        if (d.lat && d.lng) {
            d.lng *= prot.jsonData.options.customImageWidth.value
            d.lat *= prot.jsonData.options.customImageHeight.value
        }
        superReceiveResponse(d)
    }
    
    return pub;
}
