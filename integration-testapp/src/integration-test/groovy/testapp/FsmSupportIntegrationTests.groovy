package testapp

import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

import static testapp.Estado.*
import static testapp.EstadoEnvio.ENVIO_FINAL
import static testapp.Mood.*
import static testapp.Status.*

@Integration
@Rollback
class FsmSupportIntegrationTests extends Specification {

    void testInitialValues() {
        when:
        FsmSupportDummy testa = new FsmSupportDummy()

        then:
        testa.status == INITIAL
        testa.mood == testa.getMood()
        testa.mood == NONE
    }

    void testErrors() {
        when:
        FsmSupportDummy samp1 = new FsmSupportDummy()
        samp1.fire("nonflow", LAUNCH)

        then:
        Exception e = thrown()
        e.message == "Can't fire on flow 'nonflow' which is not defined in 'class testapp.FsmSupportDummy'"

        when:
        samp1.fire('mood', NON_EVENT)

        then:
        AssertionError error = thrown()
        error.message.contains("Invalid event 'NON_EVENT'")

    }

    void testComplexFsm() {
        given:
        FsmSupportDummy samp1 = new FsmSupportDummy()
        Enum currentState

        when:
        samp1.fire('status', LAUNCH)

        then: "Amount == 0 => Won't launch"
        INITIAL == samp1.status

        when:
        samp1.amount = 3  // Change amount so it will allow starting

        then: "Not running, Mood will not change!!"
        NONE == samp1.fire_mood(UP)
        NONE == samp1.fire('mood', UP)
        NONE == samp1.fire('mood', UP)

        when:
        samp1.fire('status', LAUNCH)

        then:
        RUNNING == samp1.status

        when:
        currentState = samp1.fire('mood', UP)

        then:
        HIGH == currentState

        when:
        samp1.fire('status', STOPPED)

        then:
        STOPPED == samp1.status

        when:
        samp1.fire('status', CONTINUE)

        then:
        STOPPED != samp1.status
        RUNNING == samp1.status  // Property delegeated in the domain class!

        when:
        currentState = samp1.fire('mood', UP)

        then:
        HIGH == currentState

        when:
        currentState = samp1.fire('mood', DOWN)

        then:
        LOW == currentState

        when:
        FsmSupportDummy samp2 = new FsmSupportDummy()

        then:
        NONE == samp2.fire('mood', DOWN)
        NONE == samp2.fire('mood', DOWN)

        when:
        samp2.amount = 1
        samp2.fire('status', LAUNCH)

        then:
        RUNNING == samp2.status
        LOW == samp2.fire('mood', DOWN)
        HIGH == samp2.fire('mood', UP)
        HIGH == samp2.fire('mood', UP)
        LOW == samp2.fire('mood', DOWN)
    }

    void testReloadedStates() {
        when:
        Foo2 testFoo = new Foo2()
        testFoo.estadoEnvio = ENVIO_FINAL
        testFoo.save()

        then: "(PRE) Debemos estar en estado inicial, envio envioFinal"
        INICIAL == testFoo.estado
        ENVIO_FINAL == testFoo.estadoEnvio

        when:
        testFoo.fire('estado', COMANDO)

        then: "(POST) Debemos estar en estado final"
        FINAL == testFoo.estado
    }

}
