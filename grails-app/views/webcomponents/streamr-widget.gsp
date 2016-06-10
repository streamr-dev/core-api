<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="jquery"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-widget" attributes="canvas module dashboard resendAll resendLast">
	<template>
		<streamr-client id="client"></streamr-client>
		<div id="streamr-widget-container" class="streamr-widget-container"></div>
	</template>
	
	<script>
		Polymer('streamr-widget',{
			attached: function() {
				this.isAttached = true
			},
			detached: function() {
				var _this = this
				this.isAttached = false
				setTimeout(function() {
					// If the element is not attached again immediately, unsubscribe
					if (!_this.isAttached)
						_this.unsubscribe()
				})
			},
			bindEvents: function(container) {
				container.parentNode.addEventListener("resize", function() {
					container.dispatchEvent(new Event('resize'))
				})
			},
			subscribe: function(messageHandler, options) {
				var _this = this

				this.getModuleJson(function(moduleJson) {
					if (!moduleJson.uiChannel)
						throw "Module JSON does not have an UI channel: "+JSON.stringify(moduleJson)

					options = options || _this.getResendOptions(moduleJson)

					// Pass on the access context to the subscribe request
					options = $.extend(options, _this.getAccessContext())

					_this.$.client.getClient(function(client) {
						_this.sub = client.subscribe(
								moduleJson.uiChannel.id,
								messageHandler,
								options
						)
					})
				})
			},
			unsubscribe: function() {
				var _this = this
				this.$.client.getClient(function(client) {
					client.unsubscribe(_this.sub)
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
					resendOptions = {}

					if (this.resendAll !== undefined) {
						resendOptions.resend_all = (this.resendAll.toLowerCase() === 'true')
					}
					else if (this.resendLast !== undefined) {
						try {
							resendOptions.resend_last = parseInt(this.resendLast)
						} catch (err) {
							console.error("resendLast could not be parsed to an integer! Value was: %s", this.resendLast)
						}
					}
				}

				return resendOptions
			},
			getModuleJson: function(callback) {
				var _this = this

				if (this.cachedModuleJson)
					callback(this.cachedModuleJson)
				else {
					// Get JSON from the server to initialize options
					$.ajax({
						type: 'GET',
						url: "${createLink(uri: '/api/v1/canvases', absolute:'true')}" + '/' + this.canvas + "/modules/" + this.module + "?" + $.param(_this.getAccessContext()),
						dataType: 'json',
						success: function(response) {
							_this.cachedModuleJson = response
							if (callback)
								callback(response)
						},
						error: function (xhr) {
							console.log("Error while communicating with widget: " + xhr.responseText)
							try {
								var response = JSON.parse(xhr.responseText)
								_this.fire('error', response, undefined, false)
							} catch (err) {
								_this.fire('error', xhr.responseText, undefined, false)
							}
						}
					});
				}
			},
			sendRequest: function(msg, callback) {
				var _this = this
				$.ajax({
					type: 'POST',
					url: "${createLink(uri: '/api/v1/canvases', absolute:'true')}"+'/'+this.canvas+'/modules/'+this.module+'/request' + "?" + $.param(_this.getAccessContext()),
					data: JSON.stringify(msg),
					dataType: 'json',
					contentType: 'application/json; charset=utf-8',
					success: callback,
					error: function(xhr) {
						console.log("Error while communicating with widget: %s", xhr.responseText)
						if (xhr.responseJSON)
							_this.fire('error', xhr.responseJSON, undefined, false)
						else
							_this.fire('error', xhr.responseText, undefined, false)
					}

				});
			},
			getAccessContext: function() {
				// extend cleans off keys with undefined values
				return $.extend({}, {
					canvas: this.canvas,
					dashboard: this.dashboard ? this.dashboard : undefined
				})
			},

			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	</script>
</polymer-element>