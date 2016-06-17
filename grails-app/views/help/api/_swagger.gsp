<r:script>
        $(function () {

			// Swagger init from swagger.json
            $.getJSON('${resource(dir: 'misc', file: 'swagger.json', plugin: 'unifina-core')}', function(data) {

                // http://domain.com:8080/subpath/ => domain.com:8080/subpath
                data.host = '${createLink(uri: "/", absolute: true).replaceAll("https?://", "")}'

                // Remove trailing slash if necessary
                if (data.host.substr(data.host.length - 1) === "/") {
                    data.host = data.host.substr(0, data.host.length - 1)
                }


                // Grab protocol from uri
                data.schemes = ['${createLink(uri: "/", absolute: true).split("://")[0]}']

                window.swaggerUi = new SwaggerUi({
                    spec: data,
                    dom_id: "swagger-ui-container",
                    onComplete: function(swaggerApi, swaggerUi) {

                        $('pre code').each(function(i, e) {
                            hljs.highlightBlock(e)
                        });

						<g:if test="${user}">
                        	addApiKeyAuthorization("${user.apiKey}");
						</g:if>

						// Add bootstrap styling to controls created by swagger
						$("#swagger-ui-container input[type=submit]").addClass("btn btn-default")
						$("#swagger-ui-container button").addClass("btn btn-default")
						$("#swagger-ui-container select").addClass("form-control")
						$("#swagger-ui-container textarea").addClass("form-control")
                    },
                    onFailure: function(data) {
                        log("Unable to Load SwaggerUI");
                    },
                    docExpansion: "none",
                    jsonEditor: false,
                    apisSorter: "alpha",
                    defaultModelRendering: 'schema',
                    showRequestHeaders: false,
                    validatorUrl: null
                });

                function addApiKeyAuthorization(key){
                    if (key && key.trim() != "") {
                        var apiKeyAuth = new SwaggerClient.ApiKeyAuthorization("Authorization", "token " + key, "header")
                        window.swaggerUi.api.clientAuthorizations.add("key", apiKeyAuth);
                        log("added key " + key);
                    }
                }

                $('#input_apiKey').change(function() {
                    var key = encodeURIComponent($('#input_apiKey')[0].value);
                    addApiKeyAuthorization(key);
                });

                window.swaggerUi.load();

                function log() {
                    if ('console' in window) {
                        console.log.apply(console, arguments);
                    }
                }
            })
        });
</r:script>

<div class="swagger-section">
	<div class="swagger-ui-wrap">
		<form id='api_selector'>
			<div class='input'><input placeholder="${createLink(uri: "/api/v1", absolute: true)}" id="input_baseUrl" name="baseUrl" type="hidden"/></div>
			<g:if test="${!user}">
				<div class='input'><input placeholder="API Key" id="input_apiKey" name="apiKey" type="text" class="form-control"/></div>
			</g:if>
		</form>
	</div>

	<div id="message-bar" class="swagger-ui-wrap" data-sw-translate>&nbsp;</div>
	<div id="swagger-ui-container" class="swagger-ui-wrap"></div>
</div>
