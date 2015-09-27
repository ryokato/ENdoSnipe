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
package jp.co.acroquest.endosnipe.collector;

import static jp.co.acroquest.endosnipe.collector.LogMessageCodes.SSL_SERVER_SOCKET_CREATE_FAILURE;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import jp.co.acroquest.endosnipe.collector.DataCollectorClient.DataCollectorClientListener;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.CommunicationServer;
import jp.co.acroquest.endosnipe.communicator.CommunicatorListener;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.accessor.ConnectNotifyAccessor;
import jp.co.acroquest.endosnipe.communicator.entity.CommunicatorSetting;
import jp.co.acroquest.endosnipe.communicator.entity.ConnectNotifyData;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;

/**
 * クライアントからの接続を待ち受けるDataCollectorサーバ。<br />
 * 
 * @author matsuoka
 */
public class DataCollectorServer implements CommunicationServer, Runnable
{
    /** バインド間隔のデフォルト値 (msec) */
    private static final int DEFAULT_BIND_INTERVAL = 5 * 1000;

    /** 接続待ち受けスレッド名 */
    private static final String ACCEPT_THREAD_NAME = "JavelinServerThread";

    /** クライアントスレッドグループ名 */
    private static final String CLIENT_THREAD_GROUP_NAME = "JavelinClientThreadGroup";

    /** クライアントスレッド名 */
    private static final String CLIENT_THREAD_NAME = "JavelinClientThread";

    /** Javelin/BottleneckEyeを待ち受けるポート番号 */
    private CommunicatorSetting setting_;

    /** サーバソケット */
    private ServerSocket serverSocket_ = null;

    /** クライアントIDをキーとしたクライアントのリスト */
    protected Map<String, DataCollectorClient> clientList_ =
        new HashMap<String, DataCollectorClient>();

    /** スレッド処理中かどうかを表すフラグ */
    boolean isRunning_ = false;

    /** ポートリスニング中かどうかを表すフラグ */
    boolean isListening_ = false;

    /** 通信用スレッド */
    private Thread acceptThread_;

    /** Bind失敗時の再試行間隔 */
    private int bindInterval_;

    /** クライアントIDシーケンス番号 */
    private BigInteger seqClientId_ = BigInteger.ZERO;

    /** クライアント状態が変化した時のリスナ */
    private final List<ClientNotifyListener> clientNotifyListenerList_ =
        new ArrayList<ClientNotifyListener>();

    /** Javelinクライアントの電文を処理するリスナ */
    private final List<TelegramListener> javelinClientTelegramListener_ =
        new ArrayList<TelegramListener>();

    /** 制御クライアントの電文を処理するリスナ */
    private final List<TelegramListener> controlClientTelegramListener_ =
        new ArrayList<TelegramListener>();

    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger
        .getLogger(DataCollectorServer.class);

    /**
     * クライアントからの接続・切断を処理するためのリスナ。
     * @author matsuoka
     */
    public interface ClientNotifyListener
    {
        /**
         * クライアントとの接続が確立したときにコールされる。
         * @param client クライアント
         */
        void clientConnected(DataCollectorClient client);

        /**
         * クライアントとの接続が切断されたときにコールされる。
         * @param client クライアント
         * @param forceDisconnected 強制切断された場合は <code>true</code>
         */
        void clientDisconnected(DataCollectorClient client, boolean forceDisconnected);
    }

    /**
     * {@inheritDoc}
     */
    public void init()
    {
        // 何もしない
    }

    /**
     * {@inheritDoc}
     */
    public void start(final CommunicatorSetting setting)
    {
        start(setting, DEFAULT_BIND_INTERVAL);
    }

    /**
     * サーバを開始する。
     * @param setting 通信設定
     * @param bindInterval Bind失敗時の再試行間隔
     */
    public void start(final CommunicatorSetting setting, final int bindInterval)
    {
        setting_ = setting;
        bindInterval_ = bindInterval;

        if (this.serverSocket_ != null)
        {
            return;
        }
        if (acceptThread_ != null)
        {
            return;
        }

        // クライアント接続の受付を開始する。
        acceptThread_ = new Thread(this, ACCEPT_THREAD_NAME);
        acceptThread_.setDaemon(true);
        acceptThread_.start();
    }

