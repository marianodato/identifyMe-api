package webserver

import grails.transaction.Transactional
import webserver.exception.BadRequestException
import webserver.exception.NotFoundException

import java.util.concurrent.TimeUnit

@Transactional
class RegistrationRecordService {

    def utilsService

    def createRegistrationRecord(User user) {
        log.info("RegistrationRecordService - createRegistrationRecord")

        RegistrationRecord newRegistrationRecord = new RegistrationRecord(user: user, departureTime: null, secondsInSystem: null)
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

    def modifyRegistrationRecord(User user) {
        log.info("RegistrationRecordService - modifyRegistrationRecord")

        def registrationRecords = RegistrationRecord.findAll("from RegistrationRecord as r where r.user=? order by r.id desc",
                [user], [max: 1])

        if (!registrationRecords) {
            log.error("Cannot find user record: " + registrationRecords)
            throw new NotFoundException("No se pudo encontrar registro!")
        }

        def registrationRecord = registrationRecords[0]

        if (registrationRecord.departureTime != null || registrationRecord.secondsInSystem != null) {
            log.error("Record already modified: " + registrationRecord)
            throw new BadRequestException("El registro ya fue modificado!")
        }

        def date = new Date()
        registrationRecord.departureTime = date
        registrationRecord.secondsInSystem = utilsService.getDateDiff(registrationRecord.entryTime, registrationRecord.departureTime, TimeUnit.SECONDS)

        if (!utilsService.saveInstance(registrationRecord)) {
            registrationRecord.discard()
            registrationRecord.errors.each {
                log.error("Error: Error saving to RegistrationRecord table: " + it + " . RegistrationRecord: " + registrationRecord)
            }
            throw new RuntimeException("Error saving to RegistrationRecord table. RegistrationRecord: " + registrationRecord)
        }
        log.info("RegistrationRecord modified!")
        return registrationRecord
    }
}
