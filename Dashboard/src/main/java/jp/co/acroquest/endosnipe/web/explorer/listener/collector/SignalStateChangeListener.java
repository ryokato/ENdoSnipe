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
package jp.co.acroquest.endosnipe.web.explorer.listener.collector;

import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.communicator.AbstractTelegramListener;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.web.explorer.dto.SignalDefinitionDto;
import jp.co.acroquest.endosnipe.web.explorer.dto.SignalTreeMenuDto;
import jp.co.acroquest.endosnipe.web.explorer.dto.TreeMenuDto;
import jp.co.acroquest.endosnipe.web.explorer.manager.EventManager;
import jp.co.acroquest.endosnipe.web.explorer.manager.ResourceSender;
import jp.co.acroquest.endosnipe.web.explorer.util.SignalUtil;

import org.wgp.manager.WgpDataManager;

/**
 * DataCollectorからシグナル状態通知電文
 * @author fujii
 *
 */
public class SignalStateChangeListener extends AbstractTelegramListener
{
    /**
     * コンストラクタです。
     */
    public SignalStateChangeListener()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Telegram doReceiveTelegram(final Telegram telegram)
    {
        List<TreeMenuDto> signalTreeMenuDtoList = new ArrayList<TreeMenuDto>();

        Body[] resourceAlarmBodys = telegram.getObjBody();

        // DataCollectorから送信された電文から、
        // Explorerのクライアントに通知するためのイベントを作成する。
        String[] treeIds = null;
        long[] signalIds = null;
        int[] signalValues = null;
        double[] escalationPeriods = null;
        int[] levels = null;
        String[] patternValues = null;
        String[] matchingPatterns = null;
        String[] sendMails = null;
        // String[] alarmTypes = null;

        for (Body body : resourceAlarmBodys)
        {
            String objectNameInTelegram = body.getStrObjName();
            String itemNameInTelegram = body.getStrItemName();
            if (TelegramConstants.OBJECTNAME_RESOURCEALARM.equals(objectNameInTelegram) == false)
            {
                continue;
            }
            int loopCount = body.getIntLoopCount();
            Object[] measurementItemValues = body.getObjItemValueArr();
            // 閾値判定定義情報IDの項目に対する処理
            if (TelegramConstants.ITEMNAME_SIGNAL_ID.equals(itemNameInTelegram))
            {
                signalIds = getLongValues(loopCount, measurementItemValues);
            }
            // 計測ID(シグナル名）の項目に対する処理
            else if (TelegramConstants.ITEMNAME_SIGNAL_NAME.equals(itemNameInTelegram))
            {
                treeIds = getStringValues(loopCount, measurementItemValues);
            }
            // 閾値超過・復旧判定する時間
            else if (TelegramConstants.ITEMNAME_ESCALATION_PERIOD.equals(itemNameInTelegram))
            {
                escalationPeriods = getDoubleValues(loopCount, measurementItemValues);
            }
            // vの項目に対する処理
            else if (TelegramConstants.ITEMNAME_SIGNAL_LEVEL.equals(itemNameInTelegram))
            {
                levels = getIntValues(loopCount, measurementItemValues);
            }
            // 各レベル毎の閾値に対する処理
            else if (TelegramConstants.ITEMNAME_PATTERN_VALUE.equals(itemNameInTelegram))
            {
                patternValues = getStringValues(loopCount, measurementItemValues);
            }
            // 閾値判定パターンに対する処理
            else if (TelegramConstants.ITEMNAME_MATCHING_PATTERN.equals(itemNameInTelegram))
            {
                matchingPatterns = getStringValues(loopCount, measurementItemValues);
            }
            // 閾値判定結果に対する処理
            else if (TelegramConstants.ITEMNAME_SIGNAL_VALUE.equals(itemNameInTelegram))
            {
                signalValues = getIntValues(loopCount, measurementItemValues);
            }
            // 閾値判定結果に対する処理
            else if (TelegramConstants.ITEMNAME_SEND_MAIL.equals(itemNameInTelegram))
            {
                sendMails = getStringValues(loopCount, measurementItemValues);
            }
            // アラーム種類の項目に対する処理
            //else if (TelegramConstants.ITEMNAME_ALARM_TYPE.equals(itemNameInTelegram))
            //{
            //　アラームの種別（現状は使用しない）
            // alarmTypes = getStringValues(loopCount, measurementItemValues);
            //}
        }

        for (int cnt = 0; cnt < treeIds.length; cnt++)
        {
            SignalDefinitionDto signalDefinitionDto = new SignalDefinitionDto();
            signalDefinitionDto.setSignalId(signalIds[cnt]);
            signalDefinitionDto.setSignalName(treeIds[cnt]);
            signalDefinitionDto.setEscalationPeriod(escalationPeriods[cnt]);
            signalDefinitionDto.setLevel(levels[cnt]);
            signalDefinitionDto.setPatternValue(patternValues[cnt]);
            signalDefinitionDto.setMatchingPattern(matchingPatterns[cnt]);
            signalDefinitionDto.setSignalValue(signalValues[cnt]);
            signalDefinitionDto.setSendMail(Boolean.valueOf(sendMails[cnt]));
            SignalTreeMenuDto signalTreeMenu = SignalUtil.createSignalMenu(signalDefinitionDto);

            signalTreeMenuDtoList.add(signalTreeMenu);
        }

        // シグナルの状態更新を通知する。
        EventManager eventManager = EventManager.getInstance();
        WgpDataManager dataManager = eventManager.getWgpDataManager();
        ResourceSender resourceSender = eventManager.getResourceSender();
        if (dataManager == null || resourceSender == null)
        {
            return null;
        }

        resourceSender.send(signalTreeMenuDtoList, "update");

        return null;
    }

