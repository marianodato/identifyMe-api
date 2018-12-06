package webserver

import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(UtilsService)
@Mock([User])
class UtilsServiceSpec {

    void "test UtilsService saveInstance"() {
        given:
        User user = new User(username: "Pepepe", name: "Pepe Pepe", dni: 1234567890, gender: "male", email: "pepepe@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: false, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")

        when:
        def resp = service.saveInstance(user)

        then:
        resp == true
    }
}
