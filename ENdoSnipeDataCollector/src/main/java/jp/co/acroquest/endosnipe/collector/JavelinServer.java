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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jp.co.acroquest.endosnipe.collector.DataCollectorServer.ClientNotifyListener;
import jp.co.acroquest.endosnipe.collector.data.JavelinConnectionData;
import jp.co.acroquest.endosnipe.collector.listener.AllNotifyListener;
import jp.co.acroquest.endosnipe.collector.listener.CommonResponseListener;
import jp.co.acroquest.endosnipe.collector.listener.ControlGetNotifyListener;
import jp.co.acroquest.endosnipe.collector.listener.ControlUpdateNotifyListener;
import jp.co.acroquest.endosnipe.collector.listener.JavelinClientTelegramListener;
import jp.co.acroquest.endosnipe.collector.listener.JvnFileNotifyListener;
import jp.co.acroquest.endosnipe.collector.listener.ProfileNotifyListener;
import jp.co.acroquest.endosnipe.collector.listener.SignalChangeListener;
import jp.co.acroquest.endosnipe.collector.listener.SignalStateListener;
import jp.co.acroquest.endosnipe.collector.listener.SqlPlanNotifyListener;
import jp.co.acroquest.endosnipe.collector.listener.SummarySignalChangeListener;
import jp.co.acroquest.endosnipe.collector.listener.SystemResourceListener;
import jp.co.acroquest.endosnipe.collector.listener.SystemResourceNotifyListener;
import jp.co.acroquest.endosnipe.collector.listener.TelegramNotifyListener;
import jp.co.acroquest.endosnipe.collector.listener.ThreadDumpNotifyListener;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.TelegramSender;
import jp.co.acroquest.endosnipe.communicator.accessor.ConnectNotifyAccessor;
import jp.co.acroquest.endosnipe.communicator.accessor.SystemResourceGetter;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.CommunicatorSetting;
import jp.co.acroquest.endosnipe.communicator.entity.ConnectNotifyData;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.service.HostInfoManager;

/**
 * Javelin からデータを受信するためのサーバです。<br />
 * 
 * @author matsuoka
 */
public class JavelinServer implements TelegramSender, TelegramConstants
{
    /** サーバインスタンス */
    private final DataCollectorServer server_ = new DataCollectorServer();

    /** DB名をキーとしたJavelinクライアントのリスト */
    private final Map<String, DataCollectorClient> javelinClientList_ =
        new HashMap<String, DataCollectorClient>();

    /** DB名をキーとした制御クライアントのリスト */
    private final Map<String, Set<DataCollectorClient>> controlClientList_ =
        new HashMap<String, Set<DataCollectorClient>>();

    /** DB名の増減を通知するクライアント */
    private DataCollectorClient databaseAdminClient_ = null;

    /** キュー */
    private JavelinDataQueue queue_;

    /** DB名をキーとした通知リスナのマップ */
    private Map<String, List<TelegramNotifyListener>> notifyListenerMap_ =
        new HashMap<String, List<TelegramNotifyListener>>();

    /** リソース取得 */
    private SystemResourceGetter resourceGetter_;

    /** 動作モード */
    private BehaviorMode behaviorMode_;

    /** Javelinクライアントとの接続が確立するまで電文を滞留させておくためのリスト */
    private final List<Telegram> waitingTelegramList_ = new ArrayList<Telegram>();

    /** 接続するデータベース名。 */
    private String dbName_;

    private static final int AGENTINDEX = 4;

    /** エージェント名の分割パターン */
    private static final Pattern AGENT_SPLIT_PATTERN = Pattern.compile("/");

