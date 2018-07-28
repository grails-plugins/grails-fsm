package testapp

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import static testapp.Mood.NONE
import static testapp.Status.INITIAL

@Integration
class SampleDomainSpec extends Specification {

    // tag::initialValueTest[]
    void "test initial values"() {

        when: "create a domain object"
        SampleDomain instance = new SampleDomain()

        then: "fsm fields will get initial values"
        instance.status == INITIAL
        instance.mood == NONE
    }
    // end::initialValueTest[]
}
