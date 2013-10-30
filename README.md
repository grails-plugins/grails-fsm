grails-fsm-trunk
================

This plugin injects the ability to define and execute simple workflows into domain classes. The current state of the flows will be held in class-properties, that must be explicitely defined in the class (like 'status' and 'mood' in the example below).

Flow definition will be in a static block called 'fsm_def', which can hold several flow definition, each of them tied to one property in the class.


Official docs: http://grails.org/plugin/fsm
