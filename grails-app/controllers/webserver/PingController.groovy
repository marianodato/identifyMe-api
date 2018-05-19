package webserver

import grails.converters.JSON

class PingController {

    def index() {
        log.info("Entered Ping Controller!")
        def resp = [:]
        resp.ping = "pong"
        render resp as JSON
    }
}