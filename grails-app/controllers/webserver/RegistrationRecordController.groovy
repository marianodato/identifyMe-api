package webserver

import grails.converters.JSON
import webserver.exception.BadRequestException

class RegistrationRecordController {

    def utilsService
    def userService
    def registrationRecordService
    def tokenService

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
        def newRegistrationRecord = registrationRecordService.createRegistrationRecord(user)

        def resp = [:]
        resp.id = newRegistrationRecord.id
        resp.entryTime = utilsService.formatDate(newRegistrationRecord.entryTime)
        resp.departureTime = null
        resp.timeInSystem = null
        resp.userId = newRegistrationRecord.user.id
        resp.userUsername = newRegistrationRecord.user.username
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

        params.fingerprintId = params.fingerprintId as Integer

        def user = userService.getUserByFingerprintId(params.fingerprintId)
        def registrationRecord = registrationRecordService.modifyRegistrationRecord(user)
        log.info("modifyRegistrationRecord: " + registrationRecord)

        def resp = [:]
        resp.id = registrationRecord.id
        resp.entryTime = utilsService.formatDate(registrationRecord.entryTime)
        resp.departureTime = utilsService.formatDate(registrationRecord.departureTime)
        String timeInSystem = new GregorianCalendar(0, 0, 0, 0, 0, registrationRecord.secondsInSystem as Integer).time.format('HH:mm:ss')
        resp.timeInSystem = timeInSystem
        resp.userId = registrationRecord.user.id
        resp.userUsername = registrationRecord.user.username
        response.status = 200
        render resp as JSON
    }

    def searchRegistrationRecords() {
        log.info("RegistrationRecordController - searchRegistrationRecords")

        log.info("Params: " + params)
        def parameters = [:]
        parameters.accessToken = params.accessToken
        parameters.limit = params.limit
        parameters.offset = params.offset

        utilsService.validateFields('params', parameters, ['accessToken'])
        def user = tokenService.getUser(params.accessToken)

        if (params.userId) {
            params.userId = params.userId as Integer
        }

        if (user.id != params.userId) {
            userService.validateAdminUser(user)
        }

        if (!params.offset || (params.offset as Integer) < 0) {
            params.offset = 0
        } else {
            params.offset = params.offset as Integer
        }

        if (!params.limit || (params.limit as Integer) < 1 || (params.limit as Integer) > 10) {
            params.limit = 10
        } else {
            params.limit = params.limit as Integer
        }

        Map queryRegistrationRecords = registrationRecordService.searchRegistrationRecords(params.offset, params.limit, params.userId)
        log.info("QueryRegistrationRecords: " + queryRegistrationRecords)

        def resp = [:]
        def registrationRecords = []

        queryRegistrationRecords.results.each {
            def newRegistrationRecord = [:]
            newRegistrationRecord.id = it.id
            newRegistrationRecord.entryTime = utilsService.formatDate(it.entryTime)
            if (it.departureTime != null) {
                newRegistrationRecord.departureTime = utilsService.formatDate(it.departureTime)
            } else {
                newRegistrationRecord.departureTime = null
            }
            if (it.secondsInSystem != null) {
                String timeInSystem = new GregorianCalendar(0, 0, 0, 0, 0, it.secondsInSystem as Integer).time.format('HH:mm:ss')
                newRegistrationRecord.timeInSystem = timeInSystem
            } else {
                newRegistrationRecord.timeInSystem = null
            }
            newRegistrationRecord.userId = it.user.id
            newRegistrationRecord.userFingerprintId = it.user.fingerprintId
            newRegistrationRecord.userFingerprintStatus = it.user.fingerprintStatus
            newRegistrationRecord.userUsername = it.user.username
            registrationRecords.add(newRegistrationRecord)
        }

        def paging = [:]
        paging.total = queryRegistrationRecords.total
        paging.limit = params.limit
        paging.offset = queryRegistrationRecords.offset

        resp.paging = paging
        resp.results = registrationRecords
        response.status = 200
        render resp as JSON
    }
}