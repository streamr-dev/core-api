<script id="rsp-template" type="text/template">
    <a class="rsp-title">
        <span class="mm-text mmc-dropdown-delay animated fadeIn">{{ name }}</span>
        <span class="howmanychecked badge badge-primary"></span>
    </a>
</script>

<script id="uichannel-template" type="text/template">
        <a href="#" class="uichannel-title">
        <!--title="{{ id }}"-->
            <i class="menu-icon fa fa-square"></i>
            <i class="menu-icon fa fa-check-square"></i>
            {{ name ? name : id }}
        </a>
</script>

<script id="streamr-widget-template" type="text/template">
    <div class="contains">
        <div class="stat-panel">
            <div class="stat-row">
                <!-- Dark gray background, small padding, extra small text, semibold text -->
                <div class="title stat-cell bg-dark-gray padding-sm text-s text-semibold">
                    
                </div>
            </div> <!-- /.stat-row -->
            <div class="stat-row">
                <!-- Bordered, without top border, without horizontal padding -->
                <div class="widget-content stat-cell bordered no-border-t no-padding-hr text-center">
                </div>
            </div> <!-- /.stat-row -->
        </div> <!-- /.stat-panel -->
    </div>
</script>

<script id="streamr-label-template" type="text/template">
    <h1><streamr-label class="streamr-label" channel="{{ uiChannel.id }}"></streamr-label></h1>
</script>

<script id="streamr-heatmap-template" type="text/template">
    <streamr-heatmap class="streamr-heatmap" channel="{{ uiChannel.id }}"></streamr-heatmap>
</script>

<script id="streamr-chart-template" type="text/template">
    <streamr-chart class="streamr-chart" channel="{{ uiChannel.id }}"></streamr-chart>    
</script>

<script id="titlebar-template" type="text/template">
        <div class="col-xs-7">
            <span class="titlebar">{{ title ? title : "&nbsp;" }}</span>
            <input class="titlebar-edit name-input form-control input-sm" type="text" value="{{ title }}" placeholder="Title" name="dashboard-item-name"></input>
        </div>
        <div class="panel-heading-controls text-left">
            <button class="close-edit btn btn-xs btn-outline dark" title="Ready"><i class="fa fa-check"></i></button>
            <button class="compress-btn btn btn-xs btn-outline dark" title="Compress" ><i class="fa fa-compress"></i></button>
            <button class="expand-btn btn btn-xs btn-outline dark" title="Expand"><i class="fa fa-expand"></i></button>

            <button class="delete-btn btn btn-xs btn-outline dark" title="Remove"><i class="fa fa-times"></i></button>
        </div>
</script>

<script id="button-template" type="text/template">
    <div class="menu-content">
        <button class='save-button btn btn-block btn-primary' title='Save dashboard'>Save</button>
        <form method="post" role="form" id="toolbarForm">
            <g:hiddenField name="id" value="${params.id}" />
            <button id='deleteButton' class='delete-button btn btn-block btn-default confirm' data-action="${createLink(action:'delete')}" data-confirm="Really delete dashboard {{ name }}?" title: 'Delete dashboard'>Delete</button>
        </form>
    </div>
</script>