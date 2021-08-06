package com.family.task.controller

import com.family.task.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class FamilyController {
    @Autowired
    UserService userService

    @RequestMapping(value = "family/createfamily",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    def generateFamily(@RequestBody String familyJsonStr) {
        def result = userService.createFamily(familyJsonStr)
        return result
    }

    @RequestMapping(value = "family/{familyId}/user",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def getUserListByFamily(@PathVariable("familyId") String familyId) {
        def result = userService.getUsersByFamilyId(familyId.trim())
        return result
    }

    @RequestMapping(value = "family/{familyId}/category",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def getCategoryByFamily(@PathVariable("familyId") String familyId) {
        def result = userService.getCategoryListByFamilyId(familyId)
        return result
    }

    @RequestMapping(value = "family/{familyId}/category",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    def updateCategoryByFamily(@PathVariable("familyId") String familyId,
                               @RequestBody String payload) {
        def result = userService.changeFamilyCategoryList(familyId,payload)
        return result
    }

    @RequestMapping(value = "family/{familyId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def removeFamily(@PathVariable("familyId") String familyId) {
        def result = userService.deleteFamily(familyId)
        return result
    }
}
