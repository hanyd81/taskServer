package com.family.task.authentication

import com.family.task.constants.Constants
import com.family.task.jdbc.TaskDataJdbc
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService {
    @Autowired
    TaskDataJdbc taskDataJdbc
    @Autowired
    JsonSlurper jsonSlurper
    @Autowired
    JwtTokenUtil jwtTokenUtil
    @Autowired
    BCryptPasswordEncoder passwordEncoder

    def verifiyUser(String loginJsonStr) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: "",
                jwt    : null
        ]

        def loginJson = jsonSlurper.parseText(loginJsonStr)
        String userId = loginJson.getAt("userId").toString()
        String password = loginJson.getAt("password").toString()

        if (userId == null || password == null || password == "null") {
            result.message = "Missing userId or password"
            return result
        }

        def userInfo = taskDataJdbc.getUserPassWordById(userId)
        if (userInfo == null || userInfo.size() == 0) {
            result.message = "invalid user name"
            return result
        }
        String password2 = userInfo[0].getAt("passwords").toString()
        String roles = userInfo[0].getAt("roles").toString()
        int familyId

        try {
            familyId = userInfo[0].getAt("familyId").intValue()
        } catch (Exception e) {
            System.out.println(e.message)
            result.message = "Invalid familyId"
            return result
        }

        if (passwordEncoder.matches(password, password2)) {
            String jwt = jwtTokenUtil.generateJWT(userId, familyId, roles)
            result.jwt = jwt
            result.result = Constants.RESULT_SUCCESS
            return result
        }
        result.message = "Invalid password"
        return result
    }
}
