package com.family.task.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MainConfig {
    @Value('${ENABLE_SECURITY}') public boolean enableSecurity
    @Value('${JWT_SECRET}') private String jwtSecret;
}
