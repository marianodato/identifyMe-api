package webserver

import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import spock.lang.Shared
import webserver.exception.*

class UserControllerIntegrationSpec extends IntegrationSpec {

    @Shared
    UserController controller = new UserController()

    void setup() {
        org.apache.log4j.BasicConfigurator.configure()
        LogManager.getRootLogger().setLevel(Level.INFO)
    }

    static void revokeMetaClassChanges(Class type, def instance = null) {
        GroovySystem.metaClassRegistry.removeMetaClass(type)
        if (instance != null) {
            instance.metaClass = null
        }
    }

    void "test createUser bad request 1"() {
        given:
        controller.request.method = 'POST'
        when:
        controller.createUser()
        then:
        thrown BadRequestException
    }

    void "test createUser bad request 2"() {
        given:
        controller.request.method = 'POST'
        controller.request.JSON.username = "Test77"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown BadRequestException
    }

    void "test createUser cannot authenticate"() {
        given:
        controller.request.method = 'POST'
        controller.params.accessToken = "accessToken"
        controller.request.JSON.username = "Test77"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown AuthException
    }

    void "test createUser forbidden access"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Test77"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown ForbiddenException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test createUser username already registered"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test createUser username invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "pepe"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test createUser password invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Papapa"
        controller.request.JSON.password = "Password"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test createUser username same as password"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Passw-0rd"
        controller.request.JSON.password = "Passw-0rd"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test createUser gender invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Papapa"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "males"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test createUser email invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Papapa"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test createUser phoneNumber invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Papapa"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "(11)1234-5678"
        controller.request.JSON.isAdmin = false
        when:
        controller.createUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test createUser save fails"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Papapa"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = "hola"
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return false
        }
        when:
        controller.createUser()
        then:
        thrown RuntimeException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test createUser ok"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Pepepe2", name: "Pepe Pepe 2", dni: 12345678902, gender: "male", email: "pepepe2@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "Test77"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.dni = 123456789
        controller.request.JSON.name = "Test77"
        controller.request.JSON.gender = "male"
        controller.request.JSON.email = "test77@gmail.com"
        controller.request.JSON.phoneNumber = "+54(11)1234-5678"
        controller.request.JSON.isAdmin = false
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.createUser()
        then:
        controller.response.status == 201
        controller.response.json == JSON.parse("{\"message\": \"Registro exitoso!\"}")
        User newUser = User.findByUsername("Test77")
        newUser.fingerprintId == null
        newUser.fingerprintStatus == "unenrolled"
        User.count() == 3
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test searchUser bad request nodemcu"() {
        given:
        controller.request.method = 'GET'
        controller.request.addHeader("User-Agent", "NodeMCU")
        when:
        controller.searchUsers()
        then:
        thrown BadRequestException
    }

    void "test searchUser invalid signature 1 nodemcu"() {
        given:
        controller.request.method = 'GET'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02082"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3c"
        controller.request.JSON.compileDate = "May  1 2018 11:00:49"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        when:
        controller.searchUsers()
        then:
        thrown AuthException
    }

    void "test searchUser invalid signature 2 nodemcu"() {
        given:
        controller.request.method = 'GET'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c5"
        when:
        controller.searchUsers()
        then:
        thrown AuthException
    }

    void "test searchUser bad request 1"() {
        given:
        controller.request.method = 'GET'
        when:
        controller.searchUsers()
        then:
        thrown BadRequestException
    }

    void "test searchUser cannot authenticate"() {
        given:
        controller.request.method = 'GET'
        controller.params.accessToken = "accessToken"
        when:
        controller.searchUsers()
        then:
        thrown AuthException
    }

    void "test searchUser forbidden access"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        when:
        controller.searchUsers()
        then:
        thrown ForbiddenException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchUser invalid fingerprintStatus"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.fingerprintStatus = "unknown"
        when:
        controller.searchUsers()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchUser cannot find users"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.fingerprintStatus = "pending"
        when:
        controller.searchUsers()
        then:
        thrown NotFoundException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchUser ok 1"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.fingerprintStatus = "unenrolled"
        when:
        controller.searchUsers()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 1, \"limit\": 10, \"offset\":0}, results: [{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\", \"isAdmin\": true, \"lastUpdated\": \"$dateParsed\"}]}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchUser ok 2"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.offset = 10
        controller.params.limit = 20
        when:
        controller.searchUsers()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 1, \"limit\": 10, \"offset\":0}, results: [{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\", \"isAdmin\": true, \"lastUpdated\": \"$dateParsed\"}]}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchUser ok 3"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        User user3 = new User(username: "Pipipi", name: "Pipi Pipi", dni: 1234567892, gender: "male", email: "pipipi@gmail.com", phoneNumber: "+54(11)1234-5670", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date3 = new Date()
        user3.dateCreated = date3
        user3.lastUpdated = date3
        user3.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.offset = 1
        controller.params.limit = 2
        when:
        controller.searchUsers()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 3, \"limit\": 2, \"offset\":1}, results: [{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\", \"isAdmin\": true, \"lastUpdated\": \"$dateParsed\"}]}")
        User.count() == 3
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchUser ok 4"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        User user3 = new User(username: "Pipipi", name: "Pipi Pipi", dni: 1234567892, gender: "male", email: "pipipi@gmail.com", phoneNumber: "+54(11)1234-5670", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date3 = new Date()
        user3.dateCreated = date3
        user3.lastUpdated = date3
        user3.save(flush: true)
        User user4 = new User(username: "Popopo", name: "Popo Popo", dni: 1234567893, gender: "male", email: "popopo@gmail.com", phoneNumber: "+54(11)1234-5671", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date4 = new Date()
        user4.dateCreated = date4
        user4.lastUpdated = date4
        user4.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.offset = 1
        controller.params.limit = 2
        when:
        controller.searchUsers()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        def dateParsed2 = user2.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 4, \"limit\": 2, \"offset\":1}, results: [{\"id\": $user2.id, \"username\": \"Papapa\", \"name\": \"Papa Papa\", \"dni\": 1234567891, \"gender\":\"male\", \"email\": \"papapa@gmail.com\", \"phoneNumber\": \"+54(11)1234-5679\", \"dateCreated\": \"$dateParsed2\", \"fingerprintId\": 1, \"fingerprintStatus\": \"pending\", \"isAdmin\": false, \"lastUpdated\": \"$dateParsed2\"},{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\", \"isAdmin\": true, \"lastUpdated\": \"$dateParsed\"}]}")
        User.count() == 4
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchUser ok nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        User user3 = new User(username: "Pipipi", name: "Pipi Pipi", dni: 1234567892, gender: "male", email: "pipipi@gmail.com", phoneNumber: "+54(11)1234-5670", isAdmin: false, fingerprintId: 2, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date3 = new Date()
        user3.dateCreated = date3
        user3.lastUpdated = date3
        user3.save(flush: true)
        User user4 = new User(username: "Popopo", name: "Popo Popo", dni: 1234567893, gender: "male", email: "popopo@gmail.com", phoneNumber: "+54(11)1234-5671", isAdmin: false, fingerprintId: 3, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date4 = new Date()
        user4.dateCreated = date4
        user4.lastUpdated = date4
        user4.save(flush: true)
        controller.request.method = 'GET'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.offset = 0
        controller.params.limit = 2
        controller.params.fingerprintStatus = "pending"
        when:
        controller.searchUsers()
        then:
        controller.response.status == 200
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 3, \"limit\": 2, \"offset\":0}, results: [{\"id\": $user4.id, \"name\": \"Popo Popo\", \"fingerprintId\": 3},{\"id\": $user3.id, \"name\": \"Pipi Pipi\", \"fingerprintId\": 2} ]}")
        User.count() == 4
        cleanup:
        User.deleteAll(User.list())
    }

    void "test getUser bad request"() {
        given:
        controller.request.method = 'GET'
        when:
        controller.getUser()
        then:
        thrown BadRequestException
    }

    void "test getUser cannot authenticate"() {
        given:
        controller.request.method = 'GET'
        controller.params.accessToken = "accessToken"
        controller.params.id = 1
        when:
        controller.getUser()
        then:
        thrown AuthException
    }

    void "test getUser forbidden access"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.id = user.id + 1
        when:
        controller.getUser()
        then:
        thrown ForbiddenException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test getUser cannot find user"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.id = user.id + 1
        when:
        controller.getUser()
        then:
        thrown NotFoundException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test getUser ok"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.id = user.id
        when:
        controller.getUser()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\", \"isAdmin\": false, \"lastUpdated\": \"$dateParsed\"}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test deleteUser bad request"() {
        given:
        controller.request.method = 'DELETE'
        when:
        controller.deleteUser()
        then:
        thrown BadRequestException
    }

    void "test deleteUser cannot authenticate"() {
        given:
        controller.request.method = 'DELETE'
        controller.params.accessToken = "accessToken"
        controller.params.id = 1
        when:
        controller.deleteUser()
        then:
        thrown AuthException
    }

    void "test deleteUser forbidden access"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'DELETE'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.id = user.id + 1
        when:
        controller.deleteUser()
        then:
        thrown ForbiddenException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test deleteUser cannot find user"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'DELETE'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.id = user.id + 1
        when:
        controller.deleteUser()
        then:
        thrown NotFoundException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test deleteUser ok"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'DELETE'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.id = user.id
        when:
        controller.deleteUser()
        then:
        controller.response.status == 204
        controller.response.json == JSON.parse("{}")
        User.count() == 0
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser bad request 1 nodemcu"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
    }

    void "test modifyUser bad request 2 nodemcu"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.params.id = 1
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
    }

    void "test modifyUser invalid signature 1 nodemcu"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.params.id = 1
        controller.request.JSON.serialNumber = "75330303035351F02082"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3c"
        controller.request.JSON.compileDate = "May  1 2018 11:00:49"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        when:
        controller.modifyUser()
        then:
        thrown AuthException
    }

    void "test modifyUser invalid signature 2 nodemcu"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.params.id = 1
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c5"
        when:
        controller.modifyUser()
        then:
        thrown AuthException
    }

    void "test modifyUser bad request 1"() {
        given:
        controller.request.method = 'PUT'
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
    }

    void "test modifyUser cannot authenticate"() {
        given:
        controller.request.method = 'PUT'
        controller.params.id = 1
        controller.params.accessToken = "accessToken"
        when:
        controller.modifyUser()
        then:
        thrown AuthException
    }

    void "test modifyUser forbidden access"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = 2
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        when:
        controller.modifyUser()
        then:
        thrown ForbiddenException
        User.count() == 2
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser user not found access"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = 2
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        when:
        controller.modifyUser()
        then:
        thrown NotFoundException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser cannot modify username"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.username = "username"
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser password invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.password = "Password"
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser gender invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.gender = "males"
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser email invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.email = "test77gmail.com"
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser phoneNumber invalid"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.phoneNumber = "(11)1234-5678"
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }


    void "test modifyUser username same as password"() {
        given:
        User user = new User(username: "Pepepe-1", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.password = "Pepepe-1"
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser save fails"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.password = "Pepepe-1"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return false
        }
        when:
        controller.modifyUser()
        then:
        thrown RuntimeException
        // TODO TEST TRANSACTIONAL ROLLBACK
        // user.password == "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a"
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser ok 1"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.password = "Password1*"
        controller.request.JSON.gender = "female"
        controller.request.JSON.email = "papapa@gmail.com"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        def lastUpdatedParsed = user.lastUpdated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"female\", \"email\": \"papapa@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\", \"isAdmin\": false, \"lastUpdated\": \"$lastUpdatedParsed\"}")
        user.password != "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a"
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser ok 2"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user2.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.phoneNumber = "+54(11)1234-5677"
        controller.request.JSON.name = "Pipi Pipi"
        controller.request.JSON.dni = 1234567892
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        def dateParsed = user2.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        def lastUpdatedParsed = user2.lastUpdated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"id\": $user2.id, \"username\": \"Papapa\", \"name\": \"Pipi Pipi\", \"dni\": 1234567892, \"gender\":\"male\", \"email\": \"papapa@gmail.com\", \"phoneNumber\": \"+54(11)1234-5677\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\", \"isAdmin\": false, \"lastUpdated\": \"$lastUpdatedParsed\"}")
        User.count() == 2
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser ok 1 nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.request.JSON.gender = "female"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        user.gender == "female"
        controller.response.json == JSON.parse("{\"id\": $user.id, \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\"}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser forbidden"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = true
        when:
        controller.modifyUser()
        then:
        thrown ForbiddenException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser ok 2 nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.request.JSON.isAdmin = true
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        user.isAdmin
        controller.response.json == JSON.parse("{\"id\": $user.id, \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\"}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser bad request"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "test"
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
        // TODO TEST TRANSACTIONAL ROLLBACK
        // user.isAdmin
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser ok 3"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "unenrolled"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        def lastUpdatedParsed = user.lastUpdated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": null, \"fingerprintStatus\": \"unenrolled\", \"isAdmin\": false, \"lastUpdated\": \"$lastUpdatedParsed\"}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser bad request 2"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "enrolled"
        when:
        controller.modifyUser()
        then:
        thrown BadRequestException
        // TODO TEST TRANSACTIONAL ROLLBACK
        // user.isAdmin
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyUser ok 4"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "enrolled"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        def lastUpdatedParsed = user.lastUpdated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": 1, \"fingerprintStatus\": \"enrolled\", \"isAdmin\": false, \"lastUpdated\": \"$lastUpdatedParsed\"}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser save fails 2"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: true, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "pending"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return false
        }
        when:
        controller.modifyUser()
        then:
        thrown RuntimeException
        // TODO TEST TRANSACTIONAL ROLLBACK
        // user.isAdmin
        // user2.fingerprintId == 1
        // user2.fingerprintStatus == "pending"
        User.count() == 2
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser surpass limit capacity"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: true, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "pending"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        controller.userService.metaClass.getFingerprintId { _ ->
            throw new InsufientStorageException("Se ha exedido el límite máximo de usuarios")
        }
        when:
        controller.modifyUser()
        then:
        thrown InsufientStorageException
        // TODO TEST TRANSACTIONAL ROLLBACK
        // user.isAdmin
        // user2.fingerprintId == 1
        // user2.fingerprintStatus == "pending"
        User.count() == 2
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
        revokeMetaClassChanges(UserService, controller.userService)
    }

    void "test modifyUser ok 5"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "pending"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        def lastUpdatedParsed = user.lastUpdated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": 1, \"fingerprintStatus\": \"pending\", \"isAdmin\": false, \"lastUpdated\": \"$lastUpdatedParsed\"}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser ok 6"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: true, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "pending"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        def lastUpdatedParsed = user.lastUpdated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": 1, \"fingerprintStatus\": \"pending\", \"isAdmin\": false, \"lastUpdated\": \"$lastUpdatedParsed\"}")
        user2.fingerprintStatus == "unenrolled"
        user2.fingerprintId == null
        User.count() == 2
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser ok 7"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.JSON.isAdmin = false
        controller.request.JSON.fingerprintStatus = "pending"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        def dateParsed = user.dateCreated.format("yyyy-MM-dd HH:mm:ss")
        def lastUpdatedParsed = user.lastUpdated.format("yyyy-MM-dd HH:mm:ss")
        controller.response.json == JSON.parse("{\"id\": $user.id, \"username\": \"Pepepe\", \"name\": \"Pepe Pepe\", \"dni\": 1234567890, \"gender\":\"male\", \"email\": \"pepepe@gmail.com\", \"phoneNumber\": \"+54(11)1234-5678\", \"dateCreated\": \"$dateParsed\", \"fingerprintId\": 2, \"fingerprintStatus\": \"pending\", \"isAdmin\": false, \"lastUpdated\": \"$lastUpdatedParsed\"}")
        user2.fingerprintStatus == "enrolled"
        user2.fingerprintId == 1
        User.count() == 2
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }

    void "test modifyUser ok 3 nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.params.id = user.id
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.request.JSON.fingerprintStatus = "enrolled"
        controller.userService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.modifyUser()
        then:
        controller.response.status == 200
        controller.response.json == JSON.parse("{\"id\": $user.id, \"fingerprintId\": 1, \"fingerprintStatus\": \"enrolled\"}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.userService.utilsService)
    }
}
