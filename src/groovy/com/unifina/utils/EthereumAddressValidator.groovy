package com.unifina.utils

class EthereumAddressValidator {
	static validate = { String address ->
		if (address == null) {
			return false
		}
		return address.matches("^0x[a-fA-F0-9]{40}\$")
	}
}
