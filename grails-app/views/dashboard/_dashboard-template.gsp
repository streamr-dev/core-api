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

<script id="streamr-label-template" type="text/template">
    <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3 col-centered">
        <!-- Centered text -->
        <div class="stat-panel text-center">
            <div class="stat-row">
                <!-- Dark gray background, small padding, extra small text, semibold text -->
                <div class="stat-cell bg-dark-gray padding-sm text-s text-semibold">
                    ${'<%= title ? title : "&nbsp;" %>'}
                </div>
            </div> <!-- /.stat-row -->
            <div class="stat-row">
                <!-- Bordered, without top border, without horizontal padding -->
                <div class="stat-cell bordered no-border-t no-padding-hr">
                    <streamr-label class="streamr-label" channel="${'<%= uiChannel.id %>'}"></streamr-label>
                </div>
            </div> <!-- /.stat-row -->
        </div> <!-- /.stat-panel -->
    </div>
</script>

<script id="streamr-heatmap-template" type="text/template">
    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered">
        <!-- Centered text -->
        <div class="stat-panel text-center">
            <div class="stat-row">
                <!-- Dark gray background, small padding, extra small text, semibold text -->
                <div class="stat-cell bg-dark-gray padding-sm text-s text-semibold">
                    ${'<%= title ? title : "&nbsp;" %>'}
                </div>
            </div> <!-- /.stat-row -->
            <div class="stat-row">
                <!-- Bordered, without top border, without horizontal padding -->
                <div class="stat-cell bordered no-border-t no-padding-hr no-padding-vr" style="height:400px">
                    <streamr-heatmap class="streamr-heatmap" channel="${'<%= uiChannel.id%>'}"></streamr-heatmap>
                </div>
            </div> <!-- /.stat-row -->
        </div> <!-- /.stat-panel -->
    </div>
</script>

<script id="streamr-chart-template" type="text/template">
    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered">
        <!-- Centered text -->
        <div class="stat-panel text-center">
            <div class="stat-row">
                <!-- Dark gray background, small padding, extra small text, semibold text -->
                <div class="stat-cell bg-dark-gray padding-sm text-s text-semibold">
                    ${'<%= title ? title : "&nbsp;" %>'}
                </div>
            </div> <!-- /.stat-row -->
            <div class="stat-row">
                <!-- Bordered, without top border, without horizontal padding -->
                <div class="stat-cell bordered no-border-t no-padding-vr">
                    <streamr-chart class="streamr-chart" channel="${'<%= uiChannel.id %>'}"></streamr-chart>
                </div>
            </div> <!-- /.stat-row -->
        </div> <!-- /.stat-panel -->
    </div>
</script>