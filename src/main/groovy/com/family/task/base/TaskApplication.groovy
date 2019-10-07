package com.family.task.base

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = 'com.family.task')
class TaskApplication {

	static void main(String[] args) {
		SpringApplication.run(TaskApplication, args)
	}

}
