package fr.arby.controller;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Le controller des Ocoms pour réceptionner les différents appels CRUD
 *
 * @author tpiche
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {

    @RequestMapping(value="", method = RequestMethod.GET)
    private String test(ModelMap model) {
        return "test.jsp";
    }

}