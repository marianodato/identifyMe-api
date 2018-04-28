package webserver

import grails.converters.JSON
import webserver.exception.BadRequestException

class LoginController {

    def loginService
    def tokenService

    def login(){

        log.info("Params: " + params)

        if (!params.username || !params.password || params.accessToken){
            log.error("Invalid Paramenters!")
            throw new BadRequestException("Parámetros inválidos!")
        }

        def accessToken = loginService.doLogin(params.username, params.password)
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
        resp.message = "User logged out!"
        response.status = 201
        render resp as JSON
    }
}
