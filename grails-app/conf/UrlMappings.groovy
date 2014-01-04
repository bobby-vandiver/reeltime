class UrlMappings {

	static mappings = {

		"/"(view:"/index")
        "500"(view:'/error')

        "/transcoder/notification" (controller: 'notification', action: 'jobStatusChange')
    }
}
