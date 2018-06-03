package webserver

class RegistrationRecord {

    Date entryTime
    Date departureTime
    Long secondsInSystem

    static belongsTo = [user: User]

    static mapping = {
        version false
        autoTimestamp false
    }

    static constraints = {
        entryTime nullable: false
        departureTime nullable: true
        secondsInSystem nullable: true
    }
}