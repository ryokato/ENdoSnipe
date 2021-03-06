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
package jp.co.acroquest.endosnipe.perfdoctor.rule.code;

import java.util.Map;

import jp.co.acroquest.endosnipe.common.event.EventConstants;
import jp.co.acroquest.endosnipe.common.parser.JavelinConstants;
import jp.co.acroquest.endosnipe.common.parser.JavelinLogColumnNum;
import jp.co.acroquest.endosnipe.javelin.JavelinLogUtil;
import jp.co.acroquest.endosnipe.javelin.parser.JavelinLogElement;
import jp.co.acroquest.endosnipe.javelin.parser.JavelinParser;
import jp.co.acroquest.endosnipe.perfdoctor.rule.SingleElementRule;

/**
 * メモリリークが発生した可能性があることを警告するルール
 * @author s.Kimura
 */
public class LeakDetectedRule extends SingleElementRule
{
    private static final String NOT_RECORDED = "[not recorded] ";

    /**
     * コンストラクタ。
     */
    public LeakDetectedRule()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doJudgeElement(final JavelinLogElement element)
    {
        String type = element.getBaseInfo().get(JavelinLogColumnNum.ID);
        boolean isEvent = JavelinConstants.MSG_EVENT.equals(type);

        if (!isEvent)
        {
            return;
        }

        String eventName = element.getBaseInfo().get(JavelinLogColumnNum.EVENT_NAME);

        if (EventConstants.NAME_LEAK_DETECTED.equals(eventName))
        {
            Map<String, String> eventInfoMap =
                                               JavelinLogUtil.parseDetailInfo(element,
                                                                              JavelinParser.TAG_TYPE_EVENTINFO);
            String identifier = eventInfoMap.get(EventConstants.PARAM_LEAK_IDENTIFIER);
            String threshold = eventInfoMap.get(EventConstants.PARAM_LEAK_THRESHOLD);
            String count = eventInfoMap.get(EventConstants.PARAM_LEAK_COUNT);
            String size = eventInfoMap.get(EventConstants.PARAM_LEAK_SIZE);
            if (size == null)
            {
                // サイズを計測していない場合、その旨をメッセージに出力するよう設定。
                size = NOT_RECORDED;
            }
            String thread = element.getBaseInfo().get(JavelinLogColumnNum.EVENT_THREADID);
            String className = eventInfoMap.get(EventConstants.PARAM_LEAK_CLASS_NAME);
            if (className == null)
            {
                className = " - ";
            }
            String stackTrace = eventInfoMap.get(EventConstants.PARAM_LEAK_STACK_TRACE);

            addError(true, stackTrace, element, threshold, identifier, count, size, thread,
                     className);
        }
    }

}
