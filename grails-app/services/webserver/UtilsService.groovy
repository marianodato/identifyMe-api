package webserver

import webserver.exception.AuthException
import webserver.exception.BadRequestException

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class UtilsService {

    static transactional = false
    def grailsApplication

    def saveInstance(def instance){
        return instance.save(flush: true)
    }

    def validateFields(request, def expectedFields, def nonExpectedFields = null) {
        expectedFields.each {
            if (request[it] == null) {
                log.error("Invalid Paramenters: the field ${it} cannot be null")
                throw new BadRequestException("Parámetros inválidos: el campo ${it} no puede ser null!")
            }
        }
        if (nonExpectedFields) {
            nonExpectedFields.each {
                if (request[it] != null) {
                    log.error("Invalid Paramenters: the field ${it} must be null")
                    throw new BadRequestException("Parámetros inválidos: el campo ${it} debe ser null!")
                }
            }
        }
    }

    def checkSignature(def request) {
        String valid = getEncryptedSignature(request)
        if (!valid.equalsIgnoreCase(request.signature.toString())) {
            log.error("Cannot authenticate user!")
            throw new AuthException("No se pudo autenticar al usuario!")
        }
    }

    String getEncryptedSignature(def request) {
        String signatureKey = grailsApplication.config.arduino.signatureKey
        return encryptHmacSHA256(signatureKey, request.serialNumber.toString() + request.atMacAddress.toString() + request.compileDate.toString())
    }

    String encryptHmacSHA256(String key, String value) {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA256")
        Mac mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        return mac.doFinal(value.getBytes()).encodeHex().toString()
    }
}
