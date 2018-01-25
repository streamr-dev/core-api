SignalPath.EthereumContractInput = function(json, parentDiv, module, type, pub) {
    pub = pub || {};
    pub = SignalPath.Input(json, parentDiv, module, type, pub);

    pub.setContract = function(contract) {
        var oldContract = pub.json.value
        pub.json.value = contract

        // Update this module if the contract has changed
        if (!_.isEqual(oldContract, contract)) {
            SignalPath.updateModule(pub.module)
        }
    }

    var super_createDiv = pub.createDiv
    pub.createDiv = function() {
        var div = super_createDiv()

        function setContractOnSourceUpdate() {
            pub.setContract(pub.getSource().module.getContract())
        }

        div.bind("spConnect", function(event, output) {
            if (!pub.module.updating && !output.module.updating) {
                pub.setContract(output.module.getContract())
            }
            $(output.module).on('updated', setContractOnSourceUpdate)
        })
        div.bind("spDisconnect", function(event, output) {
            if (!pub.module.updating && !output.module.updating) {
                pub.setContract(undefined)
            }
            $(output.module).off('updated', setContractOnSourceUpdate)
        })

        return div
    }

    return pub
}