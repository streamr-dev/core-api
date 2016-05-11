<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="help.api.title" /></title>

    <r:require module="swagger"/>
	<r:require module="scrollspy-helper"/>
	<r:require module="codemirror"/>

	<r:script>
		// Draws sidebar with scrollspy. If h1 -> first level title. If h2 -> second level title.
		// Scrollspy uses only titles to track scrolling. div.help-text elements are not significant for the scrollspy.
		new ScrollSpyHelper("#api-docs-wrapper", "#sidebar")

		$(function() {
			// style vanilla elements from markdown with bootstrap styles
			$("table").addClass("table table-striped table-hover")
		})
	</r:script>

    <r:script>
        $(function () {

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

                        addApiKeyAuthorization("${user?.apiKey}");
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

                // if you have an apiKey you would like to pre-populate on the page for demonstration purposes...
                /*
                 var apiKey = "myApiKeyXXXX123456789";
                 $('#input_apiKey').val(apiKey);
                 */


                window.swaggerUi.load();

				setTimeout(function() {
					// Add bootstrap styling to controls created by swagger
					$("#swagger-ui-container input[type=submit]").addClass("btn btn-default")
					$("#swagger-ui-container button").addClass("btn btn-default")
					$("#swagger-ui-container select").addClass("form-control")
					$("#swagger-ui-container textarea").addClass("form-control")
				}, 1000)

                function log() {
                    if ('console' in window) {
                        console.log.apply(console, arguments);
                    }
                }
            })
        });
    </r:script>
</head>

<body class="help-page">

<ui:flashMessage/>

<ui:breadcrumb>
	<g:render template="/help/apiBreadcrumb"/>
</ui:breadcrumb>

<div class="row">
	<div class="col-sm-12">
		<div class="scrollspy-wrapper col-md-9 col-lg-offset-2 col-lg-6" id="api-docs-wrapper">

			<markdown:renderHtml template="api/introduction" />
			<hr>
			<markdown:renderHtml template="api/data-input" />

			<hr>
			<markdown:renderHtml template="api/data-output" />
			<hr>
			<markdown:renderHtml template="api/resources" />

			<div class="swagger-section">
				<div class="swagger-ui-wrap">
					<!--<a id="logo" href="http://swagger.io">swagger</a>-->
					<form id='api_selector'>
						<div class='input'><input placeholder="http://example.com/api" id="input_baseUrl" name="baseUrl" type="hidden"/></div>
						<div class='input'><input placeholder="API Key" id="input_apiKey" name="apiKey" type="text"/></div>
						<div class='input'><a id="explore" href="#" data-sw-translate>Explore</a></div>
					</form>
				</div>

				<div id="message-bar" class="swagger-ui-wrap" data-sw-translate>&nbsp;</div>
				<div id="swagger-ui-container" class="swagger-ui-wrap"></div>
			</div>

		</div>

		<!-- Don't remove these divs -->
		<div class="col-xs-0 col-lg-1"></div>
		<div class="col-xs-0 col-sm-0 col-md-3 col-lg-3" id="sidebar"></div>
	</div>
</div>

</body>
</html>
