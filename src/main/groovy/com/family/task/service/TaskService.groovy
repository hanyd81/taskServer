package com.family.task.service

import com.family.task.constants.Constants
import com.family.task.constants.TaskStatus
import com.family.task.exception.TaskServerException
import com.family.task.jdbc.TaskDataJdbc
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Slf4j
@Service
class TaskService {

    @Autowired
    JsonSlurper jsonSlurper
    @Autowired
    TaskDataJdbc taskDataJdbc


    def createTask(Map theTask, String token) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]

        // set values
        def name = theTask["name"]
        def points = theTask["points"]
        def familyId = theTask["familyId"]
        def effectDays = theTask["effectDays"]

        // validation
        if (name == null || name.toString().size() == 0) {
            throw new TaskServerException("Missing taskName", HttpStatus.BAD_REQUEST)
        }

        if (familyId.class != Integer || familyId == 0) {
            throw new TaskServerException("Missing or invalid family id", HttpStatus.BAD_REQUEST)
        }

//        if (!ServiceHelper.isTokenFamilyIdMatch(token, familyId)) {
//            throw new TaskServerException("FamilyId do not match", HttpStatus.BAD_REQUEST)
//        }

        //set the values
        String category = "OTHER"
        String description = ""
        Date date = new Date()
        def createDate = theTask["createDate"]
        String id = name.toString().toUpperCase()[0] + date.format("MMddHHmmssSS") + familyId.toString()[-1]
        theTask["taskid"] = id
        theTask["status"] = TaskStatus.OPEN.value

        //todo check different date format
        if (createDate != null ) {
            try{
                date = Date.parse("yyyy-MM-dd", createDate.toString())
            }catch (Exception e){
                log.info(e.message)
                throw new TaskServerException("createDate is not valid", HttpStatus.BAD_REQUEST)
            }

        } else {
            createDate=date.format("yyyy-MM-dd")
            theTask["createDate"] = createDate
        }

        if (effectDays == null || effectDays.class != Integer || effectDays <= 0) {
            effectDays = 1
        }

        String deadline = date.plus(effectDays).format("yyyy-MM-dd")
        theTask["deadline"] = deadline

        //todo validate the category?
        if (theTask["category"] != null) {
            category = theTask["category"]
        }

        if (theTask["description"] != null) {
            description = theTask["description"]
        }

        String taskJson = JsonOutput.toJson(theTask)
        int queryResult = taskDataJdbc.insertTaskRecord(id, name, description, points, category, theTask["status"],
                taskJson, familyId, createDate, deadline, theTask.getAt("assignee").toString())

        if (queryResult == 0) {
            throw new TaskServerException("Fail to create task", HttpStatus.INTERNAL_SERVER_ERROR)
        }
        result.result = Constants.RESULT_SUCCESS
        result.taskId = id
        return result
    }

    def getTaskById(String taskId) {
        def result = [
                result: Constants.RESULT_FAIL,
                task  : null
        ]

        result.task = taskDataJdbc.getTaskByTaskId(taskId)
        if (result.task == null) {
            throw new TaskServerException("Task not found", HttpStatus.NOT_FOUND)
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }


    def getTaskListByFamilyId(int familyId, String orderBy) {
        def result = [
                result    : Constants.RESULT_FAIL,
                totalcount: 0,
                taskList  : []
        ]
        def taskList = taskDataJdbc.getTaskByFamilyId(familyId, orderBy)

        result.totalcount = taskList.size()
        for (task in taskList) {
            result.taskList.push(jsonSlurper.parseText(task))
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }

    def getTaskListByAssignee(String assignee, String orderBy) {
        def result = [
                result    : Constants.RESULT_FAIL,
                totalcount: 0,
                taskList  : []
        ]
        def taskList = taskDataJdbc.getTaskByAssignee(assignee, orderBy)

        if (taskList == null) {
            return result
        }

        result.totalcount = taskList.size()
        for (task in taskList) {
            result.taskList.push(jsonSlurper.parseText(task))
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }

    def getUnAssignedTaskList(int familyId, String orderBy) {
        def result = [
                result    : Constants.RESULT_FAIL,
                totalcount: 0,
                taskList  : []
        ]
        def taskList = taskDataJdbc.getUnAssignedTaskByFamilyId(familyId, orderBy)

        if (taskList == null) {
            return result
        }
        result.totalcount = taskList.size()
        for (task in taskList) {
            result.taskList.push(jsonSlurper.parseText(task))
        }
        result.result = Constants.RESULT_SUCCESS
        return result
    }

    def changeTaskStatus(String taskId, String status) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]

        int queryResult = taskDataJdbc.updateStatusByTaskId(taskId, status)

        if (queryResult < 1) {
            throw new TaskServerException("Task not found", HttpStatus.BAD_REQUEST)
        }
        result.result = Constants.RESULT_SUCCESS
        result.message = "task status updated to " + status
        return result
    }

    def changeTaskAssignee(String taskId, String assignee) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]
        int queryResult = taskDataJdbc.updateAssigneeByTaskId(taskId, assignee)
        if (queryResult < 1) {
            throw new TaskServerException("Task not found", HttpStatus.BAD_REQUEST)
        }
        result.result = Constants.RESULT_SUCCESS
        result.message = "task assignee updated to " + assignee
        return result
    }

    def redeemTaskPoints(String taskId) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]
        def payload = taskDataJdbc.getPointsAssgneeByTaskId(taskId)

        if (payload.size() == 0) {
            throw new TaskServerException("Task not found", HttpStatus.BAD_REQUEST)
        }

        String userId = payload[0].getAt("assignee").toString()
        String points = payload[0].getAt("points").toString()

        def queryResult = taskDataJdbc.addOrSubtractPointsByUserId(userId, points)
        if (queryResult < 1) {
            result.message = "fail to update user"
            return result
        }

        queryResult = taskDataJdbc.updatePointsStatusByTaskId(taskId, "0", TaskStatus.REDEEMED.value)
        if (queryResult < 1) {
            result.message = "fail to update task"
            taskDataJdbc.addOrSubtractPointsByUserId(userId, points, false)
            return result
        }

        result.result = Constants.RESULT_SUCCESS
        return result

    }

    def deleteTask(String taskId) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]
        int queryResult = taskDataJdbc.deleteTaskByTaskId(taskId)
        if (queryResult < 1) {
            throw new TaskServerException("Task not found", HttpStatus.NOT_FOUND)
        }
        result.result = Constants.RESULT_SUCCESS
        result.message = "task " + taskId + " deleted "
        return result
    }
}
