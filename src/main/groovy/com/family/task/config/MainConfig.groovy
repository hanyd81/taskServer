package com.family.task.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MainConfig {
    @Value('${ENABLE_SECURITY}') public boolean enableSecurity
    @Value('${JWT_SECRET}') public String jwtSecret
    @Value('${TOKEN_MINUTE}') public int tokenMinute=30;
}
