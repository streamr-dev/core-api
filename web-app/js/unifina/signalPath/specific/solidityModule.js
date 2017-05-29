SignalPath.SolidityModule = function(data,canvas,prot) {
	prot = prot || {}
	var pub = SignalPath.CustomModule(data,canvas,prot)
	
	var super_compile = prot.compile
	prot.compile = function(callback) {
		// Slight hack: set temporary flag in json to communicate command to backend
		prot.jsonData.compile = true
		super_compile(function(data, err) {
			if (callback) {
				callback(data, err)
			}
		})
		delete prot.jsonData.deploy
	}

	var super_createModuleFooter = prot.createModuleFooter
	prot.createModuleFooter = function() {
		var footer = super_createModuleFooter()

        if (!pub.getContract) { return }

		if (pub.getContract() && pub.getContract().address) {
            var $addressField = $("<input style='width:100%' value='" + pub.getContract().address +"'>")
            $addressField.on("change", function () {
                var address = $addressField.val()
                if (address.length === 42 && address.slice(0, 2) === "0x") {
                    prot.jsonData.contract.address = address
                }
            })
			footer.append($addressField)
		} else {
			var deployButton = $("<button class='btn btn-block btn-default btn-sm'>Deploy</button>");
			deployButton.click(function() {
				deployButton.html("<i class='fa fa-spinner fa-pulse'></i>")
				prot.jsonData.deploy = true
				SignalPath.updateModule(pub)
				delete prot.jsonData.deploy
			});
			footer.append(deployButton);
		}

		return footer
	}

	return pub
}