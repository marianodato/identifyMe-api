package webserver

import webserver.exception.AuthException

class SessionService {

    static transactional = false
    def tokenService

    def doLogin(String username, String password){
        log.info("SessionService - doLogin")

        def user = User.findByUsername(username)
        log.info("User: " + user)

        if (!user){
            log.error("Invalid username!")
            throw new AuthException("No se pudo autenticar al usuario!")
        }

        if (PasswordHash.validatePassword(password,user.password)) {
            def accessToken = tokenService.generateAccessToken(user.id)
            def resp = [:]
            resp.id = user.id
            resp.accessToken = accessToken
            log.info("Log in successful!")
            return resp
        }

        log.error("Invalid password!")
        throw new AuthException("No se pudo autenticar al usuario!")
    }
}