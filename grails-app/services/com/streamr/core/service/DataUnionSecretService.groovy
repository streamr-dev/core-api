package com.streamr.core.service

import com.streamr.core.domain.DataUnionSecret
import com.streamr.core.utils.IdGenerator

class DataUnionSecretService {
	IdGenerator generator = new IdGenerator()

	List<DataUnionSecret> findAll(String contractAddress) {
		return DataUnionSecret.findAllWhere(contractAddress: contractAddress)
	}

	DataUnionSecret create(String contractAddress, DataUnionSecretCommand cmd) {
		DataUnionSecret result = new DataUnionSecret()
		result.contractAddress = contractAddress
		result.name = cmd.name
		result.secret = cmd.secret ?: generator.generate() // Specifying a secret is deprecated
		result.save(validate: true, failOnError: true)
		return result
	}

	DataUnionSecret find(String contractAddress, String id) {
		DataUnionSecret result = DataUnionSecret.createCriteria().get {
			idEq(id)
			eq("contractAddress", contractAddress, [ignoreCase: true])
		}
		return result
	}

	DataUnionSecret update(String contractAddress, String DataUnionSecretId, DataUnionSecretCommand cmd) {
		DataUnionSecret result = DataUnionSecret.findWhere(contractAddress: contractAddress, id: DataUnionSecretId)
		if (result == null) {
			throw new NotFoundException("Secret not found")
		}
		if (cmd.name) {
			result.name = cmd.name
		}
		if (cmd.secret) {
			throw new BadRequestException("Can't change the secret, please create a new secret")
		}
		result.save(validate: true, failOnError: true)
		return result
	}

	void delete(String contractAddress, String DataUnionSecretId) {
		DataUnionSecret result = DataUnionSecret.findWhere(contractAddress: contractAddress, id: DataUnionSecretId)
		if (result == null) {
			throw new NotFoundException("Secret not found")
		}
		result.delete(flush: true)
	}
}
