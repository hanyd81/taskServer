package com.family.task.service

import com.family.task.authentication.JwtTokenUtil
import com.family.task.constants.Constants
import com.family.task.jdbc.TaskDataJdbc
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService {

    @Autowired
    TaskDataJdbc taskDataJdbc
    @Autowired
    JsonSlurper jsonSlurper
    @Autowired
    JwtTokenUtil jwtTokenUtil
    @Autowired
    BCryptPasswordEncoder passwordEncoder


    def createFamily(String familyJsonStr) {
        def result = [
                result  : Constants.RESULT_FAIL,
                familyId: 0,
                message : ""
        ]

        def family = jsonSlurper.parseText(familyJsonStr)

        // set initial values
        String familyName = family.getAt("familyName").toString()
        String id = family.getAt("id").toString()
        String rawPassword = family.getAt("passwords").toString()
        String categories = Constants.DEFAULT_CATEGORY
        String nickName = Constants.DEFAULT_USER_NICKNAME
        String roles = Constants.ROLE_LIST[1]

        // check values
        if (familyName == "" || familyName == "null") {
            result.message = "missing family name "
            return result
        }

        if (id == "" || id == "null") {
            result.message = "missing userId"
            return result
        }
        if (rawPassword == "" || rawPassword == "null") {
            result.message = "missing password"
            return result
        }

        if (family.getAt("categories") != null && family.getAt("categories").toString().length() > 0) {
            categories = family.getAt("categories").toString()
        }

        //check if user exists
        if (taskDataJdbc.checkUserExist(id)) {
            result.message = "user name already used"
            return result
        }

        String passwords = passwordEncoder.encode(rawPassword)

        //create family record
        int queryResult = taskDataJdbc.insertFamily(familyName.replace("'", "''"), categories)
        if (queryResult > 0) {
            result.familyId = queryResult
        }

        if (result.familyId == 0) {
            result.message = "fail to create family"
            return result
        }
        // create user
        int queryResult2 = taskDataJdbc.insertUser(id, passwords, roles, nickName, result.familyId)

        if (queryResult2 > 0) {
            result.result = Constants.RESULT_SUCCESS
            result.userId = id
            String jwt = jwtTokenUtil.generateJWT(id, result.familyId, roles)
            result.jwt = jwt
        } else {
            result.message = "unable to insert user"
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

    def createUser(String userJsonStr, String token) {
        def result = [
                result : Constants.RESULT_FAIL,
                userId : "",
                message: ""
        ]

        def userJson = jsonSlurper.parseText(userJsonStr)

        String id = userJson.getAt("id").toString()
        String rawPassword = userJson.getAt("passwords").toString()
        String roles = Constants.ROLE_LIST[0]
        String nickName = Constants.DEFAULT_USER_NICKNAME
        int familyId = ServiceHelper.getIntValue(userJson, "familyId")

        if (familyId == 0) {
            result.message = "invalid familyId"
            return result
        }

        if (!ServiceHelper.isTokenFamilyIdMatch(token, familyId)) {
            result.message = "familyId not match"
            return result
        }

        if (id == "" || id == "null") {
            result.message = "missing userId"
            return result
        }
        if (rawPassword == "" || rawPassword == "null") {
            result.message = "missing password"
            return result
        }

        String passwords = passwordEncoder.encode(rawPassword)

        if (userJson.getAt("roles") != null) {
            def rolestr = userJson.getAt("roles").toString().trim()
            if (rolestr.toUpperCase() in Constants.ROLE_LIST) {
                roles = rolestr.toUpperCase()
            }
        }

        if (userJson.getAt("nickName") != null) {
            nickName = userJson.getAt("nickName").toString()
        }

        int queryResult = taskDataJdbc.insertUser(id, passwords, roles, nickName, familyId)

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
                result    : Constants.RESULT_FAIL,
                userExists: true
        ]

        boolean userExists = true

        try {
            userExists = taskDataJdbc.checkUserExist(id)

        } catch (Exception e) {
            result.message = e.message
            return result
        }

        result.result = Constants.RESULT_SUCCESS
        result.userExists = userExists

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
