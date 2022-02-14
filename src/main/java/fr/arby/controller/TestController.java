package fr.arby.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Le controller des Ocoms pour réceptionner les différents appels CRUD
 *
 * @author tpiche
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {

    @GetMapping("")
    private void test2() {
        System.out.println("test2");
    }

}