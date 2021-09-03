package com.family.task.authentication

import com.family.task.constants.Constants
import com.family.task.exception.TaskServerException
import com.family.task.jdbc.TaskDataJdbc
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Slf4j
@Service
class AuthenticationService {
    @Autowired
    TaskDataJdbc taskDataJdbc
    @Autowired
    JwtTokenUtil jwtTokenUtil
    @Autowired
    BCryptPasswordEncoder passwordEncoder

    def verifiyUser(Map loginJsonStr) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]

        String userName = loginJsonStr["userName"]
        String password = loginJsonStr.getAt("password").toString()

        if (userName == null || password == null || userName == "null"|| password == "null") {
            throw new TaskServerException("Missing password or userName", HttpStatus.BAD_REQUEST)
        }

        def userInfo=null
        try{
            userInfo = taskDataJdbc.getUserPassWordByName(userName)[0]
            if(userInfo==null){
                throw new TaskServerException("Invalid userName", HttpStatus.BAD_REQUEST)
            }
        }catch (TaskServerException te){
            throw te
        }catch (Exception e){
            log.error(e.message)
            throw new TaskServerException("Invalid userName", HttpStatus.BAD_REQUEST)
        }
        String password2 = userInfo["passwords"]
        String roles = userInfo["roles"]
        int userId=userInfo["userId"]
        int familyId=userInfo["familyId"]

        if (passwordEncoder.matches(password, password2)) {
            String jwt = jwtTokenUtil.generateJWT(userName, userId, familyId, roles)
            result.jwt = jwt
            result.result = Constants.RESULT_SUCCESS
            return result
        }else{
            throw new TaskServerException("password not match", HttpStatus.BAD_REQUEST)
        }
    }
}
