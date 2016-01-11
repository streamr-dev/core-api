<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
	<r:require module="streamr-client"/>

	<r:layoutResources disposition="head"/>
	<r:layoutResources disposition="defer"/>
</g:if>



<polymer-element name="streamr-client" attributes="server autoconnect autodisconnect">
	<script>
	(function(){
		var streamrClient

		function createClient(cb) {
			if (streamrClient) {
				cb(streamrClient)
			}
			else if (typeof StreamrClient !== 'undefined') {
				streamrClient = new StreamrClient()
				cb(streamrClient)
			}
			else {
				setTimeout(function() {
					createClient(cb)
				}, 100)
			}
		}

		Polymer('streamr-client', {
			publish: {
				// Leave the default value of server as undefined, because we identify 
				// the "top" element by checking if this.server is defined
				server: undefined, 
				autoconnect: true,
				autodisconnect: true
			},
			// This function is executed multiple times, once for each element!
			ready: function() {
				createClient(function(client) {
					// This should hold true for only one <streamr-client> element instance on the page
					if (this.server) {
						var myOptions = {
							server: this.server,
							autoConnect: this.autoconnect,
							autoDisconnect: this.autodisconnect
						}
						Object.keys(myOptions).forEach(function(key) {
							if (myOptions[key]!=null) {
								client.options[key] = myOptions[key]
							}
						})
					}
				})
			},
			getClient: function(cb) {
				createClient(cb)
			},
			<g:if test="${params.lightDOM}">
				parseDeclaration: function(elementElement) {
					return this.lightFromTemplate(this.fetchTemplate(elementElement))
				}
			</g:if>
		});
	})();
	</script>
	
</polymer-element>
