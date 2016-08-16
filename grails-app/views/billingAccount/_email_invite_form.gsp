
<div class="col-md-6">
	<form method="post" id="emailInvite" action="emailInvite">
		<ui:panel title="Users">
			<div class="row">
				<div class="col-xs-12">
					<table class="table">
						<thead>
							<tr>
								<th>Username</th>
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
				</div>
				<div class="col-xs-12">
					<div class="form-group col-xs-12">
						<label>Insert email address and invite person to this Billing Account</label>
						<div class="form-inline row">
							<div class="col-xs-12 col-sm-8 col-md-10">
								<input type="text" class="form-control" name="emailInvite" value="" style="width: 100%;"/>
							</div>
							<button type="submit" class="col-xs-4 col-md-2 save btn btn-primary">Invite</button>
						</div>
					</div>

				</div>
			</div>
		</ui:panel>
	</form>
</div>
