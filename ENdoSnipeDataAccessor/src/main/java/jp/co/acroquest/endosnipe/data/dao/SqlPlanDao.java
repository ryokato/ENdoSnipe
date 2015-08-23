/*
 * Copyright (c) 2004-2013 Acroquest Technology Co., Ltd. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 *
 * THE  SOFTWARE IS  PROVIDED BY  Acroquest Technology Co., Ltd., WITHOUT  WARRANTY  OF
 * ANY KIND,  EXPRESS  OR IMPLIED,  INCLUDING BUT  NOT LIMITED  TO THE
 * WARRANTIES OF  MERCHANTABILITY,  FITNESS FOR A  PARTICULAR  PURPOSE
 * AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
 * CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package jp.co.acroquest.endosnipe.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import jp.co.acroquest.endosnipe.common.util.SQLUtil;
import jp.co.acroquest.endosnipe.data.TableNames;
import jp.co.acroquest.endosnipe.data.entity.SqlPlan;

/**
 * {@link SqlPlan} のための DAO です。
 * 
 * @author miyasaka
 *
 */
public class SqlPlanDao extends AbstractDao implements TableNames
{
    /**
     * SQL実行計画のレコードを追加します。<br />
     * 
     * @param database データベース名
     * @param sqlPlan 挿入するSQL実行計画
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public static void insert(final String database, final SqlPlan sqlPlan)
        throws SQLException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            conn = getConnection(database);
            pstmt =
                conn.prepareStatement("insert into " + SQL_PLAN
                    + " (MEASUREMENT_ITEM_NAME, SQL_STATEMENT,"
                    + " EXECUTION_PLAN, GETTING_PLAN_TIME, STACK_TRACE)" + " values (?,?,?,?,?)");
            PreparedStatement delegated = getDelegatingStatement(pstmt);
            // CHECKSTYLE:OFF
            String measurementItemName = sqlPlan.measurementItemName;
            String sqlStatement = sqlPlan.sqlStatement;
            String executionPlan = sqlPlan.executionPlan;
            Timestamp gettingPlanTime = sqlPlan.gettingPlanTime;
            String stackTrace = sqlPlan.stackTrace;

            delegated.setString(1, measurementItemName);
            delegated.setString(2, sqlStatement);
            delegated.setString(3, executionPlan);
            delegated.setTimestamp(4, gettingPlanTime);
            delegated.setString(5, stackTrace);
            // CHECKSTYLE:ON
            delegated.execute();
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }
    }

    /**
     * SQL実行計画のレコードを追加します。<br />
     * 
     * @param database データベース名
     * @param sqlPlan 挿入するSQL実行計画
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public static int update(final String database, final SqlPlan sqlPlan)
        throws SQLException
    {
        int updateCount = 0;
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            conn = getConnection(database);
            pstmt =
                conn.prepareStatement("update " + SQL_PLAN
                    + " SET EXECUTION_PLAN = ?, GETTING_PLAN_TIME = ?"
                    + " WHERE MEASUREMENT_ITEM_NAME = ? AND SQL_STATEMENT = ? AND STACK_TRACE = ?");
            PreparedStatement delegated = getDelegatingStatement(pstmt);
            // CHECKSTYLE:OFF
            String measurementItemName = sqlPlan.measurementItemName;
            String sqlStatement = sqlPlan.sqlStatement;
            String executionPlan = sqlPlan.executionPlan;
            Timestamp gettingPlanTime = sqlPlan.gettingPlanTime;
            String stackTrace = sqlPlan.stackTrace;

            delegated.setString(1, executionPlan);
            delegated.setTimestamp(2, gettingPlanTime);
            delegated.setString(3, measurementItemName);
            delegated.setString(4, sqlStatement);
            delegated.setString(5, stackTrace);
            // CHECKSTYLE:ON
            delegated.execute();
            updateCount = delegated.getUpdateCount();
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }
        
        return updateCount;
    }

    /**
     * 指定したインデックスのテーブルを truncate します。
     *
     * @param database データベース名
     * @param tableIndex テーブルインデックス
     * @param year 次にこのテーブルに入れるデータの年
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public static void truncate(final String database, final int tableIndex, final int year)
        throws SQLException
    {
        String tableName = String.format("%s_%02d", SQL_PLAN, tableIndex);
        truncate(database, tableName);
        alterCheckConstraint(database, tableName, tableIndex, "GETTING_PLAN_TIME", year);
    }
}
