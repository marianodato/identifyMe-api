package webserver

import grails.converters.JSON
import webserver.exception.BadRequestException

class LoginController {

    def loginService
    def tokenService

    def login(){

        def request = request.JSON
        log.info("Params: " + params)
        log.info("Request: " + request)

        if (!request.username || !request.password || params.accessToken) {
            log.error("Invalid Paramenters!")
            throw new BadRequestException("Parámetros inválidos!")
        }

        def accessToken = loginService.doLogin(request.username, request.password)
        def resp = [:]
        resp.accessToken = accessToken
        response.status = 201
        render resp as JSON
    }

    def logout(){
        log.info("Params: " + params)

        def user = tokenService.getUser(params.accessToken)
        tokenService.deleteAccessToken(user)

        def resp = [:]
        resp.message = "Usuario deslogueado!"
        response.status = 201
        render resp as JSON
    }
}
