package com.family.task.controller

import com.family.task.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
class UserController {
    @Autowired
    UserService userService

    @RequestMapping(value = "user/create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    def createNewUser(@RequestBody String userJson,
                     @RequestHeader("Authorization") String token) {
        def result = userService.createUser(userJson, token)
        return result
    }

    @RequestMapping(value = "user/{userId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def getUserDetail(@PathVariable("userId") String id) {
        def result = userService.getUserDetailById(id.trim())
        return result
    }

    @RequestMapping(value = "user/changenickname/{userId}/",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def changeUserNickname(@PathVariable("userId") String userId,
                           @RequestParam("nickName") String nickName) {
        def result = userService.updateUserNickmane(userId.trim(), nickName.trim())
        return result
    }

    @RequestMapping(value = "user/checkusername",
            method = RequestMethod.GET)
    def checkUserName(@RequestParam("userName") String id) {
        def result = userService.checkUserNameExist(id.trim())
        return result
    }

    @RequestMapping(value = "user/delete/{userId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def removeUser(@PathVariable("userId") String userId) {
        def result = userService.deleteUser(userId.trim())
        return result
    }


}
