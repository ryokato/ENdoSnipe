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
package jp.co.acroquest.endosnipe.web.explorer.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.CommunicationClient;
import jp.co.acroquest.endosnipe.communicator.CommunicationFactory;
import jp.co.acroquest.endosnipe.communicator.entity.ConnectNotifyData;
import jp.co.acroquest.endosnipe.data.dao.MeasurementInfoDao;
import jp.co.acroquest.endosnipe.data.db.DBManager;
import jp.co.acroquest.endosnipe.data.db.DatabaseType;
import jp.co.acroquest.endosnipe.web.explorer.config.AgentSetting;
import jp.co.acroquest.endosnipe.web.explorer.config.ConfigurationReader;
import jp.co.acroquest.endosnipe.web.explorer.config.DataBaseConfig;
import jp.co.acroquest.endosnipe.web.explorer.constants.EventConstants;
import jp.co.acroquest.endosnipe.web.explorer.constants.LogMessageCodes;
import jp.co.acroquest.endosnipe.web.explorer.exception.InitializeException;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.AlarmNotifyListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.AlarmThreadDumpNotifyListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.CollectorListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.ControlGetPropertyListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.ControlUpdatePropertyListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.ProfileStatusListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.SignalStateChangeListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.SummarySignalStateChangeListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.TreeStateAddListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.collector.TreeStateDeleteListener;
import jp.co.acroquest.endosnipe.web.explorer.listener.javelin.JavelinNotifyListener;
import jp.co.acroquest.endosnipe.web.explorer.manager.ConnectionClient;
import jp.co.acroquest.endosnipe.web.explorer.manager.DatabaseManager;
import jp.co.acroquest.endosnipe.web.explorer.service.DashboardService;
import jp.co.acroquest.endosnipe.web.explorer.service.MultipleResourceGraphService;
import jp.co.acroquest.endosnipe.web.explorer.service.ReportService;
import jp.co.acroquest.endosnipe.web.explorer.service.SignalService;
import jp.co.acroquest.endosnipe.web.explorer.service.SummarySignalService;
import jp.co.acroquest.endosnipe.web.explorer.service.TreeMenuService;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.AgentInformationProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.AgentNotifyProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.AlarmNotifyStartProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.AlarmNotifyStopProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.AutoMeasurementEndProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.AutoMeasurementStartProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.AutoMeasurementStopProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.EventProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.GetReportListRequestProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.ResourceAlarmStartProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.ResourceAlarmStopProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.ResourceStateAllProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.TermAlarmNotifyProcessor;
import jp.co.acroquest.endosnipe.web.explorer.service.processor.TermMeasurementDataProcessor;
import jp.co.acroquest.endosnipe.web.explorer.template.TemplateCreator;
import jp.co.acroquest.endosnipe.web.explorer.template.TemplateLoader;
import jp.co.acroquest.endosnipe.web.explorer.template.meta.Template;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * クライアントから通知要求を受信するためのサーブレットです。
 * @author fujii
 *
 */
public class ExplorerNotifyServlet extends HttpServlet
{
    /** シリアルID */
    private static final long serialVersionUID = -6688090852275089760L;

    /** ロガー */
    private static final ENdoSnipeLogger LOGGER =
            ENdoSnipeLogger.getLogger(ExplorerNotifyServlet.class);

    /** テンプレートディレクトリのパス */
    private static final String TEMPLATE_DIR = "/WEB-INF" + File.separator + "classes"
            + File.separator + "dashboard" + File.separator + "template";

    /** テンプレートファイルの拡張子 */
    private static final String TEMPLATE_EXTENTION = ".xml";

    /** 空白文字 */
    private static final String BLANK = "";

    /** イベント処理クラスのMap */
    private final Map<Integer, EventProcessor> processorMap_ =
            new HashMap<Integer, EventProcessor>();

