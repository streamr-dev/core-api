<form method="post" action="emailInvite">
	<div class="row">
		<div class="col-sm-12 col-md-offset-2 col-md-8">
			<ui:panel title="Users">
				<table class="table">
					<thead>
					<tr>
						<td>Username</td>
					</tr>
					</thead>
					<tbody>
					<g:each in="${billingAccountUsers}">
						<tr>
							<td>${it.username}</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<legend>Insert email address and invite person to this Billing Account</legend>
				<div class="form-group col-sm-6">
					<input type="text" class="form-control" name="emailInvite" value=""/>
				</div>
				<input type="submit" class="save btn btn-lg btn-primary" value="Invite" />
			</ui:panel>
		</div>
	</div>
</form>