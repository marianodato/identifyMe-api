package webserver

import grails.converters.JSON
import webserver.exception.BadRequestException

class UserController {

    def tokenService
    def userService
    def utilsService

    def createUser() {
        log.info("UserController - createUser")

        def request = request.JSON
        log.info("Params: " + params)
        log.info("Request: " + request)
        def parameters = [:]
        parameters.accessToken = params.accessToken

        utilsService.validateFields('fields', request, ['username', 'password', 'name', 'dni', 'gender', 'email', 'phoneNumber', 'isAdmin'])
        utilsService.validateFields('params', parameters, ['accessToken'])

        def user = tokenService.getUser(params.accessToken)
        userService.validateAdminUser(user)
        userService.createUser(request)

        def resp = [:]
        resp.message = "Registro exitoso!"
        response.status = 201
        render resp as JSON
    }

    def searchUsers() {
        log.info("UserController - searchUsers")

        log.info("Params: " + params)
        def userAgent = request.getHeader("User-Agent")
        def request = request.JSON
        log.info("Request: " + request)

        def parameters = [:]
        parameters.accessToken = params.accessToken
        parameters.limit = params.limit
        parameters.offset = params.offset

        if (userAgent == "NodeMCU") {
            log.info("Is NodeMCU request")
            utilsService.validateFields('fields', request, ['serialNumber', 'atMacAddress', 'compileDate', 'signature'])
            utilsService.checkSignature(request)
        } else {

            utilsService.validateFields('params', parameters, ['accessToken'])
            def user = tokenService.getUser(params.accessToken)
            userService.validateAdminUser(user)
        }

        if (!params.offset || (params.offset as Integer) < 0) {
            params.offset = 0
        } else {
            params.offset = params.offset as Integer
        }

        if (!params.limit || (params.limit as Integer) < 1 || (params.limit as Integer) > 10) {
            params.limit = 10
        } else {
            params.limit = params.limit as Integer
        }

        if (params.fingerprintStatus && (params.fingerprintStatus != "unenrolled" && params.fingerprintStatus != "pending" && params.fingerprintStatus != "enrolled")) {
            log.error("Invalid value for parameter: fingerprintStatus")
            throw new BadRequestException("Valor inválido para el parámetro: fingerprintStatus")
        }

        def queryUsers = userService.searchUsers(params.offset, params.limit, params.fingerprintStatus)
        log.info("QueryUsers: " + queryUsers)

        def resp = [:]
        def users = []

        queryUsers.results.each {
            def newUser = [:]
            newUser.id = it.id
            newUser.fingerprintId = it.fingerprintId
            newUser.name = it.name
            if (userAgent != "NodeMCU") {
                newUser.username = it.username
                newUser.dni = it.dni
                newUser.gender = it.gender
                newUser.email = it.email
                newUser.phoneNumber = it.phoneNumber
                newUser.dateCreated = it.dateCreated.format("yyyy-MM-dd HH:mm:ss")
                newUser.fingerprintStatus = it.fingerprintStatus
                newUser.isAdmin = it.isAdmin
            }
            users.add(newUser)
        }

        def paging = [:]
        paging.total = queryUsers.total
        paging.limit = params.limit
        paging.offset = queryUsers.offset

        resp.paging = paging
        resp.results = users
        response.status = 200
        render resp as JSON
    }

    def getUser() {
        log.info("UserController - getUser")

        log.info("Params: " + params)
        def parameters = [:]
        parameters.accessToken = params.accessToken
        parameters.id = params.id

        utilsService.validateFields('params', parameters, ['accessToken', 'id'])

        def user = tokenService.getUser(params.accessToken)

        if (user.id != params.id) {
            userService.validateAdminUser(user)
        }

        def queryUser = userService.getUser(params.id)
        log.info("QueryUser: " + queryUser)
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
