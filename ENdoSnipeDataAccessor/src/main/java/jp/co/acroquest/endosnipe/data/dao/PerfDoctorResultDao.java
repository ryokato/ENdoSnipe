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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.acroquest.endosnipe.common.util.SQLUtil;
import jp.co.acroquest.endosnipe.data.dto.PerfDoctorResultDto;
import jp.co.acroquest.endosnipe.util.ResourceDataDaoUtil;

/**
 * PerformanceDoctor診断結果蓄積テーブルのDAO
 * 
 * @author hiramatsu
 *
 */
public class PerfDoctorResultDao extends AbstractDao
{

    /** テーブル名 */
    private static final String PERFDOCTOR_RESULT_TABLE = "PERFDOCTOR_RESULT";

    /**
     * レコードを挿入します。<br />
     *
     * @param database データベース名
     * @param pDResult 挿入する値
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public static void insert(final String database, PerfDoctorResultDto pDResult)
        throws SQLException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = getConnection(database);
            Date occurrenceDate = new Date(pDResult.getOccurrenceTime().getTime());
            String tableName =
                ResourceDataDaoUtil.getTableNameToInsert(occurrenceDate, PERFDOCTOR_RESULT_TABLE);
            String sql =
                "insert into " + tableName + "(OCCURRENCE_TIME, DESCRIPTION, LEVEL, "
                    + "CLASS_NAME, METHOD_NAME, JAVELIN_LOG_NAME, MEASUREMENT_ITEM_NAME)"
                    + " values (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            // CHECKSTYLE:OFF
            pstmt.setTimestamp(1, pDResult.getOccurrenceTime());
            pstmt.setString(2, pDResult.getDescription());
            pstmt.setString(3, pDResult.getLevel());
            pstmt.setString(4, pDResult.getClassName());
            pstmt.setString(5, pDResult.getMethodName());
            pstmt.setString(6, pDResult.getLogFileName());
            pstmt.setString(7, pDResult.getMeasurementItemName());
            // CHECKSTYLE:ON
            pstmt.execute();
        }
        finally
        {
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }
    }

    /**
     * 期間と項目名を指定してデータを取得する。
     * 
     * @param dbName データベース名
     * @param start 開始時刻
     * @param end 終了時刻
     * @param dataGroupId ID
     * @return 診断結果DTOのリスト
     * @throws SQLException select失敗時
     */
    public static List<PerfDoctorResultDto> selectByTermAndName(String dbName, Timestamp start,
        Timestamp end, String dataGroupId)
        throws SQLException
    {
        List<PerfDoctorResultDto> result = new ArrayList<PerfDoctorResultDto>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {

            conn = getConnection(dbName, true);
            String sql =
                createSelectSqlByTermAndName(PERFDOCTOR_RESULT_TABLE, start, end, dataGroupId);
            pstmt = conn.prepareStatement(sql);
            PreparedStatement delegated = getDelegatingStatement(pstmt);
            setTimestampByTerm(delegated, start, end);
            rs = delegated.executeQuery();

            // 結果をリストに１つずつ格納する
            while (rs.next() == true)
            {
                PerfDoctorResultDto dto = new PerfDoctorResultDto();
                //                setJavelinLogFromResultSet(dto, rs, false);
                setPerfDoctorResultFromResultSet(dto, rs, false);
                result.add(dto);
            }
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }

        return result;
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
        String tableName = String.format("%s_%02d", PERFDOCTOR_RESULT_TABLE, tableIndex);
        truncate(database, tableName);
        alterCheckConstraint(database, tableName, tableIndex, "OCCURRENCE_TIME", year);
    }

    private static void setPerfDoctorResultFromResultSet(final PerfDoctorResultDto dto,
        final ResultSet rs, final boolean outputLog)
        throws SQLException
    { // CHECKSTYLE:OFF
        dto.setLogId(rs.getLong(1));
        dto.setOccurrenceTime(rs.getTimestamp(2));
        dto.setDescription(rs.getString(3));
        dto.setLevel(rs.getString(4));
        dto.setMethodName(rs.getString(5));
        dto.setClassName(rs.getString(6));
        dto.setLogFileName(rs.getString(7));
        dto.setMeasurementItemName(rs.getString(8));
        // CHECKSTYLE:ON
    }

    private static void setTimestampByTerm(final PreparedStatement delegated,
        final Timestamp start, final Timestamp end)
        throws SQLException
    {
        if (start != null && end != null)
        {
            delegated.setTimestamp(1, start);
            delegated.setTimestamp(2, end);
        }
        else if (start != null && end == null)
        {
            delegated.setTimestamp(1, start);
        }
        else if (start == null && end != null)
        {
            delegated.setTimestamp(1, end);
        }
    }

    private static String createSelectSqlByTermAndName(final String tableName,
        final Timestamp start, final Timestamp end, final String name)
    {
        String sql = "select * from " + tableName;
        if (start != null && end != null)
        {
            sql += " where ? <= OCCURRENCE_TIME and OCCURRENCE_TIME <= ?";
        }
        else if (start != null && end == null)
        {
            sql += " where ? <= OCCURRENCE_TIME";
        }
        else if (start == null && end != null)
        {
            sql += " where OCCURRENCE_TIME <= ?";
        }
        if (name != null)
        {
            sql +=
                ((start == null && end == null) ? " where " : " and ")
                    + "MEASUREMENT_ITEM_NAME like '" + name + "%'";
        }
        sql += " order by OCCURRENCE_TIME desc";
        return sql;
    }
}
