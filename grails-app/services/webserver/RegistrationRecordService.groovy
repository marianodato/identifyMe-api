package webserver

import grails.transaction.Transactional

@Transactional
class RegistrationRecordService {

    def utilsService

    def createRegistrationRecord(User user) {
        log.info("RegistrationRecordService - createRegistrationRecord")

        RegistrationRecord newRegistrationRecord = new RegistrationRecord(user: user, departureTime: null, timeInSystem: null)
        def date = new Date()
        newRegistrationRecord.entryTime = date

        if (!utilsService.saveInstance(newRegistrationRecord)) {
            newRegistrationRecord.discard()
            newRegistrationRecord.errors.each {
                log.error("Error: Error saving to RegistrationRecord table: " + it + " . RegistrationRecord: " + newRegistrationRecord)
            }
            throw new RuntimeException("Error saving to RegistrationRecord table. RegistrationRecord: " + newRegistrationRecord)
        }
        log.info("RegistrationRecord created!")
    }
}
