SignalPath.SolidityModule = function(data,canvas,prot) {
	prot = prot || {}
	var pub = SignalPath.CustomModule(data,canvas,prot)

	var super_createModuleFooter = prot.createModuleFooter
	prot.createModuleFooter = function() {
		var $footer = super_createModuleFooter()
		if (pub.getContract() && pub.getContract().address) {
			$footer.append("<p>" + pub.getContract().address +"</p>")
		}
	}

	pub.getContract = prot.getContract = function() {
		return prot.jsonData.contract
	}

	return pub
}