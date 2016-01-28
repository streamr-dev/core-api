<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
    <r:require module="streamr-text-field"/>

    <r:layoutResources disposition="head"/>
    <r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-text-field" extends="streamr-input">
    <template>
        <shadow></shadow>
    </template>

    <script>
        Polymer('streamr-text-field', {
            createWidget: function(json) {
                return new StreamrTextField(this.$["streamr-widget-container"], json, {
                    widthLocked: true
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