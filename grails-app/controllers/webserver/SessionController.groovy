package webserver

import grails.converters.JSON

class SessionController {

    def sessionService
    def tokenService
    def utilsService

    def doLogin() {
        log.info("SessionController - doLogin")

        def request = request.JSON
        log.info("Params: " + params)
        log.info("Request: " + request)
        def parameters = [:]
        parameters.accessToken = params.accessToken

        utilsService.validateFields('fields', request, ['username', 'password'])
        utilsService.validateFields('params', parameters, null, ['accessToken'])

        def resp = sessionService.doLogin(request.username, request.password)
        log.info("Response: " + resp)
        response.status = 201
        render resp as JSON
    }

    def doLogout() {
        log.info("SessionController - doLogout")

        log.info("Params: " + params)
        def parameters = [:]
        parameters.accessToken = params.accessToken

        utilsService.validateFields('params', parameters, ['accessToken'])

        def user = tokenService.getUser(params.accessToken)
        tokenService.deleteAccessToken(user)

        def resp = [:]
        response.status = 204
        render resp as JSON
    }
}