    /**
     * コンストラクタ
     */
    public ExplorerNotifyServlet()
    {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init()
        throws ServletException
    {
        ServletConfig servletConfig = getServletConfig();
        String filePath = servletConfig.getInitParameter("collector.property");

        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_START_AUTO_MEASUREMENT),
                               new AutoMeasurementStartProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_STOP_AUTO_MEASUREMENT),
                               new AutoMeasurementStopProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_END_AUTO_MEASUREMENT),
                               new AutoMeasurementEndProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_NOTIFY_TERM_MEASUREMENT_REQUEST),
                               new TermMeasurementDataProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_START_ALARM_NOTIFY),
                               new AlarmNotifyStartProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_STOP_ALARM_NOTIFY),
                               new AlarmNotifyStopProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_TERM_NOTIFY_ALARM_REQUEST),
                               new TermAlarmNotifyProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_START_RESOURCE_ALARM),
                               new ResourceAlarmStartProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_STOP_RESOURCE_ALARM),
                               new ResourceAlarmStopProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_RESOURCE_STATE_ALL_REQUEST),
                               new ResourceStateAllProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_AGENT_LIST_REQUEST),
                               new AgentInformationProcessor());
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_GET_AGENT_INFO),
                               new AgentNotifyProcessor());

        // レポート一覧を取得するイベントの処理
        // TODO Report画面用のServletを別に用意したほうがよいか、検討すること
        String reportDir = servletConfig.getInitParameter("report.directory");
        GetReportListRequestProcessor getReportListReqEvProc = new GetReportListRequestProcessor();
        getReportListReqEvProc.setReportDir(reportDir);
        this.processorMap_.put(Integer.valueOf(EventConstants.EVENT_REPORT_LIST_REQUEST),
                               getReportListReqEvProc);

        DataBaseConfig dbConfig = null;
        dbConfig = loadConfig(filePath);
        if (dbConfig == null)
        {
            return;
        }
        // DBの初期化
        setDatabase(dbConfig);

        DatabaseManager manager = DatabaseManager.getInstance();
        manager.setDataBaseConfig(dbConfig);

        connectDataCollector();

        // テンプレートの読み込み
        TemplateLoader templateLoader = new TemplateLoader();
        String templateDirPath = servletConfig.getServletContext().getRealPath(TEMPLATE_DIR);
        Map<String, Template> templates = templateLoader.loadAll(templateDirPath);

        ServletContext context = servletConfig.getServletContext();
        WebApplicationContext webContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        MultipleResourceGraphService multipleResourceGraphService =
                (MultipleResourceGraphService)webContext.getBean("multipleResourceGraphService");
        DashboardService dashboardService =
                (DashboardService)webContext.getBean("dashboardService");
        SignalService signalService = (SignalService)webContext.getBean("signalService");
        SummarySignalService summarySignalService =
                (SummarySignalService)webContext.getBean("summarySignalService");
        TreeMenuService treeMenuService = (TreeMenuService)webContext.getBean("treeMenuService");

        TemplateCreator templateCreator =
                new TemplateCreator(multipleResourceGraphService, dashboardService, signalService,
                                    summarySignalService, treeMenuService);

        for (Entry<String, Template> templateEntry : templates.entrySet())
        {
            String name = templateEntry.getKey();
            name = name.replaceAll(TEMPLATE_EXTENTION, BLANK);
            Template template = templateEntry.getValue();
            templateCreator.create(name, template);
        }

        // レポート出力用スレッドを起動させる。
        ReportService reportService = (ReportService)webContext.getBean("reportService");
        reportService.runThread();

        // コールバック関数を作成するために、初期化メソッドを呼び出す。
        MeasurementInfoDao.initialize();
    }

    /**
     * データベースの初期設定を行う。
     * @param config {@link DataBaseConfig}オブジェクト
     */
    private void setDatabase(final DataBaseConfig config)
    {

        String host = config.getDatabaseHost();

        String port = config.getDatabasePort();

        String dbName = config.getDatabaseName();

        String userName = config.getDatabaseUserName();

        String password = config.getDatabasePassword();

        String dbDir = config.getBaseDir();

        DatabaseType dbType = config.getDatabaseType();

        boolean useDefaultDb = true;
        if (dbType.getId().equals("postgres"))
        {
            useDefaultDb = false;
        }

        DBManager.updateSettings(useDefaultDb, dbDir, host, port, dbName, userName, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
    {
        doRequest(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
    {
        doRequest(request, response);
    }

    /**
     * クライアントから通知要求を受信するためのサーブレットです。
     * @param request {@link HttpServletRequest}オブジェクト
     * @param response {@link HttpServletResponse}オブジェクト
     */
    public void doRequest(final HttpServletRequest request, final HttpServletResponse response)
    {
        String eventId = request.getParameter(EventConstants.EVENT_ID);
        if (eventId == null)
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_EVENT_ID);
            return;
        }
        try
        {
            Integer eventIdInt = Integer.valueOf(eventId);
            EventProcessor processor = this.processorMap_.get(eventIdInt);
            if (processor == null)
            {
                LOGGER.log(LogMessageCodes.UNKNOWN_EVENT_ID, eventId);
                return;
            }
            //char setの設定を行う。
            ServletContext servletContext = getServletConfig().getServletContext();
            String charset = servletContext.getInitParameter(EventConstants.CHAR_SET_CODE);
            String context = EventConstants.HTML_SETTING_CODE + charset;
            response.setContentType(context);
            processor.process(request, response);
        }
        catch (NumberFormatException ex)
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_EVENT_ID, eventId);
        }

    }

    /**
     * 設定ファイルを読み込みます。
     * @param filePath ファイルパス
     * @return {@link DataBaseConfig}オブジェクト
     */
    private DataBaseConfig loadConfig(final String filePath)
    {
        DataBaseConfig config = null;
        try
        {
            config = ConfigurationReader.load(filePath);
        }
        catch (InitializeException ex)
        {
            LOGGER.log(LogMessageCodes.CANNOT_FIND_PROPERTY, filePath);
            return null;
        }
        catch (IOException ex)
        {
            LOGGER.log(LogMessageCodes.CANNOT_FIND_PROPERTY, filePath);
            return null;
        }
        if ("client".equals(config.getConnectionMode()))
        {
            List<AgentSetting> agentList = config.getAgentSettingList();
            if (agentList == null || agentList.size() == 0)
            {
                LOGGER.log(LogMessageCodes.CANNOT_FIND_HOST, filePath);
                return null;
            }
        }

        return config;
    }

    public void connectDataCollector()
    {
        //　通信用オブジェクトの作成
        DataBaseConfig dbConfig = null;

        // DBの設定が行われるのを待ち続ける。

        DatabaseManager manager = DatabaseManager.getInstance();
        dbConfig = manager.getDataBaseConfig();
        if (dbConfig == null)
        {
            return;
        }
        // client modeの場合、設定ファイルのエージェントごとに、threadを作成する
        if ("client".equals(dbConfig.getConnectionMode()))
        {

            List<AgentSetting> agentSettings = dbConfig.getAgentSettingList();

            ConnectionClient connectionClient = ConnectionClient.getInstance();
            List<CommunicationClient> clientList = connectionClient.getClientList();
            for (int cnt = 0; cnt < agentSettings.size(); cnt++)
            {
                AgentSetting setting = agentSettings.get(cnt);
                // DataCollectorに接続する。
                // TODO 複数エージェント対応・エラーチェック
                String javelinHost = setting.acceptHost_;
                int javelinPort = setting.acceptPort_;
                int agentId = cnt + 1;
                String clientId = createClientId(javelinHost, javelinPort);

                CommunicationClient client =
                        CommunicationFactory.getCommunicationClient("DataCollector-ClientThread-"
                                + clientId);

                client.init(javelinHost, javelinPort);
                client.addTelegramListener(new CollectorListener(agentId, setting.databaseName_));
                client.addTelegramListener(new AlarmNotifyListener(agentId));
                client.addTelegramListener(new SignalStateChangeListener());
                client.addTelegramListener(new SummarySignalStateChangeListener());
                client.addTelegramListener(new TreeStateAddListener());
                client.addTelegramListener(new TreeStateDeleteListener());
                client.addTelegramListener(new AlarmThreadDumpNotifyListener());
                client.addTelegramListener(new ProfileStatusListener());
                client.addTelegramListener(new ControlGetPropertyListener());
                client.addTelegramListener(new ControlUpdatePropertyListener());

                ConnectNotifyData connectNotify = new ConnectNotifyData();
                connectNotify.setKind(ConnectNotifyData.KIND_CONTROLLER);
                connectNotify.setPurpose(ConnectNotifyData.PURPOSE_GET_RESOURCE);
                connectNotify.setAgentName(setting.databaseName_);

                client.connect(connectNotify);
                clientList.add(client);
            }
        }
        else if ("server".equals(dbConfig.getConnectionMode()))
        {
            ConnectionClient connectionClient = ConnectionClient.getInstance();
            List<CommunicationClient> clientList = connectionClient.getClientList();

            CommunicationClient client =
                    CommunicationFactory.getCommunicationClient("DataCollector-JavelinNotify-Thread");
            client.init(dbConfig.getServerModeAgentSetting().acceptHost_,
                        dbConfig.getServerModeAgentSetting().acceptPort_);
            ConnectNotifyData connectNotify = new ConnectNotifyData();
            connectNotify.setKind(ConnectNotifyData.KIND_CONTROLLER);
            connectNotify.setPurpose(ConnectNotifyData.PURPOSE_GET_DATABASE);
            connectNotify.setAgentName("noDatabase");
            client.addTelegramListener(new JavelinNotifyListener());
            client.addTelegramListener(new AlarmThreadDumpNotifyListener());

            client.connect(connectNotify);
            clientList.add(client);
        }
    }

    /**
     * ホスト名とポート番号からクライアント ID を生成します。<br />
     *
     * @param host ホスト名
     * @param port ポート番号
     * @return クライアント ID
     */
    public static String createClientId(final String host, final int port)
    {
        return host + ":" + port;
    }

}
