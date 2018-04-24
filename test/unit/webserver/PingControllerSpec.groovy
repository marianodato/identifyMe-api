package webserver

import grails.test.mixin.TestFor
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import spock.lang.Specification

@TestFor(PingController)
class PingControllerSpec extends Specification {

    void setup(){
        org.apache.log4j.BasicConfigurator.configure()
        LogManager.getRootLogger().setLevel(Level.INFO)
    }

    void "test ping"() {
        when:
        controller.index()
        then:
        controller.response.text == 'pong'
    }
}