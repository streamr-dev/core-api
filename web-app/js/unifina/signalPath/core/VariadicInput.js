/**
 *
 */
SignalPath.VariadicInput = function(json, parentDiv, module, type, pub) {
    pub = pub || {};
    pub = SignalPath.Input(json, parentDiv, module, type, pub);

    var growVariadic = function(div) {
        if (!SignalPath.isLoading() && !module.moduleClosed) {

            if (json.variadic.isLast) {
                var jsonCopy = jQuery.extend(true, {}, json) // deep-copy object

                json.variadic.isLast = false
                div.removeClass("last-variadic")
                json.requiresConnection = true // does not work
                json.connected = div.data("spObject").isConnected()

                jsonCopy.connected = false
                delete jsonCopy.id
                delete jsonCopy.longName
                delete jsonCopy.sourceId
                jsonCopy.name = "endpoint" + Date.now()
                jsonCopy.displayName = "in" + (json.variadic.index + 1)
                jsonCopy.requiresConnection = false
                jsonCopy.export = false
                jsonCopy.variadic.isLast = true
                jsonCopy.variadic.index += 1

                if (json.variadic.linkedOutput) {
                    var linkedOutput = module.getOutput(json.variadic.linkedOutput)
                    linkedOutput.div.css("display", "")
                    var newOutput = linkedOutput.makeNewOutput()
                    jsonCopy.variadic.linkedOutput = newOutput.getName()
                    module.redraw()
                }

                module.addInput(jsonCopy)
            }
        }
    }

    var shrinkVariadic = function(div) {
        if (!SignalPath.isLoading() && !module.moduleClosed) {
            if (json.variadic.linkedOutput) {
                module.removeOutput(json.variadic.linkedOutput)
            }
            if (!div.hasClass("export") && !div.data("spObject").isConnected()) {
                module.removeInput(json.name)
            }
        }
    }

    var super_setExport = pub.setExport
    pub.setExport = function (div, data, value) {
        super_setExport(div, data, value)
        if (value) {
            growVariadic(div)
        } else {
            shrinkVariadic(div)
        }
    }

    var super_createDiv = pub.createDiv
    pub.createDiv = function() {
        var div = super_createDiv()

        if (json.variadic.isLast) {
            div.addClass("last-variadic")
            div.find(".ioname").append("&nbsp;<i class='variadic-plus fa fa-plus'>")
        }

        div.bind("spConnect", function(event, output) {
            growVariadic(div)
        })

        div.bind("spDisconnect", function(event, output) {
            shrinkVariadic(div)
        })

        return div
    }

    return pub;
}