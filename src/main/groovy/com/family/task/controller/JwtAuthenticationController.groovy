package com.family.task.controller

import com.family.task.authentication.AuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = "auth")
class JwtAuthenticationController {

    @Autowired
    AuthenticationService authenticationService

    @RequestMapping(value = "login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    def userlogin(@RequestBody Map loginJsonStr) {
        return authenticationService.verifiyUser(loginJsonStr)
    }
}
