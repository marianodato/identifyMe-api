import grails.util.Environment
import webserver.User

class BootStrap {

    def init = { servletContext ->

        if (Environment.current != Environment.TEST) {
            log.info("Initializing data...")

            if (User.countByIsAdmin(true) == 0){
                //password: Password1*
                User adminuser = new User(username: "adminuser", name: "Admin User", dni: 1234567890, gender: "male", email: "admin@gmail.com", phoneNumber: "+54(11)1234-5678", isAdmin: true, fingerprintId: null, fingerprintStatus: "unenrolled", password: "100000:b9f0cdb48f6dd12694eaf1de44ab4a071da56765498abe8732dcf941966bf81ce839dfa4dae220e656760b8ff0d3a83103913a67bc9685083f445dda464449b2:51e7688ee57d721ad50622f50bb248ca55e34d01d5ee7168db050990585bfffcb49d3a9fa655cd3178ace50b668b201411a6bbdca18b8d4177307a33e842db6a", accessToken: null)
                adminuser.dateCreated = new Date()
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
