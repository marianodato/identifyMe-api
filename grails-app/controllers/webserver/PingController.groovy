package webserver

import grails.converters.JSON

class PingController {

    def index() {
        def resp = [:]
        resp.ping = "pong"
        render resp as JSON
    }
}