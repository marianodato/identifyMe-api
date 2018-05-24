package webserver

import grails.converters.JSON

class PingController {

    def index() {
        log.info("PingController - index")
        def resp = [:]
        resp.ping = "pong"
        response.status = 200
        render resp as JSON
    }
}