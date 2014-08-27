<html>
<head>
	<r:require module="remote-tabs"/>

	<script id="remote-tabs-template" type="x-tmpl-mustache">
		<div class="remote-tabs-content" tabindex="-1">
			<ul class="nav nav-tabs" role="tablist">
			{{#tabs}}
				<li class="{{active}}">
					<a data-target="#{{id}}" data-url="{{url}}" role="tab" data-toggle="tab">
						{{title}}
					</a>
				</li>
			{{/tabs}}
			</ul>

			<div class="tab-content">
			{{#tabs}}
				<div class="tab-pane {{active}}" id="{{id}}"></div>
			{{/tabs}}
			</div>
		</div>
	</script>

</head>
