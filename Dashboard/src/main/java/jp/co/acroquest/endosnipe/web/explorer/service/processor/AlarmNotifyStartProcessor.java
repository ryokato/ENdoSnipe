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
package jp.co.acroquest.endosnipe.web.explorer.service.processor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.web.explorer.config.AlarmSetting;
import jp.co.acroquest.endosnipe.web.explorer.constants.EventConstants;
import jp.co.acroquest.endosnipe.web.explorer.constants.LogMessageCodes;
import jp.co.acroquest.endosnipe.web.explorer.manager.EventManager;
import jp.co.acroquest.endosnipe.web.explorer.util.EventUtil;
import jp.co.acroquest.endosnipe.web.explorer.util.RequestUtil;

/**
 * パフォーマンスドクターの警告の通知開始要求を処理するクラスです。
 * @author fujii
 *
 */
public class AlarmNotifyStartProcessor implements EventProcessor
{
    /** ロガー */
    private static final ENdoSnipeLogger LOGGER =
            ENdoSnipeLogger.getLogger(AlarmNotifyStartProcessor.class);

    /**
     * コンストラクタ
     */
    public AlarmNotifyStartProcessor()
    {

    }

    /**
     * {@inheritDoc}
     */
    public void process(final HttpServletRequest request, final HttpServletResponse response)
    {
        String agentIds = request.getParameter(EventConstants.AGENT_IDS);
        String clientId = request.getParameter(EventConstants.CLIENT_ID);
        String alarmLevelStr = request.getParameter(EventConstants.ALARM_LEVEL);

        if (agentIds == null)
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_AGENT_ID);
            return;
        }
        if (clientId == null)
        {
            LOGGER.log(LogMessageCodes.NO_CLIENT_ID);
            return;
        }

        List<Integer> agentIdList = RequestUtil.getAgentIdList(agentIds);
        if (agentIdList == null || agentIdList.size() == 0)
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_AGENT_ID, agentIds);
            return;
        }
        int alarmLevel = EventUtil.getAlarmLevel(alarmLevelStr);

        EventManager manager = EventManager.getInstance();

        AlarmSetting setting = manager.getAlarmSetting(clientId);
        if (setting == null)
        {
            setting = new AlarmSetting();
            manager.addAlarmSetting(clientId, setting);
        }
        setting.setAlarmLevel(alarmLevel);

        // 計測項目を追加します。
        for (Integer agentId : agentIdList)
        {
            setting.addAgent(agentId);
            setting.setNotify(true);
        }
    }

}
