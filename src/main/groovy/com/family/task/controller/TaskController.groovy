package com.family.task.controller

import com.family.task.service.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(value = "task")
class TaskController {

    @Autowired
    TaskService taskService

    @RequestMapping(value = "tasks",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    def createTask(@RequestBody String newTask,
                   @RequestHeader("Authorization") String token) {
        def taskId = taskService.createTask(newTask,token)
        return taskId
    }

    @RequestMapping(value = "tasks/{taskId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    def getTastDetail(@PathVariable("taskId") String taskId) {
        def task = taskService.getTaskById(taskId)
        if (task == null) {
            System.out.println("failed to retrive task " + taskId)
        }
        System.out.println("retrive task " + taskId)
        return task
    }

    @RequestMapping(value = "tasklist/{familyId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    def getFamilyTaskList(@PathVariable("familyId") int familyId,
                          @RequestParam(value = "orderBy", defaultValue = "create_date") String orderBy) {
        def task = taskService.getTaskListByFamilyId(familyId, orderBy)
        return task
    }

    @RequestMapping(value = "tasks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    def getTaskList(@RequestParam("assignee") String assignee,
                    @RequestParam(value = "orderBy", defaultValue = "status") String orderBy) {
        def tasks = taskService.getTaskListByAssignee(assignee, orderBy)
        return tasks
    }

    @RequestMapping(value = "tasks/unassigned/{familyId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    def getUnAssignedTaskList(@PathVariable("familyId") int familyId,
                              @RequestParam(value = "orderBy", defaultValue = "category") String orderBy) {
        def tasks = taskService.getUnAssignedTaskList(familyId, orderBy)
        return tasks
    }

    @RequestMapping(value = "tasks/{taskId}/status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    def updateTaskStatus(@PathVariable("taskId") String taskId,
                         @RequestParam("status") String status) {
        def result = taskService.changeTaskStatus(taskId, status)
        return result
    }

    @RequestMapping(value = "tasks/{taskId}/assignee", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    def updateTaskAssignee(@PathVariable("taskId") String taskId,
                           @RequestParam("assignee") String assignee) {
        def result = taskService.changeTaskAssignee(taskId, assignee)
        return result
    }

    @RequestMapping(value = "tasks/{taskId}/redeem", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    def useTaskpoints(@PathVariable("taskId") String taskId) {
        def result = taskService.redeemTaskPoints(taskId)
        return result
    }

    @RequestMapping(value = "tasks/{taskId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    def removeTask(@PathVariable("taskId") String taskId) {
        def result = taskService.deleteTask(taskId)
        return result
    }

}
