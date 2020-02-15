package org.digitalpanda.backend.application.northbound.ressource;

import org.digitalpanda.backend.application.northbound.ressource.greeting.GreetingDTO;
import org.digitalpanda.backend.application.northbound.ressource.greeting.GreetingUiController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GreetingDTOUiControllerTest {
    private static final String  FIRST_NAME= "heinrich";
    private GreetingUiController greetingUiController;

    @Before
    public void init() {
        this.greetingUiController = new GreetingUiController();
    }

    @Test
    public void should_get_greeting_with_name() {
        GreetingDTO greetingDTO = this.greetingUiController.greeting(FIRST_NAME);
        assertTrue( "should contain the name in the response",
                    greetingDTO.getContent().contains(FIRST_NAME));
    }
}
