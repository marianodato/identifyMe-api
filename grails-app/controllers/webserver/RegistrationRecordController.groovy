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

    def modifyRegistrationRecord() {
        log.info("RegistrationRecordController - modifyRegistrationRecord")

        def userAgent = request.getHeader("User-Agent")
        def request = request.JSON
        log.info("Request: " + request)
        log.info("Params: " + params)
        def parameters = [:]
        parameters.fingerprintId = params.fingerprintId

        if (userAgent != "NodeMCU") {
            log.error("Invalid value for header: user-agent!")
            throw new BadRequestException("Valor inválido para el header: user-agent!")
        }

        log.info("Is NodeMCU request")
        utilsService.validateFields('fields', request, ['serialNumber', 'atMacAddress', 'compileDate', 'signature'])
        utilsService.validateFields('params', parameters, ['fingerprintId'])
        utilsService.checkSignature(request)

        def user = userService.getUserByFingerprintId(params.fingerprintId)
        def registrationRecord = registrationRecordService.modifyRegistrationRecord(user)

        log.info("modifyRegistrationRecord: " + registrationRecord)
        def resp = [:]
        resp.id = registrationRecord.id
        resp.userId = registrationRecord.user.id
        resp.userFingerprintId = registrationRecord.user.fingerprintId
        resp.entryTime = utilsService.formatDate(registrationRecord.entryTime)
        resp.departureTime = utilsService.formatDate(registrationRecord.departureTime)
        resp.secondsInSystem = registrationRecord.secondsInSystem
        response.status = 200
        render resp as JSON
    }
}
