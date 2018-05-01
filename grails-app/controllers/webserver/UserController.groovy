package webserver

import grails.converters.JSON

class UserController {

    def tokenService
    def userService
    def utilsService

    def createUser() {

        def request = request.JSON
        log.info("Params: " + params)
        log.info("Request: " + request)
        request.accessToken = params.accessToken

        utilsService.validateFields(request, ['accessToken', 'username', 'password', 'name', 'dni', 'gender', 'email', 'phoneNumber', 'isAdmin'])

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
        def request = [:]
        request.accessToken = params.accessToken
        request.id = params.id

        utilsService.validateFields(request, ['accessToken', 'id'])

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

    def getPendingUser() {
        def request = request.JSON
        log.info("Request: " + request)

        utilsService.validateFields(request, ['serialNumber', 'atMacAddress', 'compileDate', 'signature'])
        utilsService.checkSignature(request)

        User user = userService.getPendingUser()
        def resp = [:]
        resp.id = user.fingerprintId
        response.status = 200
        render resp as JSON
    }
}
