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
        log.info("UsersService - validateAdminUser")
        if (!user.isAdmin) {
            log.error("Forbidden access for user: " + user)
            throw new ForbiddenException("Acceso denegado para el usuario!")
        }
    }

    def getFingerprintId(def idsUsed, User user, def users) {
        log.info("UsersService - getFingerprintId")
        def result
        for (int i = 1; i < 163; i++) {
            result = idsUsed.find { it == i }
            if (result == null)
                return i
        }
        user.discard()
        users.each {
            it.discard()
        }
        log.error("System surpassed limit capacity!")
        throw new InsufientStorageException("Se ha exedido el límite máximo de usuarios!")
    }

    def validateUserFields(def request) {
        log.info("UsersService - validateUserFields")
        if (request.username && !request.username.matches('(?=^.{6,20}\$)^[a-zA-Z][a-zA-Z0-9]*[._-]?[a-zA-Z0-9]+\$')) {
            log.error("Incorrect value for field: username!")
            throw new BadRequestException("Valor incorrecto para el campo username! Formato: Sólo un caracter especial (._-) permitido y no debe estar en los extremos. El primer caracter no puede ser numérico. Todos los demás caracteres permitidos son letras y números. La longitud total debe estar entre 6 y 20 caracteres")
        }

        if (request.password && !request.password.matches('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\$@\$!#%*?&._-])[A-Za-z\\d\$@\$!#%*?&._-]{8,}')) {
            log.error("Incorrect value for field: password!")
            throw new BadRequestException("Valor incorrecto para el campo password! Formato: Mínimo 8 caracteres, al menos 1 en mayúscula, 1 en minúscula, 1 número y 1 caracter especial")
        }

        if (request.username && request.password && request.username == request.password) {
            log.error("Username cannot be the same as password!")
            throw new BadRequestException("El usuario no puede ser igual a la contraseña!")
        }

        if (request.gender && !request.gender.matches('^male$|^female$')) {
            log.error("Incorrect value for field: gender!")
            throw new BadRequestException("Valor incorrecto para el campo gender! Valores aceptados: male o female")
        }

        if (request.email && !request.email.contains("@")) {
            log.error("Incorrect value for field: email!")
            throw new BadRequestException("Valor incorrecto para el campo email! Debes ingresar un mail válido")
        }

        if (request.phoneNumber && !request.phoneNumber.matches('[\\+]\\d{2}[\\(]\\d{2}[\\)]\\d{4}[\\-]\\d{4}')) {
            log.error("Incorrect value for field: phoneNumber!")
            throw new BadRequestException("Valor incorrecto para el campo phoneNumber! Formato: +54(11)1234-5678")
        }
    }

    def createUser(def request) {
        log.info("UsersService - createUser")
        def user = User.findByUsername(request.username.toString())
        log.info("User: " + user)

        if (user) {
            log.error("Username already registered!")
            throw new BadRequestException("Usuario ya registrado!")
        }

        validateUserFields(request)

        def hash = PasswordHash.createHash(request.password.toString())

        User newUser = new User(username: request.username, name: request.name, dni: request.dni, gender: request.gender, email: request.email, phoneNumber: request.phoneNumber, isAdmin: request.isAdmin, fingerprintId: null, fingerprintStatus: "unenrolled", password: hash, accessToken: null)
        def date = new Date()
        newUser.dateCreated = date
        newUser.lastUpdated = date
        if (!utilsService.saveInstance(newUser)) {
            newUser.discard()
            newUser.errors.each {
                log.error("Error: Error saving to User table: " + it + " . User: " + newUser)
            }
            throw new RuntimeException("Error saving to User table. User: " + newUser)
        }
        log.info("Sign up successful!")
    }

    def searchUsers(def offset, def limit, String fingerprintStatus = null) {
        log.info("UsersService - searchUsers")
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
            users = User.findAll("from User as u where u.fingerprintStatus=? order by u.username asc",
                    [fingerprintStatus], [max: limit, offset: limit * (offset)])
        } else {
            users = User.findAll("from User as u order by u.username asc",
                    [max: limit, offset: limit * (offset)])
        }

        if (!users) {
            log.error("Cannot find users: " + users)
            throw new NotFoundException("No se pudo encontrar usuarios!")
        }

        return [results: users, offset: offset, total: total]
    }

    def getUser(def userId) {
        log.info("UsersService - getUser")
        def user = User.findById(userId)
        log.info("User: " + user)
        if (!user) {
            log.error("Cannot find userId: " + userId)
            throw new NotFoundException("No se pudo encontrar al usuario!")
        }
        log.info("Get successful!")
        return user
    }

    def getUserByFingerprintId(def fingerprintId) {
        log.info("UsersService - getUserByFingerprintId")
        def user = User.findByFingerprintId(fingerprintId)
        log.info("User: " + user)
        if (!user) {
            log.error("Cannot find fingerprintId: " + fingerprintId)
            throw new NotFoundException("No se pudo encontrar al usuario!")
        }
        log.info("Get successful!")
        return user
    }

    def deleteUser(def userId) {
        log.info("UsersService - deleteUser")
        def user = User.findById(userId)
        log.info("User: " + user)
        if (!user) {
            log.error("Cannot find userId: " + userId)
            throw new NotFoundException("No se pudo encontrar al usuario!")
        }

        user.delete(flush: true)

        log.info("Delete successful!")
    }

    def modifyUser(def request, User caller, User user, boolean isNodeMCU) {
        log.info("UsersService - modifyUser")
        if (request.username) {
            log.error("Cannot modify field: username!")
            throw new BadRequestException("No se puede modificar el campo: username!")
        }

        validateUserFields(request)

        if (request.password) {
            if (request.password == user.username) {
                log.error("Username cannot be the same as password!")
                throw new BadRequestException("El usuario no puede ser igual a la contraseña!")
            }
            def hash = PasswordHash.createHash(request.password.toString())
            user.password = hash
        }

        if (request.gender) {
            user.gender = request.gender
        }

        if (request.email) {
            user.email = request.email
        }

        if (request.phoneNumber) {
            user.phoneNumber = request.phoneNumber
        }

        if (request.name) {
            user.name = request.name
        }

        if (request.dni) {
            user.dni = request.dni
        }

        if (request.isAdmin != null || request.fingerprintStatus) {
            if ((caller && !caller.isAdmin) && !isNodeMCU) {
                user.discard()
                log.error("Cannot update fields: isAdmin and fingerprintStatus without admin privileges!")
                throw new ForbiddenException("No se puede modificar los campos: isAdmin y fingerprintStatus sin permisos de administrador!")
            }

            if (request.isAdmin != null) {
                user.isAdmin = request.isAdmin
            }

            if (request.fingerprintStatus) {
                if (request.fingerprintStatus != "unenrolled" && request.fingerprintStatus != "pending" && request.fingerprintStatus != "enrolled") {
                    user.discard()
                    log.error("Invalid value for parameter: fingerprintStatus!")
                    throw new BadRequestException("Valor inválido para el parámetro: fingerprintStatus!")
                } else {

                    if (request.fingerprintStatus == "unenrolled") {
                        user.fingerprintId = null

                    } else if (request.fingerprintStatus == "pending") {

                        def users = User.findAllByFingerprintStatus("pending")
                        users.each {
                            it.fingerprintStatus = "unenrolled"
                            it.fingerprintId = null
                            if (!utilsService.saveInstance(it)) {
                                it.discard()
                                it.errors.each {
                                    log.error("Error: Error saving to User table: " + it)
                                }
                                throw new RuntimeException("Error saving to User table.User: " + it)
                            }
                        }
                        def idsUsed = User.executeQuery('select u.fingerprintId from User u where u.fingerprintId != null')
                        def fingerprintId = getFingerprintId(idsUsed, user, users)
                        user.fingerprintId = fingerprintId

                    } else { //enrolled
                        if (user.fingerprintStatus != "pending") {
                            user.discard()
                            log.error("Invalid value for parameter: fingerprintStatus!")
                            throw new BadRequestException("Valor inválido para el parámetro: fingerprintStatus!")
                        }
                    }
                    user.fingerprintStatus = request.fingerprintStatus
                }
            }
        }

        def date = new Date()
        user.lastUpdated = date
        if (!utilsService.saveInstance(user)) {
            user.discard()
            user.errors.each {
                log.error("Error: Error saving to User table: " + it + " . User: " + user)
            }
            throw new RuntimeException("Error saving to User table.User: " + user)
        }
        log.info("Update successful!")
        return user
    }

}
