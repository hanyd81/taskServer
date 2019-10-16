package com.family.task.service

import com.auth0.jwt.JWTDecoder
import com.auth0.jwt.interfaces.DecodedJWT
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

class ServiceHelper {

    static int getIntValue(Map json, String key) {
        int value = 0
        String number = json.getAt(key).toString()
        if (number.isInteger()) {
            value = number as Integer
        }
        return value
    }

    static boolean isTokenFamilyIdMatch(String token, int familyId) {
        DecodedJWT jwtMap = new JWTDecoder(token)
        int familyIdFromToken

        try {
            familyIdFromToken = jwtMap.getClaim("familyId").asInt()
        } catch (GroovyCastException e) {
            return false
        }
        return familyId == familyIdFromToken
    }
}
