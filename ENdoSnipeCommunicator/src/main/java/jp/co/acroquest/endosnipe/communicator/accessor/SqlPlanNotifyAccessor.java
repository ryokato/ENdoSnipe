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
package jp.co.acroquest.endosnipe.communicator.accessor;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.TelegramUtil;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.ResponseBody;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramItemNameIdMap;

/**
 * SQL実行計画通知電文のためのアクセサクラスです。<br />
 * 
 * @author miyasaka
 *
 */
public class SqlPlanNotifyAccessor implements TelegramConstants
{
    private static final ENdoSnipeLogger LOGGER =
        ENdoSnipeLogger.getLogger(SystemResourceGetter.class);

    /**
     * プライベートコンストラクタです。<br />
     */
    private SqlPlanNotifyAccessor()
    {
        // Do nothing
    }

    /**
     * SQL実行計画通知電文から内容を取り出します。<br />
     * 電文種別がログ通知電文ではない場合や、内容が防いである場合は <code>null</code> を返します。<br />
     * 
     * @param telegram SQL実行計画通知電文
     * @param agentName エージェント名
     * @return 電文内容
     */
    public static SqlPlanEntry[] getSqlPlanEntries(final Telegram telegram, String agentName)
    {
        if (checkTelegram(telegram) == false)
        {
            return null;
        }

        Body[] bodies = telegram.getObjBody();

        // 項目名とIDのマップを取得し、圧縮された電文を解凍する準備をする
        List<TelegramItemNameIdMap> itemNameIdMapList =
            TelegramUtil.getTelegramItemNameIdMapList(bodies);

        int measurementListLength = bodies.length;
        List<String> measurmentItemNames = new ArrayList<String>(measurementListLength);
        List<String> sqlStatements = new ArrayList<String>(measurementListLength);
        List<String> executionPlans = new ArrayList<String>(measurementListLength);
        List<Timestamp> gettingPlanTimes = new ArrayList<Timestamp>(measurementListLength);
        List<String> stackTraces = new ArrayList<String>(measurementListLength);

        for (Body body : bodies)
        {
            String objectName = body.getStrObjName();

            ResponseBody responseBody = (ResponseBody)body;
            if (OBJECTNAME_SQL_STATEMENT.equals(objectName) == true)
            {
                Object[] objItemValueArr = responseBody.getObjItemValueArr();
                if (objItemValueArr.length == 0)
                {
                    continue;
                }

                String sqlStatement = (String)objItemValueArr[0];
                sqlStatements.add(sqlStatement);

                // 項目名が圧縮されている場合も考慮して、解凍後の計測項目名を取得する
                String mearsurmentItemName =
                    TelegramUtil.getMeasurementItemName(itemNameIdMapList, responseBody, agentName);

                measurmentItemNames.add(mearsurmentItemName);
            }
            else if (OBJECTNAME_SQL_EXECUTION_PLAN.equals(objectName) == true)
            {
                Object[] objItemValueArr = responseBody.getObjItemValueArr();
                if (objItemValueArr.length == 0)
                {
                    continue;
                }

                String sqlExecution = (String)objItemValueArr[0];
                executionPlans.add(sqlExecution);
            }
            else if (OBJECTNAME_GETTING_PLAN_TIME.equals(objectName) == true)
            {
                Object[] objItemValueArr = responseBody.getObjItemValueArr();
                if (objItemValueArr.length == 0)
                {
                    continue;
                }

                String gettingPlanTimeStr = (String)objItemValueArr[0];
                Timestamp timestamp;

                // TimeStampへのパースが失敗した場合は、現在時刻のTimeStampを作成する
                try
                {
                    timestamp =
                        new Timestamp(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                            .parse(gettingPlanTimeStr).getTime());
                }
                catch (ParseException pEx)
                {
                    timestamp = new Timestamp(System.currentTimeMillis());

                    LOGGER.log("WECC0103", pEx);
                    pEx.printStackTrace();
                }
                gettingPlanTimes.add(timestamp);
            }
            else if (OBJECTNAME_STACK_TRACE.equals(objectName) == true)
            {
                Object[] objItemValueArr = responseBody.getObjItemValueArr();
                if (objItemValueArr.length == 0)
                {
                    continue;
                }

                String stackTrace = (String)objItemValueArr[0];
                stackTraces.add(stackTrace);
            }
        }

        int entriesSize = measurmentItemNames.size();
        if (entriesSize != sqlStatements.size() || entriesSize != executionPlans.size()
            || entriesSize != gettingPlanTimes.size() || entriesSize != stackTraces.size())
        {
            return null;
        }

        SqlPlanEntry[] entries = new SqlPlanEntry[entriesSize];
        for (int i = 0; i < measurmentItemNames.size(); i++)
        {
            entries[i] = new SqlPlanEntry();
            entries[i].measurementItemName = measurmentItemNames.get(i);
            entries[i].sqlStatement = sqlStatements.get(i);
            entries[i].executionPlan = executionPlans.get(i);
            entries[i].gettingPlanTime = gettingPlanTimes.get(i);
            entries[i].stackTrace = stackTraces.get(i);
        }

        return entries;
    }

    /**
     * 電文がSQL実行計画通知電文であることを確認する。
     * 
     * @param telegram SQL実行計画通知電文
     * @return 電文がSQL実行計画通知電文の場合にture、そうでない場合にfalse
     */
    private static boolean checkTelegram(final Telegram telegram)
    {
        Header header = telegram.getObjHeader();
        return BYTE_TELEGRAM_KIND_SQL_PLAN == header.getByteTelegramKind() ? true : false;
    }

    /**
     * SQL実行計画を保持するためのクラスです。<br />
     * 
     * @author miyasaka
     */
    public static class SqlPlanEntry
    {
        /** 計測名。 */
        public String measurementItemName;

        /** SQL文。 */
        public String sqlStatement;

        /** SQLの実行計画。 */
        public String executionPlan;

        /** 実行計画が取得できた時間。 */
        public Timestamp gettingPlanTime;

        /** スタックトレース。 */
        public String stackTrace;
    }
}
