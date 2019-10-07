package com.family.task.controller

import com.family.task.service.TaskService
import com.family.task.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
//@RequestMapping(value = "/")
class UserDataSource {

    @Autowired
    UserService userService

    @RequestMapping(value = "family", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    def generateFamily(@RequestBody String familyJsonStr) {
        def result = userService.createFamily(familyJsonStr)
        return result
    }

    @RequestMapping(value = "family/{familyId}/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    def getUserListByFamily(@PathVariable("familyId") String familyId) {
        def result = userService.getUsersByFamilyId(familyId)
        return result
    }

    @RequestMapping(value = "family/{familyId}/category", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    def getCategoryByFamily(@PathVariable("familyId") String familyId) {
        def result = userService.getCategoryListByFamilyId(familyId)
        return result
    }

    @RequestMapping(value = "family/{familyId}/category", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    def updateCategoryByFamily(@PathVariable("familyId") String familyId,
                               @RequestBody String payload) {
        def result = userService.changeFamilyCategoryList(familyId,payload)
        return result
    }

    @RequestMapping(value = "family/{familyId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    def removeFamily(@PathVariable("familyId") String familyId) {
        def result = userService.deleteFamily(familyId)
        return result
    }

    @RequestMapping(value = "users", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    def generateUser(@RequestBody String userJson) {
        def result = userService.createUser(userJson)
        return result
    }

    @RequestMapping(value = "users/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    def getUserDetail(@PathVariable("userId") String id) {
        def result = userService.getUserDetailById(id)
        return result
    }

    @RequestMapping(value = "users/{userId}/nickname", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    def changeUserNickname(@PathVariable("userId") String userId,
                           @RequestParam("nickName") String nickName) {
        def result = userService.updateUserNickmane(userId, nickName)
        return result
    }

    @RequestMapping(value = "checkuserid", method = RequestMethod.GET)
    def checkUserId(@RequestParam("id") String id) {
        def result = userService.checkUserIdExist(id)
        return result
    }

    @RequestMapping(value = "users/{userId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    def removeUser(@PathVariable("userId") String userId) {
        def result = userService.deleteUser(userId)
        return result
    }


}
