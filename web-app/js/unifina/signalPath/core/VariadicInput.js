/**
 *
 */
SignalPath.VariadicInput = function(json, parentDiv, module, type, pub) {
    pub = pub || {};
    pub = SignalPath.Input(json, parentDiv, module, type, pub);

    var super_createDiv = pub.createDiv
    pub.createDiv = function() {
        var div = super_createDiv()

        div.bind("spConnect", function(event, output) {
            if (!SignalPath.isBeingReloaded && !module.moduleClosed) {
                console.log("connected")

                if (json.variadic.isLast) {
                    var jsonCopy = jQuery.extend(true, {}, json) // deep-copy object

                    json.variadic.isLast = false
                    json.requiresConnection = true // does not work
                    json.connected = true

                    jsonCopy.connected = false
                    delete jsonCopy.id
                    delete jsonCopy.longName
                    delete jsonCopy.sourceId
                    jsonCopy.name = "endpoint" + Date.now()
                    jsonCopy.displayName = "in" + (json.variadic.index + 1)
                    jsonCopy.requiresConnection = false
                    jsonCopy.variadic.isLast = true
                    jsonCopy.variadic.index += 1

                    if (json.variadic.linkedOutput) {
                        var newOutput = module.getOutput(json.variadic.linkedOutput).makeNewOutput()
                        jsonCopy.variadic.linkedOutput = newOutput.getName()
                    }

                    module.addInput(jsonCopy)
                }
            }
        })

        div.bind("spDisconnect", function(event, output) {
            if (!SignalPath.isBeingReloaded && !module.moduleClosed) {
                console.log("disconnected")
                if (json.variadic.linkedOutput) {
                    module.removeOutput(json.variadic.linkedOutput)
                }
                module.removeInput(json.name)
            }
        })

        return div
    }

    return pub;
}