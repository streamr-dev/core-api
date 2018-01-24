class BootStrap {

	def bootService
	
    def init = { servletContext ->
		bootService.onInit()
    }
    def destroy = {
    }
}

