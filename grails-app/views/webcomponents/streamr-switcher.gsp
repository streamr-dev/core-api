<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
    <r:require module="streamr-switcher"/>

    <r:layoutResources disposition="head"/>
    <r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-switcher" extends="streamr-widget">
    <template>
        <shadow></shadow>
    </template>

    <script>
        Polymer('streamr-switcher', {
            ready: function() {
                var _this = this
                this.bindEvents(_this.$["streamr-widget-container"])

                this.getModuleJson(function(json) {
                    var resendOptions = _this.getResendOptions(json)

                    _this.switcher = new StreamrSwitcher(_this.$["streamr-widget-container"], {}, {

                    })

                    _this.subscribe(
                        function(message) {
                            _this.switcher.receiveResponse(message)
                        },
                        resendOptions
                    )
                })

            },
            getSwitcher: function() {
                return this.switcher
            },
            <g:if test="${params.lightDOM}">
            parseDeclaration: function(elementElement) {
                return this.lightFromTemplate(this.fetchTemplate(elementElement))
            }
            </g:if>
        });
    </script>
</polymer-element>