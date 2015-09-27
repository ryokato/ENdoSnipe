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
package jp.co.acroquest.endosnipe.communicator.impl;

import static jp.co.acroquest.endosnipe.communicator.entity.Header.HEADER_LENGTH;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.TelegramUtil;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;

/**
 * 電文の受信処理を行うためのクラスです。<br />
 * 
 * @author y-komori
 */
public class TelegramReader implements Runnable
{
    /** ロガークラス */
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger.getLogger(TelegramReader.class);

    private static final int SO_TIMEOUT = 10000;

    /***/
    private static final int READ_WAIT = 100;

    private static final int RETRY_MAX = 100;

    private volatile boolean isRunning_;

    private volatile boolean isShutdown_ = false;

    private Socket socket_;

    /** 電文を転送するターゲットオブジェクトのリスト */
    private final List<TelegramListener> telegramListenerList_;

    /** 再起動用TcpStatsVisionEditor */
    private CommunicationClientImpl comminicationClient_ = null;

    /** リトライ時間 */
    private static final int RETRY_INTERVAL = 10000;

    private int retryCount_ = 0;

    /** ログ出力有無 */
    private boolean isOutputLog_ = true;

    /** サーバとの通信用コネクション */
    private JavelinClientConnection clientConnection_;

    /** 電文送信用スレッド名. */
    private String sendThreadName_;

    /** 電文送信用スレッド. */
    private Thread clientSendThread_;

    /**
     * {@link TelegramReader} を構築します。<br />
     *
     * @param communicationClient {@link CommunicationClientImpl} オブジェクト
     * @param sendThreadName 電文送信用スレッド名
     * @param objSocket 電文送信用ソケット
     * @param discard Discardの実施有無
     * @param isOutputLog ログ出力有無
     * @throws IOException 入出力例外が発生した場合
     */
    public TelegramReader(final CommunicationClientImpl communicationClient,
        final String sendThreadName, final Socket objSocket, final boolean discard,
        final boolean isOutputLog)
        throws IOException
    {
        this.clientConnection_ = new JavelinClientConnection(objSocket, discard);

        this.isRunning_ = false;
        this.comminicationClient_ = communicationClient;
        this.sendThreadName_ = sendThreadName;
        this.isOutputLog_ = isOutputLog;

        // telegramListenerList_ は synchronized を行う必要があるが、
        // 拡張 for 文（ iterator ）を使用する場合は Collections.synchronizedList でラップしても無駄なため、
        // 自前で synchronized 処理を行う
        this.telegramListenerList_ = new ArrayList<TelegramListener>();
    }

    /**
     * 電文処理用オブジェクトを追加します。<br />
     *
     * @param telegramListener 電文処理用オブジェクト
     */
    public void addTelegramListener(final TelegramListener telegramListener)
    {
        synchronized (this.telegramListenerList_)
        {
            this.telegramListenerList_.add(telegramListener);
        }
    }

    /**
     * 電文受信ループ。<br />
     */
    public void run()
    {
        JavelinClientSendRunnable clientSendRunnable =
            new JavelinClientSendRunnable(this.clientConnection_);
        this.clientSendThread_ = new Thread(clientSendRunnable, this.sendThreadName_);
        this.clientSendThread_.setDaemon(true);
        this.clientSendThread_.start();

        this.retryCount_ = 0;
        //        this.headerBuffer_.rewind();
        while (this.isRunning_)
        {
            try
            {
                mainLoop();
            }
            catch (Exception ex)
            {
                outputLog("WECC0105", ex);
            }
        }

    }

    private void mainLoop()
    {
        if (this.comminicationClient_.isStart() == false)
        {
            try
            {
                Thread.sleep(RETRY_INTERVAL);
            }
            catch (InterruptedException ex)
            // CHECKSTYLE:OFF
            {
                // interruptする。
            }
            // CHECKSTYLE:ON
            return;
        }
        this.socket_ = this.comminicationClient_.getSocket();

        if (this.socket_ == null)
        {
            setRunning(false);
            this.clientConnection_.close();
            return;
        }

        byte[] telegramBytes = null;
        try
        {
            telegramBytes = this.readTelegramBytes();
        }
        catch (SocketTimeoutException ste)
        {
            outputLog("WECC0107", ste);
            return;
        }
        catch (IOException ioe)
        {
            if (isShutdown_ == false)
            {
                // 切断された
                outputLog("WECC0201", ioe);
                setRunning(false);
                this.clientConnection_.close();
            }
            return;
        }
        Telegram telegram = TelegramUtil.recoveryTelegram(telegramBytes);

        if (telegram == null)
        {
            outputLog("WECC0106");
            return;
        }

        synchronized (this.telegramListenerList_)
        {
            for (TelegramListener listener : this.telegramListenerList_)
            {
                try
                {
                    Telegram response = listener.receiveTelegram(telegram);

                    // 応答電文がある場合のみ、応答を返す
                    if (response != null)
                    {
                        List<byte[]> byteList = TelegramUtil.createTelegram(response);
                        for (byte[] byteOutputArr : byteList)
                        {
                            this.clientConnection_.sendAlarm(byteOutputArr);
                        }
                    }
                }
                catch (Exception ex)
                {
                    outputLog("WECC0105", ex);
                }
            }
        }
    }

