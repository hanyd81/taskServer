package com.family.task.service

import com.family.task.constants.Constants
import com.family.task.jdbc.TaskDataJdbc
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TaskService {

    @Autowired
    JsonSlurper jsonSlurper
    @Autowired
    JsonBuilder jsonBuilder
    @Autowired
    TaskDataJdbc taskDataJdbc


    def createTask(String taskJsonStr) {
        def result = [
                result : Constants.RESULT_FAIL,
                taskId : null,
                message: ""
        ]

        def theTask = jsonSlurper.parseText(taskJsonStr)

        if (theTask.getAt("name") == null || theTask.getAt("name").toString().length() == 0) {
            result.message = "missing name "
            return result
        }
        if (theTask.getAt("familyId") == null || theTask.getAt("familyId").toString().length() == 0) {
            result.message = "missing family id"
            return result
        }

        // set default values
        String name = theTask.getAt("name").toString()
        int familyId = theTask.getAt("familyId").intValue()
        int points = 0
        int effectDays = 1
        String category = "OTHER"
        String description = ""

        Date date = new Date()
        String createDate = date.format("yyyy-MM-dd")
        String id = name.toUpperCase()[0] + date.format("MMddHHmmssSS") + familyId.toString()[-1]

        //set the values
        theTask.putAt("taskid", id)
        theTask.putAt("status", Constants.STATUS_LIST[0])

        if (theTask.getAt("createDate") != null) {
            createDate = theTask.getAt("createDate")
            date = Date.parse("yyyy-MM-dd", createDate)

        } else {
            theTask.putAt("createDate", createDate)
        }

        if (theTask.getAt("effectDays") != null) {
            effectDays = theTask.getAt("effectDays").intValue()
        }
        String deadline = date.plus(effectDays).format("yyyy-MM-dd")
        theTask.putAt("deadline", deadline)

        if (theTask.getAt("points") != null) {
            points = theTask.getAt("points").intValue()
        }

        if (theTask.getAt("category") != null) {
            category = theTask.getAt("category")
        }

        if (theTask.getAt("description") != null) {
            description = theTask.getAt("description")
        }

        String taskJson = JsonOutput.toJson(theTask)
        int queryResult = taskDataJdbc.insertTaskRecord(id, name, description, points, category, Constants.STATUS_LIST[0],
                taskJson, familyId, createDate, deadline, theTask.getAt("assignee").toString())

        if (queryResult > 0) {
            result.result = Constants.RESULT_SUCCESS
            result.taskId = id
        }

        return result
    }

    def getTaskById(String taskId) {
        def result = [
                result: Constants.RESULT_FAIL,
                task  : null
        ]

        result.task = taskDataJdbc.getTaskByTaskId(taskId)
        if (result.task != null) {
            result.result = Constants.RESULT_SUCCESS
        }
        return result
    }


    def getTaskListByFamilyId(int familyId, String orderBy) {
        def result = [
                result    : Constants.RESULT_FAIL,
                totalcount: 0,
                taskList  : []
        ]
        def taskList = taskDataJdbc.getTaskByFamilyId(familyId, orderBy)

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
        String standardStatus = null
        for (stat in Constants.STATUS_LIST) {
            if (status.toUpperCase() == stat) {
                standardStatus = stat
                break
            }
        }
        if (standardStatus == null) {
            result.message = "unknown status"
            return result
        }

        int queryResult = taskDataJdbc.updateStatusByTaskId(taskId, standardStatus)

        if (queryResult < 1) {
            result.result = Constants.RESULT_FAIL
            result.message = "fail to update task"
            return result
        }
        result.result = Constants.RESULT_SUCCESS
        result.message = "task status updated to " + status
    }

    def changeTaskAssignee(String taskId, String assignee) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]
        int queryResult = taskDataJdbc.updateAssigneeByTaskId(taskId, assignee)
        if (queryResult < 1) {
            result.message = "fail to update task"
            return result
        }
        result.result = Constants.RESULT_SUCCESS
        result.message = "task assignee updated to " + assignee
    }

    def redeemTaskPoints(String taskId) {
        def result = [
                result : Constants.RESULT_FAIL,
                message: ""
        ]
        def payload = taskDataJdbc.getPointsAssgneeByTaskId(taskId)

        if (payload.size() == 0) {
            result.message = "task " + taskId + " not found"
            return result
        }

        String userId = payload[0].getAt("assignee").toString()
        String points = payload[0].getAt("points").toString()

        def queryResult = taskDataJdbc.addOrSubtractPointsByUserId(userId, points)
        if (queryResult < 1) {
            result.message = "fail to update user"
            return result
        }

        queryResult = taskDataJdbc.updatePointsStatusByTaskId(taskId, "0", Constants.STATUS_LIST[-1])
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
            result.message = "fail to delete task"
            return result
        }
        result.result = Constants.RESULT_SUCCESS
        result.message = "task " + taskId + " deleted "
    }
}
