package org.digitalpanda.backend.application.northbound.ressource.greeting;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ui/greeting")
public class GreetingUiController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET)
    public GreetingDTO greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new GreetingDTO(counter.incrementAndGet(),
                            String.format(template, name));
    }
}
