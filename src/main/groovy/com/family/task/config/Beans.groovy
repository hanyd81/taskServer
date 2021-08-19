package com.family.task.config

import com.family.task.constants.TaskStatus
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@PropertySource("classpath:application.properties")
class Beans {

    @Autowired
    Environment environment

    @Bean
    DriverManagerDataSource dataSource() {
        final URL = "jdbc:postgresql://" + environment.getProperty("host") + ":" +
                environment.getProperty("port") + "/" + environment.getProperty("db")
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setUrl(URL);
        driverManagerDataSource.setUsername(environment.getProperty("user"));
        driverManagerDataSource.setPassword(environment.getProperty("password"));
        driverManagerDataSource.setDriverClassName(environment.getProperty("driver"));
        return driverManagerDataSource;
    }


    @Bean
    public JdbcTemplate sql() {
        return new JdbcTemplate(dataSource())
    }

    @Bean
    public JsonSlurper jsonSlurper() {
        return new JsonSlurper()
    }

    @Bean
    public JsonBuilder jsonBuilder() {
        return new JsonBuilder()
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(5) {

        }
    }
}
