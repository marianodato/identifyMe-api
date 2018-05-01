package webserver

class User {

    String username
    String password
    String name
    Long dni
    String gender
    String email
    String phoneNumber
    Boolean isAdmin
    Long fingerprintId
    String fingerprintStatus
    Date dateCreated
    String accessToken
    static hasMany = [registrationRecords: RegistrationRecord]

    static mapping = {
        version false
        autoTimestamp false
    }

    static constraints = {
        username blank:false, nullable: false, size: 6..20, unique: true, matches: '(?=^.{6,20}$)^[a-zA-Z][a-zA-Z0-9]*[._-]?[a-zA-Z0-9]+$'
        password blank:false, nullable: false, size: 264..264, matches: '(?=^.{264}$)^[0-9]*[:]?[a-fA-F0-9]*[:]?[a-fA-F0-9]+$'
        name blank:false, nullable: false
        dni nullable: false, unique: true
        gender blank:false, nullable: false, matches: '^male$|^female$'
        email blank:false, nullable: false, unique: true, email: true
        phoneNumber blank:false, nullable: false, matches: '[\\+]\\d{2}[\\(]\\d{2}[\\)]\\d{4}[\\-]\\d{4}'
        isAdmin nullable: false
        fingerprintId nullable: false, unique: true
        fingerprintStatus blank:false, nullable: false, matches: '^unenrolled$|^pending$|^enrolled$'
        dateCreated nullable: false
        accessToken blank:false, nullable: true, size: 128..128, unique: true
    }
}