package com.family.task.exception

import org.springframework.http.HttpStatus

class TaskServerException extends Exception{
    HttpStatus httpStatus

    TaskServerException(String message, HttpStatus httpStatus){
        super(message)
        this.httpStatus=httpStatus
    }
}
