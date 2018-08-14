<html>
	<head>
		<r:require module="streamr"/>
		<webpack:cssBundle name="authPage"/>
		<style>
			body {
				background-color: #0D009A;
			}
			#authPageRoot {
				height: 100%;
				width: 100%;
			}
		</style>
	</head>
	<body>
		<div id="authPageRoot"></div>
		<webpack:jsBundle name="commons"/>
		<webpack:jsBundle name="authPage"/>
	</body>
</html>