    /**
     * サーバを開始する。
     * 
     * @param setting {@link CommunicatorSetting}
     * @param queue データキュー
     * @param dbName 接続するデータベース名。
     * @param resourceGetter システムリソース取得
     * @param behaviorMode DataCollectorの動作モード
     */
    public void start(final CommunicatorSetting setting, final JavelinDataQueue queue,
        final String dbName, final SystemResourceGetter resourceGetter,
        final BehaviorMode behaviorMode)
    {
        dbName_ = dbName;
        queue_ = queue;
        resourceGetter_ = resourceGetter;
        behaviorMode_ = behaviorMode;

        server_.addClientNotifyListener(new ClientNotifyListener() {

            public void clientDisconnected(final DataCollectorClient client,
                final boolean forceDisconnected)
            {
                ConnectNotifyData notifyData = client.getConnectNotifyData();
                if (notifyData == null)
                {
                    return;
                }
                String agentName = client.getAgentName();
                if (client.getAgentName() == null || "".equals(agentName))
                {
                    client.setAgentName(notifyData.getAgentName());
                }

                switch (notifyData.getKind())
                {
                case ConnectNotifyData.KIND_JAVELIN:
                    notifyJavelinDisconnected(dbName_, forceDisconnected);
                    removeJavelinClient(dbName_);
                    notifyDelJavelin(dbName_);
                    break;

                case ConnectNotifyData.KIND_CONTROLLER:
                    removeControlClient(dbName_);
                    break;

                default:
                    break;
                }

            }

            public void clientConnected(final DataCollectorClient client)
            {
                ConnectNotifyData notifyData = client.getConnectNotifyData();
                if (notifyData == null)
                {
                    return;
                }
                String agentName = client.getAgentName();
                if (client.getAgentName() == null || "".equals(agentName))
                {
                    client.setAgentName(notifyData.getAgentName());
                }

                switch (notifyData.getKind())
                {
                case ConnectNotifyData.KIND_JAVELIN:
                    addJavelinClient(client);
                    initializeJavelinClient(client);
                    sendWaitingTelegram(client);
                    notifyAddJavelin(dbName_);
                    break;

                case ConnectNotifyData.KIND_CONTROLLER:
                    switch (notifyData.getPurpose())
                    {
                    case ConnectNotifyData.PURPOSE_GET_RESOURCE:
                        addControlClient(client);
                        initializeControlClient(client);
                        break;
                    case ConnectNotifyData.PURPOSE_GET_DATABASE:
                        setDatabaseAdminClient(client);
                        sendDatabaseName();
                        break;
                    default:
                        addControlClient(client);
                        initializeControlClient(client);
                        break;
                    }
                    break;

                default:
                    break;
                }

            }
        });

        server_.start(setting);
    }

    /**
     * サーバを停止する。
     */
    public void stop()
    {
        server_.stop();
    }

    /**
     * クライアントに通知するためのリスナを登録します。
     *
     * @param notifyListenerList クライアントに通知するためのリスナ
     */
    public void setTelegramNotifyListener(
        final Map<String, List<TelegramNotifyListener>> notifyListenerList)
    {
        if (notifyListenerList != null)
        {
            notifyListenerMap_ = notifyListenerList;

            // 本メソッドのコール時には、まだクライアントと接続されていない。
            // そのため、通知リスナの送信先にダミーを設定する。
            setDummyTelegramSenders();
        }
        else
        {
            notifyListenerMap_ = new HashMap<String, List<TelegramNotifyListener>>();
        }
    }

    /**
     * 指定されたクライアントIDのクライアントを取得する。
     * @param clientId キーとなるクライアントID
     * @return 指定されたクライアントIDのクライアントを返す。
     */
    public DataCollectorClient getClient(final String clientId)
    {
        return server_.getClient(clientId);
    }

