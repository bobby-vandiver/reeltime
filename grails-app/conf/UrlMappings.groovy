class UrlMappings {

	static mappings = {

        "/"(view:"/index")
        "500"(view:'/error')

        "/transcoder/notification/$action" (controller: 'notification')
    }
}
