package com.family.task.jdbc

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Repository
class TaskDataJdbc {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    JsonSlurper jsonSlurper

    // table names
    final String TASK_TABLE = "task"
    final String USER_TABLE = "task_user"
    final String FAMILY_TABLE = "task_family"

    @Transactional
    int insertTaskRecord(String taskId, String name, String description, int points, String category, String status,
                         String taskJson, int familyId, String createDate, String deadline, String assignee) {
        String sqlColumns = "INSERT INTO " + TASK_TABLE +
                "(taskid, name, description, status, points, category,task_json,familyId,create_date,dead_line"

        String sqlValues = "VALUES ( '" +
                taskId + "', '" +
                name + "', '" +
                description + "','" +
                status + "'," +
                points + ", '" +
                category + "','" +
                taskJson + "'," +
                familyId + ",'" +
                createDate + "','" +
                deadline

        if (assignee == 'null' || assignee == "") {
            sqlColumns += ") "
            sqlValues += "')"
        } else {
            sqlColumns += ", assignee) "
            sqlValues += "','" + assignee + "')"
        }

        String sql = sqlColumns + sqlValues

        try {
            def result = jdbcTemplate.update(sql)
            return result
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return 0
        }
    }

    def getTaskByTaskId(String taskId) {
        def task
        try {
            def row = jdbcTemplate.queryForObject(
                    "SELECT task_json FROM " + TASK_TABLE + " where taskid='" + taskId + "'", String.class)
            task = jsonSlurper.parseText(row)
        } catch (EmptyResultDataAccessException ignored) {
            log.debug("${taskId} : task Not Found")
            return null
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return null
        }

        return task
    }

    // get task list by familyId
    def getTaskByFamilyId(int familyId, String orderBy) {
        String sql = "SELECT task_json FROM " + TASK_TABLE + " WHERE familyId=" + familyId + " ORDER BY " + orderBy
        def tasklist
        try {
            tasklist = jdbcTemplate.queryForList(sql, String.class)

        } catch (EmptyResultDataAccessException ignored) {
            log.debug("Task Not Found for family ${familyId}")
            return null
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return null
        }

        return tasklist
    }

    // get task list for assignee
    def getTaskByAssignee(String assignee, String orderBy) {
        String sql = "SELECT task_json FROM " + TASK_TABLE + " WHERE assignee= '" + assignee + "' ORDER BY " + orderBy
        def taskList
        try {
            taskList = jdbcTemplate.queryForList(sql, String.class)
        } catch (EmptyResultDataAccessException ignored) {
            def txt = "Task Not Found for assignee" + assignee
            log.debug(txt)
            return null
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return null
        }

        return taskList
    }

    // get unassigned task list in a family
    def getUnAssignedTaskByFamilyId(int familyId, String orderBy) {
        String sql = "SELECT task_json FROM " + TASK_TABLE + " WHERE familyId=" + familyId +
                " and assignee is null ORDER BY " + orderBy
        def taskList
        try {
            taskList = jdbcTemplate.queryForList(sql, String.class)
        } catch (EmptyResultDataAccessException ignored) {
            def txt = "Task Not Found "
            log.debug(txt)
            return null
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return null
        }

        return taskList
    }

    def getPointsAssgneeByTaskId(String taskId) {
        String sql = "SELECT points, assignee FROM " + TASK_TABLE + " WHERE taskid='" + taskId + "'"
        try {
            def result = jdbcTemplate.queryForList(sql)
            return result
        } catch (EmptyResultDataAccessException ignored) {
            log.debug("task " + taskId + " not found")
            return null
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return null
        }
    }

    def updateStatusByTaskId(String taskId, String status) {
        String sql = "UPDATE task SET status='" + status +
                "', task_json=jsonb_set(task_json, '{status}','\"" + status + "\"')" +
                " WHERE taskid='" + taskId + "'"
        try {
            def result = jdbcTemplate.update(sql)
            return result
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return 0
        }
    }

    def updateAssigneeByTaskId(String taskId, String assignee) {
        String sql = "UPDATE task SET assignee='" + assignee +
                "', task_json=jsonb_set(task_json, '{assignee}','\"" + assignee + "\"')" +
                " WHERE taskid='" + taskId + "'"
        try {
            def result = jdbcTemplate.update(sql)
            return result
        } catch (Exception ex) {
            log.info(ex.getMessage())
            return 0
        }
    }

    def updatePointsByTaskId(String taskId, String points) {
        String sql = "UPDATE task SET points=" + points +
                ", task_json=jsonb_set (task_json, '{points}','" + points + "')" +
                " where taskid='" + taskId + "'"
        try {
            def result = jdbcTemplate.update(sql)
            return result
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return 0
        }
    }

    // for redeem points
    def updatePointsStatusByTaskId(String taskId, String points, String status) {
        String sql = "UPDATE task SET points=" + points +
                ", status='" + status + "', " +
                "task_json=task_json::jsonb || " +
                "'{ \"points\":" + points + ", " +
                "\"status\":\"" + status + "\"}'" +
                " where taskid='" + taskId + "'"
        try {
            def result = jdbcTemplate.update(sql)
            return result
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return 0
        }
    }

    def deleteTaskByTaskId(String taskId) {
        String sql = "DELETE FROM task WHERE taskId='" + taskId + "'"
        try {
            def result = jdbcTemplate.update(sql)
            return result
        } catch (Exception ex) {
            log.debug(ex.getMessage())
            return 0
        }
    }

    //-------------family table ---------------------------------------//

