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
package jp.co.acroquest.endosnipe.collector.util;

import java.util.List;

import jp.co.acroquest.endosnipe.collector.JavelinDataLogger;
import jp.co.acroquest.endosnipe.collector.manager.SignalStateManager;
import jp.co.acroquest.endosnipe.collector.notification.AlarmEntry;
import jp.co.acroquest.endosnipe.collector.processor.AlarmData;
import jp.co.acroquest.endosnipe.collector.processor.AlarmType;
import jp.co.acroquest.endosnipe.common.entity.ItemType;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.dto.SignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.dto.SummarySignalDefinitionDto;

/**
 * データコレクターで用いる電文のユーティリティ
 * 
 * @author fujii
 */
public class CollectorTelegramUtil
{
    /**  閾値超過アラームの電文本体のサイズ */
    public static final int RESPONSEALARM_BODY_SIZE = 9;

    /** 電文本体の位置(閾値判定定義ID) */
    private static final int BODY_COUNT_SIGNAL_ID = 0;

    /** 電文本体の位置() */
    private static final int BODY_COUNT_SIGNAL_NAME = 1;

    /** 電文本体の位置() */
    private static final int BODY_COUNT_ESCALATION_PERIOD = 2;

    /** 電文本体の位置() */
    private static final int BODY_COUNT_SIGNAL_LEVEL = 3;

    /** 電文本体の位置() */
    private static final int BODY_COUNT_PATTERN_VALUE = 4;

    /** 電文本体の位置() */
    private static final int BODY_COUNT_MATCHING_PATTERN = 5;

    /** 電文本体の位置() */

    private static final int BODY_COUNT_SIGNAL_VALUE = 6;

    /** 電文本体の位置 */
    private static final int BODY_COUNT_ALARM_TYPE = 7;

    /** 電文本体の位置(メール送信有無) */
    private static final int BODY_COUNT_SEND_MAIL = 8;

    /** size of telegram for summary signal */
    public static final int RESPONSEALARM_BODY_ADD_SUMMARY_SIGNAL_SIZE = 6;

    /** index for telegram of summary signal */
    public static final int SUMMARY_SIGNAL_INDEX1 = 3;

    /** index for telegram of summary signal */
    public static final int SUMMARY_SIGNAL_INDEX2 = 4;

    /** index for telegram of summary signal */
    public static final int SUMMARY_SIGNAL_INDEX3 = 5;

    /**
     * インスタンス化を防止するprivateコンストラクタです。
     */
    private CollectorTelegramUtil()
    {
        // Do Nothing.
    }

    /**
     * 閾値超過アラーム通知電文を作成します。
     * @param alarmEntryList {@link AlarmEntry}のリストオブジェクト
     * @return 閾値超過アラーム通知電文
     */
    public static Telegram createAlarmTelegram(final List<AlarmEntry> alarmEntryList)
    {
        return createAlarmTelegram(alarmEntryList, TelegramConstants.BYTE_REQUEST_KIND_NOTIFY);
    }

