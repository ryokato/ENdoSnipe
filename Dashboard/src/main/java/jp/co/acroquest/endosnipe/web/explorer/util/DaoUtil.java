/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Acroquest Technology Co.,Ltd.
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
package jp.co.acroquest.endosnipe.web.explorer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.data.dao.JavelinLogDao;
import jp.co.acroquest.endosnipe.data.entity.JavelinLog;
import jp.co.acroquest.endosnipe.perfdoctor.WarningUnit;
import jp.co.acroquest.endosnipe.web.explorer.constants.EventConstants;
import jp.co.acroquest.endosnipe.web.explorer.constants.LogMessageCodes;
import jp.co.acroquest.endosnipe.web.explorer.entity.AlarmNotifyEntity;
import jp.co.acroquest.endosnipe.web.explorer.manager.DatabaseManager;
import jp.co.acroquest.endosnipe.web.explorer.service.JvnFileEntryJudge;

/**
 * Dao処理に関するユーティリティクラスです。
 * @author tsukano
 *
 */
public class DaoUtil
{
    /** ロガーオブジェクト */
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger.getLogger(DaoUtil.class);

    /** どのレベルのアラームレベルまでアラームとして含めるかを定める定数 */
    private static final int ALARM_LEVEL = 20;

    /** agentIDを定めた定数 */
    private static final int AGENT_ID = 1;

    /** イベントの終了を表す文字列 */
    private static final String EVENT_INFO_END = "<<javelin.EventInfo_END>>";

    /**
     * インスタンス化を防止するプライベートコンストラクタです。
     */
    private DaoUtil()
    {
        // Do Nothing.
    }