    /**
     * サーバからデータを読み込みます。<br />
     *
     * @return 受信したデータ
     * @throws IOException 入出力例外が発生した場合
     */
    @SuppressWarnings("deprecation")
    public byte[] readTelegramBytes()
        throws IOException
    {
        this.socket_.setSoTimeout(SO_TIMEOUT);
        byte finalTelegram = TelegramConstants.HALFWAY_TELEGRAM;

        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();

        do
        {
            byte[] header = new byte[HEADER_LENGTH];
            try
            {
                readFull(this.socket_.getInputStream(), header, 0, Header.HEADER_LENGTH);
            }
            catch (SocketTimeoutException ex)
            {
                if (this.retryCount_ > RETRY_MAX)
                {
                    throw new IOException();
                }
                this.retryCount_++;
                try
                {
                    Thread.sleep(READ_WAIT);
                }
                catch (InterruptedException iex)
                {
                    LOGGER.warn(iex);
                }
                continue;
            }
            this.retryCount_ = 0;
            ByteBuffer headerBuffer = ByteBuffer.wrap(header);

            headerBuffer.rewind();
            int telegramLength = headerBuffer.getInt();
            headerBuffer.getLong();
            finalTelegram = headerBuffer.get();

            // ヘッダ部しかない場合はそのまま返す。
            if (telegramLength <= HEADER_LENGTH)
            {
                headerBuffer.rewind();
                return headerBuffer.array();
            }

            int capacity = telegramLength - HEADER_LENGTH;

            if (resultStream.size() == 0)
            {
                resultStream.write(headerBuffer.array());
            }

            byte[] body = new byte[capacity];
            readFull(this.socket_.getInputStream(), body, 0, capacity);
            ByteBuffer bodyBuffer = ByteBuffer.wrap(body);

            resultStream.write(bodyBuffer.array());
        }
        while (finalTelegram != TelegramConstants.FINAL_TELEGRAM);

        byte[] telegramBytes = resultStream.toByteArray();
        int telegramLength = telegramBytes.length;
        ByteBuffer outputBuffer = ByteBuffer.wrap(telegramBytes);

        // ヘッダを変換する
        outputBuffer.rewind();
        outputBuffer.putInt(telegramLength);

        return outputBuffer.array();
    }

    /**
     * 電文を指定した長さで読み込む。
     * @param in {@link InputStream}
     * @param data 読み込み後のデータを格納するオブジェクト
     * @param offset オフセット
     * @param length 読み込む長さ
     * @throws IOException 入出力例外が発生した場合
     */
    private void readFull(InputStream in, byte[] data, final int offset, final int length)
        throws IOException
    {
        // read() は1回ですべてのデータを読み込むことができないため、すべてのデータを読み込むまで繰り返す
        int pos = offset;
        int remainLength = length;
        int lastCount = 0;
        while (remainLength > 0)
        {
            int inputCount = in.read(data, pos, remainLength);
            if (inputCount < 0)
            {
                throw new IOException("Cannot read.");
            }
            pos += inputCount;
            remainLength -= inputCount;
            lastCount = inputCount;
        }
    }

    /**
     * 実行状態を設定します。<br />
     * 
     * @param isRunning 実行状態
     */
    public void setRunning(final boolean isRunning)
    {
        this.isRunning_ = isRunning;
    }

    /**
     * 電文をデバッグ出力します。<br />
     * 
     * @param message メッセージ
     * @param response 受信電文
     * @param length 電文長
     */
    public void logTelegram(final String message, final Telegram response, final int length)
    {
        String telegramStr = TelegramUtil.toPrintStr(response, length);
        outputLog("DECC0108", message, this.socket_.getInetAddress().getHostAddress(),
                  this.socket_.getPort(), telegramStr);
    }

    /**
     * close
     */
    public void shutdown()
    {
        isShutdown_ = true;
        setRunning(false);

        // 電文送信スレッドに割り込んで停止する。
        this.clientSendThread_.interrupt();
    }

    /**
     * ログを出力します。<br />
     * 
     * @param messageCode メッセージコード
     * @param throwable 例外
     * @param args 引数
     */
    private void outputLog(final String messageCode, final Throwable throwable,
        final Object... args)
    {
        if (isOutputLog_)
        {
            LOGGER.log(messageCode, throwable, args);
        }
    }

    /**
     * ログを出力します。<br />
     * 
     * @param messageCode メッセージコード
     * @param args 引数
     */
    private void outputLog(final String messageCode, final Object... args)
    {
        if (isOutputLog_)
        {
            LOGGER.log(messageCode, args);
        }
    }
}
