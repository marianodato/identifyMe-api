package webserver

import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import spock.lang.Shared
import webserver.exception.AuthException
import webserver.exception.BadRequestException
import webserver.exception.ForbiddenException
import webserver.exception.NotFoundException

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class RegistrationRecordControllerIntegrationSpec extends IntegrationSpec {

    @Shared
    RegistrationRecordController controller = new RegistrationRecordController()
    SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm:ss", new Locale("es", "AR"))

    void setup() {
        org.apache.log4j.BasicConfigurator.configure()
        LogManager.getRootLogger().setLevel(Level.INFO)
        sdf.setTimeZone(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"))
    }

    static void revokeMetaClassChanges(Class type, def instance = null) {
        GroovySystem.metaClassRegistry.removeMetaClass(type)
        if (instance != null) {
            instance.metaClass = null
        }
    }

    void "test createRegistrationRecord bad request nodemcu 1"() {
        given:
        controller.request.method = 'POST'
        when:
        controller.createRegistrationRecord()
        then:
        thrown BadRequestException
    }

    void "test createRegistrationRecord bad request nodemcu 2"() {
        given:
        controller.request.method = 'POST'
        controller.request.addHeader("User-Agent", "NodeMCU")
        when:
        controller.createRegistrationRecord()
        then:
        thrown BadRequestException
    }

    void "test createRegistrationRecord invalid signature 1 nodemcu"() {
        given:
        controller.request.method = 'POST'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02082"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3c"
        controller.request.JSON.compileDate = "May  1 2018 11:00:49"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.request.JSON.fingerprintId = 1
        when:
        controller.createRegistrationRecord()
        then:
        thrown AuthException
    }

    void "test createRegistrationRecord invalid signature 2 nodemcu"() {
        given:
        controller.request.method = 'POST'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c5"
        controller.request.JSON.fingerprintId = 1
        when:
        controller.createRegistrationRecord()
        then:
        thrown AuthException
    }

    void "test createRegistrationRecord cannot find user nodemcu"() {
        given:
        controller.request.method = 'POST'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.request.JSON.fingerprintId = 1
        when:
        controller.createRegistrationRecord()
        then:
        thrown NotFoundException
    }

    void "test createRegistrationRecord save fails nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.request.JSON.fingerprintId = 1
        controller.registrationRecordService.utilsService.metaClass.saveInstance { def instance ->
            return false
        }
        when:
        controller.createRegistrationRecord()
        then:
        thrown RuntimeException
        User.count() == 1
        RegistrationRecord.count() == 0
        cleanup:
        User.deleteAll(User.list())
        revokeMetaClassChanges(UtilsService, controller.registrationRecordService.utilsService)
    }

    void "test createRegistrationRecord ok nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'POST'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.request.JSON.fingerprintId = 1
        controller.registrationRecordService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        controller.createRegistrationRecord()
        then:
        User.count() == 1
        RegistrationRecord.count() == 1
        RegistrationRecord registrationRecord = RegistrationRecord.findByUser(user)
        !registrationRecord.departureTime
        !registrationRecord.secondsInSystem
        registrationRecord.entryTime
        controller.response.status == 201
        controller.response.json == JSON.parse("{\"message\": \"Registro exitoso!\"}")
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
        revokeMetaClassChanges(UtilsService, controller.registrationRecordService.utilsService)
    }

    void "test modifyRegistrationRecord bad request nodemcu 1"() {
        given:
        controller.request.method = 'PUT'
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown BadRequestException
    }

    void "test modifyRegistrationRecord bad request nodemcu 2"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown BadRequestException
    }

    void "test modifyRegistrationRecord bad request nodemcu 3"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02082"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3c"
        controller.request.JSON.compileDate = "May  1 2018 11:00:49"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown BadRequestException
    }

    void "test modifyRegistrationRecord invalid signature 1 nodemcu"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02082"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3c"
        controller.request.JSON.compileDate = "May  1 2018 11:00:49"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.fingerprintId = 1
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown AuthException
    }

    void "test modifyRegistrationRecord invalid signature 2 nodemcu"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c5"
        controller.params.fingerprintId = 1
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown AuthException
    }

    void "test modifyRegistrationRecord cannot find user nodemcu"() {
        given:
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.fingerprintId = 1
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown NotFoundException
    }

    void "test modifyRegistrationRecord cannot find registrationRecord nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.fingerprintId = 1
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown NotFoundException
        User.count() == 1
        RegistrationRecord.count() == 0
        cleanup:
        User.deleteAll(User.list())
    }

    void "test modifyRegistrationRecord already modified 1 nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, secondsInSystem: null)
        def date2 = new Date()
        registrationRecord.entryTime = date
        registrationRecord.departureTime = date2
        registrationRecord.save(flush: true)
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.fingerprintId = 1
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown BadRequestException
        User.count() == 1
        RegistrationRecord.count() == 1
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }

    void "test modifyRegistrationRecord already modified 2 nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: true, fingerprintId: 2, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: 2)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        RegistrationRecord registrationRecord2 = new RegistrationRecord(user: user2, departureTime: null, secondsInSystem: null)
        registrationRecord2.entryTime = date
        registrationRecord2.save(flush: true)
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.fingerprintId = 1
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown BadRequestException
        User.count() == 2
        RegistrationRecord.count() == 2
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }

    void "test modifyRegistrationRecord already modified 3 nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: 1)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        RegistrationRecord registrationRecord2 = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: 2)
        registrationRecord2.entryTime = date
        registrationRecord2.save(flush: true)
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.fingerprintId = 1
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown BadRequestException
        User.count() == 1
        RegistrationRecord.count() == 2
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }

    void "test modifyRegistrationRecord save fails nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.fingerprintId = 1
        controller.registrationRecordService.utilsService.metaClass.saveInstance { def instance ->
            return false
        }
        when:
        controller.modifyRegistrationRecord()
        then:
        thrown RuntimeException
        RegistrationRecord savedRegistrationRecord = RegistrationRecord.findById(registrationRecord.id)
        savedRegistrationRecord.entryTime != null
        savedRegistrationRecord.departureTime == null
        savedRegistrationRecord.secondsInSystem == null
        User.count() == 1
        RegistrationRecord.count() == 1
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
        revokeMetaClassChanges(UtilsService, controller.registrationRecordService.utilsService)
    }

    void "test modifyRegistrationRecord ok nodemcu"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: 1, fingerprintStatus: "enrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        controller.request.method = 'PUT'
        controller.request.addHeader("User-Agent", "NodeMCU")
        controller.request.JSON.serialNumber = "75330303035351F02081"
        controller.request.JSON.atMacAddress = "de:4f:22:26:3f:3b"
        controller.request.JSON.compileDate = "May  1 2018 11:00:48"
        controller.request.JSON.signature = "eab4ce65e5dc4a6b924d1a5258ff3f288769538ce52ee12d3137b4a936aac0c4"
        controller.params.fingerprintId = 1
        controller.registrationRecordService.utilsService.metaClass.saveInstance { def instance ->
            return instance.save(flush: true)
        }
        when:
        TimeUnit.SECONDS.sleep(3)
        controller.modifyRegistrationRecord()
        then:
        controller.response.status == 200
        RegistrationRecord savedRegistrationRecord = RegistrationRecord.findById(registrationRecord.id)
        savedRegistrationRecord.entryTime != null
        savedRegistrationRecord.departureTime != null
        savedRegistrationRecord.secondsInSystem >= 3
        def entryTimeParsed = sdf.format(savedRegistrationRecord.entryTime)
        def departureTimeParsed = sdf.format(savedRegistrationRecord.departureTime)
        controller.response.json == JSON.parse("{\"id\": $registrationRecord.id, \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\": \"$entryTimeParsed\", \"departureTime\":\"$departureTimeParsed\", \"secondsInSystem\": $savedRegistrationRecord.secondsInSystem}")
        User.count() == 1
        RegistrationRecord.count() == 1
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
        revokeMetaClassChanges(UtilsService, controller.registrationRecordService.utilsService)
    }

    void "test searchRegistrationRecords bad request"() {
        given:
        controller.request.method = 'GET'
        when:
        controller.searchRegistrationRecords()
        then:
        thrown BadRequestException
    }

    void "test searchRegistrationRecords cannot authenticate"() {
        given:
        controller.request.method = 'GET'
        controller.params.accessToken = "accessToken"
        when:
        controller.searchRegistrationRecords()
        then:
        thrown AuthException
    }

    void "test searchRegistrationRecords forbidden access 1"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        when:
        controller.searchRegistrationRecords()
        then:
        thrown ForbiddenException
        User.count() == 2
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchRegistrationRecords forbidden access 2"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.userId = 2
        when:
        controller.searchRegistrationRecords()
        then:
        thrown ForbiddenException
        User.count() == 2
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchRegistrationRecords cannot find user"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.userId = 2
        when:
        controller.searchRegistrationRecords()
        then:
        thrown NotFoundException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchRegistrationRecords cannot find record"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.userId = 1
        when:
        controller.searchRegistrationRecords()
        then:
        thrown NotFoundException
        User.count() == 1
        cleanup:
        User.deleteAll(User.list())
    }

    void "test searchRegistrationRecords ok 1"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.userId = user.id
        when:
        controller.searchRegistrationRecords()
        then:
        controller.response.status == 200
        def dateParsed = sdf.format(user.dateCreated)
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 1, \"limit\": 10, \"offset\":0}, results: [{\"id\": $registrationRecord.id, \"username\": \"$user.username\", \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\":\"$dateParsed\", \"departureTime\": null, \"secondsInSystem\": null}]}")
        User.count() == 1
        RegistrationRecord.count() == 1
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }

    void "test searchRegistrationRecords ok 2"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.offset = 10
        controller.params.limit = 20
        when:
        controller.searchRegistrationRecords()
        then:
        controller.response.status == 200
        def dateParsed = sdf.format(user.dateCreated)
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 1, \"limit\": 10, \"offset\":0}, results: [{\"id\": $registrationRecord.id, \"username\": \"$user.username\", \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\":\"$dateParsed\", \"departureTime\": null, \"secondsInSystem\": null}]}")
        User.count() == 1
        RegistrationRecord.count() == 1
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }

    void "test searchRegistrationRecords ok 3"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        TimeUnit.SECONDS.sleep(1)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        User user3 = new User(username: "Pipipi", name: "Pipi Pipi", dni: 1234567892, gender: "male", email: "pipipi@gmail.com", phoneNumber: "+54(11)1234-5670", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        TimeUnit.SECONDS.sleep(1)
        def date3 = new Date()
        user3.dateCreated = date3
        user3.lastUpdated = date3
        user3.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        RegistrationRecord registrationRecord2 = new RegistrationRecord(user: user2, departureTime: null, secondsInSystem: null)
        registrationRecord2.entryTime = date2
        registrationRecord2.save(flush: true)
        RegistrationRecord registrationRecord3 = new RegistrationRecord(user: user3, departureTime: null, secondsInSystem: null)
        registrationRecord3.entryTime = date3
        registrationRecord3.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.offset = 1
        controller.params.limit = 2
        when:
        controller.searchRegistrationRecords()
        then:
        controller.response.status == 200
        def dateParsed = sdf.format(user.dateCreated)
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 3, \"limit\": 2, \"offset\":1}, results: [{\"id\": $registrationRecord.id, \"username\": \"$user.username\", \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\":\"$dateParsed\", \"departureTime\": null, \"secondsInSystem\": null}]}")
        User.count() == 3
        RegistrationRecord.count() == 3
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }

    void "test searchRegistrationRecords ok 4"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        TimeUnit.SECONDS.sleep(1)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        User user3 = new User(username: "Pipipi", name: "Pipi Pipi", dni: 1234567892, gender: "male", email: "pipipi@gmail.com", phoneNumber: "+54(11)1234-5670", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        TimeUnit.SECONDS.sleep(1)
        def date3 = new Date()
        user3.dateCreated = date3
        user3.lastUpdated = date3
        user3.save(flush: true)
        User user4 = new User(username: "Popopo", name: "Popo Popo", dni: 1234567893, gender: "male", email: "popopo@gmail.com", phoneNumber: "+54(11)1234-5671", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        TimeUnit.SECONDS.sleep(1)
        def date4 = new Date()
        user4.dateCreated = date4
        user4.lastUpdated = date4
        user4.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        RegistrationRecord registrationRecord2 = new RegistrationRecord(user: user2, departureTime: null, secondsInSystem: null)
        registrationRecord2.entryTime = date2
        registrationRecord2.save(flush: true)
        RegistrationRecord registrationRecord3 = new RegistrationRecord(user: user3, departureTime: null, secondsInSystem: null)
        registrationRecord3.entryTime = date3
        registrationRecord3.save(flush: true)
        RegistrationRecord registrationRecord4 = new RegistrationRecord(user: user4, departureTime: null, secondsInSystem: null)
        registrationRecord4.entryTime = date4
        registrationRecord4.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.offset = 1
        controller.params.limit = 2
        when:
        controller.searchRegistrationRecords()
        then:
        controller.response.status == 200
        def dateParsed = sdf.format(user.dateCreated)
        def dateParsed2 = sdf.format(user2.dateCreated)
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 4, \"limit\": 2, \"offset\":1}, results: [{\"id\": $registrationRecord2.id, \"username\": \"$user2.username\", \"userId\": $user2.id, \"userFingerprintId\": $user2.fingerprintId, \"entryTime\":\"$dateParsed2\", \"departureTime\": null, \"secondsInSystem\": null}, {\"id\": $registrationRecord.id, \"username\": \"$user.username\", \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\":\"$dateParsed\", \"departureTime\": null, \"secondsInSystem\": null}]}")
        User.count() == 4
        RegistrationRecord.count() == 4
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }

    void "test searchRegistrationRecords ok 5"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        TimeUnit.SECONDS.sleep(1)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        RegistrationRecord registrationRecord2 = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord2.entryTime = date2
        registrationRecord2.save(flush: true)
        TimeUnit.SECONDS.sleep(1)
        def date3 = new Date()
        RegistrationRecord registrationRecord3 = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord3.entryTime = date3
        registrationRecord3.save(flush: true)
        TimeUnit.SECONDS.sleep(1)
        def date4 = new Date()
        RegistrationRecord registrationRecord4 = new RegistrationRecord(user: user2, departureTime: null, secondsInSystem: null)
        registrationRecord4.entryTime = date4
        registrationRecord4.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.offset = 0
        controller.params.limit = 2
        controller.params.userId = user.id
        when:
        controller.searchRegistrationRecords()
        then:
        controller.response.status == 200
        def dateParsed2 = sdf.format(date2)
        def dateParsed3 = sdf.format(date3)
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 3, \"limit\": 2, \"offset\":0}, results: [{\"id\": $registrationRecord3.id, \"username\": \"$user.username\", \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\":\"$dateParsed3\", \"departureTime\": null, \"secondsInSystem\": null}, {\"id\": $registrationRecord2.id, \"username\": \"$user.username\", \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\":\"$dateParsed2\", \"departureTime\": null, \"secondsInSystem\": null}]}")
        User.count() == 2
        RegistrationRecord.count() == 4
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }

    void "test searchRegistrationRecords ok 6"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
        def date = new Date()
        user.dateCreated = date
        user.lastUpdated = date
        user.save(flush: true)
        User user2 = new User(username: "Papapa", name: "Papa Papa", dni: 1234567891, gender: "male", email: "papapa@gmail.com", phoneNumber: "+54(11)1234-5679", isAdmin: false, fingerprintId: 1, fingerprintStatus: "pending", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a")
        TimeUnit.SECONDS.sleep(1)
        def date2 = new Date()
        user2.dateCreated = date2
        user2.lastUpdated = date2
        user2.save(flush: true)
        RegistrationRecord registrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord.entryTime = date
        registrationRecord.save(flush: true)
        RegistrationRecord registrationRecord2 = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
        registrationRecord2.entryTime = date2
        registrationRecord2.save(flush: true)
        TimeUnit.SECONDS.sleep(1)
        def date3 = new Date()
        RegistrationRecord registrationRecord3 = new RegistrationRecord(user: user2, departureTime: null, secondsInSystem: null)
        registrationRecord3.entryTime = date3
        registrationRecord3.save(flush: true)
        controller.request.method = 'GET'
        controller.params.accessToken = "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4"
        controller.params.offset = 0
        controller.params.limit = 3
        controller.params.userId = user.id
        when:
        controller.searchRegistrationRecords()
        then:
        controller.response.status == 200
        def dateParsed = sdf.format(date)
        def dateParsed2 = sdf.format(date2)
        controller.response.json == JSON.parse("{\"paging\": {\"total\": 2, \"limit\": 3, \"offset\":0}, results: [{\"id\": $registrationRecord2.id, \"username\": \"$user.username\", \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\":\"$dateParsed2\", \"departureTime\": null, \"secondsInSystem\": null}, {\"id\": $registrationRecord.id, \"username\": \"$user.username\", \"userId\": $user.id, \"userFingerprintId\": $user.fingerprintId, \"entryTime\":\"$dateParsed\", \"departureTime\": null, \"secondsInSystem\": null}]}")
        User.count() == 2
        RegistrationRecord.count() == 3
        cleanup:
        User.deleteAll(User.list())
        RegistrationRecord.deleteAll(RegistrationRecord.list())
    }


}
