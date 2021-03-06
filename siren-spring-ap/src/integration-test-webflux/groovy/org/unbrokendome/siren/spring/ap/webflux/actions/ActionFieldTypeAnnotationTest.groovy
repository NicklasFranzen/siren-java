package org.unbrokendome.siren.spring.ap.webflux.actions

import org.springframework.http.HttpMethod
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.unbrokendome.siren.model.ActionBuilder
import org.unbrokendome.siren.model.ActionField
import org.unbrokendome.siren.spring.ap.testsupport.CompilerTest
import org.unbrokendome.siren.spring.ap.testsupport.SourceCode
import spock.lang.Specification

import java.util.function.Consumer


class ActionFieldTypeAnnotationTest extends Specification implements CompilerTest {

    @SourceCode('example.HelloController')
    static final String CONTROLLER_SOURCE = '''
        package example;
        
        import org.unbrokendome.siren.annotation.ActionFieldType;
        import org.unbrokendome.siren.model.ActionField;
        import org.unbrokendome.siren.model.RootEntity;
        import org.springframework.web.bind.annotation.*;
        
        public class HelloController {
            @RequestMapping(value="/hello", method=RequestMethod.POST)
            public RootEntity hello(@RequestParam @ActionFieldType(ActionField.Type.EMAIL) String email) {
                return RootEntity.builder().build();
            }
        }
        '''


    def "Should generate ControllerActions class"() {
        when:
            compile()
        then:
            classPresent('example.HelloControllerActions')
    }


    def "Should generate action for controller method"() {
        given:
            def HelloControllerActions = assumeClassPresent('example.HelloControllerActions')
        and:
            def request = MockServerRequest.builder()
                    .method(HttpMethod.GET)
                    .uri(URI.create('http://api.example.com/'))
                    .build()

        when:
            Consumer<ActionBuilder> actionSpec = HelloControllerActions.hello(request)
            def action = new ActionBuilder('hello').with(actionSpec).build()

        then:
            action.href == 'http://api.example.com/hello'
            action.method == 'POST'
            action.fields?.size() == 1
            action.fields[0].name == 'email'
            action.fields[0].type == ActionField.Type.EMAIL
    }
}