    /**
     * 閾値超過アラーム通知電文を作成します。
     * @param alarmEntryList {@link AlarmEntry}のリストオブジェクト
     * @param requestKind リクエストの種類
     * @return 閾値超過アラーム通知電文
     */
    public static Telegram createAlarmTelegram(final List<AlarmEntry> alarmEntryList,
        final byte requestKind)
    {
        Header responseHeader = new Header();
        responseHeader.setByteTelegramKind(TelegramConstants.BYTE_TELEGRAM_SIGNAL_STATE_CHANGE);
        responseHeader.setByteRequestKind(requestKind);

        Telegram responseTelegram = new Telegram();

        Body[] responseBodys = new Body[CollectorTelegramUtil.RESPONSEALARM_BODY_SIZE];
        int alarmEntrySize = alarmEntryList.size();

        // 閾値判定定義情報ID
        Body signalIdsBody =
            createBody(TelegramConstants.ITEMNAME_SIGNAL_ID, ItemType.ITEMTYPE_LONG, alarmEntrySize);
        Long[] signalIds = new Long[alarmEntrySize];

        // シグナル名
        Body signalNamesBody =
            createBody(TelegramConstants.ITEMNAME_SIGNAL_NAME, ItemType.ITEMTYPE_STRING,
                       alarmEntrySize);
        String[] signalNames = new String[alarmEntrySize];

        // 計測間隔
        Body escalationPeriodsBody =
            createBody(TelegramConstants.ITEMNAME_ESCALATION_PERIOD, ItemType.ITEMTYPE_DOUBLE,
                       alarmEntrySize);
        Double[] escalationPeriods = new Double[alarmEntrySize];

        // 上限レベル
        Body signalLevelBody =
            createBody(TelegramConstants.ITEMNAME_SIGNAL_LEVEL, ItemType.ITEMTYPE_INT,
                       alarmEntrySize);
        Integer[] signalLevel = new Integer[alarmEntrySize];

        // 各レベル毎の閾値
        Body patternValuesBody =
            createBody(TelegramConstants.ITEMNAME_PATTERN_VALUE, ItemType.ITEMTYPE_STRING,
                       alarmEntrySize);
        String[] patternValues = new String[alarmEntrySize];

        // 各レベル毎の閾値
        Body matchingPatternsBody =
            createBody(TelegramConstants.ITEMNAME_MATCHING_PATTERN, ItemType.ITEMTYPE_STRING,
                       alarmEntrySize);
        String[] matchingPatterns = new String[alarmEntrySize];

        // 閾値判定結果
        Body signalValueBody =
            createBody(TelegramConstants.ITEMNAME_SIGNAL_VALUE, ItemType.ITEMTYPE_INT,
                       alarmEntrySize);
        Integer[] signalValues = new Integer[alarmEntrySize];

        // アラームの種類
        Body alarmTypeBody =
            createBody(TelegramConstants.ITEMNAME_ALARM_TYPE, ItemType.ITEMTYPE_STRING,
                       alarmEntrySize);
        String[] alarmTypeItems = new String[alarmEntrySize];

        // メール送信有無
        Body sendMailBody =
            createBody(TelegramConstants.ITEMNAME_SEND_MAIL, ItemType.ITEMTYPE_STRING,
                       alarmEntrySize);
        String[] sendMailItems = new String[alarmEntrySize];

        // 計測ID、アラーム種類のBodyにAlarmEntryの結果を格納する。
        for (int cnt = 0; cnt < alarmEntrySize; cnt++)
        {
            AlarmEntry alarmEntry = alarmEntryList.get(cnt);

            signalIds[cnt] = alarmEntry.getSignalId();
            signalNames[cnt] = alarmEntry.getSignalName();
            escalationPeriods[cnt] = alarmEntry.getEscalationPeriod();
            signalLevel[cnt] = alarmEntry.getSignalLevel();
            patternValues[cnt] = alarmEntry.getPatternValue();
            matchingPatterns[cnt] = alarmEntry.getMatchingPattern();
            signalValues[cnt] = alarmEntry.getSignalValue();
            AlarmType alarmType = alarmEntry.getAlarmType();
            alarmTypeItems[cnt] = String.valueOf(alarmType);
            sendMailItems[cnt] = String.valueOf(alarmEntry.isSendMail());
        }
        signalIdsBody.setObjItemValueArr(signalIds);
        signalNamesBody.setObjItemValueArr(signalNames);
        escalationPeriodsBody.setObjItemValueArr(escalationPeriods);
        signalLevelBody.setObjItemValueArr(signalLevel);
        patternValuesBody.setObjItemValueArr(patternValues);
        matchingPatternsBody.setObjItemValueArr(matchingPatterns);
        signalValueBody.setObjItemValueArr(signalValues);
        alarmTypeBody.setObjItemValueArr(alarmTypeItems);
        sendMailBody.setObjItemValueArr(sendMailItems);

        responseTelegram.setObjHeader(responseHeader);

        responseBodys[BODY_COUNT_SIGNAL_ID] = signalIdsBody;
        responseBodys[BODY_COUNT_SIGNAL_NAME] = signalNamesBody;
        responseBodys[BODY_COUNT_ESCALATION_PERIOD] = escalationPeriodsBody;
        responseBodys[BODY_COUNT_SIGNAL_LEVEL] = signalLevelBody;
        responseBodys[BODY_COUNT_PATTERN_VALUE] = patternValuesBody;
        responseBodys[BODY_COUNT_MATCHING_PATTERN] = matchingPatternsBody;
        responseBodys[BODY_COUNT_SIGNAL_VALUE] = signalValueBody;
        responseBodys[BODY_COUNT_ALARM_TYPE] = alarmTypeBody;
        responseBodys[BODY_COUNT_SEND_MAIL] = sendMailBody;

        responseTelegram.setObjBody(responseBodys);

        return responseTelegram;
    }

