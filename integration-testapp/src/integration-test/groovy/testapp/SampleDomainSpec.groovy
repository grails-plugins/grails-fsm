package testapp

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class SampleDomainSpec extends Specification {

    // tag::initialValueTest[]
    void "test initial values"() {

        when: "create a domain object"
        SampleDomain instance = new SampleDomain()

        then: "fsm fields will get initial values"
        instance.status == "initial"
        instance.mood == "none"
    }
    // end::initialValueTest[]
}
