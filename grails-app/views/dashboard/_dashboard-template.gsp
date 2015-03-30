<script id="rsp-template" type="text/template">
    <a><span class="mm-text mmc-dropdown-delay animated fadeIn">${'<%= name %>'}</span></a>
</script>

<script id="uichannel-template" type="text/template">
        <label class="px-single"><input class="toggle px" type="checkbox" ${"<%= checked ? 'checked' : '' %>"}><span class="lbl"></span></label>
    
    <a>${'<%= name ? name : id %>'}</a>
</script>

<script id="di-template" type="text/template">
    <span>${'<%= title %>'}</span>
    <span>${'<%= uiChannel.id %>'}</span>
</script>

<script id="streamr-widget-template" type="text/template">
        <!-- Centered text -->
        <div class="stat-panel text-center">
            <div class="stat-row">
                <!-- Dark gray background, small padding, extra small text, semibold text -->
                 <div class="stat-cell bg-dark-gray padding-sm text-s text-semibold">
                    <span>${'<%= title ? title : "&nbsp;" %>'}</span>
                    <div class="panel-heading-controls">
                            <button class="edit btn btn-xs btn-outline dark"><i class="fa fa-pencil"></i></button>
                            <button class="delete btn btn-xs btn-outline dark"><i class="fa fa-times"></i></button>
                        </div>
                </div>
            </div> <!-- /.stat-row -->
            <div class="stat-row">
                <!-- Bordered, without top border, without horizontal padding -->
                <div class="widget-content stat-cell bordered no-border-t no-padding-hr">
                </div>
            </div> <!-- /.stat-row -->
        </div> <!-- /.stat-panel -->
</script>

<script id="streamr-label-template" type="text/template">
    <h1><streamr-label class="streamr-label" channel="${'<%= uiChannel.id %>'}"></streamr-label></h1>
</script>

<script id="streamr-heatmap-template" type="text/template">
    <streamr-heatmap class="streamr-heatmap" channel="${'<%= uiChannel.id%>'}"></streamr-heatmap>
</script>

<script id="streamr-chart-template" type="text/template">
    <streamr-chart class="streamr-chart" channel="${'<%= uiChannel.id %>'}"></streamr-chart>    
</script>