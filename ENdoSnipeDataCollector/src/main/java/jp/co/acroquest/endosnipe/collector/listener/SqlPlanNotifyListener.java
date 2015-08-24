/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Acroquest Technology Co.,Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package jp.co.acroquest.endosnipe.collector.listener;

import java.sql.SQLException;

import jp.co.acroquest.endosnipe.collector.LogMessageCodes;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.AbstractTelegramListener;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.accessor.SqlPlanNotifyAccessor;
import jp.co.acroquest.endosnipe.communicator.accessor.SqlPlanNotifyAccessor.SqlPlanEntry;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.dao.SqlPlanDao;
import jp.co.acroquest.endosnipe.data.entity.SqlPlan;

/**
 * SQL実行計画通知電文を受信するためのクラスです。<br />
 *
 * @author miyasaka
 *
 */
public class SqlPlanNotifyListener extends AbstractTelegramListener
    implements TelegramListener, LogMessageCodes, AgentNameListener
{
    /** ロガー。 */
    private static final ENdoSnipeLogger LOGGER =
        ENdoSnipeLogger.getLogger(JvnFileNotifyListener.class);

    /** DB名。 */
    private String databaseName_ = "";

    /** エージェント名。 */
    private String agentName_ = null;

    @Override
    protected Telegram doReceiveTelegram(final Telegram telegram)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.log(JVN_FILE_NOTIFY_RECEIVED);
        }

        SqlPlanEntry[] entries = SqlPlanNotifyAccessor.getSqlPlanEntries(telegram, agentName_);

        int entryLength = entries.length;

        for (int index = 0; index < entryLength; index++)
        {
            SqlPlanEntry sqlPlanEntry = entries[index];
            SqlPlan sqlPlan = convertSqlPlan(sqlPlanEntry);

            try
            {
                int count = SqlPlanDao.update(databaseName_, sqlPlan);
                if (count == 0)
                {
                    SqlPlanDao.insert(databaseName_, sqlPlan);
                }
            }
            catch (SQLException ex)
            {
                LOGGER.log(DATABASE_ACCESS_ERROR, ex, ex.getMessage());
            }
        }

        return null;
    }

    @Override
    protected byte getByteRequestKind()
    {
        return TelegramConstants.BYTE_REQUEST_KIND_NOTIFY;
    }

    @Override
    protected byte getByteTelegramKind()
    {
        return TelegramConstants.BYTE_TELEGRAM_KIND_SQL_PLAN;
    }

    /**
     * Agent名を取得する。
     *
     * @return Agent名
     */
    public String getAgentName()
    {
        return agentName_;
    }

    /**
     *  Agent名を設定する。
     *
     *  @param agentName Agent名
     */
    public void setAgentName(final String agentName)
    {
        this.agentName_ = agentName;
    }

    /**
      * DB名を設定する。
      *
      * @param databaseName DB名
      */
    public void setDatabaseName(final String databaseName)
    {
        databaseName_ = databaseName;
    }

    /**
     * SqlPlanEntryオブジェクトをSqlPlanオブジェクトに変更する。
     *
     * @param entry 変換対象のSqlPlanEntryオブジェクト
     * @return SqlPlanオブジェクト
     */
    private SqlPlan convertSqlPlan(final SqlPlanEntry entry)
    {
        SqlPlan sqlPlan = new SqlPlan();
        sqlPlan.measurementItemName = entry.measurementItemName;
        sqlPlan.executionPlan = entry.executionPlan;
        sqlPlan.sqlStatement = entry.sqlStatement;
        sqlPlan.gettingPlanTime = entry.gettingPlanTime;
        sqlPlan.stackTrace = entry.stackTrace;

        return sqlPlan;
    }
}
