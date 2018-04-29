package webserver

import grails.converters.JSON

class UserController {

    def tokenService
    def userService

    def createUser() {

        def request = request.JSON
        log.info("Params: " + params)
        log.info("Request: " + request)

        def user = tokenService.getUser(params.accessToken)
        userService.validateAdminUser(user)
        userService.createUser(request)

        def resp = [:]
        resp.message = "Registro exitoso!"
        response.status = 201
        render resp as JSON
    }
}
