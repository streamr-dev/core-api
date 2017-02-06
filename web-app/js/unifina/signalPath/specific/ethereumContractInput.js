SignalPath.EthereumContractInput = function(json, parentDiv, module, type, pub) {
    pub = pub || {};
    pub = SignalPath.Input(json, parentDiv, module, type, pub);

    var super_createDiv = pub.createDiv
    pub.createDiv = function() {
        var div = super_createDiv()
        div.bind("spConnect", function(event, output) {
            var hadContract = pub.json.value
            pub.json.value = output.module.getContract()

            // Update this module if got contract for the first time
            if (pub.json.value && !hadContract) {
                SignalPath.updateModule(pub.module)
            }
        })
        div.bind("spDisconnect", function(event, output) {
            var hadContract = pub.json.value
            delete pub.json.value

            if (hadContract && !pub.module.moduleClosed) {
                SignalPath.updateModule(pub.module)
            }
        })

        return div
    }

    return pub
}