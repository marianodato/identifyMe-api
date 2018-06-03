package webserver

import webserver.exception.AuthException
import webserver.exception.BadRequestException

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class UtilsService {

    static transactional = false
    def grailsApplication

    def saveInstance(def instance){
        log.info("utilsService - saveInstance")
        return instance.save(flush: true)
    }

    def validateFields(def type, def map, def expectedFields, def nonExpectedFields = null) {
        log.info("utilsService - validateFields")
        if (expectedFields) {
            expectedFields.each {
                if (map[it] == null) {
                    if (type == "params") {
                        log.error("Invalid Paramenters: the param ${it} cannot be null!")
                        throw new BadRequestException("Parámetros inválidos: el parámetro ${it} no puede ser null!")
                    } else {
                        log.error("Invalid Fields: the field ${it} cannot be null!")
                        throw new BadRequestException("Campos inválidos: el campo ${it} no puede ser null!")
                    }
                }
            }
        }

        if (nonExpectedFields) {
            nonExpectedFields.each {
                if (map[it] != null) {
                    if (type == "params") {
                        log.error("Invalid Paramenters: the param ${it} must be null!")
                        throw new BadRequestException("Parámetros inválidos: el parámetro ${it} debe ser null!")
                    } else {
                        log.error("Invalid Fields: the field ${it} must be null!")
                        throw new BadRequestException("Campos inválidos: el campo ${it} debe ser null!")
                    }
                }
            }
        }
    }

    def checkSignature(def request) {
        log.info("utilsService - checkSignature")
        String valid = getEncryptedSignature(request)
        if (!valid.equalsIgnoreCase(request.signature.toString())) {
            log.error("Cannot authenticate user!")
            throw new AuthException("No se pudo autenticar al usuario!")
        }
    }

    String getEncryptedSignature(def request) {
        log.info("utilsService - getEncryptedSignature")
        String signatureKey = grailsApplication.config.arduino.signatureKey
        return encryptHmacSHA256(signatureKey, request.serialNumber.toString() + request.atMacAddress.toString() + request.compileDate.toString())
    }

    String encryptHmacSHA256(String key, String value) {
        log.info("utilsService - encryptHmacSHA256")
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA256")
        Mac mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        return mac.doFinal(value.getBytes()).encodeHex().toString()
    }

    def formatDate(def date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm:ss", new Locale("es", "AR"))
        sdf.setTimeZone(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"))
        return sdf.format(date)
    }

    def getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime()
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS)
    }
}
