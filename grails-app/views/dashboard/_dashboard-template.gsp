<script id="sidebar-template" type="text/template">
    <div class="menu-content">
        <label>Dashboard Name</label>
        <input type="text" class="dashboard-name title-input form-control" name="dashboard-name" placeholder="Dashboard Name" value="{{ name }}" />
    </div>
    <ul class="navigation" id="rsp-list">
        <li class="canvas-title">
            <label>Live Canvases</label>
        </li>
    </ul>
    <div class="menu-content">
        <button class='save-button btn btn-block btn-primary' title='Save dashboard'>Save</button>
        <g:if test="${shareable}">
            <ui:shareButton id="share-button" class="btn-block" name="Dashboard {{ name }}" url="${createLink(uri: "/api/v1/dashboards/" + dashboard.id)}" > Share </ui:shareButton>
        </g:if>
        <form method="post" role="form" id="deleteDashboardForm">
            <g:hiddenField name="id" value="${params.id}" />
            <button id='deleteButton' class='delete-button btn btn-block btn-default confirm' data-action="${createLink(action:'delete')}" data-confirm="Really delete dashboard {{ name }}?" title='Delete dashboard'>Delete</button>
        </form>
    </div>
</script>
<script id="canvas-template" type="text/template">
    <a class="canvas-title" title="{{state != 'running' ? state : ''}}">
        <span class="mm-text mmc-dropdown-delay animated fadeIn">{{ name }}</span>
        <span class="howmanychecked badge badge-primary"></span>
    </a>
</script>

<script id="module-template" type="text/template">
        <a href="#" class="module-title">
            <i class="menu-icon fa fa-square"></i>
            <i class="menu-icon fa fa-check-square"></i>
            {{ uiChannel && uiChannel.name ? uiChannel.name : (name ? name : id) }}
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
    <h1><streamr-label class="streamr-widget non-draggable" canvas="{{ canvas }}" module="{{ module }}"></streamr-label></h1>
</script>

<script id="streamr-heatmap-template" type="text/template">
    <streamr-heatmap class="streamr-widget non-draggable" canvas="{{ canvas }}" module="{{ module }}"></streamr-heatmap>
</script>

<script id="streamr-chart-template" type="text/template">
    <streamr-chart class="streamr-widget non-draggable" canvas="{{ canvas }}" module="{{ module }}"></streamr-chart>
</script>

<script id="streamr-table-template" type="text/template">
    <streamr-table class="streamr-widget non-draggable text-left" canvas="{{ canvas }}" module="{{ module }}"></streamr-table>
</script>

<script id="streamr-button-template" type="text/template">
    <streamr-button class="streamr-widget non-draggable" canvas="{{ canvas }}" module="{{ module }}"></streamr-button>
</script>

<script id="streamr-switcher-template" type="text/template">
    <streamr-switcher class="streamr-widget non-draggable" canvas="{{ canvas }}" module="{{ module }}"></streamr-switcher>
</script>

<script id="streamr-text-field-template" type="text/template">
    <streamr-text-field class="streamr-widget non-draggable" canvas="{{ canvas }}" module="{{ module }}"></streamr-text-field>
</script>

<script id="streamr-map-template" type="text/template">
    <streamr-map class="streamr-widget non-draggable" canvas="{{ canvas }}" module="{{ module }}"></streamr-map>
</script>

<script id="titlebar-template" type="text/template">
        <div class="col-xs-7">
            <span class="titlebar">{{ title ? title : "&nbsp;" }}</span>
            <span class="titlebar-clickable" title="Edit title">{{ title ? title : "&nbsp;" }}</span>
            <input class="titlebar-edit name-input form-control input-sm" type="text" value="{{ title }}" placeholder="Title" name="dashboard-item-name"></input>
        </div>
        <div class="panel-heading-controls text-left">
                <button class="edit-btn btn btn-xs btn-outline dark" title="Edit title"><i class="fa fa-edit"></i></button>
                <button class="close-edit btn btn-xs btn-outline dark" title="Ready"><i class="fa fa-check"></i></button>
                <div class="btn-group btn-group-xs">
                    <button data-toggle="dropdown" type="button" class="btn btn-outline dark dropdown-toggle" title="Edit size">
                        <span class="fa fa-expand"></span>
                        &nbsp;
                        <span class="fa fa-caret-down"></span>
                    </button>
                    <ul class="dropdown-menu pull-right">
                        <li>
                            <a href="#" class="make-small-btn">
                                <i class="fa fa-check"></i> Small 
                            </a>
                        </li>
                        <li>
                            <a href="#" class="make-medium-btn">
                                <i class="fa fa-check"></i> Medium
                            </a>
                        </li>
                        <li>
                            <a href="#" class="make-large-btn">
                                <i class="fa fa-check"></i> Large
                            </a>
                        </li>
                    </ul>
                </div>   
                <button class="delete-btn btn btn-xs btn-outline dark" title="Remove"><i class="fa fa-times"></i></button>
        </div>
</script>