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

    def getUser() {
        log.info("Params: " + params)
        def user = tokenService.getUser(params.accessToken)

        if (user.id != params.id) {
            userService.validateAdminUser(user)
        }

        def queryUser = userService.getUser(params.id)
        def resp = [:]
        resp.id = queryUser.id
        resp.username = queryUser.username
        resp.name = queryUser.name
        resp.dni = queryUser.dni
        resp.gender = queryUser.gender
        resp.email = queryUser.email
        resp.phoneNumber = queryUser.phoneNumber
        resp.dateCreated = queryUser.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        resp.fingerprintId = queryUser.fingerprintId
        resp.fingerprintStatus = queryUser.fingerprintStatus
        resp.isAdmin = queryUser.isAdmin
        response.status = 200
        render resp as JSON

    }
}
