<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="jquery"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-widget" attributes="channel resendAll resendLast">
	<template>
		<streamr-client id="client"></streamr-client>
		<div id="container" class="container"></div>
	</template>
	
	<script>
		Polymer('streamr-widget',{
			publish: {
				channel: undefined,
				resendLast: undefined,
				resendAll: undefined
			},
			bindEvents: function(container) {
				container.parentNode.addEventListener("remove", function() {
					var _this = this
					this.$.client.getClient(function(client) {
						client.unsubscribe(_this.sub)
					})
				})
				container.parentNode.addEventListener("resize", function() {
					container.dispatchEvent(new Event('resize'))
				})
			},
			subscribe: function(messageHandler, resendOptions) {
				var _this = this

				this.$.client.getClient(function(client) {
					_this.sub = client.subscribe(
							_this.channel,
							messageHandler,
							resendOptions
					)
				})
			},
			getResendOptions: function(json) {
				// Default resend options
				var resendOptions = {
					resend_last: 1
				}

				// Can be overridden by module options
				if (json.options && (json.options.uiResendAll.value || json.options.uiResendLast.value!=null)) {
					resendOptions = {
						resend_all: (json.options && json.options.uiResendAll ? json.options.uiResendAll.value : undefined),
						resend_last: (json.options && (!json.options.uiResendAll || !json.options.uiResendAll.value) && json.options.uiResendLast ? json.options.uiResendLast.value : undefined)
					}
				}

				// Can be overridden by tag attributes
				if (this.resendAll || this.resendLast!=null) {
					resendOptions = {
						resend_all: this.resendAll,
						resend_last: this.resendLast
					}
				}

				return resendOptions
			},
			getModuleJson: function(callback) {
				// Get JSON from the server to initialize options
				$.ajax({
					type: 'POST',
					url: "${createLink(uri: '/api/v1/live/getModuleJson', absolute:'true')}",
					data: {
						channel: this.channel
					},
					success: callback,
					dataType: 'json'
				});
			},
			sendRequest: function(msg, callback) {
				var _this = this
				$.ajax({
					type: 'POST',
					url: "${createLink(uri: '/api/v1/live/request', absolute:'true')}",
					data: JSON.stringify({
						channel: this.channel,
						msg: msg
					}),
					dataType: 'json',
					contentType: 'application/json; charset=utf-8',
					success: function(data) {
						if (!data.success) {
							console.log("Error while communicating with widget: "+(data.response ? data.response.error : data.error))
							_this.fire('error', {
								error: (data.response ? data.response.error : data.error)
							}, undefined, false)
						}
						else if (callback)
							callback(data.response)
					}
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