    /**
     * 接続待ち受けのメイン処理。
     */
    public void run()
    {
        isRunning_ = true;

        serverSocket_ = createServerSocket();
        if (serverSocket_ == null)
        {
            return;
        }

        ThreadGroup group = new ThreadGroup(CLIENT_THREAD_GROUP_NAME);
        while (isRunning_)
        {
            try
            {
                accept(group);
            }
            catch (Throwable th)
            {
                // CHECKSTYLE:OFF
                ; // 何もしない
                  // CHECKSTYLE:ON
            }
        }

        synchronized (clientList_)
        {
            for (DataCollectorClient client : clientList_.values())
            {
                client.stop();
            }
        }

        try
        {
            if (serverSocket_ != null && !serverSocket_.isClosed())
            {
                serverSocket_.close();
            }
        }
        catch (IOException e)
        {
            // CHECKSTYLE:OFF
            ; // 何もしない
              // CHECKSTYLE:ON
        }
    }

    /**
     * サーバソケットを作成する。
     * @return 作成したサーバソケットを返す。
     */
    private ServerSocket createServerSocket()
    {
        ServerSocket socket = null;

        while (isRunning_)
        {
            try
            {
                if (setting_.sslEnable)
                {
                    socket = createSslServerSocket();
                }
                else
                {
                    socket = new ServerSocket(setting_.port);
                }
                break;
            }
            catch (IOException e)
            {
                // ソケットの作成に失敗したら、指定間隔スリープしてから再試行する。
                try
                {
                    Thread.sleep(bindInterval_);
                }
                catch (InterruptedException iex)
                {
                    // CHECKSTYLE:OFF
                    ; // 何もしない
                      // CHECKSTYLE:ON
                }
            }
        }
        isListening_ = true;

        return socket;
    }

    private SSLServerSocket createSslServerSocket()
    {
        SSLServerSocket serverSocket = null;
        try
        {
            // KeyStoreの読み込み
            char[] keyPassChar = setting_.keyStorePass.toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(setting_.keyStore), keyPassChar);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, keyPassChar);

