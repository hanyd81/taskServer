package com.family.task.exception

import com.family.task.constants.Constants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

import java.time.LocalDateTime

@ControllerAdvice
class ControllerAdvisor extends ResponseEntityExceptionHandler{

    @ExceptionHandler(TaskServerException.class)
    ResponseEntity<Object> handleTaskServerException(TaskServerException ex){
        def body=[
                result : Constants.RESULT_FAIL,
                message:ex.getMessage(),
                timestamp: LocalDateTime.now()
        ]
        return new ResponseEntity<Object>(body,ex.httpStatus)
    }
}
