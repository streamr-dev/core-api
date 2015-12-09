<html>
<head>
    <meta name="layout" content="main" />
    <title>Invite List</title>
</head>

<body>

	<ui:flashMessage/>

	<div class="panel">
		<table class="table">
			<thead>
				<tr>
					<th>Email</th>
					<th>Sent</th>
					<th>Used</th>
					<th>Created</th>
					<th>Updated</th>
					<th>Invite</th>
				</tr>
			</thead>
			<tbody>
				<g:each var="i" in="${invites}">
					<tr>
						<td>${i.username}</td>
						<td>${i.sent}</td>
						<td>${i.used}</td>
						<td><g:formatDate date="${i.dateCreated}"/></td>
						<td><g:formatDate date="${i.lastUpdated}"/></td>
						<td>
							<g:if test="${!i.used}">
								<g:link action="sendInvite" params="[code:i.code]">
									${i.sent ? "Resend" : "Send invite now"}
								</g:link>
							</g:if>
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>

</body>
</html>
