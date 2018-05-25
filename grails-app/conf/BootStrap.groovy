import grails.util.Environment
import webserver.User

class BootStrap {

    def init = { servletContext ->

        if (Environment.current != Environment.TEST) {
            log.info("Initializing data...")

            if (User.countByIsAdmin(true) == 0){
                //password: Password1*
                User adminuser = new User(username: "adminuser", name: "Admin User", dni: 1234567890, gender: "male", email: "admin@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: "52f49b38931efb982422f593a0b1c261e357e943c81fbe4855d39e15f26db502553dd6423246480e537af51768ae27a45b3ffdea1ef8dbb61b1a5fac0a428fd4")
                def date = new Date()
                adminuser.dateCreated = date
                adminuser.lastUpdated = date
                if (!adminuser.save(flush: true))
                {
                    adminuser.discard()
                    adminuser.errors.each {
                        log.error("Error: Error saving to User table: " + it + " . User: " + adminuser)
                    }
                }
            }
        }
    }

    def destroy = {
    }
}
