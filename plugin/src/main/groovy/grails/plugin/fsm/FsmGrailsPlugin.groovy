package grails.plugin.fsm

import grails.plugins.Plugin
import grails.util.GrailsClassUtils
import groovy.util.logging.Slf4j
import org.springframework.beans.BeanUtils
import org.springframework.context.ApplicationContext

@Slf4j
class FsmGrailsPlugin extends Plugin {
    def grailsVersion = "3.3.6 > *"
    def loadAfter = ['controllers', 'hibernate', 'domainClass']
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "Finite State Machine behaviour for domain classes"
    def author = "Jorge Uriarte"
    def authorEmail = "jorge.uriarte@omelas.net"
    def description = '''\\
        This plugin allow definition of simple workflows attached to domain classes, including
        states, events, transitions and conditions.
        Current workflow's state will be held in domain class' property that must be defined.
        Multiple workflows can be defined on every domain class.
'''

    def documentation = "http://grails.org/plugin/fsm"
    def license = "APACHE"
    def developers = [[name: "Puneet Behl", email: "joe@bloggs.net"]]
    def issueManagement = [system: "Github", url: "https://github.com/gpc/grails-fsm/issues"]
    def scm = [url: "https://github.com/gpc/grails-fsm"]

    void doWithDynamicMethods() {
        ApplicationContext ctx = applicationContext
        grailsApplication.domainClasses.each { domainClass ->
            final Map<String, Map<String, Closure>> fsm = GrailsClassUtils.getStaticPropertyValue(domainClass.clazz, FsmUtils.FSMDEF)
            if (fsm) {
                // Will create the proper FsmSupport instance!
                fsm.each { String p, definition ->
                    definition.each { String start, Closure defclosure ->
                        def mp = domainClass.metaClass.getMetaProperty(p)
                        if (!mp)
                            throw new FsmSupportException("Error in FSM definition: '${domainClass.clazz}' does not have '${p}' property to hold defined workflow status!")
                        def tmp = domainClass.clazz.newInstance()
                        if (tmp[p] != null)
                            log.warn("Default value of '${domainClass.clazz}.${p}' will be overriden by FSM definition for that property. ")

                        // Modify the metaclass so new instances will have new behaviour!!
                        domainClass.metaClass.setProperty("_fsm${p}", null)  // internal, will hold FsmSupport instance
                        domainClass.metaClass.fire = FsmUtils.fireClosure
                        domainClass.metaClass."fire_${p}" = FsmUtils.fireClosure.curry(p)
                        domainClass.metaClass.fireable = FsmUtils.fireableClosure
                        domainClass.metaClass."fireable_${p}" = FsmUtils.fireableClosure.curry(p)
                    }
                }
                // This code is a COPY of DomainClassGrailsPlugin.enhanceDomainClasses
                // because I cannot seem to be able to decorate it.
                // We just added the "${p}" initializing!
                domainClass.metaClass.constructor = { ->
                    def bean
                    if (ctx.containsBean(domainClass.fullName)) {
                        bean = ctx.getBean(domainClass.fullName)
                    } else {
                        bean = BeanUtils.instantiateClass(domainClass.clazz)
                    }
                    bean."$FsmUtils.FSMDEF".each { propertyName, definitions ->
                        definitions.each { state, clos ->
                            bean."${propertyName}" = state
                        }
                    }
                    bean
                }
                domainClass.metaClass.static.create = { ->
                    def bean = ctx.getBean(domainClass.getFullName())
                    bean."$FsmUtils.FSMDEF".each { propertyName, definitions ->
                        definitions.each { state, clos ->
                            bean."${propertyName}" = state
                        }
                    }
                    bean
                }
            }
        }
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }
}
