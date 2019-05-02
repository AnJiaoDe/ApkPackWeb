package com.cy.apkpack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.stereotype.Controller
@RequestMapping("/apkPack")
public class HtmlController {
    @GetMapping("/index")
    public String index() {
        return "/html/index.html";
    }
    @GetMapping("/apk_decode")
    public String apk_decode() {

        return "/html/apk_decode.html";
    }

}
