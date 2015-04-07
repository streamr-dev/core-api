<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="jquery"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-widget" attributes="channel resendAll resendLast">
	<template>
		<streamr-client id="client"></streamr-client>
		<div id="container"></div>
	</template>
	
	<script>
		Polymer('streamr-widget',{
			publish: {
				channel: undefined,
				resendLast: undefined,
				resendAll: undefined
			},
			subscribe: function(messageHandler, resendOptions) {
				var _this = this

				var trySubscribe = function() {
					if(_this.$.client.streamrClient) {
						var client = _this.$.client.streamrClient
						client.subscribe(
						    _this.channel, 
						    messageHandler,
						    resendOptions
						)
					}
					else {
						setTimeout(trySubscribe, 200)
					}
				}
				trySubscribe()
			},
			getResendOptions: function(json) {
				json = json || {}

				var resendOptions = $.extend(
					// By default, resend all
					{
						resend_all: true
					},
					// Default can be overridden by module options
					{
						resend_all: (json.options && json.options.uiResendAll ? json.options.uiResendAll.value : undefined),
						resend_last: (json.options && json.options.uiResendLast ? json.options.uiResendLast.value : undefined)
					},
					// Module options can be overridden by tag attributes
					{
						resend_all: this.resendAll,
						resend_last: this.resendLast
					}
				)
				if (resendOptions.resend_all)
					delete resendOptions['resend_last']
				else 
					delete resendOptions['resend_all']

				return resendOptions
			},
			getModuleJson: function(callback) {
				// Get JSON from the server to initialize options
				$.ajax({
					type: 'POST',
					url: "${createLink(controller:'live', action:'getModuleJson', absolute:'true')}",
					data: {
						channel: this.channel
					},
					success: callback,
					dataType: 'json'
				});
			},
			sendRequest: function(msg, callback) {
				$.ajax({
					type: 'POST',
					url: "${createLink(controller:'live', action:'request', absolute:'true')}",
					data: {
						channel: this.channel,
						msg: JSON.stringify(msg)
					},
					success: function(data) {
						if (!data.success) {
							//handleError(data.error)
						}
						else if (callback)
							callback(data.response)
					},
					dataType: 'json'
				});
			},
			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	</script>
</polymer-element>