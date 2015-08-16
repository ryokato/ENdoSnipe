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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.acroquest.endosnipe.common.util.SQLUtil;
import jp.co.acroquest.endosnipe.data.TableNames;
import jp.co.acroquest.endosnipe.data.dto.SummarySignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.entity.SummarySignalDefinition;

/**
 * {@link SummarySignalDefinition} のための DAO です。
 * 
 * @author pin
 *
 */
public class SummarySignalDefinitionDao extends AbstractDao implements TableNames
{
    private static final String SUMMARY_SIGNAL_DEFINITION = "SUMMARY_SIGNAL_DEFINITION";

    private static final int SUMMARY_SIGNAL_INDEX1 = 3;

    private static final int SUMMARY_SIGNAL_INDEX2 = 4;

    /**
     * 指定されたデータベースのSummarySignal定義を全て取得します。<br />
     *
     * SummarySignal定義が登録されていない場合はerrorMessageにmessageを入れます。<br />
     *
     * @param databaseName データベース名
     * @return SummarySignalDefinitionのリスト
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public static List<SummarySignalDefinition> selectAll(String databaseName)
        throws SQLException
    {
        List<SummarySignalDefinition> result = new ArrayList<SummarySignalDefinition>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String summarySignal;
        try
        {
            conn = getConnection(databaseName, false);
            stmt = conn.createStatement();
            String sql = "select * from " + SUMMARY_SIGNAL_DEFINITION;
            rs = stmt.executeQuery(sql);
            while (rs.next() == true)
            {
                // CHECKSTYLE:OFF
                SummarySignalDefinition summarySignalInfo = new SummarySignalDefinition();
                List<String> summarySignalList = new ArrayList<String>();
                summarySignalInfo.summarySignalId = rs.getLong(1);
                summarySignalInfo.summarySignalName = rs.getString(2);
                summarySignal = rs.getString(3);
                String[] temp = null;
                temp = summarySignal.split(",");
                for (int index = 0; index < temp.length; index++)
                {
                    summarySignalList.add(temp[index]);
                }
                summarySignalInfo.signalList = summarySignalList;
                summarySignalInfo.summarySignalType = rs.getInt(4);
                summarySignalInfo.priority = rs.getInt(5);
                summarySignalInfo.sendMail = rs.getBoolean(6);
                result.add(summarySignalInfo);
            }
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(stmt);
            SQLUtil.closeConnection(conn);
        }
        return result;
    }

    /**
     * Get the SummarySignalDefinition List according to the selected name.<br/>
     *
     * @param dbName データベース名
     * @param summarySignalName SummarySignal名
     * @return SummarySignalDefinitionのリスト
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public List<SummarySignalDefinition> selectByName(String dbName, String summarySignalName)
        throws SQLException
    {
        List<SummarySignalDefinition> result = new ArrayList<SummarySignalDefinition>();
        List<String> summarySignalList = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String summarySignal;
        try
        {
            conn = getConnection(dbName, false);
            String sql =
                "select * from " + SUMMARY_SIGNAL_DEFINITION + " where SUMMARY_SIGNAL_NAME=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, summarySignalName);
            rs = pstmt.executeQuery();
            while (rs.next() == true)
            {
                // CHECKSTYLE:OFF
                SummarySignalDefinition summarySignalInfo = new SummarySignalDefinition();
                summarySignalInfo.summarySignalId = rs.getLong(1);
                summarySignalInfo.summarySignalName = rs.getString(2);
                summarySignal = rs.getString(3);
                String[] temp = null;
                temp = summarySignal.split(",");
                for (int index = 0; index < temp.length; index++)
                {
                    summarySignalList.add(temp[index]);
                }
                summarySignalInfo.signalList = summarySignalList;
                summarySignalInfo.summarySignalType = rs.getInt(4);
                summarySignalInfo.priority = rs.getInt(5);
                summarySignalInfo.sendMail = rs.getBoolean(6);
                // CHECKSTYLE:ON
                result.add(summarySignalInfo);
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
     * Insert new Summary Signal Data
     *
     * @param dataBaseName データベース名
     * @param summarySignalDefinition summarySignalDefinition
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public static void insert(String dataBaseName, SummarySignalDefinition summarySignalDefinition)
        throws SQLException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = getConnection(dataBaseName);
            String sql =
                "insert into " + SUMMARY_SIGNAL_DEFINITION
                    + "(SUMMARY_SIGNAL_NAME, TARGET_SIGNAL_ID,SIGNAL_TYPE, PRIORITY_NO, SEND_MAIL)"
                    + " values (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, summarySignalDefinition.getSummarySignalName());

            String signalList = "";
            if (summarySignalDefinition.getSignalList() != null
                && summarySignalDefinition.getSignalList().size() > 0)
            {
                for (int count = 0; count < summarySignalDefinition.getSignalList().size(); count++)
                {
                    signalList += summarySignalDefinition.getSignalList().get(count);
                    if (count != summarySignalDefinition.getSignalList().size() - 1)
                    {
                        signalList += ",";
                    }
                }
            }

            pstmt.setString(2, signalList);
            pstmt.setInt(SUMMARY_SIGNAL_INDEX1, summarySignalDefinition.getSummarySignalType());
            pstmt.setInt(SUMMARY_SIGNAL_INDEX2, summarySignalDefinition.getPriority());
            pstmt.setBoolean(5, summarySignalDefinition.sendMail);
            pstmt.execute();
        }
        finally
        {
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }
    }

    /**
     * Get the SummarySignalDefinition sequence id num. <br/>
     *
     * @param dataBaseName データベース名
     * @param summarySignalDefinition summarySignal
     * @return SummarySignalDefinitionのID
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public int selectSequenceNum(String dataBaseName,
        SummarySignalDefinition summarySignalDefinition)
        throws SQLException
    {
        int result = 0;
        String summarySignalName = summarySignalDefinition.getSummarySignalName();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            conn = getConnection(dataBaseName, false);

            String sql =
                "select SUMMARY_SIGNAL_ID from " + SUMMARY_SIGNAL_DEFINITION
                    + " where SUMMARY_SIGNAL_NAME=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, summarySignalName);
            rs = pstmt.executeQuery();
            while (rs.next() == true)
            {
                int summarySignalId = rs.getInt(1);
                result = summarySignalId;
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
     * Get the SummarySignalDefinition List by ordering the priority.<br/>
     *
     * @param dataBaseName データベース名
     * @return SummarySignalDefinitionのリスト
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public List<SummarySignalDefinition> selectAllByPriority(String dataBaseName)
        throws SQLException
    {
        List<SummarySignalDefinition> result = new ArrayList<SummarySignalDefinition>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String summarySignal;
        try
        {
            conn = getConnection(dataBaseName, false);
            stmt = conn.createStatement();
            String sql =
                "select * from " + SUMMARY_SIGNAL_DEFINITION + " ORDER BY PRIORITY_NO DESC";
            rs = stmt.executeQuery(sql);
            while (rs.next() == true)
            {
                List<String> summarySignalList = new ArrayList<String>();
                // CHECKSTYLE:OFF
                SummarySignalDefinition summarySignalInfo = new SummarySignalDefinition();
                summarySignalInfo.summarySignalId = rs.getLong(1);
                summarySignalInfo.summarySignalName = rs.getString(2);
                summarySignal = rs.getString(3);
                String[] temp = null;
                temp = summarySignal.split(",");
                for (int index = 0; index < temp.length; index++)
                {
                    summarySignalList.add(temp[index]);
                }
                summarySignalInfo.signalList = summarySignalList;
                summarySignalInfo.summarySignalType = rs.getInt(4);
                summarySignalInfo.priority = rs.getInt(5);
                summarySignalInfo.sendMail = rs.getBoolean(6);
                result.add(summarySignalInfo);
            }
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(stmt);
            SQLUtil.closeConnection(conn);
        }

        return result;
    }

    /**
     * Delete the summary signal data according to the name.<br/>
     *
     * @param dataBaseName データベース名
     * @param summarySignalName summarySignalのname
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public void delete(String dataBaseName, String summarySignalName)
        throws SQLException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = getConnection(dataBaseName, false);
            String sql =
                "delete from " + SUMMARY_SIGNAL_DEFINITION + " where SUMMARY_SIGNAL_NAME=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, summarySignalName);
            pstmt.execute();
        }

        finally
        {
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);

        }
    }

    /**
     * Get all summary signal name
     *
     * @param dataBaseName データベース名
     * @return summary signal definition map
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public Map<String, SummarySignalDefinition> selectAllSignalName(String dataBaseName)
        throws SQLException
    {
        Map<String, SummarySignalDefinition> result =
            new HashMap<String, SummarySignalDefinition>();
        List<String> summarySignalList = new ArrayList<String>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String summarySignal;
        try
        {
            conn = getConnection(dataBaseName, false);
            stmt = conn.createStatement();
            String sql = "select * from " + SUMMARY_SIGNAL_DEFINITION;
            rs = stmt.executeQuery(sql);
            while (rs.next() == true)
            {
                SummarySignalDefinition summarySignalInfo = new SummarySignalDefinition();
                summarySignalInfo.summarySignalId = rs.getLong(1);
                summarySignalInfo.summarySignalName = rs.getString(2);
                summarySignal = rs.getString(3);
                String[] temp = null;
                temp = summarySignal.split(",");
                for (int index = 0; index < temp.length; index++)
                {
                    summarySignalList.add(temp[index]);
                }
                summarySignalInfo.signalList = summarySignalList;
                summarySignalInfo.summarySignalType = rs.getInt(4);
                summarySignalInfo.priority = rs.getInt(5);
                summarySignalInfo.sendMail = rs.getBoolean(6);
                result.put(summarySignalInfo.summarySignalName, summarySignalInfo);
            }
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(stmt);
            SQLUtil.closeConnection(conn);
        }

        return result;
    }

    /**
     * update the summary signal Information by matching the name<br/>
     *
     * @param dataBaseName データベース名
     * @param summarySignalId summary signal のid
     * @param summarySignalDefinitionDto summarySignalのDto
     * @param priorityFlag flag to update all data or not
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public void update(String dataBaseName, long summarySignalId,
        SummarySignalDefinitionDto summarySignalDefinitionDto, boolean priorityFlag)
        throws SQLException
    {

        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = null;
        try
        {
            conn = getConnection(dataBaseName);
            if (priorityFlag)
            {
                sql =
                    "update " + SUMMARY_SIGNAL_DEFINITION
                        + " SET PRIORITY_NO=? where SUMMARY_SIGNAL_NAME=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, summarySignalDefinitionDto.getPriority());
                pstmt.setString(2, summarySignalDefinitionDto.getSummarySignalName());

            }
            else
            {
                sql =
                    "update "
                        + SUMMARY_SIGNAL_DEFINITION
                        + " SET SUMMARY_SIGNAL_NAME=?, TARGET_SIGNAL_ID=?,SIGNAL_TYPE=?, PRIORITY_NO=?,SEND_MAIL=? where SUMMARY_SIGNAL_NAME=?";

                pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, summarySignalDefinitionDto.getSummarySignalName());
                String signalList = "";
                if (summarySignalDefinitionDto.getSignalList() != null
                    && summarySignalDefinitionDto.getSignalList().size() > 0)
                {
                    for (int count = 0; count < summarySignalDefinitionDto.getSignalList().size(); count++)
                    {
                        signalList += summarySignalDefinitionDto.getSignalList().get(count);
                        if (count != summarySignalDefinitionDto.getSignalList().size() - 1)
                        {
                            signalList += ",";
                        }
                    }
                }

                pstmt.setString(2, signalList);
                pstmt.setInt(3, summarySignalDefinitionDto.getSummmarySignalType());
                pstmt.setInt(4, summarySignalDefinitionDto.getPriority());
                pstmt.setString(5, summarySignalDefinitionDto.getSummarySignalName());
                pstmt.setBoolean(6, summarySignalDefinitionDto.isSendMail());

            }

            pstmt.executeUpdate();
        }
        finally
        {
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }

    }
}