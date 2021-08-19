package com.family.task.controller

import com.family.task.config.MainConfig
import com.family.task.constants.TaskStatus
import com.family.task.exception.ErrorResponse
import com.family.task.exception.TaskServerException
import com.family.task.service.TaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(value = "tasks")
class TaskController {

    @Autowired
    TaskService taskService
    @Autowired
    MainConfig mainConfig

    @RequestMapping(value = "task/create",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    def createTask(@RequestBody String newTask,
                   @RequestHeader("Authorization") String token) {
        def taskId = taskService.createTask(newTask, token)
        return taskId
    }

    @RequestMapping(value = "task/{taskId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def getTastDetail(@PathVariable("taskId") String taskId) throws TaskServerException, Exception {

        return taskService.getTaskById(taskId)
    }

    @RequestMapping(value = "tasklist/family/{familyId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def getFamilyTaskList(@PathVariable("familyId") int familyId,
                          @RequestParam(value = "orderBy", defaultValue = "create_date") String orderBy)
            throws TaskServerException, Exception{
        return taskService.getTaskListByFamilyId(familyId, orderBy)
    }

    @RequestMapping(value = "tasklist/assignee/{assigneeId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def getTaskList(@PathVariable("assigneeId") String assignee,
                    @RequestParam(value = "orderBy", defaultValue = "status") String orderBy)
            throws TaskServerException, Exception{
        if(assignee==null || assignee.size()==0){
            return ErrorResponse.createErrorResponse("Missing assignee",HttpStatus.BAD_REQUEST)
        }
        return taskService.getTaskListByAssignee(assignee, orderBy)

    }

    @RequestMapping(value = "tasklist/unassigned/{familyId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def getUnAssignedTaskList(@PathVariable("familyId") int familyId,
                              @RequestParam(value = "orderBy", defaultValue = "category") String orderBy)
            throws TaskServerException, Exception {
        def tasks = taskService.getUnAssignedTaskList(familyId, orderBy)
        return tasks
    }

    @RequestMapping(value = "task/{taskId}/status",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def updateTaskStatus(@PathVariable("taskId") String taskId,
                         @RequestParam(value = "status", required = true) String status)
            throws TaskServerException, Exception {

        if(!mainConfig.statusList.contains(status.toUpperCase())){
            return ErrorResponse.createErrorResponse("Invalid status",HttpStatus.BAD_REQUEST)
        }
        def result = taskService.changeTaskStatus(taskId, status)
        return result
    }

    @RequestMapping(value = "task/{taskId}/assignee",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def updateTaskAssignee(@PathVariable("taskId") String taskId,
                           @RequestParam("assignee") String assignee) {
        def result = taskService.changeTaskAssignee(taskId, assignee)
        return result
    }

    @RequestMapping(value = "task/{taskId}/redeem",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def useTaskpoints(@PathVariable("taskId") String taskId) {
        def result = taskService.redeemTaskPoints(taskId)
        return result
    }

    @RequestMapping(value = "task/{taskId}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    def removeTask(@PathVariable("taskId") String taskId) {
        def result = taskService.deleteTask(taskId)
        return result
    }

}
