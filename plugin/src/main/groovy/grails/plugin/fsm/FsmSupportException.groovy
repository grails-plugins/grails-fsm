package grails.plugin.fsm

class FsmSupportException extends Exception {
    FsmSupportException(String message) {
        super((String) message)
    }
}