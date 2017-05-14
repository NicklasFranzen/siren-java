package org.unbrokendome.siren.spring.ap.actions

import org.springframework.http.HttpMethod
import org.unbrokendome.siren.spring.ap.testsupport.CompilerTest
import org.unbrokendome.siren.spring.ap.testsupport.RequestContextTest
import org.unbrokendome.siren.model.ActionBuilder
import org.unbrokendome.siren.spring.ap.testsupport.SourceCode
import spock.lang.Specification

import java.util.function.Consumer


class ActionWithMultipleRequestMethodsTest extends Specification implements CompilerTest, RequestContextTest {

    @SourceCode('example.PingController')
    static final String CONTROLLER_SOURCE = '''
        package example;
        
        import org.unbrokendome.siren.model.RootEntity;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestMethod;
        
        public class PingController {
            @RequestMapping(value="/", method={ RequestMethod.POST, RequestMethod.PUT })
            public RootEntity ping() {
                return RootEntity.builder().build();
            }
        }
        '''


    def "Should generate ControllerActions class"() {
        when:
            compile()
        then:
            classPresent('example.PingControllerActions')
    }


    def "Should generate separate POST action for controller method"() {
        given:
            def PingControllerActions = assumeClassPresent('example.PingControllerActions')
        and:
            currentRequest(HttpMethod.GET, 'http://api.example.com/')

        when:
            Consumer<ActionBuilder> actionSpec = PingControllerActions.pingPOST()
            def action = new ActionBuilder('ping').with(actionSpec).build()

        then:
            action.href == 'http://api.example.com/'
            action.method == 'POST'
    }


    def "Should generate separate PUT action for controller method"() {
        given:
            def PingControllerActions = assumeClassPresent('example.PingControllerActions')
        and:
            currentRequest(HttpMethod.GET, 'http://api.example.com/')

        when:
            Consumer<ActionBuilder> actionSpec = PingControllerActions.pingPUT()
            def action = new ActionBuilder('ping').with(actionSpec).build()

        then:
            action.href == 'http://api.example.com/'
            action.method == 'PUT'
    }
}