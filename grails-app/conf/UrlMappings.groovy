class UrlMappings {

    static mappings = {

        "500"(controller: "error", action: "handleError")
        "/**"(controller: "error", action: "notFound")
    }
}
