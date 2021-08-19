package com.family.task.exception

import com.family.task.constants.Constants
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import java.time.LocalDateTime

class ErrorResponse {

    static ResponseEntity createErrorResponse(message, HttpStatus status) {
        Map body = [result : Constants.RESULT_FAIL,
                    timestamp: LocalDateTime.now(),
                    message: message]
        return new ResponseEntity(body,status)
    }
}
