package webserver

import grails.converters.JSON

class LoginController {

    def loginService
    def tokenService
    def utilsService

    def login(){

        def request = request.JSON
        log.info("Params: " + params)
        log.info("Request: " + request)
        request.accessToken = params.accessToken

        utilsService.validateFields(request, ['username', 'password'], ['accessToken'])

        def accessToken = loginService.doLogin(request.username, request.password)
        def resp = [:]
        resp.accessToken = accessToken
        response.status = 201
        render resp as JSON
    }

    def logout(){
        log.info("Params: " + params)
        def request = [:]
        request.accessToken = params.accessToken

        utilsService.validateFields(request, ['accessToken'])

        def user = tokenService.getUser(params.accessToken)
        tokenService.deleteAccessToken(user)

        def resp = [:]
        resp.message = "Usuario deslogueado!"
        response.status = 201
        render resp as JSON
    }
}
