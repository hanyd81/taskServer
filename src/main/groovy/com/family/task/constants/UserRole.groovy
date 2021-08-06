package com.family.task.constants

enum UserRole {
    USER("user"),
    ADMIN("admin"),
    MANAGER("manager")

    String value

    UserRole(String value) {
        this.value = value
    }
}