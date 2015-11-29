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
package jp.co.acroquest.endosnipe.javelin.communicate;

import jp.co.acroquest.endosnipe.communicator.CommunicationClient;
import jp.co.acroquest.endosnipe.communicator.CommunicatorListener;
import jp.co.acroquest.endosnipe.communicator.entity.CommunicatorSetting;
import jp.co.acroquest.endosnipe.communicator.impl.CommunicationClientImpl;

/**
 * Javelin/DataCollectorに接続して電文送信するクライアント。
 * 
 * @author eriguchi
 */
public class SimpleENdoSnipeClient
{
    /** タイムアウト時間 */
    protected static final int TIMEOUT_MILLIS = 5000;

    /** 通信用クライアント。 */
    protected CommunicationClient client_;

    /** タイムアウトに使用するオブジェクト。 */
    protected Object timeoutObject_;

    /** コンストラクタ 
     * @param threadName スレッド名
     */
    public SimpleENdoSnipeClient(String threadName)
    {
        client_ = new CommunicationClientImpl(threadName);
        timeoutObject_ = new Object();
    }

    /**
     * 指定したホスト、ポートのJavelinに接続する。
     * 
     * @param host ホスト名。
     * @param port ポート番号。
     * 
     * @return 接続に成功したか、失敗したか。
     */
    public boolean connect(String host, int port)
    {
        CommunicatorSetting setting = new CommunicatorSetting();
        setting.port = port;
        setting.sslEnable = false;

        client_.init(host, setting);

        client_.addCommunicatorListener(new CommunicatorListener() {

            public void clientDisconnected(boolean forceDisconnected)
            {
                // 何もしない。
            }

            public void clientConnected(String hostName, String ipAddress, int port)
            {
                synchronized (SimpleENdoSnipeClient.this.timeoutObject_)
                {
                    SimpleENdoSnipeClient.this.timeoutObject_.notifyAll();
                }
            }
        });

        client_.connect(null);

        if (client_.isConnected() == false)
        {
            try
            {
                synchronized (this.timeoutObject_)
                {
                    this.timeoutObject_.wait(TIMEOUT_MILLIS);
                }
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }

            if (client_.isConnected() == false)
            {
                System.out.println("connect timeout. ");
                return false;
            }
        }

        return true;
    }

    /**
     * 切断する。
     */
    public void disconnect()
    {
        client_.disconnect();
        client_.shutdown();
    }

}