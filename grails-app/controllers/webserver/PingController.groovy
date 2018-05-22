package webserver

import grails.converters.JSON

class PingController {

    def index() {
        log.info("PingController - index")
        def resp = [:]
        resp.ping = "pong"
        render resp as JSON
    }
}