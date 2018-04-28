package webserver

class RegistrationRecord {

    Date entryTime
    Date departureTime
    Date timeInSystem

    static belongsTo = [user: User]

    static mapping = {
        version false
        autoTimestamp false
    }

    static constraints = {
        entryTime nullable: false
        departureTime nullable: true
        timeInSystem nullable: true
    }
}