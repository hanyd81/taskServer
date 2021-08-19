package com.family.task.constants

enum TaskStatus {
    OPEN("OPEN"),
    DONE("DONE"),
    VERIFIED("VERIFIED"),
    REDEEMED("REDEEMED")

    String value

    TaskStatus(String value) {
        this.value = value
    }


}