package webserver

import grails.transaction.Transactional
import webserver.exception.BadRequestException
import webserver.exception.ForbiddenException
import webserver.exception.InsufientStorageException
import webserver.exception.NotFoundException

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
        def user = User.findByUsername(request.username.toString())
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
        def hash = PasswordHash.createHash(request.password.toString())

        User newUser = new User(username: request.username, name: request.name, dni: request.dni, gender: request.gender, email: request.email, phoneNumber: request.phoneNumber, isAdmin: request.isAdmin, fingerprintId: fingerprintId, fingerprintStatus: "unenrolled", password: hash, accessToken: null)
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

    def searchUsers(def offset, def limit, String fingerprintStatus = null) {
        def total

        if (fingerprintStatus) {
            total = User.countByFingerprintStatus(fingerprintStatus)
        } else {
            total = User.count()
        }

        def offsetCount = (total / limit) as Integer

        if (total % limit != 0)
            offsetCount += 1

        if (offset >= offsetCount)
            offset = 0

        def users

        if (fingerprintStatus) {
            users = User.findAll("from User as u where u.fingerprintStatus=? order by u.dateCreated desc",
                    [fingerprintStatus], [max: limit, offset: limit * (offset)])
        } else {
            users = User.findAll("from User as u order by u.dateCreated desc",
                    [max: limit, offset: limit * (offset)])
        }

        if (!users) {
            log.error("Cannot find users: " + users)
            throw new NotFoundException("No se pudo encontrar usuarios!")
        }

        return [results: users, offset: offset, total: total]
    }

    def getUser(Long userId) {
        def user = User.findById(userId)
        log.info("User: " + user)
        if (!user) {
            log.error("Cannot find userId: " + userId)
            throw new NotFoundException("No se pudo encontrar al usuario!")
        }
        log.info("Get successful!")
        return user
    }

    def getPendingUser() {
        def user = User.findByFingerprintStatus("pending")
        log.info("User: " + user)
        if (!user) {
            log.error("Cannot find pending user")
            throw new NotFoundException("No se pudo encontrar al usuario pendiente de carga!")
        }
        log.info("Get successful!")
        return user
    }
}
