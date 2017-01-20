/**
 *
 */
SignalPath.VariadicOutput = function(json, parentDiv, module, type, pub) {
    pub = pub || {};
    pub = SignalPath.Output(json, parentDiv, module, type, pub);

    var div;

    var growVariadic = function(div) {
        if (!SignalPath.isLoading() && !module.moduleClosed) {
            pub.makeNewOutput()
        }
    }

    var shrinkVariadic = function(div) {
        if (!SignalPath.isLoading() && !module.moduleClosed && !module.getOutput(json.name).isConnected()) {
            if (!div.hasClass("export") && !div.data("spObject").isConnected()) {
                module.removeOutput(json.name)
            }
        }
    }

    var super_setExport = pub.setExport
    pub.setExport = function (div, data, value) {
        super_setExport(div, data, value)
        if (!json.variadic.disableGrow) {
            if (value) {
                growVariadic(div)
            } else {
                shrinkVariadic(div)
            }
        }
    }

    var super_createDiv = pub.createDiv
    pub.createDiv = function() {
        div = super_createDiv()

        if (!json.variadic.disableGrow) {

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
        }

        if (json.variadic.isLast && json.variadic.disableGrow) {
            div.css('display', 'none')
        }

        return div
    }

    pub.makeNewOutput = function() {
        if (json.variadic.isLast) {
            var jsonCopy = jQuery.extend(true, {}, json) // deep-copy object

            json.variadic.isLast = false
            div.removeClass("last-variadic")
            json.connected = div.data("spObject").isConnected()

            jsonCopy.connected = false
            delete jsonCopy.id
            delete jsonCopy.longName
            delete jsonCopy.sourceId
            jsonCopy.targets = []
            jsonCopy.name = "endpoint" + Date.now()
            jsonCopy.displayName = "out" + (json.variadic.index + 1)
            jsonCopy.export = false
            jsonCopy.variadic.isLast = true
            jsonCopy.variadic.index += 1
            return module.addOutput(jsonCopy)
        }
    }

    return pub;
}