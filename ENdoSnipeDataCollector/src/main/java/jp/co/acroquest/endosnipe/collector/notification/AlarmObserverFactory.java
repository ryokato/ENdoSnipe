/*******************************************************************************
 * ENdoSnipe 6.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Acroquest Technology Co.,Ltd.
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
package jp.co.acroquest.endosnipe.collector.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.collector.exception.InitializeException;
import jp.co.acroquest.endosnipe.collector.notification.smtp.SMTPSender;

/**
 * アラーム通知を行うObserverクラスの
 * @author fujii
 *
 */
public class AlarmObserverFactory
{
    private SMTPSender smtpSender_;

    /**
     * アラーム通知を行うオブジェクトを取得する。
     * @param entry {@link AlarmEntry}
     * @return アラーム通知を行うオブジェクト一覧
     */
    public List<AlarmObserver> getAlarmObserverList(final AlarmEntry entry)
    {
        List<AlarmObserver> observerList = new ArrayList<AlarmObserver>();

        if (entry.isSendMail())
        {
            if (smtpSender_ == null)
            {
                try
                {
                    smtpSender_ = new SMTPSender();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                catch (InitializeException ex)
                {
                    ex.printStackTrace();
                }
            }
            observerList.add(smtpSender_);
        }
        return observerList;

    }
}
