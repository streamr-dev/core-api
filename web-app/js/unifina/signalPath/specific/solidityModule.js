SignalPath.SolidityModule = function(data,canvas,prot) {
	prot = prot || {}
	var pub = SignalPath.CustomModule(data,canvas,prot)

	pub.getContract = prot.getContract = function() {
		return prot.jsonData.contract
	}

	var super_compile = prot.compile
	prot.compile = function(callback) {
		// Slight hack: set temporary flag in json to communicate command to backend
		prot.jsonData.compile = true
		super_compile(function(data, err) {
			delete prot.jsonData.compile
			if (callback) {
				callback(data, err)
			}
		})
	}

	var super_createModuleFooter = prot.createModuleFooter
	prot.createModuleFooter = function() {
		var footer = super_createModuleFooter()

		if (pub.getContract() && pub.getContract().address) {
			footer.append("<input style='width:100%' value='" + pub.getContract().address +"'>")
		} else {
			var deployButton = $("<button class='btn btn-block btn-default btn-sm'>Deploy</button>");
			deployButton.click(function() {
				prot.jsonData.deploy = true
				SignalPath.updateModule(pub, function(data, err) {
					delete prot.jsonData.deploy
				})
			});
			footer.append(deployButton);
		}

		return footer
	}

	return pub
}