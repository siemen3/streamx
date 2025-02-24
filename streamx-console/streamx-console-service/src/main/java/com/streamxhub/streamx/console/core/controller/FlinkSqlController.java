/*
 *  Copyright (c) 2019 The StreamX Project
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamxhub.streamx.console.core.controller;

import com.streamxhub.streamx.common.enums.SQLErrorType;
import com.streamxhub.streamx.console.base.controller.BaseController;
import com.streamxhub.streamx.console.base.domain.RestResponse;
import com.streamxhub.streamx.console.base.exception.ServiceException;
import com.streamxhub.streamx.console.core.entity.Application;
import com.streamxhub.streamx.console.core.entity.FlinkSql;
import com.streamxhub.streamx.console.core.service.FlinkSqlService;
import com.streamxhub.streamx.flink.common.util.SQLCommandUtil;
import com.streamxhub.streamx.flink.common.util.SQLError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author benjobs
 */
@Slf4j
@Validated
@RestController
@RequestMapping("flink/sql")
public class FlinkSqlController extends BaseController {

    @Autowired
    private FlinkSqlService flinkSqlService;

    private String SQL_SPARSE_FAILED_REGEXP = "SQL\\sparse\\sfailed\\.\\sEncountered\\s\"(.*)\"\\sat\\sline\\s\\d,\\scolumn\\s\\d.*";

    @PostMapping("verify")
    public RestResponse verify(String sql) {
        SQLError sqlError = SQLCommandUtil.verifySQL(sql);
        if (sqlError != null) {
            String[] array = sqlError.sql().trim().split("\n");
            String start = array[0].trim();
            String end = array.length > 1 ? array[array.length - 1].trim() : null;

            //记录错误类型,出错的sql,原因,错误的开始行和结束行内容(用于前端查找mod节点)
            RestResponse response = RestResponse.create()
                    .data(false)
                    .message(sqlError.exception())
                    .put("type", sqlError.errorType().errorType)
                    .put("sql", sqlError.sql())
                    .put("start", start)
                    .put("end", end);
            //语法异常
            if (sqlError.errorType().equals(SQLErrorType.SYNTAX_ERROR)) {
                String exception = sqlError.exception().replaceAll("\r|\n", "");
                if (exception.matches(SQL_SPARSE_FAILED_REGEXP)) {
                    String[] lineColumn = exception
                            .replaceAll("^.*\\sat\\sline\\s", "")
                            .replaceAll(",\\scolumn\\s", ",")
                            .replaceAll("\\.(.*)", "")
                            .trim()
                            .split(",");

                    //记录第几行出错.
                    response.put("line", lineColumn[0])
                            .put("column ", lineColumn[1]);
                }
            }
            return response;
        } else {
            return RestResponse.create().data(true);
        }
    }

    @PostMapping("get")
    public RestResponse get(String id) throws ServiceException {
        String[] array = id.split(",");
        FlinkSql flinkSql1 = flinkSqlService.getById(array[0]);
        flinkSql1.base64Encode();
        if (array.length == 1) {
            return RestResponse.create().data(flinkSql1);
        }
        FlinkSql flinkSql2 = flinkSqlService.getById(array[1]);
        flinkSql2.base64Encode();
        return RestResponse.create().data(new FlinkSql[]{flinkSql1, flinkSql2});
    }

    @PostMapping("history")
    public RestResponse sqlhistory(Application application) {
        List<FlinkSql> sqlList = flinkSqlService.history(application);
        return RestResponse.create().data(sqlList);
    }

}
