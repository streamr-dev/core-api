<div class="col-sm-6 col-md-4">
	<ui:panel title="API Credentials">
		<g:render template="userStreamCredentials" model="[stream:stream]"/>
	</ui:panel>
</div>

<div class="col-sm-6 col-md-4">
	<g:render template="fieldsPanel" model="[config:config]"/>
</div>

<div class="col-sm-12">
	<ui:panel title="History">
		<div class="col-sm-6">
			<g:include controller="stream" action="files" params="[id:stream.id]"/>
		</div>

		<div class="col-sm-6">
			<script>
			$(document).ready(function() {
				var dz = $("#dropzone").dropzone({
					url: "${createLink(controller:'stream', action:'upload', params:[id:stream.id])}",
					paramName: "file", // The name that will be used to transfer the file
					maxFilesize: 512, // MB
	
					addRemoveLinks : true,
					dictResponseError: "Can't upload file!",
					thumbnailWidth: 138,
					thumbnailHeight: 120,
	
					previewTemplate: '<div class="dz-preview dz-file-preview"><div class="dz-details"><div class="dz-filename"><span data-dz-name></span></div><div class="dz-size">File size: <span data-dz-size></span></div><div class="dz-thumbnail-wrapper"><div class="dz-thumbnail"><img data-dz-thumbnail><span class="dz-nopreview">No preview</span><div class="dz-success-mark"><i class="fa fa-check-circle-o"></i></div><div class="dz-error-mark"><i class="fa fa-times-circle-o"></i></div><div class="dz-error-message"><span data-dz-errormessage></span></div></div></div></div><div class="progress progress-striped active"><div class="progress-bar progress-bar-success" data-dz-uploadprogress></div></div></div>',

					acceptedFiles: '.csv',
					resize: function(file) {
						var info = { srcX: 0, srcY: 0, srcWidth: file.width, srcHeight: file.height },
							srcRatio = file.width / file.height;
						if (file.height > this.options.thumbnailHeight || file.width > this.options.thumbnailWidth) {
							info.trgHeight = this.options.thumbnailHeight;
							info.trgWidth = info.trgHeight * srcRatio;
							if (info.trgWidth > this.options.thumbnailWidth) {
								info.trgWidth = this.options.thumbnailWidth;
								info.trgHeight = info.trgWidth / srcRatio;
							}
						} else {
							info.trgHeight = file.height;
							info.trgWidth = file.width;
						}
						return info;
					}
				});
				
				var redirects = []
				var errorMessages = []
				dz[0].dropzone.on("error", function(e, msg, xhr){
					if(msg.redirect)
						redirects.push(msg.redirect)
					errorMessages.push(msg)
				})

				dz[0].dropzone.on("queuecomplete", function() {
					if(redirects.length)
						window.location = redirects[0]
					else if(errorMessages.length) {
						errorMessages.forEach(function (msg) {
							Streamr.showError(msg.error)
						})
						errorMessages = []
					} else
						location.reload()
				})
			});
			</script>
			
			<div id="dropzone" class="dropzone-box">
				<div class="dz-default dz-message">
					<i class="fa fa-cloud-upload"></i>
					Drop .csv files here to load history<br><span class="dz-text-small">or click to pick manually</span>
				</div>
				<g:form action="upload" controller="stream">
					<div class="fallback">
						<g:hiddenField name="id" value="${stream.id}"/>
						<input name="file" type="file" multiple="" />
					</div>
				</g:form>
			</div>

		</div>
	</ui:panel>
</div>