    @Transactional
    int insertFamily(String familyName, String category) {
        String sql = "INSERT INTO " + FAMILY_TABLE + "(familyname, categories) VALUES ( '" +
                familyName + "','" + category + "') RETURNING familyId"

        return jdbcTemplate.queryForObject(sql, Integer.class)
    }

    String getCategoryByFamilyID(String familyId) {
        String sql = "SELECT categories FROM " + FAMILY_TABLE + " WHERE familyid='" + familyId + "'"

        return jdbcTemplate.queryForObject(sql, String.class)
    }

    @Transactional
    def updateCategoriesByFamilyId(String familyId, String category) {
        String sql = "UPDATE " + FAMILY_TABLE + " SET categories='" + category + "' WHERE familyid='" + familyId + "'"

        return jdbcTemplate.update(sql)
    }

    @Transactional
    def deleteFamilyByFamilyId(String familyId) {
        String sql = "DELETE FROM " + FAMILY_TABLE + " WHERE familyid='" + familyId + "'"

        return jdbcTemplate.update(sql)
    }

    Map insertFamilyAndUser(String familyName, String category, String userName, String passwords, String roles, String nickname) {
        int familyId = 0;
        int userId = 0
        jdbcTemplate.execute("BEGIN")
        try {
            String insertFamilySql = "INSERT INTO " + FAMILY_TABLE + "(familyname, categories) VALUES ( '" +
                    familyName + "','" + category + "') RETURNING familyId"
            familyId = jdbcTemplate.queryForObject(insertFamilySql, Integer.class)
            String insertUsersql = "INSERT INTO " + USER_TABLE + "(username,passwords,roles,nickname,familyid) VALUES ( '" +
                    userName + "','" +
                    passwords + "','" +
                    roles + "','" +
                    nickname + "'," +
                    familyId +
                    ")  RETURNING id"

            userId = jdbcTemplate.queryForObject(insertUsersql, Integer.class)
        } catch (Exception e) {
            jdbcTemplate.execute("ROLLBACK")
            throw e
        }
        jdbcTemplate.execute("COMMIT")
        return ["familyId":familyId,
                "userId":userId,
        ]
    }
    //-------------user table ---------------------------------------/
    @Transactional
    int insertUser(String userName, String passwords, String roles, String nickname, int familyId) {
        String sql = "INSERT INTO " + USER_TABLE + "(username,passwords,roles,nickname,familyid) VALUES ( '" +
                userName + "','" +
                passwords + "','" +
                roles + "','" +
                nickname + "'," +
                familyId +
                ")  RETURNING id"

        int result = jdbcTemplate.queryForObject(sql, Integer.class)
        return result
    }

    def getUserById(String id) {
        String sql = "SELECT " +
                "id, " +
                "username as \"userName\"," +
                "roles, " +
                "nickname as \"nickName\"," +
                "points, " +
                "familyid as \"familyId\" " +
                " FROM " + USER_TABLE + " WHERE id='" + id + "'"

        return jdbcTemplate.queryForMap(sql)
    }

    def getUserById(int id) {
        String sql = "SELECT " +
                "username as \"userName\", " +
                "roles, " +
                "familyid as \"familyId\" " +
                " FROM " + USER_TABLE + " WHERE id= ${id}"

        return jdbcTemplate.queryForList(sql)
    }

    def getUserPassWordByName(String userName) {
        String sql = "SELECT " +
                "id as \"userId\"," +
                "passwords, " +
                "roles, " +
                "familyid as \"familyId\" " +
                " FROM " + USER_TABLE + " WHERE username='" + userName + "'"

        return jdbcTemplate.queryForList(sql)
    }

    def getUserByFamilyId(String familyId) {
        String sql = "SELECT " +
                "id, " +
                "username as \"userName\"," +
                "roles, " +
                "nickname as \"nickName\"," +
                "points, " +
                "familyid as \"familyId\" " +
                " FROM " + USER_TABLE + " WHERE familyid=" + familyId

        return jdbcTemplate.queryForList(sql)
    }

    @Transactional
    def addOrSubtractPointsByUserId(String id, String points, Boolean isAdd = true) {
        String op = isAdd ? "+" : "-"
        String sql = "UPDATE " + USER_TABLE + " SET points=points" + op +
                points + " WHERE id='" + id + "'"

        return jdbcTemplate.update(sql)
    }

    def updateNicknameByUserId(String userId, String nickName) {
        String sql = "UPDATE " + USER_TABLE + " SET nickname='" + nickName + "'" + " WHERE id='" + userId + "'"

        return jdbcTemplate.update(sql)
    }

    def checkUserExist(String id) {
        String sql = "SELECT id FROM " + USER_TABLE + " WHERE id='" + id + "'"
        String result
        try {
            result = jdbcTemplate.queryForObject(sql, String.class)
        } catch (EmptyResultDataAccessException e) {
            log.debug(e.getMessage())
            return false
        }
        if (result.length() == 0) {
            return false
        }
        return true
    }

    def checkUserExistByUserName(String userName) {
        String sql = "SELECT id FROM " + USER_TABLE + " WHERE userName='" + userName + "'"
        String result
        try {
            result = jdbcTemplate.queryForObject(sql, String.class)
        } catch (EmptyResultDataAccessException e) {
            log.debug(e.getMessage())
            return false
        }
        if (result.length() == 0) {
            return false
        }
        return true
    }

    def deleteUserById(String id) {
        String sql = "DELETE FROM " + USER_TABLE + " WHERE id='" + id + "'"

        return jdbcTemplate.update(sql)
    }

}
