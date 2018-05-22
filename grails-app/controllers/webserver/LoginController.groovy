package webserver

import grails.converters.JSON

class LoginController {

    def loginService
    def tokenService
    def utilsService

    def login(){
        log.info("LoginController - login")

        def request = request.JSON
        log.info("Params: " + params)
        log.info("Request: " + request)
        def parameters = [:]
        parameters.accessToken = params.accessToken

        utilsService.validateFields('fields', request, ['username', 'password'])
        utilsService.validateFields('params', parameters, null, ['accessToken'])

        def accessToken = loginService.doLogin(request.username, request.password)
        log.info("AccessToken: " + accessToken)
        def resp = [:]
        resp.accessToken = accessToken
        response.status = 201
        render resp as JSON
    }

    def logout(){
        log.info("LoginController - logout")

        log.info("Params: " + params)
        def parameters = [:]
        parameters.accessToken = params.accessToken

        utilsService.validateFields('params', parameters, ['accessToken'])

        def user = tokenService.getUser(params.accessToken)
        tokenService.deleteAccessToken(user)

        def resp = [:]
        resp.message = "Usuario deslogueado!"
        response.status = 201
        render resp as JSON
    }
}