    /**
     * create response telegram for summary signal data
     * 
     * @param summarySignalDefinitionList list of summary signal data
     * @param process kind of telegram
     * @return telegram of summarysignal
     */
    public static Telegram createSummarySignalResponseTelegram(
        final List<SummarySignalDefinitionDto> summarySignalDefinitionList, final String process)
    {
        Header responseHeader = new Header();
        responseHeader
            .setByteTelegramKind(TelegramConstants.BYTE_TELEGRAM_KIND_ADD_STATE_CHANGE_SUMMARYSIGNAL_DEFINITION);
        responseHeader.setByteRequestKind(TelegramConstants.BYTE_REQUEST_KIND_NOTIFY);

        int summarySignalCount = summarySignalDefinitionList.size();
        Telegram responseTelegram = new Telegram();

        Body[] responseBodys =
            new Body[CollectorTelegramUtil.RESPONSEALARM_BODY_ADD_SUMMARY_SIGNAL_SIZE];

        Body summarySignalProcessBody = new Body();
        summarySignalProcessBody.setStrObjName(TelegramConstants.OBJECTNAME_SUMMARY_SIGNAL_CHANGE);
        summarySignalProcessBody.setStrItemName(TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_TYPE);
        summarySignalProcessBody.setByteItemMode(ItemType.ITEMTYPE_STRING);
        summarySignalProcessBody.setIntLoopCount(summarySignalCount);
        String[] summarySignalProcess = new String[summarySignalCount];//{summaryProcess};

        Body summarySignalIDBody = new Body();

        summarySignalIDBody.setStrObjName(TelegramConstants.OBJECTNAME_SUMMARY_SIGNAL_CHANGE);
        summarySignalIDBody.setStrItemName(TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_ID);
        summarySignalIDBody.setByteItemMode(ItemType.ITEMTYPE_LONG);
        summarySignalIDBody.setIntLoopCount(summarySignalCount);
        Long[] summarySignalID = new Long[summarySignalCount];

        Body summarySignalNameBody = new Body();

        summarySignalNameBody.setStrObjName(TelegramConstants.OBJECTNAME_SUMMARY_SIGNAL_CHANGE);
        summarySignalNameBody.setStrItemName(TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_NAME);
        summarySignalNameBody.setByteItemMode(ItemType.ITEMTYPE_STRING);
        summarySignalNameBody.setIntLoopCount(summarySignalCount);
        String[] summarySignalNameList = new String[summarySignalCount];

        Body signalListBody = new Body();

        signalListBody.setStrObjName(TelegramConstants.OBJECTNAME_SUMMARY_SIGNAL_CHANGE);
        signalListBody.setStrItemName(TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_CHILDLIST);
        signalListBody.setByteItemMode(ItemType.ITEMTYPE_STRING);
        signalListBody.setIntLoopCount(summarySignalCount);
        String[] childList = new String[summarySignalCount];

        Body summarySignalStateBody = new Body();
        summarySignalStateBody.setStrObjName(TelegramConstants.OBJECTNAME_SUMMARY_SIGNAL_CHANGE);
        summarySignalStateBody.setStrItemName(TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_STATUS);
        summarySignalStateBody.setByteItemMode(ItemType.ITEMTYPE_INT);
        summarySignalStateBody.setIntLoopCount(summarySignalCount);
        Integer[] summarySignalState = new Integer[summarySignalCount];

        Body errorMessageBody = new Body();

        errorMessageBody.setStrObjName(TelegramConstants.OBJECTNAME_SUMMARY_SIGNAL_CHANGE);
        errorMessageBody.setStrItemName(TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_ERROR);
        errorMessageBody.setByteItemMode(ItemType.ITEMTYPE_STRING);
        errorMessageBody.setIntLoopCount(summarySignalCount);
        String[] errorMessages = new String[summarySignalCount];

        for (int cnt = 0; cnt < summarySignalCount; cnt++)
        {
            SummarySignalDefinitionDto summarySignalDefinition =
                summarySignalDefinitionList.get(cnt);
            Long summarySignalId = summarySignalDefinition.getSummarySignalId();
            String summarySignalName = summarySignalDefinition.getSummarySignalName();

            String summarySignalList = "";
            if (summarySignalDefinition.getSignalList() != null
                && summarySignalDefinition.getSignalList().size() > 0)
            {
                for (int count = 0; count < summarySignalDefinition.getSignalList().size(); count++)
                {
                    summarySignalList += summarySignalDefinition.getSignalList().get(count);
                    if (count != summarySignalDefinition.getSignalList().size() - 1)
                    {
                        summarySignalList += ",";
                    }
                }
            }

            int summarySignalStatus = summarySignalDefinition.getSummarySignalStatus();
            String errorMessage = summarySignalDefinition.getErrorMessage();
            if (errorMessage == null)
            {
                errorMessage = "";
            }
            summarySignalProcess[cnt] = process;
            summarySignalID[cnt] = summarySignalId;
            summarySignalNameList[cnt] = summarySignalName;
            summarySignalState[cnt] = summarySignalStatus;
            childList[cnt] = summarySignalList;
            errorMessages[cnt] = errorMessage;
        }

        summarySignalProcessBody.setObjItemValueArr(summarySignalProcess);
        summarySignalIDBody.setObjItemValueArr(summarySignalID);
        summarySignalNameBody.setObjItemValueArr(summarySignalNameList);
        signalListBody.setObjItemValueArr(childList);
        summarySignalStateBody.setObjItemValueArr(summarySignalState);
        errorMessageBody.setObjItemValueArr(errorMessages);

        responseTelegram.setObjHeader(responseHeader);

        responseBodys[0] = summarySignalProcessBody;
        responseBodys[1] = summarySignalIDBody;
        responseBodys[2] = summarySignalNameBody;
        responseBodys[SUMMARY_SIGNAL_INDEX1] = signalListBody;
        responseBodys[SUMMARY_SIGNAL_INDEX2] = summarySignalStateBody;
        responseBodys[SUMMARY_SIGNAL_INDEX3] = errorMessageBody;

        responseTelegram.setObjBody(responseBodys);
        return responseTelegram;
    }

