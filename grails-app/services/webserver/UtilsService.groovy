package webserver

class UtilsService {

    static transactional = false

    def saveInstance(def instance){
        return instance.save(flush: true)
    }
}
