package com.family.task.authentication

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.family.task.config.MainConfig
import com.family.task.constants.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import groovy.time.TimeCategory

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException

@Component
class JwtTokenUtil {

    @Autowired
    MainConfig mainConfig

    private Algorithm algorithmHS = Algorithm.HMAC256("SecretKeyToGenJWTs");
    private JWTVerifier verifier = JWT.require(algorithmHS).withIssuer(Constants.TOKEN_ISSUER).build();

    public parseToken(String tokenStr) throws
            InvalidKeyException, NoSuchAlgorithmException,
            SignatureException, JWTVerificationException {
        DecodedJWT jwtClaims = verifier.verify(tokenStr)
        String user = jwtClaims.getSubject()
        Date expireDate = jwtClaims.getExpiresAt()
        Map jwtMap = jwtClaims.getClaims()

        if (user == null) {
            throw new JWTVerificationException("Invalid token: must contain subject")
        }
        if (expireDate == null) {
            throw new JWTVerificationException("Invalid token: must contain expire date")
        }

        return jwtMap
    }

    public Map<String, Object> checkToken(String tokenStr) {
        Map jwtMap
        try {
            jwtMap = parseToken(tokenStr)
        } catch(TokenExpiredException te){
            System.out.println(te.message)
            return null
        }
        catch (JWTVerificationException jwte) {
            System.out.println(jwte.message)
            return null
        }catch(Exception e){
            System.out.println(jwte.message)
            return null
        }

        if(jwtMap==null){
            return null
        }
        return jwtMap
    }


    String generateJWT(String userID, int familyId, String roles) {
        Date now = new Date()
        Date expiredate
        use( TimeCategory ) {
            expiredate = now + mainConfig.tokenMinute
        }
        String token = JWT.create()
                .withIssuer(Constants.TOKEN_ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(expiredate)
                .withSubject(userID)
                .withClaim("id", userID)
                .withClaim("familyId", familyId)
                .withClaim("roles", roles)
                .sign(algorithmHS)
        return token

    }
}