    /**
     * シグナル状態更新電文を生成する。
     * @param signalDefitionList 閾値判定定義情報一覧
     * @return シグナル状態更新電文
     */
    public static Telegram
        createResponseTelegram(final List<SignalDefinitionDto> signalDefitionList)
    {
        Header responseHeader = new Header();
        responseHeader.setByteTelegramKind(TelegramConstants.BYTE_TELEGRAM_SIGNAL_STATE_CHANGE);
        responseHeader.setByteRequestKind(TelegramConstants.BYTE_REQUEST_KIND_NOTIFY);

        Telegram responseTelegram = new Telegram();

        Body[] responseBodys = new Body[CollectorTelegramUtil.RESPONSEALARM_BODY_SIZE];
        int signalCount = signalDefitionList.size();

        // 閾値判定定義情報ID
        Body signalIdsBody =
            createBody(TelegramConstants.ITEMNAME_SIGNAL_ID, ItemType.ITEMTYPE_LONG, signalCount);
        Long[] signalIds = new Long[signalCount];

        // シグナル名
        Body signalNamesBody =
            createBody(TelegramConstants.ITEMNAME_SIGNAL_NAME, ItemType.ITEMTYPE_STRING,
                       signalCount);
        String[] signalNames = new String[signalCount];

        // 計測間隔
        Body escalationPeriodsBody =
            createBody(TelegramConstants.ITEMNAME_ESCALATION_PERIOD, ItemType.ITEMTYPE_DOUBLE,
                       signalCount);
        Double[] escalationPeriods = new Double[signalCount];

        // 上限レベル
        Body signalLevelBody =
            createBody(TelegramConstants.ITEMNAME_SIGNAL_LEVEL, ItemType.ITEMTYPE_INT, signalCount);
        Integer[] signalLevel = new Integer[signalCount];

        // 各レベル毎の閾値
        Body patternValuesBody =
            createBody(TelegramConstants.ITEMNAME_PATTERN_VALUE, ItemType.ITEMTYPE_STRING,
                       signalCount);
        String[] patternValues = new String[signalCount];

        // 各レベル毎の閾値
        Body matchingPatternsBody =
            createBody(TelegramConstants.ITEMNAME_MATCHING_PATTERN, ItemType.ITEMTYPE_STRING,
                       signalCount);
        String[] matchingPatterns = new String[signalCount];

        // 閾値判定結果
        Body signalValueBody =
            createBody(TelegramConstants.ITEMNAME_SIGNAL_VALUE, ItemType.ITEMTYPE_INT, signalCount);
        Integer[] signalValues = new Integer[signalCount];

        // アラームの種類
        Body alarmTypeBody =
            createBody(TelegramConstants.ITEMNAME_ALARM_TYPE, ItemType.ITEMTYPE_STRING, signalCount);
        String[] alarmTypeItems = new String[signalCount];

        // 計測ID、アラーム種類のBodyにAlarmEntryの結果を格納する。
        SignalStateManager manager = SignalStateManager.getInstance();
        for (int cnt = 0; cnt < signalCount; cnt++)
        {
            SignalDefinitionDto signalDefition = signalDefitionList.get(cnt);
            String signalName = signalDefition.getSignalName();
            Long signalId = signalDefition.getSignalId();

            AlarmData alarmData = manager.getAlarmData(signalId);

            signalIds[cnt] = signalId;
            signalNames[cnt] = signalName;
            escalationPeriods[cnt] = signalDefition.getEscalationPeriod();
            patternValues[cnt] = signalDefition.getPatternValue();
            matchingPatterns[cnt] = signalDefition.getMatchingPattern();
            if (alarmData == null)
            {
                signalValues[cnt] = JavelinDataLogger.STOP_ALARM_LEVEL;
            }
            else
            {
                signalValues[cnt] = alarmData.getAlarmLevel();
            }

            signalLevel[cnt] = signalDefition.getLevel();
            alarmTypeItems[cnt] = String.valueOf(AlarmType.NONE);
        }
        signalIdsBody.setObjItemValueArr(signalIds);
        signalNamesBody.setObjItemValueArr(signalNames);
        escalationPeriodsBody.setObjItemValueArr(escalationPeriods);
        signalLevelBody.setObjItemValueArr(signalLevel);
        patternValuesBody.setObjItemValueArr(patternValues);
        matchingPatternsBody.setObjItemValueArr(matchingPatterns);
        signalValueBody.setObjItemValueArr(signalValues);
        alarmTypeBody.setObjItemValueArr(alarmTypeItems);

        responseTelegram.setObjHeader(responseHeader);

        responseBodys[BODY_COUNT_SIGNAL_ID] = signalIdsBody;
        responseBodys[BODY_COUNT_SIGNAL_NAME] = signalNamesBody;
        responseBodys[BODY_COUNT_ESCALATION_PERIOD] = escalationPeriodsBody;
        responseBodys[BODY_COUNT_SIGNAL_LEVEL] = signalLevelBody;
        responseBodys[BODY_COUNT_PATTERN_VALUE] = patternValuesBody;
        responseBodys[BODY_COUNT_MATCHING_PATTERN] = matchingPatternsBody;
        responseBodys[BODY_COUNT_SIGNAL_VALUE] = signalValueBody;
        responseBodys[BODY_COUNT_ALARM_TYPE] = alarmTypeBody;

        responseTelegram.setObjBody(responseBodys);

        return responseTelegram;
    }

    /**
     * Bodyオブジェクトを生成する。
     * @param itemName 項目名
     * @param byteItemMode 項目型
     * @param signalCount 繰り返し回数
     * @return Bodyオブジェクト
     */
    private static Body createBody(final String itemName, final ItemType byteItemMode,
        final int signalCount)
    {
        Body signalIdsBody = new Body();
        signalIdsBody.setStrObjName(TelegramConstants.OBJECTNAME_RESOURCEALARM);
        signalIdsBody.setStrItemName(itemName);
        signalIdsBody.setByteItemMode(byteItemMode);
        signalIdsBody.setIntLoopCount(signalCount);
        return signalIdsBody;
    }

}
