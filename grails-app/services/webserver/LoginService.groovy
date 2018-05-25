package webserver

import webserver.exception.AuthException

class LoginService {

    static transactional = false
    def tokenService

    def doLogin(String username, String password){

        log.info("loginService - doLogin")

        def user = User.findByUsername(username)

        log.info("User: " + user)

        if (!user){
            log.error("Invalid username!")
            throw new AuthException("No se pudo autenticar al usuario!")
        }

        if (PasswordHash.validatePassword(password,user.password)) {

            def accessToken = tokenService.generateAccessToken(user.id)

            log.info("Log in successful!")
            return accessToken
        }

        log.error("Invalid password!")
        throw new AuthException("No se pudo autenticar al usuario!")
    }
}
