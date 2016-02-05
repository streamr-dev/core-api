<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="help.api.title" /></title>
    <r:require module="swagger"/>

    <r:script>
        $(function () {

            $.getJSON('${resource(dir: 'misc', file: 'swagger.json')}', function(data) {

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
                    onComplete: function(swaggerApi, swaggerUi){
                        if(typeof initOAuth == "function") {
                            initOAuth({
                                clientId: "your-client-id",
                                clientSecret: "your-client-secret-if-required",
                                realm: "your-realms",
                                appName: "your-app-name",
                                scopeSeparator: ",",
                                additionalQueryStringParams: {}
                            });
                        }

                        $('pre code').each(function(i, e) {
                            hljs.highlightBlock(e)
                        });

                        addApiKeyAuthorization();
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

                function addApiKeyAuthorization(){
                    var key = encodeURIComponent($('#input_apiKey')[0].value);
                    if(key && key.trim() != "") {
                        var apiKeyAuth = new SwaggerClient.ApiKeyAuthorization("api_key", key, "query");
                        window.swaggerUi.api.clientAuthorizations.add("api_key", apiKeyAuth);
                        log("added key " + key);
                    }
                }

                $('#input_apiKey').change(addApiKeyAuthorization);

                // if you have an apiKey you would like to pre-populate on the page for demonstration purposes...
                /*
                 var apiKey = "myApiKeyXXXX123456789";
                 $('#input_apiKey').val(apiKey);
                 */


                window.swaggerUi.load();



                function log() {
                    if ('console' in window) {
                        console.log.apply(console, arguments);
                    }
                }
            })
        });
    </r:script>
</head>
<body class="swagger-section">
<ui:flashMessage/>

<div>
    <div class="swagger-ui-wrap">
        <!--<a id="logo" href="http://swagger.io">swagger</a>-->
        <form id='api_selector'>
            <div class='input'><input placeholder="http://example.com/api" id="input_baseUrl" name="baseUrl" type="hidden"/></div>
            <div class='input'><input placeholder="api_key" id="input_apiKey" name="apiKey" type="text"/></div>
            <div class='input'><a id="explore" href="#" data-sw-translate>Explore</a></div>
        </form>
    </div>
</div>

<div id="message-bar" class="swagger-ui-wrap" data-sw-translate>&nbsp;</div>
<div id="swagger-ui-container" class="swagger-ui-wrap"></div>
</body>
</html>