            // TrustStoreの読み込み
            char[] trustPassChar = setting_.trustStorePass.toCharArray();
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(setting_.trustStore), trustPassChar);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            KeyManager[] keyManagers = kmf.getKeyManagers();
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sContext = SSLContext.getInstance("TLS");
            sContext.init(keyManagers, trustManagers, null);

            SSLServerSocketFactory serverSocketFactory = sContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket)serverSocketFactory.createServerSocket(setting_.port);
            // クライアント認証は行わない。
            serverSocket.setNeedClientAuth(false);
        }
        catch (Exception ex)
        {
            LOGGER.log(SSL_SERVER_SOCKET_CREATE_FAILURE, ex, ex.getMessage());
        }
        return serverSocket;
    }

    private void accept(final ThreadGroup group)
        throws SocketException
    {
        Socket clientSocket = null;

        try
        {
            clientSocket = serverSocket_.accept();
        }
        catch (SocketException e)
        {
            // stop()でソケットを閉じた場合にSocketExceptionが発生する。
            throw e;
        }
        catch (IOException e)
        {
            return;
        }

        sweepClient();

        // クライアントからの要求受付用に、処理スレッドを起動する。
        DataCollectorClient clientRunnable;
        try
        {
            String clientId = getClientId();
            clientRunnable = createClientThread(clientSocket, clientId);
            String clientThreadName = String.format("%s-%s", CLIENT_THREAD_NAME, clientId);
            Thread objHandleThread = new Thread(group, clientRunnable, clientThreadName);
            objHandleThread.setDaemon(true);
            objHandleThread.start();

            // 通知のためのクライアントリストに追加する。
            synchronized (this.clientList_)
            {
                this.clientList_.put(clientId, clientRunnable);
            }
        }
        catch (IOException ioe)
        {
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void stop()
    {
        isRunning_ = false;

        if (this.isListening_ == false)
        {
            try
            {
                //　通信用ポートBind待ち状態のために、割り込みを行う
                Thread acceptThread = this.acceptThread_;
                if (acceptThread != null)
                {
                    acceptThread.interrupt();
                }
            }
            catch (Exception ex)
            {
                // CHECKSTYLE:OFF
                ;
                // CHECKSTYLE:ON
            }
        }

        if (this.isListening_)
        {
            // 待ち受けソケットを閉じることにより、accept()でSocketExceptionが
            // 発生し、待ち受けスレッドが停止する。
            if (this.serverSocket_ != null)
            {
                try
                {
                    this.serverSocket_.close();
                }
                catch (Exception ex)
                {
                    // CHECKSTYLE:OFF
                    ;
                    // CHECKSTYLE:ON
                }
            }

            this.isListening_ = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean isConnected()
    {
        int clientCount = sweepClient();
        if (clientCount == 0)
        {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int getActiveClient()
    {
        return clientList_.size();
    }

    /**
     * {@inheritDoc}
     */
    public void addCommunicatorListener(final CommunicatorListener listener)
    {
        // 何もしない
    }

    /**
     * {@inheritDoc}
     */
    public void sendTelegram(final Telegram telegram)
    {
        // 何もしない
    }

    /**
     * {@inheritDoc}
     */
    public void addTelegramListener(final TelegramListener listener)
    {
        // 何もしない
    }

    /**
     * 接続が切れたクライアントを掃除する。
     * @return
     */
    private int sweepClient()
    {
        int size;
        synchronized (clientList_)
        {
            Map<String, DataCollectorClient> newClientList =
                new HashMap<String, DataCollectorClient>();
            for (String key : clientList_.keySet())
            {
                DataCollectorClient client = clientList_.get(key);
                if (!client.isClosed())
                {
                    newClientList.put(key, client);
                }
            }
            clientList_.clear();
            clientList_ = newClientList;
            size = clientList_.size();
        }

        return size;
    }

    /**
     * JavelinClientコネクションオブジェクトを生成します。
     *
     * @param clientSocket ソケット
     * @param clientId クライアントID
     * @return JavelinClientコネクションオブジェクト
     * @throws IOException 入出力例外が発生した場合
     */
    protected DataCollectorClient createClientThread(final Socket clientSocket,
        final String clientId)
        throws IOException
    {
        DataCollectorClient client = new DataCollectorClient(clientSocket, false, clientId);

        DataCollectorClientListener listener = new DataCollectorClientListener() {
            public void disconnected(final DataCollectorClient client,
                final boolean forceDisconnected)
            {
                ConnectNotifyData notifyData = client.getConnectNotifyData();
                if (notifyData == null)
                {
                    client.stop();
                    return;
                }

                switch (notifyData.getKind())
                {
                case ConnectNotifyData.KIND_JAVELIN:
                    ENdoSnipeDataCollector.removeJavelinSequenceNo(notifyData.getAgentName(),
                                                                   client.getDbNo());
                    break;

                default:
                    break;
                }

                // クライアントが切断されたら、登録されているリスナに通知する。
                for (ClientNotifyListener listener : clientNotifyListenerList_)
                {
                    listener.clientDisconnected(client, forceDisconnected);
                }
            }

            public void receiveConnectNotify(final DataCollectorClient client)
            {
                ConnectNotifyData notifyData = client.getConnectNotifyData();
                switch (notifyData.getKind())
                {
                case ConnectNotifyData.KIND_JAVELIN:
                    String agentName = notifyData.getAgentName();
                    int no = ENdoSnipeDataCollector.getJavelinSequenceNo(agentName);
                    agentName = ConnectNotifyAccessor.createAgentName(agentName, no);
                    client.setAgentName(agentName);
                    client.setDbNo(no);
                    break;
                case ConnectNotifyData.KIND_CONTROLLER:
                    switch (notifyData.getPurpose())
                    {
                    case ConnectNotifyData.PURPOSE_GET_RESOURCE:
                        client.setTelegramListener(controlClientTelegramListener_);
                        break;
                    case ConnectNotifyData.PURPOSE_GET_DATABASE:
                    default:
                        // 処理なし
                        break;

                    }

                    break;

                default:
                    break;
                }

                // クライアントを有効化する。
                client.setEnabled(true);

                // 登録されているリスナに通知する。
                for (ClientNotifyListener listener : clientNotifyListenerList_)
                {
                    listener.clientConnected(client);
                }
            }
        };
        client.setClientListener(listener);

        return client;
    }

    /**
     * クライアント状態変換通知用のリスナを登録する。
     * @param listener リスナ
     */
    public void addClientNotifyListener(final ClientNotifyListener listener)
    {
        clientNotifyListenerList_.add(listener);
    }

    /**
     * Javelinクライアント用の電文リスナを登録する。
     * @param listener リスナ
     */
    public void addJavelinClientTelegramListener(final TelegramListener listener)
    {
        javelinClientTelegramListener_.add(listener);
    }

    /**
     * 制御クライアント用の電文リスナを登録する。
     * @param listener リスナ
     */
    public void addControlClientTelegramListener(final TelegramListener listener)
    {
        controlClientTelegramListener_.add(listener);
    }

    /**
     * クライアントIDをキーとしてクライアントを取得する。
     * @param clientId キーとなるクライアントID
     * @return 指定されたクライアントIDのクライアントを返す。
     */
    public DataCollectorClient getClient(final String clientId)
    {
        return clientList_.get(clientId);
    }

    /**
     * クライアントIDを返す。
     * @return 採番されたクライアントID
     */
    private String getClientId()
    {
        String clientId = seqClientId_.toString();
        seqClientId_ = seqClientId_.add(BigInteger.valueOf(1));
        return clientId;
    }

}
