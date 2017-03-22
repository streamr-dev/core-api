<ui:labeled label="${message(code:"stream.id.label")}">
    	<span class="stream-id">${stream.id}</span>
</ui:labeled>
<ui:labeled label="${message(code:"stream.apiKeys.label")}">
	<div class="stream-auth-keys row">
		<div class="auth-key-list">
			<g:each in="${keys}">
				<div class="auth-key-row col-xs-12">
					<span class="name-label col-xs-10">${it.id} (${it.name})</span>
					<div class="auth-key-button-row col-xs-2">
						<button class="form-group auth-key-delete-button btn btn-danger pull-right">
							<input class="streamId" type="hidden" name="streamId" value="${it.id}" />
							<span class="icon fa fa-trash-o"></span>
						</button>
					</div>
				</div>
			</g:each>
		</div>
		<div class="new-auth-key-row col-xs-12">
			<div class="input-group">
				<input type="text" class="new-auth-key-field form-control" placeholder="Enter name" autofocus="">
				<span class="input-group-btn">
					<button class="new-auth-key-button btn btn-default pull-right" type="button">
						<span class="icon fa fa-plus"></span>
					</button>
				</span>
			</div>
		</div>
	</div>
</ui:labeled>

<script type="text/javascript">
	$(document).ready(function() {
		$(".new-auth-key-button").click(function(event) {
		    var value = $(".new-auth-key-field").val()
			var url = Streamr.createLink({ uri: 'api/v1/keys' })
			$.post(url, { name: value, streamId: '${stream.id}' }, function(data) {
			    window.location.reload(false)
			})
		})

		$(".auth-key-delete-button").click(function(event) {
			var streamId = $(this).find(".streamId").val()
			var url = Streamr.createLink({ uri: 'api/v1/keys/' + streamId })
			$.ajax(url, { method: 'DELETE' }).done(function(data) {
				window.location.reload(false)
			})
		})
	})
</script>