
SignalPath.ImageMapModule = function(data, canvas, prot) {
    prot = prot || {};
    var pub = SignalPath.MapModule(data, canvas, prot)
    
    var superReceiveResponse = prot.receiveResponse
    prot.receiveResponse = function(d) {
        superReceiveResponse(d)
    }
    
    return pub;
}