    /**
     * agentId、logFileNameを指定して、JavelinLogを取得する。
     * @param agentId AgentId
     * @param logFileName JVNファイル名
     * @return JavelinLogオブジェクト
     */
    public static JavelinLog getJavelinLog(final String agentId, final String logFileName)
    {
        // パラメータチェック
        if (agentId == null)
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_AGENT_ID);
            return null;
        }
        if (logFileName == null)
        {
            return null;
        }

        // DB名を特定する
        DatabaseManager dbMmanager = DatabaseManager.getInstance();
        String dbName = dbMmanager.getDataBaseName(Integer.valueOf(agentId));
        if (dbName == null)
        {
            LOGGER.log(LogMessageCodes.FAIL_READ_DB_NAME);
            return null;
        }

        try
        {
            // Javalinログを取得する
            JavelinLog jvnLog = JavelinLogDao.selectByLogFileNameWithBinary(dbName, logFileName);
            return jvnLog;
        }
        catch (SQLException ex)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION);
            return null;
        }
    }

    /**
     * agentId、logFileNameを指定して、JavelinLogを取得する。
     * @param agentId AgentId
     * @param logFileName JVNファイル名
     * @param logFileLineNumber ログファイルの行数
     * @return JavelinLog(文字列)
     */
    public static String getJavelinLogString(final String agentId, final String logFileName,
            final int logFileLineNumber)
    {
        JavelinLog javelinLog = getJavelinLog(agentId, logFileName);

        if (javelinLog == null || javelinLog.javelinLog == null)
        {
            return null;
        }

        // JavelinLogを取得する
        BufferedReader reader = new BufferedReader(new InputStreamReader(javelinLog.javelinLog));
        StringBuilder sb = new StringBuilder();
        String line;
        int lineNum = 0;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                lineNum++;
                // 開始行以下であれば、読み込まない
                if (lineNum < logFileLineNumber)
                {
                    continue;
                }
                // 1Event分読み込んだら終了する
                if (EVENT_INFO_END.equals(line))
                {
                    sb.append(line).append("\n");
                    break;
                }

                // Eventを読み込む
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        catch (IOException ex)
        {
            LOGGER.log(LogMessageCodes.COMMUNICATION_ERROR, ex);
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException ex)
            {
                LOGGER.log(LogMessageCodes.COMMUNICATION_ERROR, ex);
            }
        }
        return null;
    }

    /**
     * Java文字列をHTML表示用に変換する。
     * @param before Java文字列
     * @return HTMLに変換した文字列
     */
    public static String convertReturnString(final String before)
    {
        String after = before.replace("<", "&lt;");
        after = after.replace(">", "&gt;");
        return after;
    }

    /**
     * JavelinLogをAlarmNotifyEntityに変換する
     * @param javelinLog AlarmNotifyEntityに変換するJavelinLog
     * @param logFileLineNumber JavelinLogの中の開始行
     * @param ruleId PerformanceDoctorの警告ID
     * @return 変換されたAlarmNotifyEntity
     */
    public static AlarmNotifyEntity convertJavelinLogToAlarmNotifyEntity(
            final JavelinLog javelinLog, final int logFileLineNumber, final String ruleId)
    {
        JvnFileEntryJudge judge = new JvnFileEntryJudge();
        List<AlarmNotifyEntity> entityList = new ArrayList<AlarmNotifyEntity>();
        List<WarningUnit> warningUnitList = new ArrayList<WarningUnit>();

        List<JavelinLog> jvnLogList = new ArrayList<JavelinLog>();
        jvnLogList.add(javelinLog);
        warningUnitList.addAll(judge.judge(jvnLogList, true, true));

        createAlarmEntity(warningUnitList, ALARM_LEVEL, AGENT_ID, entityList, javelinLog.javelinLog);
        for (AlarmNotifyEntity entry : entityList)
        {
            if (logFileLineNumber == entry.logFileLineNumber_ && entry.ruleId_.equals(ruleId))
            {
                return entry;
            }
        }
        return null;
    }

    /**
     * PerformanceDoctorの結果より、{@link AlarmNotifyEntity}のリストを作成します。
     * @param warningUnitList パフォーマンスドクターの結果
     * @param alarmLevel アラームのレベル
     * @param agentId エージェントID
     * @param entityList {@link AlarmNotifyEntity}のリスト
     * @param javelinLog JavelinLog本体。ここから必要な部分を切りだす
     */
    private static void createAlarmEntity(final List<WarningUnit> warningUnitList,
            final int alarmLevel, final int agentId, final List<AlarmNotifyEntity> entityList,
            final InputStream javelinLog)
    {
        for (WarningUnit unit : warningUnitList)
        {
            String level = unit.getLevel();
            if (EventUtil.compareLevel(level, alarmLevel) == false)
            {
                continue;
            }

            int eventId = EventConstants.EVENT_TERM_NOTIFY_ALARM_RESPONSE;
            AlarmNotifyEntity alarmNotifyEntity = createAlarmEntity(agentId, unit, level, eventId);

            entityList.add(alarmNotifyEntity);
        }
    }

    /**
     * {@link AlarmNotifyEntity}オブジェクトを作成します。
     * @param agentId エージェントID
     * @param unit {@link WarningUnit}オブジェクト
     * @param level レベル
     * @param eventId イベントID
     * @return {@link AlarmNotifyEntity}オブジェクト
     */
    public static AlarmNotifyEntity createAlarmEntity(final int agentId, final WarningUnit unit,
            final String level, final int eventId)
    {
        AlarmNotifyEntity alarmNotifyEntity = new AlarmNotifyEntity();
        alarmNotifyEntity.eventId_ = eventId;
        alarmNotifyEntity.agentId_ = agentId;
        alarmNotifyEntity.timestamp_ = new Date(unit.getStartTime());
        alarmNotifyEntity.level_ = level;
        alarmNotifyEntity.className_ = unit.getClassName();
        alarmNotifyEntity.methodName_ = unit.getMethodName();
        alarmNotifyEntity.description_ = unit.getDescription();
        alarmNotifyEntity.fileName_ = unit.getLogFileName();
        alarmNotifyEntity.logFileLineNumber_ = unit.getLogFileLineNumber();
        alarmNotifyEntity.ruleId_ = unit.getId();
        return alarmNotifyEntity;
    }
}
