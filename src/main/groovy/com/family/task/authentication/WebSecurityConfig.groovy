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
            http.authorizeRequests()  //todo need to modify it make more sense
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/checkuserid").permitAll()
                    .antMatchers(HttpMethod.POST, "/login", "/createfamily").permitAll()
                    .antMatchers(HttpMethod.GET, "/task/**", "/users", "/family/**").authenticated()
                    .antMatchers(HttpMethod.PUT, "/task/**", "/users").authenticated()
                    .antMatchers(HttpMethod.POST, "/users", "/task/tasks").hasAuthority("ADMIN")
                    .antMatchers(HttpMethod.PUT, "/family/**").hasAuthority("ADMIN")
                    .antMatchers(HttpMethod.DELETE, "/**").hasAuthority("ADMIN")
                    .anyRequest().authenticated()

        } else {
            http.authorizeRequests()
                    .anyRequest().permitAll();
        }
    }
}
