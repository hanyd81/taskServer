package com.family.task.authentication

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthorizationFilter extends OncePerRequestFilter {
    private static final String HEADER_NAME = "Authorization";

    @Autowired
    JwtTokenUtil jwtTokenUtil

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        String headerToken = req.getHeader(HEADER_NAME);
        Map jwtMap
        if (headerToken != null) {
            try {
                 jwtMap = jwtTokenUtil.checkToken(headerToken)
            } catch (any) {
                jwtMap=null
            }

            if(jwtMap!=null){
                String roles=jwtMap.getAt("roles").asString()
                Set<GrantedAuthority> authority = new HashSet<> ();
                authority.add (new SimpleGrantedAuthority (roles));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(jwtMap.getAt("sub"), "", authority);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
         chain.doFilter(req, res);
    }


}
