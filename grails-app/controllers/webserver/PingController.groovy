package webserver

import grails.converters.JSON

class PingController {

    def index() {
        log.info("Entered Ping Controller!")
        log.info("Params: " + params)
        def request = request.JSON
        log.info("Request: " + request)
        render request as JSON
    }
}