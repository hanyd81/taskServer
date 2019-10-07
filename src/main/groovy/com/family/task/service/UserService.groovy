package com.family.task.service

import com.family.task.constants.Constants
import com.family.task.jdbc.TaskDataJdbc
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService {

    @Autowired
    TaskDataJdbc taskDataJdbc
    @Autowired
    JsonSlurper jsonSlurper

    def createFamily(String familyJsonStr) {
        def result = [
                result  : Constants.RESULT_FAIL,
                familyId: 0,
                message : ""
        ]
        def family = jsonSlurper.parseText(familyJsonStr)
        String familyName
        String categories = Constants.DEFAULT_CATEGORY

        if (family.getAt("familyName") == null || family.getAt("familyName").toString().length() == 0) {
            result.message = "missing family name "
            return result
        }
        familyName = family.getAt("familyName").toString()

        if (family.getAt("categories") != null && family.getAt("categories").toString().length() > 0) {
            categories = family.getAt("categories").toString()
        }

        int queryResult = taskDataJdbc.insertFamily(familyName.replace("'", "''"), categories)

        if (queryResult > 0) {
            result.result = Constants.RESULT_SUCCESS
            result.familyId = queryResult
        }

        return result
    }

    def getCategoryListByFamilyId(String familyId) {
        def result = [
                result      : Constants.RESULT_FAIL,
                categoryList: [],
                message     : ""
        ]
        String categoryStr = taskDataJdbc.getCategoryByFamilyID(familyId)

        if (categoryStr == null || categoryStr == "") {
            result.message = "fail to retrive categoryList"
            return result
        }

        def categoryList = categoryStr.split(",")
        result.categoryList = categoryList
        result.result = Constants.RESULT_SUCCESS

        return result
    }

    def getUsersByFamilyId(String familyId) {
        def result = [
                result    : Constants.RESULT_FAIL,
                totalcount: 0,
                userList  : []
        ]
        def userList = taskDataJdbc.getUserByFamilyId(familyId)

        if (userList == null) {
            return result
        }
        result.totalcount = userList.size()
        result.userList = userList
        result.result = Constants.RESULT_SUCCESS

        return result
    }

    def changeFamilyCategoryList(String familyId, String payload) {
        def result = [
                result: Constants.RESULT_FAIL,
        ]

        def payloadJson = jsonSlurper.parseText(payload)
        String[] categoryList = payloadJson.getAt("categoryList")

        categoryList = categoryList.collect({ it.capitalize() })
        String categoryStr = categoryList.join(",")

        int queryResult = taskDataJdbc.updateCategoriesByFamilyId(familyId, categoryStr)

        if (queryResult < 1) {
            result.result = Constants.RESULT_FAIL
            result.message = "fail to update family category"
            return result
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }


    def deleteFamily(String familyId) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]
        int queryResult = taskDataJdbc.deleteFamilyByFamilyId(familyId)
        if (queryResult == 0) {
            result.message = "failed to delete family"
            return result
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }

    //---------------------user function-----------------------------------------//

    def createUser(String userJsonStr) {
        def result = [
                result : Constants.RESULT_FAIL,
                userId : "",
                message: ""
        ]

        def userJson = jsonSlurper.parseText(userJsonStr)
        if (userJson.getAt("familyId") == null || userJson.getAt("id") == null) {
            result.message = "missing familyId or userId"
            return result
        }

        String id = userJson.getAt("id").toString()
        int familyId = userJson.getAt("familyId").intValue()
        String roles = Constants.ROLE_LIST[0]
        String nickName = "user"

        if (userJson.getAt("roles") != null) {
            def rolestr = userJson.getAt("roles").toString().trim()
            if (rolestr.toUpperCase() in Constants.ROLE_LIST) {
                roles = rolestr.toUpperCase()
            }
        }

        if (userJson.getAt("nickName") != null) {
            nickName = userJson.getAt("nickName")
        }

        int queryResult = taskDataJdbc.insertUser(id, roles, nickName, familyId)

        if (queryResult > 0) {
            result.result = Constants.RESULT_SUCCESS
            result.userId = id
        } else {
            result.message = "unable to insert user"
        }
        return result
    }

    def getUserDetailById(String id) {
        def result = [
                result: Constants.RESULT_FAIL
        ]
        def userlist = taskDataJdbc.getUserById(id)

        if (userlist == null) {
            return result
        }

        result.user = userlist[0]
        result.result = Constants.RESULT_SUCCESS

        return result
    }


    def checkUserIdExist(String id) {
        def result = [
                result: Constants.RESULT_FAIL,
                userId: ""
        ]
        String queryResult = taskDataJdbc.checkUserById(id)
        if (queryResult.length() == 0) {
            result.result = Constants.RESULT_FAIL
            return result
        } else {
            result.result = Constants.RESULT_SUCCESS
            result.userId = id
        }
        return result
    }

    def updateUserNickmane(String id, String nickName) {
        def result = [
                result: Constants.RESULT_FAIL,
        ]
        int queryResult = taskDataJdbc.updateNicknameByUserId(id, nickName)
        if (queryResult < 1) {
            result.result = Constants.RESULT_FAIL
            result.message = "fail to update user"
            return result
        }
        result.result = Constants.RESULT_SUCCESS
        result.message = "nickName change to " + nickName
    }

    def deleteUser(String id) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]
        int queryResult = taskDataJdbc.deleteUserById(id)
        if (queryResult == 0) {
            result.message = "failed to delete user"
            return result
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }
}
