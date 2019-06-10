package com.unifina.signalpath.blockchain

import com.google.gson.JsonObject
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.signalpath.remote.AbstractHttpModule
import groovy.json.JsonSlurper
import org.apache.http.HttpResponse
import spock.lang.Specification

class EthereumCallSpec extends Specification {
	EthereumCall module

	def config = new JsonSlurper().parseText("""
		{
			"name": "EthereumCall",
			"options": "{gasPriceGWei={type=double, value=20}, trustSelfSigned={\\"type\\":\\"boolean\\",\\"value\\":false}, activateInHistoricalMode={type=boolean, value=false}, bodyContentType={\\"possibleValues\\":[{\\"text\\":\\"application/json\\",\\"value\\":\\"application/json\\"},{\\"text\\":\\"application/x-www-form-urlencoded\\",\\"value\\":\\"application/x-www-form-urlencoded\\"}],\\"type\\":\\"string\\",\\"value\\":\\"application/json\\"}, syncMode={\\"possibleValues\\":[{\\"text\\":\\"asynchronous\\",\\"value\\":\\"async\\"},{\\"text\\":\\"synchronized\\",\\"value\\":\\"sync\\"}],\\"type\\":\\"string\\",\\"value\\":\\"async\\"}, timeoutSeconds={\\"type\\":\\"int\\",\\"value\\":1800}, network={possibleValues=[{\\"text\\":\\"ropsten\\",\\"value\\":\\"ropsten\\"},{\\"text\\":\\"rinkeby\\",\\"value\\":\\"rinkeby\\"}], type=string, value=ropsten}}",
			"canRefresh": false,
			"id": 1020,
			"params": [
				{
					"possibleValues": [
					{
						"name": "(none)",
						"value": null
					},
					{
						"name": "lol",
						"value": "aBmKNOreR9moPJ9Tx7zHtQBPPiOEw-QnWoeMz5xoibUg"
					}
				],
					"canToggleDrivingInput": true,
					"defaultValue": null,
					"drivingInput": false,
					"type": "String",
					"connected": false,
					"requiresConnection": false,
					"name": "ethAccount",
					"canConnect": false,
					"id": "ep_DAm6rluCQwGQNOKEjk79gg",
					"acceptedTypes": [
					"String"
				],
					"export": false,
					"value": "aBmKNOreR9moPJ9Tx7zHtQBPPiOEw-QnWoeMz5xoibUg",
					"longName": "EthereumCall.ethAccount"
				},
				{
					"possibleValues": [
					{
						"name": "createAsset",
						"value": "createAsset"
					},
					{
						"name": "transferAsset",
						"value": "transferAsset"
					},
					{
						"name": "getAsset",
						"value": "getAsset"
					},
					{
						"name": "getAssetsOfAddress",
						"value": "getAssetsOfAddress"
					}
				],
					"canToggleDrivingInput": true,
					"defaultValue": "",
					"drivingInput": false,
					"type": "String",
					"connected": false,
					"updateOnChange": true,
					"requiresConnection": false,
					"name": "function",
					"isTextArea": false,
					"canConnect": true,
					"id": "ep_wz52CjwbQ9id6PKRjCRbIA",
					"acceptedTypes": [
					"String"
				],
					"export": false,
					"value": "getAssetsOfAddress",
					"longName": "EthereumCall.function"
				}
			],
			"jsModule": "GenericModule",
			"type": "module",
			"canClearState": true,
			"hash": 1
		}
	""")

	def response = new JsonSlurper().parseText("""

	""")

	def setup() {
		// mock the key for ethereum account
		SecUser user = new SecUser(name: "name", username: "name@name.com", password: "pass").save(failOnError: true, validate: false)
		IntegrationKey key = new IntegrationKey(service: IntegrationKey.Service.ETHEREUM, name: "test key", json: '{"privateKey":"lol","address":"0x1234"}', user: user, idInService: "0x1234")
		key.id = "sgKjr1eHQpqTmwz3vK3DqwUK1wFlrfRJa9mnf_xTeFJQ"
		key.save(failOnError: true, validate: true)

		mockBean(EthereumIntegrationKeyService.class, Stub(EthereumIntegrationKeyService) {
			getAllPrivateKeysForUser(user) >> [key]
		})

		module = new EthereumCall() {
			@Override
			JsonObject parseResponse(HttpResponse httpResponse) throws IOException {
				return
			}
		}
		module.globals = mockGlobals([:], user)
		module.init()

		module.onConfiguration(config)
	}

	def "CreateRequest"() {
	}

	def "SendOutput"() {
		def call = new AbstractHttpModule.HttpTransaction()
	}
}