    /**
     * Javelinクライアントに紐付く制御クライアントに電文を送信する。
     * @param clientId JavelinクライアントのクライアントID
     * @param telegram 送信する電文
     */
    public void sendTelegramToControlClient(final String clientId, final Telegram telegram)
    {
        //        DataCollectorClient javelinClient = getClient(clientId);
        //        if (javelinClient == null)
        //        {
        //            return;
        //        }
        //        ConnectNotifyData notifyData = javelinClient.getConnectNotifyData();
        //        if (notifyData == null)
        //        {
        //            return;
        //        }
        //        if (notifyData.getKind() != ConnectNotifyData.KIND_JAVELIN)
        //        {
        //            return;
        //        }

        if (behaviorMode_.equals(BehaviorMode.PLUGIN_MODE))
        {
            // プラグインモードで動作しているなら、リスナに通知する。
            String dbName = dbName_;
            List<TelegramNotifyListener> notifyListenerList = notifyListenerMap_.get(dbName);
            if (notifyListenerList != null)
            {
                for (TelegramNotifyListener notifyListener : notifyListenerList)
                {
                    if (notifyListener.isRawTelegramNeeded() == false)
                    {
                        notifyListener.receiveTelegram(telegram);
                    }
                }
            }
        }
        else
        {
            // サービスモードで動作しているなら、接続されている制御クライアントに
            // 電文を送信する。
            Set<DataCollectorClient> controlClientSet = getControlClient(dbName_);
            if (controlClientSet != null)
            {
                for (DataCollectorClient controlClient : controlClientSet)
                {
                    controlClient.sendTelegram(telegram);
                }
            }
        }

        return;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConnected()
    {
        return false;
    }

    /**
     * Javelinが接続されるまでのダミーの電文送信処理。
     * @param telegram 送信電文
     */
    public void sendTelegram(final Telegram telegram)
    {
        // 電文を滞留させる。
        waitingTelegramList_.add(telegram);
    }

    /**
     * javelinの増加を通知する
     * @param databaseName DB名
     */
    private void notifyAddJavelin(final String databaseName)
    {
        if (this.databaseAdminClient_ != null)
        {
            Set<String> databaseNameList = new HashSet<String>();
            databaseNameList.add(databaseName);
            databaseAdminClient_.sendTelegram(ConnectNotifyAccessor
                .createAddDatabaseNameTelegram(databaseNameList));
        }
    }

    /**
     * javelinの減少を通知する
     * @param databaseName DB名
     */
    private void notifyDelJavelin(final String databaseName)
    {
        if (this.databaseAdminClient_ != null)
        {
            Set<String> databaseNameList = new HashSet<String>();
            databaseNameList.add(databaseName);
            databaseAdminClient_.sendTelegram(ConnectNotifyAccessor
                .createDelDatabaseNameTelegram(databaseNameList));
        }
    }

    /**
     * 接続されたJavelinクライアントを初期化する。
     * @param client Javelinクライアント
     */
    private void initializeJavelinClient(final DataCollectorClient client)
    {
        setTelegramSenders();

        String dbName = dbName_;
        String hostName = client.getHostName();
        String ipAddress = client.getIpAddr();
        int port = client.getPort();
        String clientId = client.getClientId();
        String agentName = client.getAgentName();

        List<TelegramNotifyListener> listenerList = notifyListenerMap_.get(dbName);
        AllNotifyListener allNotifyListener = new AllNotifyListener();
        allNotifyListener.setTelegramNotifyListener(listenerList);
        client.addTelegramListener(allNotifyListener);

        JvnFileNotifyListener jvnFileNotifyListener =
            createJvnFileNotifyListener(dbName, hostName, ipAddress, port, clientId, agentName);
        SystemResourceListener systemResourceListener =
            createSystemResourceListener(dbName, hostName, ipAddress, port, clientId, agentName);
        SystemResourceListener systemResourceNotifyListener =
            createSystemResourceNotifyListener(dbName, hostName, ipAddress, port, clientId,
                                               agentName);

        if (queue_ != null)
        {
            client.addTelegramListener(jvnFileNotifyListener);
            client.addTelegramListener(systemResourceListener);
            client.addTelegramListener(systemResourceNotifyListener);

            CommonResponseListener dumpTelegramListener =
                createResponseTelegramListener(BYTE_TELEGRAM_KIND_GET_DUMP);
            client.addTelegramListener(dumpTelegramListener);
            CommonResponseListener updatePropertyListener =
                createResponseTelegramListener(BYTE_TELEGRAM_KIND_UPDATE_PROPERTY);
            client.addTelegramListener(updatePropertyListener);

            client.addTelegramListener(createSqlPlanNotifyListener(hostName, agentName, port,
                                                                   dbName));

            HostInfoManager.registerHostInfo(dbName, hostName, ipAddress, port, null);

            notifyJavelinConnected(dbName, hostName, ipAddress, port);
        }

        SignalStateListener signalStateListener = new SignalStateListener();
        SignalChangeListener signalChangeListener = new SignalChangeListener();

        SummarySignalChangeListener summarySignalChangeListener = new SummarySignalChangeListener();

        client.addTelegramListener(signalStateListener);
        client.addTelegramListener(signalChangeListener);
        client.addTelegramListener(summarySignalChangeListener);

        ThreadDumpNotifyListener threadDumpNotifyListener = new ThreadDumpNotifyListener();
        client.addTelegramListener(threadDumpNotifyListener);

        ProfileNotifyListener profileNotifyListener = new ProfileNotifyListener();
        client.addTelegramListener(profileNotifyListener);

        ControlGetNotifyListener controlGetNotifyListener = new ControlGetNotifyListener();
        client.addTelegramListener(controlGetNotifyListener);

        ControlUpdateNotifyListener controlUpdateNotifyListener = new ControlUpdateNotifyListener();
        client.addTelegramListener(controlUpdateNotifyListener);

        // 制御クライアントが存在するなら、Javelinクライアントと紐付ける。
        Set<DataCollectorClient> controlClientSet = getControlClient(dbName);
        if (controlClientSet != null)
        {
            for (DataCollectorClient controlClient : controlClientSet)
            {
                bindJavelinAndControlClient(client, controlClient);
            }
        }

        resourceGetter_.addTelegramSenderList(client);
    }

    /**
     * 制御クライアントにJavelinが接続されたことを通知する。
     * @param dbName データベース名
     * @param hostName Javelinのホスト名
     * @param ipAddress JavelinのIPアドレス
     * @param port Javelinのポート番号
     */
    private void notifyJavelinConnected(final String dbName, final String hostName,
        final String ipAddress, final int port)
    {
        List<TelegramNotifyListener> listenerList = notifyListenerMap_.get(dbName);
        if (listenerList != null)
        {
            for (TelegramNotifyListener telegramNotifyListener : listenerList)
            {
                telegramNotifyListener.clientConnected(hostName, ipAddress, port);
            }
        }

        // 接続を表すデータをキューに追加する
        Date currentDate = new Date();
        long currentTime = currentDate.getTime();
        JavelinConnectionData connectionData =
            new JavelinConnectionData(JavelinConnectionData.TYPE_CONNECTION);
        connectionData.measurementTime = currentTime;
        connectionData.setDatabaseName(dbName);
        if (queue_ != null)
        {
            queue_.offer(connectionData);
        }
    }

    /**
     * 制御クライアントにJavelinが切断されたことを通知する。
     * @param dbName データベース名
     * @param forceDisconnected 強制切断なら<code>true</code>
     */
    private void notifyJavelinDisconnected(final String dbName, final boolean forceDisconnected)
    {
        if (forceDisconnected)
        {
            // 切断を表すデータをキューに追加する（強制切断された場合）
            Date currentDate = new Date();
            long currentTime = currentDate.getTime();
            JavelinConnectionData disconnectionData =
                new JavelinConnectionData(JavelinConnectionData.TYPE_DISCONNECTION);
            disconnectionData.measurementTime = currentTime;
            disconnectionData.setDatabaseName(dbName);
            if (queue_ != null)
            {
                queue_.offer(disconnectionData);
            }
        }

        List<TelegramNotifyListener> listenerList = notifyListenerMap_.get(dbName);
        if (listenerList != null)
        {
            for (TelegramNotifyListener telegramNotifyListener : listenerList)
            {
                telegramNotifyListener.clientDisconnected(forceDisconnected);
            }
        }
    }

    /**
     * サービスモード時の初期化処理。
     * @param alarmRepository アラーム
     */
    private synchronized void initializeControlClient(final DataCollectorClient client)
    {
        addDataCollectorClientListener(client);
        for (DataCollectorClient javelinClient : this.javelinClientList_.values())
        {
            bindJavelinAndControlClient(javelinClient, client);
            resourceGetter_.addTelegramSenderList(client);
        }

    }

    private void addDataCollectorClientListener(final DataCollectorClient client)
    {
        client.addTelegramListener(new JavelinClientTelegramListener());
        SignalStateListener signalStateListener = new SignalStateListener();
        SignalChangeListener signalChangeListener = new SignalChangeListener();

        SummarySignalChangeListener summarySignalChangeListener = new SummarySignalChangeListener();

        client.addTelegramListener(signalStateListener);
        client.addTelegramListener(signalChangeListener);
        client.addTelegramListener(summarySignalChangeListener);

        ThreadDumpNotifyListener threadDumpNotifyListener = new ThreadDumpNotifyListener();
        client.addTelegramListener(threadDumpNotifyListener);

        ProfileNotifyListener profileNotifyListener = new ProfileNotifyListener();
        client.addTelegramListener(profileNotifyListener);

        ControlGetNotifyListener controlGetNotifyListener = new ControlGetNotifyListener();
        client.addTelegramListener(controlGetNotifyListener);

        ControlUpdateNotifyListener controlUpdateNotifyListener = new ControlUpdateNotifyListener();
        client.addTelegramListener(controlUpdateNotifyListener);
    }

    /**
        * Javelinと制御クライアントを紐付ける。
        * @param javelinClient Javelinクライアント
        * @param controlClient 制御クライアント
        */
    private void bindJavelinAndControlClient(final DataCollectorClient javelinClient,
        final DataCollectorClient controlClient)
    {
        // BottleneckEye->DataCollector->Javelin
        controlClient.addTelegramListener(new TelegramListener() {
            public Telegram receiveTelegram(final Telegram telegram)
            {
                Header objHeader = telegram.getObjHeader();

                if (objHeader.getByteTelegramKind() == BYTE_TELEGRAM_KIND_GET_DUMP
                    && objHeader.getByteRequestKind() == BYTE_REQUEST_KIND_REQUEST
                    || objHeader.getByteTelegramKind() == BYTE_TELEGRAM_KIND_SUMMARYSIGNAL_DEFINITION
                    && objHeader.getByteRequestKind() == BYTE_REQUEST_KIND_NOTIFY
                    || objHeader.getByteTelegramKind() == BYTE_TELEGRAM_KIND_GET
                    && objHeader.getByteRequestKind() == BYTE_REQUEST_KIND_REQUEST
                    || objHeader.getByteTelegramKind() == BYTE_TELEGRAM_KIND_RESET
                    && objHeader.getByteRequestKind() == BYTE_REQUEST_KIND_REQUEST
                    || objHeader.getByteTelegramKind() == BYTE_TELEGRAM_KIND_UPDATE_TARGET
                    && objHeader.getByteRequestKind() == BYTE_REQUEST_KIND_REQUEST
                    || objHeader.getByteTelegramKind() == BYTE_TELEGRAM_KIND_GET_PROPERTY
                    && objHeader.getByteRequestKind() == BYTE_REQUEST_KIND_REQUEST
                    || objHeader.getByteTelegramKind() == BYTE_TELEGRAM_KIND_UPDATE_PROPERTY
                    && objHeader.getByteRequestKind() == BYTE_REQUEST_KIND_REQUEST)
                {
                    sendTelegramToAgent(telegram, javelinClient);
                }
                else
                {
                    javelinClient.sendTelegram(telegram);
                }
                return null;
            }
        });

        // Javelin->DataCollector->BottleneckEye
        javelinClient.addTelegramListener(new TelegramListener() {
            public Telegram receiveTelegram(final Telegram telegram)
            {
                Header header = telegram.getObjHeader();
                byte telKind = header.getByteTelegramKind();
                byte reqKind = header.getByteRequestKind();

                if (telKind == BYTE_TELEGRAM_KIND_RESOURCENOTIFY
                    && reqKind == BYTE_REQUEST_KIND_RESPONSE)
                {
                    return null;
                }

                controlClient.sendTelegram(telegram);
                return null;
            }
        });
    }

    /**
     * Splite agentName from selected node
     * @param agentName
     * @return
     */
    private String splitAgentName(final String agentName)
    {
        String[] agentSplit = agentName.split("/");
        String spliteAgentName = "/" + agentSplit[1];
        for (int index = 2; index < AGENTINDEX; index++)
        {
            spliteAgentName += "/" + agentSplit[index];
        }
        return spliteAgentName;
    }

    /**
     * Send telegram to agent
     * @param telegram to send to agent
     * @param client destination of telegram
     */
    public void sendTelegramToAgent(final Telegram telegram, final DataCollectorClient client)
    {
        Body[] bodies = telegram.getObjBody();
        String agentName = bodies[bodies.length - 1].getStrItemName();
        if (agentName != null)
        {
            String[] agentSplit = AGENT_SPLIT_PATTERN.split(agentName);
            if (agentSplit.length < AGENTINDEX)
            {
                client.sendTelegram(telegram);
            }
            else
            {
                agentName = splitAgentName(agentName);
                //send telegram for only related agent
                if (agentName.equals(client.getAgentName()))
                {
                    client.sendTelegram(telegram);
                }
            }
        }
    }

    /**
     * JVNログ通知を処理するリスナ作成する。
     * 
     * @param dbName データベース名
     * @param hostName 接続先のホスト名
     * @param ipAddress 接続先のIPアドレス
     * @param port 接続先のポート番号
     * @param clientId クライアントID
     * @return 作成したJvnFileNotifyListener
     */
    private JvnFileNotifyListener createJvnFileNotifyListener(final String dbName,
        final String hostName, final String ipAddress, final int port, final String clientId,
        final String agentName)
    {
        JvnFileNotifyListener notifyListener = null;
        if (queue_ != null)
        {
            notifyListener = new JvnFileNotifyListener(queue_);
            notifyListener.setDatabaseName(dbName);
            notifyListener.setHostName(hostName);
            notifyListener.setIpAddress(ipAddress);
            notifyListener.setPort(port);
            notifyListener.setClientId(clientId);
            notifyListener.setAgentName(agentName);
        }
        return notifyListener;
    }

    /**
     * リソース通知を処理するリスナを作成する。
     * 
     * @param dbName データベース名
     * @param hostName 接続先のホスト名
     * @param ipAddress 接続先のIPアドレス
     * @param port 接続先のポート番号
     * @param clientId クライアントID
     * @return 作成したSystemResourceListener
     */
    private SystemResourceListener createSystemResourceListener(final String dbName,
        final String hostName, final String ipAddress, final int port, final String clientId,
        final String agentName)
    {
        SystemResourceListener notifyListener = null;
        if (queue_ != null)
        {
            notifyListener = new SystemResourceListener(queue_);
            notifyListener.setDatabaseName(dbName);
            notifyListener.setHostName(hostName);
            notifyListener.setIpAddress(ipAddress);
            notifyListener.setPort(port);
            notifyListener.setClientId(clientId);
            notifyListener.setAgentName(agentName);
        }
        return notifyListener;
    }

    /**
     * SqlPlanNotifyListenerを作成します。
     * 
     * @param hostName 接続先のホスト名
     * @param agentName Agent名
     * @param port 接続先のポート番号
     * @param dbName データベース名
     * @return SqlPlanNotifyListenerオブジェクト
     */
    private SqlPlanNotifyListener createSqlPlanNotifyListener(final String hostName,
        final String agentName, final int port, final String dbName)
    {
        SqlPlanNotifyListener notifyListener = new SqlPlanNotifyListener();

        notifyListener.setAgentName(agentName);
        notifyListener.setDatabaseName(dbName);

        return notifyListener;
    }

    /**
     * リソース通知を処理するリスナを作成する。
     * 
     * @param dbName データベース名
     * @param hostName 接続先のホスト名
     * @param ipAddress 接続先のIPアドレス
     * @param port 接続先のポート番号
     * @param clientId クライアントID
     * @return 作成したSystemResourceListener
     */
    private SystemResourceListener createSystemResourceNotifyListener(final String dbName,
        final String hostName, final String ipAddress, final int port, final String clientId,
        final String agentName)
    {
        SystemResourceListener notifyListener = null;
        if (queue_ != null)
        {
            notifyListener = new SystemResourceNotifyListener(queue_);
            notifyListener.setDatabaseName(dbName);
            notifyListener.setHostName(hostName);
            notifyListener.setIpAddress(ipAddress);
            notifyListener.setPort(port);
            notifyListener.setClientId(clientId);
            notifyListener.setAgentName(agentName);
        }
        return notifyListener;
    }

    /**
     * 応答電文を処理するリスナを作成する。
     * @param telegramKind 電文種別
     * @return 作成したリスナを返す。
     */
    private CommonResponseListener createResponseTelegramListener(final byte telegramKind)
    {
        return new CommonResponseListener(queue_, telegramKind);
    }

    /**
     * Javelinとの接続が確立するまでのダミー送信先を設定する。
     */
    private void setDummyTelegramSenders()
    {
        Set<String> keySet = notifyListenerMap_.keySet();

        for (String key : keySet)
        {
            List<TelegramNotifyListener> listenerList = notifyListenerMap_.get(key);
            for (TelegramNotifyListener notifyListener : listenerList)
            {
                notifyListener.setTelegramSender(this);
            }
        }
    }

    /**
     * 電文送信先を設定する。
     */
    private void setTelegramSenders()
    {
        Set<String> keySet = notifyListenerMap_.keySet();

        for (String key : keySet)
        {
            DataCollectorClient client = getJavelinClient(key);
            if (client != null)
            {
                List<TelegramNotifyListener> listenerList = notifyListenerMap_.get(key);
                for (TelegramNotifyListener notifyListener : listenerList)
                {
                    notifyListener.setTelegramSender(client);
                }
            }
        }
    }

    /**
     * Javelinクライアントをリストに登録する。
     * @param client 登録するJavelinクライアント
     */
    private void addJavelinClient(final DataCollectorClient client)
    {
        synchronized (javelinClientList_)
        {
            javelinClientList_.put(client.getAgentName(), client);
        }
    }

    /**
     * Javelinクライアントをリストから削除する。
     * @param dbName キーとなるDB名
     */
    private void removeJavelinClient(final String dbName)
    {
        synchronized (javelinClientList_)
        {
            javelinClientList_.remove(dbName);
        }
    }

    /**
     * DB名をキーとしてJavelinクライアントを取得する。
     * @param dbName キーとなるDB名
     * @return Javelinクライアントを返す。
     */
    private DataCollectorClient getJavelinClient(final String dbName)
    {
        synchronized (javelinClientList_)
        {
            return javelinClientList_.get(dbName);
        }
    }

    /**
     * 制御クライアントを追加する。
     * @param client 制御クライアント
     */
    private void addControlClient(final DataCollectorClient client)
    {
        synchronized (controlClientList_)
        {
            Set<DataCollectorClient> clientList = controlClientList_.get(dbName_);
            if (clientList == null)
            {
                clientList = new HashSet<DataCollectorClient>();
                clientList.add(client);
            }
            else
            {
                clientList.add(client);
            }
            controlClientList_.put(dbName_, clientList);
        }
    }

    /**
     * Javelinクライアントをリストから削除する。
     * @param dbName キーとなるDB名
     */
    private void removeControlClient(final String dbName)
    {
        synchronized (controlClientList_)
        {
            controlClientList_.remove(dbName);
        }
    }

    /**
     * DB名をキーとして制御クライアントを取得する。
     * @param dbName キーとなるDB名
     * @return 制御クライアント
     */
    private Set<DataCollectorClient> getControlClient(final String dbName)
    {
        synchronized (controlClientList_)
        {
            return controlClientList_.get(dbName);
        }
    }

    /**
     * 滞留させていた電文をすべて送信する。
     */
    private void sendWaitingTelegram(final DataCollectorClient client)
    {
        for (Telegram telegram : waitingTelegramList_)
        {
            client.sendTelegram(telegram);
        }
        waitingTelegramList_.clear();
    }

    /**
     * 接続しているDB名管理クライアントに追加したJavelinのDB名を送信します。
     */
    private void sendDatabaseName()
    {
        if (this.databaseAdminClient_ != null)
        {
            Set<String> databaseNameList = javelinClientList_.keySet();
            databaseAdminClient_.sendTelegram(ConnectNotifyAccessor
                .createAddDatabaseNameTelegram(databaseNameList));
        }
    }

    /**
     * DB名管理クライアントを取得します。
     * @return DB名管理クライアント
     */
    public DataCollectorClient getDatabaseAdminClient()
    {
        return databaseAdminClient_;
    }

    /**
     * DB名管理クライアントを設定します。
     * @param databaseAdminClient DB名管理クライアント
     */
    public void setDatabaseAdminClient(final DataCollectorClient databaseAdminClient)
    {
        databaseAdminClient_ = databaseAdminClient;
    }
}
