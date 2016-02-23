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
            streamr-switcher .switcher {
                cursor: pointer;
                display: inline-block;
                position: relative;
                -webkit-touch-callout: none;
                -webkit-user-select: none;
                -khtml-user-select: none;
                -moz-user-select: none;
                -ms-user-select: none;
                user-select: none;
                height: 24px;
                width: 60px;
            }
            streamr-switcher .switcher-inner {
                display: block;
                height: 100%;
                overflow: hidden;
                white-space: nowrap;
                width: 100%;
                word-spacing: 0;
                border-radius: 9999px;
            }
            streamr-switcher .switcher-state-on,
            streamr-switcher .switcher-state-off {
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
            streamr-switcher .switcher-state-on {
                margin-left: -100%;
            }
            streamr-switcher .switcher.checked .switcher-state-on {
                margin-left: 0;
            }
            streamr-switcher .switcher-toggler {
                left: 0;
                position: absolute;
                text-align: center;
                -webkit-transition: all 0.2s;
                -o-transition: all 0.2s;
                transition: all 0.2s;
            }
            streamr-switcher .switcher.checked .switcher-toggler {
                left: 100%;
            }
            streamr-switcher .switcher > input[type="checkbox"] {
                left: -100000px;
                position: absolute;
                visibility: hidden;
            }
            streamr-switcher .switcher + .styled-pseudo-checkbox {
                display: none !important;
            }
            streamr-switcher .switcher-toggler {
                background: #fff;
                border-radius: 9999px;
                height: 20px;
                margin-left: 1px;
                margin-top: 2px;
                width: 20px;
                -webkit-box-shadow: 0 1px 5px rgba(0, 0, 0, 0.3);
                box-shadow: 0 1px 5px rgba(0, 0, 0, 0.3);
            }
            streamr-switcher .switcher-state-on,
            streamr-switcher .switcher-state-off {
                color: #fff;
                font-size: 11px;
                font-weight: 600;
                line-height: 24px;
            }
            streamr-switcher .switcher-state-on {
                background: #5ebd5e;
                padding-right: 20px;
            }
            streamr-switcher .switcher-state-off {
                background: #444;
                padding-left: 20px;
            }
            streamr-switcher .switcher.checked .switcher-toggler {
                margin-left: -21px;
            }
            streamr-switcher .switcher-theme-square .switcher-inner {
                border-radius: 3px;
            }
            streamr-switcher .switcher-theme-square .switcher-toggler {
                border-radius: 2px;
                margin-left: 2px;
            }
            streamr-switcher .switcher-theme-square.switcher.checked .switcher-toggler {
                margin-left: -22px;
            }
            streamr-switcher .switcher-lg {
                width: 70px;
                height: 30px;
            }
            streamr-switcher .switcher-lg .switcher-toggler {
                height: 26px;
                width: 26px;
                margin-left: 1px;
            }
            streamr-switcher .switcher-lg .switcher-state-on,
            streamr-switcher .switcher-lg .switcher-state-off {
                font-size: 13px;
                line-height: 30px;
            }
            streamr-switcher .switcher-lg .switcher-state-on {
                padding-right: 26px;
            }
            streamr-switcher .switcher-lg .switcher-state-off {
                padding-left: 26px;
            }
            streamr-switcher .switcher-lg.checked .switcher-toggler {
                margin-left: -27px;
            }
            streamr-switcher .switcher-lg.switcher-theme-square .switcher-inner {
                border-radius: 3px;
            }
            streamr-switcher .switcher-lg.switcher-theme-square .switcher-toggler {
                margin-left: 2px;
            }
            streamr-switcher .switcher-lg.switcher-theme-square.switcher.checked .switcher-toggler {
                margin-left: -28px;
            }
            streamr-switcher .switcher.disabled {
                cursor: not-allowed !important;
                opacity: .5 !important;
                filter: alpha(opacity=50);
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