    /**
     * 電文をString型にして返します。
     * @param loopCount ループ回数
     * @param telegramValuesOfobject 電文の値オブジェクト
     * @return String型の電文の値配列
     */
    private String[] getStringValues(final int loopCount, final Object[] telegramValuesOfobject)
    {
        String[] telegramValues = new String[loopCount];
        for (int cnt = 0; cnt < telegramValuesOfobject.length; cnt++)
        {
            if (cnt >= loopCount)
            {
                break;
            }
            String alarmType = (String)telegramValuesOfobject[cnt];
            telegramValues[cnt] = alarmType;
        }
        return telegramValues;
    }

    /**
     * 電文をlong型にして返します。
     * @param loopCount ループ回数
     * @param telegramValuesOfobject 電文の値オブジェクト
     * @return long型の電文の値配列
     */
    private long[] getLongValues(final int loopCount, final Object[] telegramValuesOfobject)
    {
        long[] telegramValues = new long[loopCount];
        for (int cnt = 0; cnt < telegramValuesOfobject.length; cnt++)
        {
            if (cnt >= loopCount)
            {
                break;
            }
            Long value = (Long)telegramValuesOfobject[cnt];
            telegramValues[cnt] = value.longValue();
        }
        return telegramValues;
    }

    /**
     * 電文の配列をint型にして返します。
     * @param loopCount ループ回数
     * @param telegramValuesOfobject 電文の値オブジェクト
     * @return int型の電文の値配列
     */
    private int[] getIntValues(final int loopCount, final Object[] telegramValuesOfobject)
    {
        int[] telegramValues = new int[loopCount];
        for (int cnt = 0; cnt < telegramValuesOfobject.length; cnt++)
        {
            if (cnt >= loopCount)
            {
                break;
            }
            Integer value = (Integer)telegramValuesOfobject[cnt];
            telegramValues[cnt] = value.intValue();
        }
        return telegramValues;
    }

    /**
     * 電文の配列をdouble型にして返します。
     * @param loopCount ループ回数
     * @param telegramValuesOfobject 電文の値オブジェクト
     * @return double型の電文の値配列
     */
    private double[] getDoubleValues(final int loopCount, final Object[] telegramValuesOfobject)
    {
        double[] telegramValues = new double[loopCount];
        for (int cnt = 0; cnt < telegramValuesOfobject.length; cnt++)
        {
            if (cnt >= loopCount)
            {
                break;
            }
            Double value = (Double)telegramValuesOfobject[cnt];
            telegramValues[cnt] = value.doubleValue();
        }
        return telegramValues;
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
        return TelegramConstants.BYTE_TELEGRAM_SIGNAL_STATE_CHANGE;
    }

}
