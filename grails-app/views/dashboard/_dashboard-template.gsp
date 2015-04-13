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
    <div class="titlebar">
        <span>{{ title ? title : "&nbsp;" }}</span>
        <div class="panel-heading-controls">
            <button class="edit btn btn-xs btn-outline dark" title="Edit"><i class="fa fa-pencil"></i></button>
            <button class="delete btn btn-xs btn-outline dark" title="Remove"><i class="fa fa-times"></i></button>
        </div>
    </div>

    <div class="titlebar-edit">
        <div class="col-xs-7">
            <input class="name-input form-control input-sm" type="text" value="{{ title }}" placeholder="Title" name="dashboard-item-name"></input>
        </div>
        <div class="panel-heading-controls text-left">

            <!--button class="prev-order btn btn-xs btn-outline dark" title="Move left"><i class="fa fa-arrow-circle-left"></i></button>
            <button class="next-order btn btn-xs btn-outline dark" title="Move right" ><i class="fa fa-arrow-circle-right"></i></button-->

            <button class="close-edit btn btn-xs btn-outline dark" title="Ready"><i class="fa fa-check"></i></button>
        </div>
    </div>
</script>


<script id="template" type="text/template">
    {{ name }} {{ id }}
</script>