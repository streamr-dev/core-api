SignalPath.VariadicContext = function(config, module) {
    var prot = {};
    var pub = {};

    // Read config
    prot.baseName = config.baseName;
    prot.numOfEndpoints = config.numOfEndpoints;
    prot.startIndex = config.startIndex;
    prot.linkedTo = config.linkedTo || null;
    prot.disableGrowOnConnection = config.disableGrowOnConnection || null;
    if (prot.baseName === undefined || prot.numOfEndpoints === undefined || prot.startIndex === undefined) {
        throw "Variadic not fully configured: " + prot.baseName + ", " + prot.numOfEndpoints + ", " + prot.startIndex;
    }

    // Register endpoints as well as collect names and indices
    prot.endpoints = [];

    for (var i=0; i < prot.numOfEndpoints; ++i) {
        var name = formEndpointName(i);
        var endpoint = module.getInput(name) || module.getOutput(name);
        if (!endpoint) {
            throw "Endpoint not found for: " + name;
        }
        register(endpoint);
    }
    getLastEndpoint().markPlaceholder(prot.disableGrowOnConnection);

    function getIndexOfEndpoint(endpoint) {
        return _.findIndex(prot.endpoints, function(ep) {
            return ep.getName() === endpoint.getName()
        });
    }

    function getLastEndpoint() {
        return prot.endpoints[prot.endpoints.length - 1];
    }

    function isLastEndpoint(endpoint) {
        return prot.endpoints[prot.endpoints.length - 1].getName() === endpoint.getName();
    }

    function formEndpointName(idx) {
        return prot.baseName + (prot.startIndex + idx);
    }

    function register(endpoint) {
        prot.endpoints.push(endpoint);
        endpoint.registerToVariadic(pub);
    }

    function notReloadingAndGrowingEnabled() {
        return !SignalPath.isLoading() && !module.getModuleClosed() && !prot.disableGrowOnConnection;
    }


    pub.getBaseName = function () {
        return prot.baseName;
    };

    pub.getEndpointForIndex = function(idx) {
        return prot.endpoints[idx];
    };

    pub.onConnectOrSetExport = function(endpoint) {
        if (notReloadingAndGrowingEnabled() && isLastEndpoint(endpoint)) {
            pub.grow();
            if (prot.linkedTo != null) {
                var linkedVariadic = module.getVariadic(prot.linkedTo);
                linkedVariadic.grow();
            }
        }
    };

    pub.onDisconnectOrUnexport = function(endpoint, div) {
        if (notReloadingAndGrowingEnabled()) {
            var idx = getIndexOfEndpoint(endpoint);
            if (!div.hasClass("export") && !div.data("spObject").isConnected()) {
                pub.shrink(endpoint);
                if (prot.linkedTo != null) {
                    var linkedVariadic = module.getVariadic(prot.linkedTo);
                    linkedVariadic.shrink(linkedVariadic.getEndpointForIndex(idx));
                }
            }
        }
    };

    pub.grow = function() {
        var endpoint = getLastEndpoint();
        var newName = formEndpointName(prot.numOfEndpoints++);
        var newEndpoint = endpoint.makeNewPlaceholder(newName);
        endpoint.unmarkPlaceholder();
        newEndpoint.markPlaceholder(prot.disableGrowOnConnection);
        register(newEndpoint);
    };

    pub.shrink = function(endpoint) {
        var idx = getIndexOfEndpoint(endpoint);
        prot.endpoints.splice(idx, 1);
        endpoint.deleteMe();
    };

    pub.toJSON = function() {
        for (var i=0; i < prot.endpoints.length; ++i) {
            prot.endpoints[i].setName(formEndpointName(i));
        }
        return { baseName: prot.baseName, numOfEndpoints: prot.endpoints.length }
    };

    return pub;
};