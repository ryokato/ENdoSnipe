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
package jp.co.acroquest.endosnipe.communicator;

import jp.co.acroquest.endosnipe.communicator.entity.CommunicatorSetting;

/**
 * コミュニケーションサーバのためのインターフェースです。<br />
 * 
 * @author y-komori
 */
public interface CommunicationServer extends AbstractCommunicator
{
    /**
     * サーバの初期化を行います。<br />
     * {@link #start(int)} メソッドを呼び出す前に、本メソッドを呼び出してください。
     */
    void init();

    /**
     * サーバを開始します。<br />
     * 
     * @param setting 通信設定
     */
    void start(CommunicatorSetting setting);

    /**
     * サーバを停止します。<br />
     */
    void stop();

    /**
     * 接続中クライアントの数を返します。<br />
     * 
     * @return クライアントの数
     */
    int getActiveClient();
}
