package webserver

import grails.transaction.Transactional
import webserver.exception.BadRequestException
import webserver.exception.ForbiddenException
import webserver.exception.InsufientStorageException

@Transactional
class UserService {

    def utilsService

    def validateAdminUser(User user) {
        if (!user.isAdmin) {
            log.error("Forbidden access for user: " + user)
            throw new ForbiddenException("Acceso denegado para el usuario!")
        }
    }

    def getFingerprintId(def idsUsed) {
        def result
        for (int i = 1; i < 163; i++) {
            result = idsUsed.find { it == i }
            if (result == null)
                return i
        }
        log.error("System surpassed limit capacity!")
        throw new InsufientStorageException("Se ha exedido el límite máximo de usuarios")
    }

    def createUser(def request) {
        def user = User.findByUsername(request.username)
        log.info("User: " + user)

        if (user) {
            log.error("Username already registered!")
            throw new BadRequestException("Usuario ya registrado!")
        }

        if (request.username == request.password) {
            log.error("Username cannot be the same as password!")
            throw new BadRequestException("El usuario no puede ser igual a la contraseña!")
        }

        def idsUsed = User.executeQuery('select u.fingerprintId from User u')
        def fingerprintId = getFingerprintId(idsUsed)
        def hash = PasswordHash.createHash(request.password)

        User newUser = new User(username: request.username, name: request.name, dni: request.dni, gender: request.gender, email: request.email, phoneNumber: request.phoneNumber, isAdmin: request.isAdmin, fingerprintId: fingerprintId, fingerprintStatus: request.fingerprintStatus ? request.fingerprintStatus : "unenrolled", password: hash, accessToken: null)
        newUser.dateCreated = new Date()
        if (!utilsService.saveInstance(newUser)) {
            newUser.discard()
            newUser.errors.each {
                log.error("Error: Error saving to User table: " + it + " . User: " + newUser)
            }
            throw new RuntimeException("Error saving to User table.User: " + newUser)
        }
        log.info("Sign up successful!")
    }

}
