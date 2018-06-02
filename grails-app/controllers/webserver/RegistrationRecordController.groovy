package webserver

import grails.converters.JSON
import webserver.exception.BadRequestException

class RegistrationRecordController {

    def utilsService
    def userService
    def registrationRecordService

    def createRegistrationRecord() {
        log.info("RegistrationRecordController - createRegistrationRecord")

        def userAgent = request.getHeader("User-Agent")
        def request = request.JSON
        log.info("Request: " + request)

        if (userAgent != "NodeMCU") {
            log.error("Invalid value for header: user-agent!")
            throw new BadRequestException("Valor inválido para el header: user-agent!")
        }

        log.info("Is NodeMCU request")
        utilsService.validateFields('fields', request, ['serialNumber', 'atMacAddress', 'compileDate', 'signature', 'fingerprintId'])
        utilsService.checkSignature(request)

        def user = userService.getUserByFingerprintId(request.fingerprintId)
        registrationRecordService.createRegistrationRecord(user)

        def resp = [:]
        resp.message = "Registro exitoso!"
        response.status = 201
        render resp as JSON
    }
}
