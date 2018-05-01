package webserver

import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import spock.lang.Shared
import webserver.exception.AuthException
import webserver.exception.BadRequestException

class LoginControllerIntegrationSpec extends IntegrationSpec {

    @Shared
    LoginController controller = new LoginController()

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

    void "test login invalid parameters 1"(){
        given:
        controller.request.method = 'POST'
        when:
        controller.login()
        then:
        thrown BadRequestException
    }

    void "test login invalid parameters 2"(){
        given:
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        when:
        controller.login()
        then:
        thrown BadRequestException
    }

    void "test login invalid parameters 3"(){
        given:
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        controller.params.accessToken = "accessToken"
        when:
        controller.login()
        then:
        thrown BadRequestException
    }

    void "test login cannot authenticate user 1"(){
        given:
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        when:
        controller.login()
        then:
        thrown AuthException
    }

    void "test login cannot authenticate user 2"(){
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: 1, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        user.dateCreated = new Date()
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "incorrect"
        when:
        controller.login()
        then:
        thrown AuthException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test login save fails"(){
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: 1, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        user.dateCreated = new Date()
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        controller.loginService.tokenService.utilsService.metaClass.saveInstance{ def instance ->
            return false
        }
        when:
        controller.login()
        then:
        thrown RuntimeException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.loginService.tokenService.utilsService)
    }

    void "test login ok"(){
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: 1, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        user.dateCreated = new Date()
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.request.JSON.username = "Pepepe"
        controller.request.JSON.password = "Password1*"
        controller.loginService.tokenService.utilsService.metaClass.saveInstance{ def instance ->
            return instance.save(flush:true)
        }
        when:
        controller.login()
        then:
        controller.response.status == 201
        controller.response.json == JSON.parse("{\"accessToken\": $user.accessToken}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.loginService.tokenService.utilsService)
    }

    void "test logout bad request"() {
        given:
        controller.request.method = 'POST'
        when:
        controller.logout()
        then:
        thrown BadRequestException
    }

    void "test logout cannot authenticate"() {
        given:
        controller.request.method = 'POST'
        controller.params.accessToken = "accessToken"
        when:
        controller.logout()
        then:
        thrown AuthException
    }

    void "test logout save fails"(){
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: 1, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        user.dateCreated = new Date()
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.loginService.tokenService.utilsService.metaClass.saveInstance{ def instance ->
            return false
        }
        when:
        controller.logout()
        then:
        thrown RuntimeException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.loginService.tokenService.utilsService)
    }

    void "test logout ok"(){
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: 1, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        user.dateCreated = new Date()
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.loginService.tokenService.utilsService.metaClass.saveInstance{ def instance ->
            return instance.save(flush:true)
        }
        when:
        controller.logout()
        then:
        controller.response.status == 201
        controller.response.json == JSON.parse("{\"message\": \"Usuario deslogueado!\"}")
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.loginService.tokenService.utilsService)
    }

}