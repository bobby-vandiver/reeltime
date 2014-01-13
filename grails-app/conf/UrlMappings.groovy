class UrlMappings {

	static mappings = {

        // TODO: Add dedicated error controller to handle exceptions and don't throw back to user
        "/"(view:"/index")
        "500"(view:'/error')

        "/transcoder/notification/$action" (controller: 'notification')

        "/video" (controller: 'video', action: 'upload')
    }
}
