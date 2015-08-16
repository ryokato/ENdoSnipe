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
package jp.co.acroquest.endosnipe.collector.listener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.co.acroquest.endosnipe.collector.LogMessageCodes;
import jp.co.acroquest.endosnipe.collector.manager.SignalStateManager;
import jp.co.acroquest.endosnipe.collector.manager.SummarySignalStateManager;
import jp.co.acroquest.endosnipe.collector.processor.AlarmData;
import jp.co.acroquest.endosnipe.collector.util.CollectorTelegramUtil;
import jp.co.acroquest.endosnipe.collector.util.SignalSummarizer;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.AbstractTelegramListener;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.dto.SignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.dto.SummarySignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.entity.SignalDefinition;

/**
 * シグナル定義変更要求電文を受信するためのクラスです。<br />
 * 
 * @author fujii
 */
public class SignalChangeListener extends AbstractTelegramListener implements TelegramListener,
    LogMessageCodes
{
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger
        .getLogger(SignalChangeListener.class);

    private static final int ITEM_VALUE_SIGNAL_ID = 0;

    private static final int ITEM_VALUE_SIGNAL_NAME = 1;

    private static final int ITEM_VALUE_SIGNAL_MATCHING_PATTERN = 2;

    private static final int ITEM_VALUE_LEVEL = 3;

    private static final int ITEM_VALUE_ESCALATION_PERIOD = 4;

    private static final int ITEM_VALUE_PATTERN_VALUE = 5;

    private static final int ITEM_VALUE_SEND_MAIL = 6;

    /** Summary Signal List */
    private static List<SummarySignalDefinitionDto> summaryDtoList__ = null;

    private final ArrayList<SummarySignalDefinitionDto> updatedSummarySignals_ =
        new ArrayList<SummarySignalDefinitionDto>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected Telegram doReceiveTelegram(final Telegram telegram)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.log(SIGNAL_DEFINITION_CHANGE_NOTIFY_RECEIVED);
        }

        // 電文を解析し、シグナル定義情報を更新する。
        updateSignalDefinition(telegram);
        Body[] bodys = telegram.getObjBody();
        String itemName = bodys[0].getStrItemName();
        if (itemName.equals(TelegramConstants.ITEMNAME_SIGNAL_DELETE)
            || itemName.equals(TelegramConstants.ITEMNAME_SIGNAL_UPDATE))
        {
            if (updatedSummarySignals_.size() == 0)
            {
                return null;
            }
            for (SummarySignalDefinitionDto dto : updatedSummarySignals_)
            {
                dto.summarySignalStatus_ =
                    SummarySignalStateManager.getInstance().getSummaryLevel(dto);
            }
            Telegram responseSummaryTelegram =
                CollectorTelegramUtil
                    .createSummarySignalResponseTelegram(updatedSummarySignals_,
                                                         TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_UPDATE);
            return responseSummaryTelegram;
        }

        return null;
    }

    /**
     * 閾値定義情報を更新する。
     * @param telegram 閾値定義情報電文一覧
     */
    private void updateSignalDefinition(final Telegram telegram)
    {
        Body[] bodys = telegram.getObjBody();
        for (Body body : bodys)
        {
            String itemName = body.getStrItemName();
            if (TelegramConstants.ITEMNAME_SIGNAL_ADD.equals(itemName)
                || TelegramConstants.ITEMNAME_SIGNAL_UPDATE.equals(itemName))
            {
                setSignalDefinition(body, itemName);
            }
            else if (TelegramConstants.ITEMNAME_SIGNAL_DELETE.equals(itemName))
            {
                deleteSignalDefinition(body);
            }
        }
    }

    /**
     * 
     * @param body {@link Body}オブジェクト
     * @param itemName 項目名
     */
    private void setSignalDefinition(final Body body, final String itemName)
    {
        SignalStateManager signalStateManager = SignalStateManager.getInstance();

        Object[] itemValues = body.getObjItemValueArr();
        String signalId = (String)itemValues[ITEM_VALUE_SIGNAL_ID];
        String signalName = (String)itemValues[ITEM_VALUE_SIGNAL_NAME];
        String matchingPattern = (String)itemValues[ITEM_VALUE_SIGNAL_MATCHING_PATTERN];
        String level = (String)itemValues[ITEM_VALUE_LEVEL];
        String escalationPeriod = (String)itemValues[ITEM_VALUE_ESCALATION_PERIOD];
        String patternValue = (String)itemValues[ITEM_VALUE_PATTERN_VALUE];
        boolean sendMail = Boolean.valueOf((String)itemValues[ITEM_VALUE_SEND_MAIL]);

        try
        {

            SignalDefinition signalDefinition = new SignalDefinition();
            signalDefinition.signalId = Long.parseLong(signalId);
            signalDefinition.signalName = signalName;
            signalDefinition.matchingPattern = matchingPattern;
            signalDefinition.level = Integer.parseInt(level);
            signalDefinition.escalationPeriod = Double.parseDouble(escalationPeriod);
            signalDefinition.patternValue = patternValue;
            signalDefinition.sendMail = sendMail;

            SignalDefinitionDto signalDefinitionDto = new SignalDefinitionDto(signalDefinition);

            signalStateManager.addSignalDefinition(signalDefinition.signalId, signalDefinitionDto);

            if (TelegramConstants.ITEMNAME_SIGNAL_UPDATE.equals(itemName))
            {
                AlarmData alarmData = signalStateManager.getAlarmData(signalDefinition.signalId);
                if (alarmData != null)
                {
                    alarmData.reset();
                    if (SummarySignalStateManager.getInstance().getAlarmChildList()
                        .contains(signalName))
                    {
                        SummarySignalStateManager.getInstance().getAlarmChildList()
                            .remove(signalName);
                        List<String> childList = new ArrayList<String>();
                        childList.add(signalName);
                        summaryDtoList__ =
                            SignalSummarizer.getInstance().calculateSummarySignalState(childList);
                    }
                }
            }
        }
        catch (NumberFormatException ex)
        {
            // シグナル定義情報の追加に失敗
        }
    }

    /**
     * 
     * @param body {@link Body}オブジェクト
     * @param itemName 項目名
     */
    private void deleteSignalDefinition(final Body body)
    {
        SignalStateManager signalStateManager = SignalStateManager.getInstance();

        Object[] itemValues = body.getObjItemValueArr();
        String signalIdStr = (String)itemValues[ITEM_VALUE_SIGNAL_ID];
        String signalName = (String)itemValues[ITEM_VALUE_SIGNAL_NAME];

        //更新されるサマリシグナルのリストを作成する
        updatedSummarySignals_.clear();
        SummarySignalStateManager summarySignalStateManager =
            SummarySignalStateManager.getInstance();
        Map<Long, SummarySignalDefinitionDto> dtoMap =
            summarySignalStateManager.getSummarySignalDefinitionMap();
        for (Entry<Long, SummarySignalDefinitionDto> entry : dtoMap.entrySet())
        {
            SummarySignalDefinitionDto dto = entry.getValue();
            if (!dto.signalList_.contains(signalName))
            {
                continue;
            }
            updatedSummarySignals_.add(dto);
        }

        try
        {
            long signalId = Long.parseLong(signalIdStr);
            signalStateManager.removeSignalDefinition(signalId);
            summaryDtoList__ =
                SummarySignalStateManager.getInstance().checkDeleteNode(signalName, true);
            signalStateManager.removeAlarmData(signalId);
        }
        catch (NumberFormatException ex)
        {
            // シグナル定義情報の追加に失敗
        }
        catch (SQLException ex)
        {
            // TODO 自動生成された catch ブロック
            ex.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte getByteRequestKind()
    {
        return TelegramConstants.BYTE_REQUEST_KIND_NOTIFY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte getByteTelegramKind()
    {
        return TelegramConstants.BYTE_TELEGRAM_KIND_SIGNAL_DEFINITION;
    }

}
