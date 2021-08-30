package com.family.task.service

import com.family.task.authentication.JwtTokenUtil
import com.family.task.constants.Constants
import com.family.task.constants.UserRole
import com.family.task.exception.TaskServerException
import com.family.task.jdbc.TaskDataJdbc
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

import java.sql.SQLException


@Slf4j
@Service
class UserService {

    @Autowired
    TaskDataJdbc taskDataJdbc
    @Autowired
    JwtTokenUtil jwtTokenUtil
    @Autowired
    BCryptPasswordEncoder passwordEncoder


    def createFamily(Map family) throws TaskServerException {
        def result = [
                result  : Constants.RESULT_FAIL,
                familyId: 0,
                message : ""
        ]

        // set initial values
        String familyName = family["familyName"]
        String userName = family["userName"]
        String rawPassword = family["password"]
        String categories = Constants.DEFAULT_CATEGORY
        String nickName = Constants.DEFAULT_USER_NICKNAME
        String roles = UserRole.ADMIN.value

        // check values
        if (familyName == null || familyName == "") {
            throw new TaskServerException("Missing familyName", HttpStatus.BAD_REQUEST)
        }

        if (userName == null || userName == "null") {
            throw new TaskServerException("Missing userName", HttpStatus.BAD_REQUEST)
        }
        if (rawPassword == null || rawPassword == "null") {
            throw new TaskServerException("Missing password", HttpStatus.BAD_REQUEST)
        }

        String[] categoryList = family["categoryList"]
        if (categoryList != null && categoryList.size() > 0) {
            categories = categoryList.collect({ it.capitalize() }).join(",")
        }

        //check if user exists
        if (taskDataJdbc.checkUserExistByUserName(userName)) {
            throw new TaskServerException("User Name exists", HttpStatus.BAD_REQUEST)
        }

        String passwords = passwordEncoder.encode(rawPassword)

        //create family record
        int queryResult
        try {
            queryResult = taskDataJdbc.insertFamily(familyName.replace("'", "''"), categories)
        } catch (Exception ex) {
            throw new TaskServerException(ex.getMessage(), HttpStatus.BAD_REQUEST)
        }
        if (queryResult > 0) {
            result.familyId = queryResult
            log.info("create new family ${familyName} with id ${queryResult}")
        } else {
            throw new TaskServerException("fail to create family", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        // create user
        try {
            int userId = taskDataJdbc.insertUser(userName, passwords, roles, nickName, result.familyId)
            result.userId = userId
        } catch (Exception ex) {
            throw new TaskServerException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)
        }

        result.result = Constants.RESULT_SUCCESS
        result.userName = userName
        String jwt = jwtTokenUtil.generateJWT(userName, result.familyId, roles)
        result.jwt = jwt
        log.info("create admin user ${userName}")

        return result
    }

    def getCategoryListByFamilyId(String familyId) {
        def result = [
                result      : Constants.RESULT_FAIL,
                categoryList: [],
                message     : ""
        ]
        String categoryStr
        try {
            categoryStr = taskDataJdbc.getCategoryByFamilyID(familyId)
        } catch (Exception ex) {
            log.info(ex.getMessage())
            throw new TaskServerException("Fail to find family with id ${familyId}", HttpStatus.NOT_FOUND)
        }

        result.categoryList = categoryStr.split(",")
        result.result = Constants.RESULT_SUCCESS
        return result
    }

    //todo should allow family without user?
    def getUsersByFamilyId(String familyId) {
        def result = [
                result    : Constants.RESULT_FAIL,
                totalcount: 0,
                userList  : []
        ]
        def userList
        try {
            userList = taskDataJdbc.getUserByFamilyId(familyId)
        } catch (Exception ex) {
            log.info(ex.getMessage())
            throw new TaskServerException("Fail to find family with id ${familyId}", HttpStatus.NOT_FOUND)
        }
        if (userList == null) {
            return result
        }
        result.totalcount = userList.size()
        result.userList = userList
        result.result = Constants.RESULT_SUCCESS

        return result
    }

    def changeFamilyCategoryList(String familyId, Map payload) {
        def result = [
                result: Constants.RESULT_FAIL,
        ]

        String[] categoryList = payload["categoryList"]
        if (categoryList == null || categoryList.size() == 0) {
            throw new TaskServerException("Missing categoryList", HttpStatus.BAD_REQUEST)
        }
        String categoryStr = categoryList.collect({ it.capitalize() }).join(",")
        int queryResult
        try {
            queryResult = taskDataJdbc.updateCategoriesByFamilyId(familyId, categoryStr)
        } catch (Exception ex) {
            log.info(ex.getMessage())
            throw new TaskServerException("Fail to update family category with id ${familyId}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
        if (queryResult < 1) {
            throw new TaskServerException("Family not found", HttpStatus.NOT_FOUND)
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }


    def deleteFamily(String familyId) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]
        int queryResult
        try {
            queryResult = taskDataJdbc.deleteFamilyByFamilyId(familyId)
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            throw new TaskServerException("failed to delete family", HttpStatus.INTERNAL_SERVER_ERROR)
        }
        if (queryResult == 0) {
            throw new TaskServerException("Family not found", HttpStatus.NOT_FOUND)
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }


    //---------------------user function-----------------------------------------//

    def createUser(Map userJson, String token) {
        def result = [
                result : Constants.RESULT_FAIL,
                userId : "",
                message: ""
        ]

        String userName = userJson['userName'].toString()
        String rawPassword = userJson['password']
        String roles = UserRole.USER.value
        String nickName = Constants.DEFAULT_USER_NICKNAME
        def familyId = userJson['familyId']

        if (familyId.class != Integer||familyId == 0) {
            throw new TaskServerException("Invalid Family Id", HttpStatus.BAD_REQUEST)
        }

//        if (!ServiceHelper.isTokenFamilyIdMatch(token, familyId)) {
//            throw new TaskServerException("FamilyId not match", HttpStatus.BAD_REQUEST)
//        }

        if (userName == "" || userName == "null") {
            throw new TaskServerException("Missing userName", HttpStatus.BAD_REQUEST)
        }
        if (rawPassword == "" || rawPassword == "null") {
            throw new TaskServerException("Password not match", HttpStatus.BAD_REQUEST)
        }

        String passwords = passwordEncoder.encode(rawPassword)

        if (userJson.getAt("role") != null) {
            def rolestr = userJson.getAt("roles").toString().trim()
            def roleList = UserRole.values().collect() { it.value }
            if (rolestr.toUpperCase() in roleList) {
                roles = rolestr.toUpperCase()
            }
        }

        if (userJson.getAt("nickName") != null) {
            nickName = userJson.getAt("nickName").toString()
        }

        try {
            taskDataJdbc.insertUser(userName, passwords, roles, nickName, familyId)
        } catch (DuplicateKeyException e) {
            log.info(e.getMessage())
            throw new TaskServerException("User name exists", HttpStatus.BAD_REQUEST)
        }
        catch (Exception ex) {
            log.info(ex.getMessage())
            throw new TaskServerException("Failed to create user", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        result.result = Constants.RESULT_SUCCESS
        result.userId = userName

        return result
    }

    def getUserDetailById(String id) {
        def result = [
                result: Constants.RESULT_FAIL
        ]
        try {
            result.user = taskDataJdbc.getUserById(id)
        } catch (EmptyResultDataAccessException ex) {
            throw new TaskServerException("User not found", HttpStatus.NOT_FOUND)
        } catch (Exception e) {
            throw new TaskServerException("Failed to retrieve user", HttpStatus.INTERNAL_SERVER_ERROR)
        }

        result.result = Constants.RESULT_SUCCESS

        return result
    }


    def checkUserNameExist(String id) {
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
        return result
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
        log.info("user ${id} deleted")
        return result
    }
}
