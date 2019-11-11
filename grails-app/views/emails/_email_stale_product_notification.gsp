<p>Dear ${user.username?.encodeAsHTML()},<br/></p>

<p>This is an automatic message to notify you that some of your products on the <a href="https://www.streamr.com/marketplace">Streamr Marketplace</a> are not receiving new data. We recommend checking that the data integration to these streams is healthy. Note that products with stale streams are unlikely to attract buyers.</p>
<p>The affected products and streams are:</p>

<ul>
	<g:each in="${staleProducts}" var="stale">
	<li>${stale.product.name?.encodeAsHTML()}
		<ul>
		<g:each in="${stale.streams}" var="msg">
			<li>${msg.stream.name?.encodeAsHTML()}: ${msg.formatDate()?.encodeAsHTML()}</li>
		</g:each>
		</ul>
	</li>
	</g:each>
</ul>
<p>You can stop these notifications by ensuring that the data is flowing. If you no longer wish to offer your data products, you can unpublish them at any time.</p>

<p>Best regards,<br/>
Automatic product health checker</p>

