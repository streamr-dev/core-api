<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
    <r:require module="streamr-switcher"/>

    <r:layoutResources disposition="head"/>
    <r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-switcher" extends="streamr-input">
    <template>
        <shadow></shadow>
    </template>

    <script>
        Polymer('streamr-switcher', {
            ready: function() {
                var _this = this
                this.module = new StreamrSwitcher(this.$["streamr-widget-container"], {}, {})
                $(this.module).on("input", function(e, value) {
                    _this.sendValue(value)
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