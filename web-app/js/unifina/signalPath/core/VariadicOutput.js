/**
 *
 */
SignalPath.VariadicOutput = function(json, parentDiv, module, type, pub) {
    pub = pub || {};
    pub = SignalPath.Output(json, parentDiv, module, type, pub);

    json.displayName = "out" + json.variadic.index

    var super_createDiv = pub.createDiv
    pub.createDiv = function() {
        var div = super_createDiv()

        div.bind("spConnect", function(event, output) {
            if (!SignalPath.isBeingReloaded) {
                console.log("connected")

                if (json.variadic.isLast) {
                    var jsonCopy = jQuery.extend(true, {}, json) // deep-copy object

                    json.variadic.isLast = false

                    jsonCopy.connected = false
                    delete jsonCopy.id
                    delete jsonCopy.longName
                    delete jsonCopy.sourceId
                    jsonCopy.name = "endpoint" + Date.now()
                    jsonCopy.variadic.isLast = true
                    jsonCopy.variadic.index += 1
                    module.addOutput(jsonCopy)
                }
            }
        })

        div.bind("spDisconnect", function(event, output) {
            if (!SignalPath.isBeingReloaded) {
                console.log("disconnected")
                module.removeOutput(json.name)
            }
        })

        return div
    }

    return pub;
}