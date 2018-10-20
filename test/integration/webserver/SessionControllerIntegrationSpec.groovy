package webserver

import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import spock.lang.Shared
import webserver.exception.AuthException
import webserver.exception.BadRequestException

class SessionControllerIntegrationSpec extends IntegrationSpec {

    @Shared
    SessionController controller = new SessionController()

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

    void "test doLogin invalid parameters 1"() {
        given:
        controller.request.method = 'POST'
        when:
        controller.doLogin()
        then:
        thrown BadRequestException
    }

    void "test doLogin invalid parameters 2"() {
        given:
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        when:
        controller.doLogin()
        then:
        thrown BadRequestException
    }

    void "test doLogin invalid parameters 3"() {
        given:
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        controller.params.accessToken = "accessToken"
        when:
        controller.doLogin()
        then:
        thrown BadRequestException
    }

    void "test doLogin cannot authenticate user 1"() {
        given:
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        when:
        controller.doLogin()
        then:
        thrown AuthException
    }

    void "test doLogin cannot authenticate user 2"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "incorrect"
        when:
        controller.doLogin()
        then:
        thrown AuthException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test doLogin save fails"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        controller.sessionService.tokenService.utilsService.metaClass.saveInstance { def instance ->
            return false
        }
        when:
        controller.doLogin()
        then:
        thrown RuntimeException
        User savedUser = User.findById(user.id)
        savedUser.accessToken == null
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.sessionService.tokenService.utilsService)
    }

    void "test doLogin ok"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        controller.sessionService.tokenService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush:true)
        }
        when:
        controller.doLogin()
        then:
        controller.response.status == 201
        controller.response.json == JSON.parse("{\"accessToken\": $user.accessToken, \"id\": $user.id}")
        User savedUser = User.findById(user.id)
        savedUser.accessToken == user.accessToken
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.sessionService.tokenService.utilsService)
    }

    void "test doLogout bad request"() {
        given:
        controller.request.method = 'POST'
        when:
        controller.doLogout()
        then:
        thrown BadRequestException
    }

    void "test doLogout cannot authenticate"() {
        given:
        controller.request.method = 'POST'
        controller.params.accessToken = "accessToken"
        when:
        controller.doLogout()
        then:
        thrown AuthException
    }

    void "test doLogout save fails"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.sessionService.tokenService.utilsService.metaClass.saveInstance { def instance ->
            return false
        }
        when:
        controller.doLogout()
        then:
        thrown RuntimeException
        User savedUser = User.findById(user.id)
        savedUser.accessToken == "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.sessionService.tokenService.utilsService)
    }

    void "test doLogout ok"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.sessionService.tokenService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush:true)
        }
        when:
        controller.doLogout()
        then:
        controller.response.status == 204
        controller.response.json == JSON.parse("{}")
        User savedUser = User.findById(user.id)
        savedUser.accessToken == null
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.sessionService.tokenService.utilsService)
    }
}