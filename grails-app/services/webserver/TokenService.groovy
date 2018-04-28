package webserver

import grails.transaction.Transactional
import webserver.exception.AuthException

@Transactional
class TokenService {

    def utilsService

    def generateAccessToken(Long user_id) {

        def user = User.findById(user_id)

        log.info("User: " + user)

        user.accessToken = PasswordHash.createRandomSaltString()

        log.info("Access Token: " + user.accessToken)

        if (!utilsService.saveInstance(user)){
            user.discard()
            user.errors.each {
                log.error("Error saving to User table: " + it + ". User: " + user)
            }
            throw new RuntimeException("Error saving to User table. User: " + user )
        }

        log.info("Access Token created!")
        def accessToken = user.accessToken

        return accessToken
    }

    def getUser(def accessToken){

        log.info("Access Token: " + accessToken)

        if (!accessToken) {
            throw new AuthException("No se pudo autenticar al usuario!")
        }

        def user = User.findByAccessToken(accessToken)

        if (!user) {
            throw new AuthException("No se pudo autenticar al usuario!")
        }

        return user
    }

    def deleteAccessToken(User user){

        log.info("User: " + user)
        user.accessToken = null

        if (!utilsService.saveInstance(user)){
            user.discard()
            user.errors.each {
                log.error("Error saving to User table: " + it + ". User: " + user)
            }
            throw new RuntimeException("Error saving to User table.User: " + user )
        }

        log.info("Access Token deleted!")
    }
}