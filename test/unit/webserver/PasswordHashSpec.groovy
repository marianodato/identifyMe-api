package webserver

import org.apache.log4j.Level
import org.apache.log4j.LogManager
import spock.lang.Specification

class PasswordHashSpec extends Specification {

    void setup(){
        org.apache.log4j.BasicConfigurator.configure()
        LogManager.getRootLogger().setLevel(Level.INFO)
    }

    void "test password hash"() {
        when:
        def ret = PasswordHash.testPasswordHash()
        then:
        ret
    }
}