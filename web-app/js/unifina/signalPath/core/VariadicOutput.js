/**
 *
 */
SignalPath.VariadicOutput = function(json, parentDiv, module, type, pub) {
    pub = pub || {};
    pub = SignalPath.Output(json, parentDiv, module, type, pub);

    var super_createDiv = pub.createDiv
    pub.createDiv = function() {
        var div = super_createDiv()

        if (!json.variadic.disableGrow) {
            div.bind("spConnect", function(event, output) {
                if (!SignalPath.isBeingReloaded && !module.moduleClosed) {
                    console.log("connected")
                    pub.makeNewOutput()
                }
            })

            div.bind("spDisconnect", function(event, output) {
                if (!SignalPath.isBeingReloaded && !module.moduleClosed) {
                    console.log("disconnected")
                    module.removeOutput(json.name)
                }
            })
        }

        if (json.variadic.isLast && json.variadic.disableGrow) {
            console.log("Hiding last output")
            div.css('display', 'none')
        }

        return div
    }

    pub.makeNewOutput = function() {
        if (json.variadic.isLast) {
            var jsonCopy = jQuery.extend(true, {}, json) // deep-copy object

            json.variadic.isLast = false
            json.connected = true

            jsonCopy.connected = false
            delete jsonCopy.id
            delete jsonCopy.longName
            delete jsonCopy.sourceId
            jsonCopy.targets = []
            jsonCopy.name = "endpoint" + Date.now()
            jsonCopy.displayName = "out" + (json.variadic.index + 1)
            jsonCopy.variadic.isLast = true
            jsonCopy.variadic.index += 1
            return module.addOutput(jsonCopy)
        }
    }

    return pub;
}