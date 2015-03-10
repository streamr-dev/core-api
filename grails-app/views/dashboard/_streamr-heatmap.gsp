<div class="col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered">
    <!-- Centered text -->
    <div class="stat-panel text-center">
        <div class="stat-row">
            <!-- Dark gray background, small padding, extra small text, semibold text -->
            <div class="stat-cell bg-dark-gray padding-sm text-s text-semibold">
                ${title}
            </div>
        </div> <!-- /.stat-row -->
        <div class="stat-row">
            <!-- Bordered, without top border, without horizontal padding -->
            <div class="stat-cell bordered no-border-t no-padding-hr no-padding-vr" style="height:400px">
                <streamr-heatmap class="streamr-heatmap" channel="${channel}"></streamr-heatmap>
            </div>
        </div> <!-- /.stat-row -->
    </div> <!-- /.stat-panel -->
</div>