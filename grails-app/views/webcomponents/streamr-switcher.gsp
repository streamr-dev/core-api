<link rel="import" href="${createLink(uri:"/webcomponents/polymer.html", plugin:"unifina-core")}">

<g:if test="${!params.noDependencies}">
    <r:require module="streamr-switcher"/>

    <r:layoutResources disposition="head"/>
    <r:layoutResources disposition="defer"/>
</g:if>

<polymer-element name="streamr-switcher" extends="streamr-input">
    <template>
        <shadow></shadow>
        <style>
            .switcher {
                cursor: pointer;
                display: inline-block;
                position: relative;
                -webkit-touch-callout: none;
                -webkit-user-select: none;
                -khtml-user-select: none;
                -moz-user-select: none;
                -ms-user-select: none;
                user-select: none;
            }
            .switcher-inner {
                display: block;
                height: 100%;
                overflow: hidden;
                white-space: nowrap;
                width: 100%;
                word-spacing: 0;
            }
            .switcher-state-on,
            .switcher-state-off {
                display: inline-block;
                width: 100%;
                height: 100%;
                margin: 0;
                padding: 0;
                text-align: center;
                -webkit-transition: all 0.2s;
                -o-transition: all 0.2s;
                transition: all 0.2s;
            }
            .switcher-state-on {
                margin-left: -100%;
            }
            .switcher.checked .switcher-state-on {
                margin-left: 0;
            }
            .switcher-toggler {
                left: 0;
                position: absolute;
                text-align: center;
                -webkit-transition: all 0.2s;
                -o-transition: all 0.2s;
                transition: all 0.2s;
            }
            .switcher.checked .switcher-toggler {
                left: 100%;
            }
            .switcher > input[type="checkbox"] {
                left: -100000px;
                position: absolute;
                visibility: hidden;
            }
            .switcher + .styled-pseudo-checkbox {
                display: none !important;
            }
        </style>
    </template>

    <script>
        Polymer('streamr-switcher', {
            onReady: function() {
                var _this = this

                $(this.widget).on("input", function(e, value) {
                    _this.sendValue(value)
                })
            },

            createWidget: function(json) {
                return new StreamrSwitcher(this.$["streamr-widget-container"], json)
            },

            <g:if test="${params.lightDOM}">
            parseDeclaration: function(elementElement) {
                return this.lightFromTemplate(this.fetchTemplate(elementElement))
            }
            </g:if>
        });
    </script>
</polymer-element>