package com.family.task.authentication

import com.family.task.config.MainConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import javax.servlet.Filter

@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    MainConfig mainConfig
    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint
    @Autowired
    JwtAuthorizationFilter jwtAuthorizationFilter

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() //todo: not sure what it about, need to check it
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint)

        if (mainConfig.enableSecurity) {
            http.authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/task/**").authenticated()
                    .antMatchers(HttpMethod.POST, "/users/**").hasAuthority("ADMIN")
                    .antMatchers(HttpMethod.DELETE, "/**").authenticated()
                    .anyRequest().denyAll();

        } else {
            http.authorizeRequests()
                    .anyRequest().permitAll();
        }
    }
}
