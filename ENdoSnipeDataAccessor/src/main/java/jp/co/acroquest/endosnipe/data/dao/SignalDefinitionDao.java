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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.common.util.SQLUtil;
import jp.co.acroquest.endosnipe.data.TableNames;
import jp.co.acroquest.endosnipe.data.entity.SignalDefinition;

/**
 * {@link SignalDefinition} のための DAO です。
 * 
 * @author miyasaka
 *
 */
public class SignalDefinitionDao extends AbstractDao implements TableNames
{
    /**
     * 指定されたデータベースのシグナル定義を全て取得します。<br />
     *
     * シグナル定義が登録されていない場合は空のリストを返します。<br />
     *
     * @param database データベース名
     * @return シグナル定義のリスト
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public static List<SignalDefinition> selectAll(final String database)
        throws SQLException
    {
        List<SignalDefinition> result = new ArrayList<SignalDefinition>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            conn = getConnection(database, false);
            stmt = conn.createStatement();
            String sql = "select * from " + SIGNAL_DEFINITION;
            rs = stmt.executeQuery(sql);
            while (rs.next() == true)
            {
                // CHECKSTYLE:OFF
                SignalDefinition signalInfo = new SignalDefinition();
                signalInfo.signalId = rs.getInt(1);
                signalInfo.signalName = rs.getString(2);
                signalInfo.matchingPattern = rs.getString(3);
                signalInfo.level = rs.getInt(4);
                signalInfo.patternValue = rs.getString(5);
                signalInfo.escalationPeriod = rs.getDouble(6);
                signalInfo.sendMail = rs.getBoolean(7);
                // CHECKSTYLE:ON
                result.add(signalInfo);
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
     * すべてのレコードを削除します。<br />
     *
     * @param database データベース名
     * @throws SQLException SQL 実行時に例外が発生した場合
     */
    public static void deleteAll(final String database)
        throws SQLException
    {
        deleteAll(database, SIGNAL_DEFINITION);
    }
}
