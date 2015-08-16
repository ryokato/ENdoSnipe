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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jp.co.acroquest.endosnipe.collector.config.DataCollectorConfig;
import jp.co.acroquest.endosnipe.collector.config.RotateConfig;
import jp.co.acroquest.endosnipe.collector.converter.CPUConverter;
import jp.co.acroquest.endosnipe.collector.data.JavelinConnectionData;
import jp.co.acroquest.endosnipe.collector.data.JavelinData;
import jp.co.acroquest.endosnipe.collector.data.JavelinLogData;
import jp.co.acroquest.endosnipe.collector.data.JavelinMeasurementData;
import jp.co.acroquest.endosnipe.collector.data.JavelinMeasurementNotifyData;
import jp.co.acroquest.endosnipe.collector.log.JavelinLogUtil;
import jp.co.acroquest.endosnipe.collector.manager.SignalStateManager;
import jp.co.acroquest.endosnipe.collector.manager.SummarySignalStateManager;
import jp.co.acroquest.endosnipe.collector.notification.AlarmEntry;
import jp.co.acroquest.endosnipe.collector.notification.AlarmObserver;
import jp.co.acroquest.endosnipe.collector.notification.AlarmObserverFactory;
import jp.co.acroquest.endosnipe.collector.processor.AlarmData;
import jp.co.acroquest.endosnipe.collector.processor.AlarmProcessor;
import jp.co.acroquest.endosnipe.collector.processor.AlarmThresholdProcessor;
import jp.co.acroquest.endosnipe.collector.processor.AlarmType;
import jp.co.acroquest.endosnipe.collector.processor.SimilarSqlProcessor;
import jp.co.acroquest.endosnipe.collector.request.CommunicationClientRepository;
import jp.co.acroquest.endosnipe.collector.util.CollectorTelegramUtil;
import jp.co.acroquest.endosnipe.collector.util.ElasticSearchUtil;
import jp.co.acroquest.endosnipe.collector.util.MulResourceGraphUtil;
import jp.co.acroquest.endosnipe.collector.util.PerfDoctorMessages;
import jp.co.acroquest.endosnipe.common.Constants;
import jp.co.acroquest.endosnipe.common.entity.ItemType;
import jp.co.acroquest.endosnipe.common.entity.MeasurementData;
import jp.co.acroquest.endosnipe.common.entity.MeasurementDetail;
import jp.co.acroquest.endosnipe.common.entity.ResourceData;
import jp.co.acroquest.endosnipe.common.jmx.JMXManager;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.common.parser.JavelinLogAccessor;
import jp.co.acroquest.endosnipe.common.util.ResourceDataUtil;
import jp.co.acroquest.endosnipe.common.util.StreamUtil;
import jp.co.acroquest.endosnipe.communicator.accessor.ResourceNotifyAccessor;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.dao.JavelinLogDao;
import jp.co.acroquest.endosnipe.data.dao.JavelinMeasurementItemDao;
import jp.co.acroquest.endosnipe.data.dao.PerfDoctorResultDao;
import jp.co.acroquest.endosnipe.data.dto.MeasurementValueDto;
import jp.co.acroquest.endosnipe.data.dto.PerfDoctorResultDto;
import jp.co.acroquest.endosnipe.data.dto.SignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.dto.SummarySignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.entity.JavelinLog;
import jp.co.acroquest.endosnipe.data.entity.JavelinMeasurementItem;
import jp.co.acroquest.endosnipe.data.util.AccumulatedValuesDefinition;
import jp.co.acroquest.endosnipe.javelin.parser.JavelinLogElement;
import jp.co.acroquest.endosnipe.javelin.parser.JavelinParser;
import jp.co.acroquest.endosnipe.javelin.parser.ParseException;
import jp.co.acroquest.endosnipe.perfdoctor.PerfDoctor;
import jp.co.acroquest.endosnipe.perfdoctor.WarningUnit;
import jp.co.acroquest.endosnipe.perfdoctor.exception.RuleCreateException;
import jp.co.acroquest.endosnipe.perfdoctor.exception.RuleNotFoundException;
import jp.co.acroquest.endosnipe.util.InsertResult;
import jp.co.acroquest.endosnipe.util.ResourceDataDaoUtil;

/**
 * {@link JavelinData} をデータベースへ格納するためのクラスです。<br />
 *
 * @author y-komori
 * @author ochiai
 */
public class JavelinDataLogger implements Runnable, LogMessageCodes
{

    /** シグナルのレベル1 */
    private static final int SIGNAL_LEVEL_1 = 1;

    /** シグナルのレベル2 */
    private static final int SIGNAL_LEVEL_2 = 2;

    /** シグナルのレベル3 */
    private static final int SIGNAL_LEVEL_3 = 3;

    /** シグナルのレベル4 */
    private static final int SIGNAL_LEVEL_4 = 4;

    /** シグナルのレベル5 */
    private static final int SIGNAL_LEVEL_5 = 5;

    /** ロガー */
    private static final String JVN_LOG_ENCODING = "UTF-8";

    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger
        .getLogger(JavelinDataLogger.class);

    private final JavelinDataQueue queue_ = new JavelinDataQueue();

    /** 設定 */
    private final DataCollectorConfig config_;

    /** データベース名をキーに持つ、ローテート設定を保持するマップ */
    private final Map<String, RotateConfig> rotateConfigMap_;

    /** デフォルトのローテーと設定 */
    private RotateConfig defaultRotateConfig_;

    private final CommunicationClientRepository clientRepository_;

    private volatile boolean isRunnning_;

    /** 前回の計測値 */
    private final Map<String, ResourceData> prevResourceDataMap_ =
        new HashMap<String, ResourceData>();

    /** 前回の計測値(積算を差分に直したもの) */
    private final Map<String, ResourceData> prevConvertedResourceDataMap_ =
        new HashMap<String, ResourceData>();

    /**
     * Javelinから接続されたときのイベント。
     * 接続データを受け取った時にセットされ、接続前の、全てが0のデータを書き込む際に用いられる。
     * 書き込まれた後、このフィールドはnullに戻される。
     */
    private JavelinConnectionData connectionData_ = null;

    /** 同一SQL判定を行う処理 */
    private SimilarSqlProcessor similarSqlProcessor = null;

    /** 閾値判定処理を行う定義を保持したマップ */
    private final Map<String, AlarmProcessor> processorMap_ =
        new ConcurrentHashMap<String, AlarmProcessor>();

    /** 閾値レベル（正常） */
    public static final int NORMAL_ALARM_LEVEL = 0;

    /** 閾値レベル（監視停止中） */
    public static final int STOP_ALARM_LEVEL = -1;

    /** measurement_item_nameをスラッシュ区切りした際に、クラスタ名が入るIndex番号。 */
    private static final int CLUSTER_INDEX = 1;

    /** measurement_item_nameをスラッシュ区切りした際に、サーバ名が入るIndex番号。 */
    private static final int SERVER_INDEX = 2;

    /** measurement_item_nameをスラッシュ区切りした際に、エージェント名が入るIndex番号。 */
    private static final int AGENT_INDEX = 3;

    /** 性能情報のキーの区切り文字 */
    private static final String KEY_SEPARETOR = "/";

    /** 測定値の閾値超時のメッセージフォーマット */
    private static final String EXCEEDS_TAT_MESSAGES = "APP.MTRC.EXCD_TAT_message";

    /** 測定値の閾値下回り時のメッセージフォーマット */
    private static final String FALLS_TAT_MESSAGES = "APP.MTRC.FALL_TAT_message";

    /** アラーム通知を行う通知オブジェクト生成 */
    private final AlarmObserverFactory alarmObserverFactory_ = new AlarmObserverFactory();

    /**
     * 初期化を行います。
     *
     * @param config {@link DataCollectorConfig} オブジェクト
     * @param clientRepository {@link CommunicationClientRepository} オブジェクト
     * @param signalDefinitionMap 閾値判定定義情報のマップ
     * @param summarySignalDefinitionMap data of summary signal
     */
    public JavelinDataLogger(final DataCollectorConfig config,
        final CommunicationClientRepository clientRepository,
        final Map<Long, SignalDefinitionDto> signalDefinitionMap,
        final Map<Long, SummarySignalDefinitionDto> summarySignalDefinitionMap)
    {
        this.rotateConfigMap_ = new HashMap<String, RotateConfig>();
        this.config_ = config;
        this.clientRepository_ = clientRepository;
        SignalStateManager signalStateManager = SignalStateManager.getInstance();
        signalStateManager.setSignalDeifinitionMap(signalDefinitionMap);

        SummarySignalStateManager summarySignalStateManager =
            SummarySignalStateManager.getInstance();

        String dataBase = config_.getDatabaseName();
        summarySignalStateManager.setDataBaseName(dataBase);
        summarySignalStateManager.setSummarySignalDefinitionMap(summarySignalDefinitionMap);
        summarySignalStateManager.createAllSummarySignalMapValue();
        similarSqlProcessor = new SimilarSqlProcessor(config);
    }

    /**
     * 初期化を行います。
     *
     * @param config {@link DataCollectorConfig} オブジェクト
     * @param clientRepository {@link CommunicationClientRepository} オブジェクト
     * @param signalDefinitionMap 閾値判定定義情報のマップ
     */
    public JavelinDataLogger(final DataCollectorConfig config,
        final CommunicationClientRepository clientRepository,
        final Map<Long, SignalDefinitionDto> signalDefinitionMap)
    {
        this.rotateConfigMap_ = new HashMap<String, RotateConfig>();
        this.config_ = config;
        this.clientRepository_ = clientRepository;
        SignalStateManager signalStateManager = SignalStateManager.getInstance();
        signalStateManager.setSignalDeifinitionMap(signalDefinitionMap);
    }

    /**
     * ローテート設定を追加します。
     *
     * @param rotateConfig ローテート設定
     */
    public void addRotateConfig(final RotateConfig rotateConfig)
    {
        this.rotateConfigMap_.put(rotateConfig.getDatabase(), rotateConfig);
    }

    /**
     * デフォルトのローテート設定を設定します。
     *
     * @param rotateConfig ローテート設定
     */
    public void setDefaultRotateConfig(final RotateConfig rotateConfig)
    {
        this.defaultRotateConfig_ = rotateConfig;
    }

    /**
     * アラームデータを入れるキューを返します。
     *
     * @return キュー
     */
    public JavelinDataQueue getQueue()
    {
        return this.queue_;
    }

    /**
     * {@inheritDoc}
     */
    public void run()
    {
        while (true)
        {
            // 終了チェック
            if (this.isRunnning_ == false && this.queue_.size() == 0)
            {
                break;
            }

            // キューからデータを取り出す
            JavelinData data = this.queue_.take();

            if (data != null)
            {
                // データベースへ書き込み
                logJavelinData(data);
            }
        }

        LOGGER.log(JAVELIN_DATA_LOGGER_STOPPED);
    }

    /**
     * スレッドを停止します。<br />
     *
     * キューに書き込むべきデータが残っている場合、
     * すべてデータベースに書き込んでから終了します。<br />
     */
    public synchronized void stop()
    {
        LOGGER.log(JAVELIN_DATA_LOGGER_STOPPING);
        this.isRunnning_ = false;
    }

    /**
     * 初期化します。<br />
     *
     * @param rotateConfigList エージェント毎のローテート設定のリスト
     */
    protected synchronized void init(final List<RotateConfig> rotateConfigList)
    {
        LOGGER.log(JAVELIN_DATA_LOGGER_STARTED);

        this.rotateConfigMap_.clear();
        for (RotateConfig rotateConfig : rotateConfigList)
        {
            addRotateConfig(rotateConfig);
        }

        this.isRunnning_ = true;
    }

    /**
     * Javelin ログを書き込みます。<br />
     *
     * @param data Javelin ログ
     */
    private void logJavelinData(final JavelinData data)
    {
        if (data instanceof JavelinConnectionData)
        {
            JavelinConnectionData connectionData = (JavelinConnectionData)data;
            String database = connectionData.getDatabaseName();

            if (connectionData.isConnectionData())
            {
                // 接続イベントの場合、
                // それを保持しておき、次回のデータ取り出し時に、計測値が全て0のデータを加える。
                this.connectionData_ = connectionData;
            }
            else if (connectionData.isConnectionData() == false)
            {
                // 切断イベントの場合、計測値が全て0のデータを加える。
                // ただし、接続後すぐに切断した場合は前回のデータが無いので、処理は行わない。
                ResourceData resourceData = this.prevConvertedResourceDataMap_.get(database);
                if (resourceData != null)
                {
                    long measurementTime = connectionData.measurementTime;
                    ResourceData allZeroData =
                        ResourceDataUtil.createAllZeroResourceData(resourceData, measurementTime,
                                                                   false);
                    logResourceData(database, allZeroData, true);
                }
            }
        }
        else if (data instanceof JavelinLogData)
        {
            // Javelin ログの場合
            JavelinLogData logData = (JavelinLogData)data;
            String database = data.getDatabaseName();
            logJavelinLogData(database, logData);

            // Javelinログの判定処理
            alarmJavelinLogData(database, logData);
        }
        else if (data instanceof JavelinMeasurementData)
        {
            // 計測値データの場合
            ResourceData resourceData = ((JavelinMeasurementData)data).getResourceData();
            String database = data.getDatabaseName();
            if (config_.isJudgeSimilarSql())
            {
                similarSqlProcessor.convertSameSql(resourceData);
            }

            if (resourceData != null && resourceData.getMeasurementMap() != null)
            {

                // 接続後の最初のデータの場合、接続を表す（計測値が全て0の）データを直前に加える。
                if (this.connectionData_ != null
                    && resourceData.getMeasurementMap().isEmpty() == false)
                {
                    long measurementTime = this.connectionData_.measurementTime;
                    ResourceData allZeroData =
                        ResourceDataUtil.createAllZeroResourceData(resourceData, measurementTime,
                                                                   true);
                    logResourceData(database, allZeroData, true);
                    this.connectionData_ = null;
                }

                logResourceData(database, resourceData);
            }
        }
        else if (data instanceof JavelinMeasurementNotifyData)
        {
            // 計測値データの場合
            ResourceData resourceData = ((JavelinMeasurementNotifyData)data).getResourceData();
            String database = data.getDatabaseName();

            if (resourceData != null && resourceData.getMeasurementMap() != null)
            {
                this.logResourceData(database, resourceData, false, true);
            }
        }
        else
        {
            // 何もしない。
            "".toString();
        }
    }

    /**
     * JVNデータに対して、イベントを送信します。<br />
     *
     * @param database データベース名
     * @param logData {@link JavelinLogData}オブジェクト
     */
    void alarmJavelinLogData(final String database, final JavelinLogData logData)
    {
        JavelinLogAccessor accesor = new JavelinLogAccessor() {
            @Override
            public InputStream getInputStream()
                throws IOException
            {
                byte[] jvnLog = logData.getContents().getBytes(JVN_LOG_ENCODING);
                return new ByteArrayInputStream(jvnLog);
            }
        };
        JavelinParser parser = new JavelinParser(accesor);
        try
        {
            parser.init();
        }
        catch (ParseException ex)
        {
            String message = ex.getMessage();
            LOGGER.log(LogMessageCodes.FAIL_PARSE_JVN_DATA, ex, message);
            return;
        }

        List<JavelinLogElement> jvnLogElmList = new ArrayList<JavelinLogElement>();
        while (true)
        {
            try
            {
                JavelinLogElement element = parser.nextElement();
                if (element == null)
                {
                    break;
                }
                jvnLogElmList.add(element);

                // DataCollectorが参照する各種情報をセットする
                element.setIpAddress(logData.getIpAddress());
                element.setPort(logData.getPort());
                element.setDatabaseName(logData.getDatabaseName());
                element.setAlarmThreshold(logData.getAlarmThreshold());
                element.setCpuAlarmThreshold(logData.getCpuAlarmThreshold());
                element.setLogFileName(logData.getLogFileName());

                element.setMeasurementItemName(logData.getAgentName());

                String clientId = logData.getClientId();
                if (clientId == null || clientId.equals(""))
                {
                    clientId =
                        JavelinClient.createClientId(logData.getIpAddress(), logData.getPort());
                }
            }
            catch (IOException ex)
            {
                String message = ex.getMessage();
                LOGGER.log(LogMessageCodes.FAIL_PARSE_JVN_DATA, ex, message);
            }
            catch (ParseException ex)
            {
                String message = ex.getMessage();
                LOGGER.log(LogMessageCodes.FAIL_PARSE_JVN_DATA, ex, message);
            }
        }

        PerfDoctor perfDoctor = new PerfDoctor();
        List<WarningUnit> warningUnitList = new ArrayList<WarningUnit>();
        try
        {
            perfDoctor.init();
            warningUnitList = perfDoctor.judgeJavelinLog(jvnLogElmList);
        }
        catch (RuleNotFoundException ex)
        {
            LOGGER.log(LogMessageCodes.CANNOT_FIND_PERFRULE, ex);
        }
        catch (RuleCreateException ex)
        {
            LOGGER.log(LogMessageCodes.FAIL_TO_CREATE_PERFRULE, ex);
        }

        List<PerfDoctorResultDto> dtoList = new ArrayList<PerfDoctorResultDto>();

        for (WarningUnit warning : warningUnitList)
        {
            PerfDoctorResultDto dto = new PerfDoctorResultDto();
            dto.setOccurrenceTime(new Timestamp(warning.getStartTime()));
            dto.setDescription(warning.getDescription());
            dto.setLevel(warning.getLevel());
            dto.setClassName(warning.getClassName());
            dto.setMethodName(warning.getMethodName());
            dto.setLogFileName(warning.getLogFileName());
            dto.setMeasurementItemName(warning.getMeasurementItemName());
            dtoList.add(dto);
        }
        ElasticSearchUtil.sendElasticSearch(this.config_, warningUnitList);

        // 診断結果をPerfDoctorResultTableに登録
        //            PerfDoctorResultDao.insert(dbName, dtoList);
        for (PerfDoctorResultDto dto : dtoList)
        {
            try
            {
                PerfDoctorResultDao.insert(database, dto);
            }
            catch (SQLException ex)
            {
                LOGGER.log(ex);
            }
        }

    }

    /**
     * 指定されたデータをデータベースに登録します。<br />
     * @param database データベース名
     * @param resourceData 登録するデータ
     */
    private void logResourceData(final String database, final ResourceData resourceData,
        final boolean isConnectionData, final boolean direct)
    {
        if (direct)
        {
            try
            {
                this.insertMeasurementDataDirect(database, resourceData);
            }
            catch (SQLException ex)
            {
                LOGGER.log(DATABASE_ACCESS_ERROR, ex, ex.getMessage());
            }
        }
        else
        {
            this.logResourceData(database, resourceData, isConnectionData);
        }
    }

    /**
     * 指定されたデータをデータベースに登録します。<br />
     * @param database データベース名
     * @param resourceData 登録するデータ
     */
    private void logResourceData(final String database, final ResourceData resourceData)
    {
        this.logResourceData(database, resourceData, false, false);
    }

    /**
     * 指定されたデータをデータベースに登録します。<br />
     * @param database データベース名
     * @param resourceData 登録するデータ
     * @param isConnectionData 接続・切断データかどうか
     */
    private void logResourceData(final String database, final ResourceData resourceData,
        final boolean isConnectionData)
    {
        if (resourceData.getMeasurementMap().isEmpty())
        {
            return;
        }
        RotateConfig rotateConfig = this.rotateConfigMap_.get(database);
        if (rotateConfig == null)
        {
            rotateConfig = defaultRotateConfig_;
        }
        int rotatePeriod = rotateConfig.getMeasureRotatePeriod();
        int rotatePeriodUnit = rotateConfig.getMeasureUnitByCalendar();

        try
        {
            // 可変数系列で新たなデータが加わっている場合、グラフの始まりを表すデータを追加する。
            String prevDataKey = resourceData.clientId;
            ResourceData prevData = this.prevConvertedResourceDataMap_.get(prevDataKey);
            if (prevData != null)
            {
                ResourceData additionalData =
                    ResourceDataUtil.createAdditionalPreviousData(prevData, resourceData);

                if (additionalData.getMeasurementMap().size() > 0)
                {
                    insertMeasurementData(database, additionalData, rotatePeriod, rotatePeriodUnit);
                }
            }

            ResourceData convertedResourceData = resourceData;
            if (isConnectionData == false)
            {
                // 積算値が入っている場合、差分にする
                ResourceData prevResource = this.prevResourceDataMap_.get(prevDataKey);
                convertedResourceData = accumulatedValueParser(prevResource, resourceData);
            }

            // CPU使用率を計算し、データに加える。
            this.calculateAndAddCpuUsageData(database, convertedResourceData);

            // カバレッジを計算し、データに加える。
            this.calculateAndAddCoverageData(database, convertedResourceData);

            // JMXの計算を行う。
            this.convertJmxRatioData(database, convertedResourceData);

            insertMeasurementData(database, convertedResourceData, rotatePeriod, rotatePeriodUnit);

            alarmThresholdExceedance(database, convertedResourceData,
                                     this.prevConvertedResourceDataMap_.get(prevDataKey));

            if (isConnectionData == false)
            {
                if (resourceData.getMeasurementMap() != null
                    && resourceData.getMeasurementMap().size() != 0)
                {
                    this.prevResourceDataMap_.put(prevDataKey, resourceData);
                    this.prevConvertedResourceDataMap_.put(prevDataKey, convertedResourceData);
                }

                notifyResource(convertedResourceData);

            }
        }
        catch (SQLException ex)
        {
            LOGGER.log(DATABASE_ACCESS_ERROR, ex, ex.getMessage());
        }
    }

    private void
        insertMeasurementData(final String database, final ResourceData convertedResourceData,
            final int rotatePeriod, final int rotatePeriodUnit)
            throws SQLException
    {
        long startTime = System.currentTimeMillis();
        InsertResult result =
            ResourceDataDaoUtil.insert(database, convertedResourceData, rotatePeriod,
                                       rotatePeriodUnit, config_.getBatchSize(),
                                       config_.getItemIdCacheSize());
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        if (result.getInsertCount() != 0)
        {
            // IEDC0022=データベースに測定値を登録しました。 
            // データベース名:{0}、経過時間:{1}、登録件数:{2}、キャッシュヒット件数:{3}、キャッシュあふれ回数:{4}
            int cacheHitCount = result.getInsertCount() - result.getCacheMissCount();
            LOGGER.log("IEDC0022", database, elapsedTime, result.getInsertCount(), cacheHitCount,
                       result.getCacheOverflowCount());
        }

        String clientId = convertedResourceData.clientId;

        if (clientId == null || "".equals(clientId))
        {
            clientId =
                JavelinClient.createClientId(convertedResourceData.ipAddress,
                                             convertedResourceData.portNum);
        }

        List<MeasurementValueDto> measurementItemList = result.getMeasurementItemList();

        if (measurementItemList != null)
        {
            Telegram addTelegram = createAddTreeNodeTelegram(measurementItemList);
            this.clientRepository_.sendTelegramToClient(clientId, addTelegram);
            List<String> itemNameList = new ArrayList<String>();
            for (MeasurementValueDto measurementItem : measurementItemList)
            {
                itemNameList.add(measurementItem.measurementItemName);
            }
            MulResourceGraphUtil.checkMatchPattern(database, itemNameList, "add");
        }
    }

    /**
     * This method create the telegram for added tree node
     * 
     * @param measurementItemList list of added tree node
     * @return telegram of added tree node
     */
    private Telegram createAddTreeNodeTelegram(final List<MeasurementValueDto> measurementItemList)
    {
        Header responseHeader = new Header();

        responseHeader
            .setByteTelegramKind(TelegramConstants.BYTE_TELEGRAM_KIND_TREE_ADD_DEFINITION);
        responseHeader.setByteRequestKind(TelegramConstants.BYTE_REQUEST_KIND_NOTIFY);

        Telegram responseTelegram = new Telegram();
        int addedNodeCount = measurementItemList.size();

        Body measurementItemNameList = new Body();
        measurementItemNameList.setStrObjName(TelegramConstants.OBJECTNAME_TREE_CHANGE);
        measurementItemNameList.setStrItemName(TelegramConstants.ITEMNAME_TREE_ADD);
        measurementItemNameList.setByteItemMode(ItemType.ITEMTYPE_STRING);
        measurementItemNameList.setIntLoopCount(addedNodeCount);
        String[] measuremntItemNames = new String[addedNodeCount];

        for (int cnt = 0; cnt < addedNodeCount; cnt++)
        {
            MeasurementValueDto measurementValue = measurementItemList.get(cnt);
            String measurementItemName = measurementValue.measurementItemName;
            measuremntItemNames[cnt] = measurementItemName;
        }

        measurementItemNameList.setObjItemValueArr(measuremntItemNames);

        responseTelegram.setObjHeader(responseHeader);

        Body[] responseBodys = {measurementItemNameList};
        responseTelegram.setObjBody(responseBodys);

        return responseTelegram;
    }

    /**
     * This method create the telegram for the deleted tree node
     * 
     * @param database database name to access the database
     * @param deleteItemList list of deleted tree node
     * @return  telegram
     */
    public Telegram createDeleteTreeNodeTelegram(final String database,
        final List<Integer> deleteItemList)
    {
        Header responseHeader = new Header();
        responseHeader
            .setByteTelegramKind(TelegramConstants.BYTE_TELEGRAM_KIND_TREE_DELETE_DEFINITION);
        responseHeader.setByteRequestKind(TelegramConstants.BYTE_REQUEST_KIND_REQUEST);

        Telegram responseTelegram = new Telegram();

        int deletedNodeCount = deleteItemList.size();

        Body idList = new Body();
        idList.setStrObjName(TelegramConstants.OBJECTNAME_TREE_CHANGE);
        idList.setStrItemName(TelegramConstants.ITEMNAME_TREE_DELETE);
        idList.setByteItemMode(ItemType.ITEMTYPE_STRING);
        idList.setIntLoopCount(deletedNodeCount);
        String[] measurementItemNames = new String[deletedNodeCount];

        for (int cnt = 0; cnt < deletedNodeCount; cnt++)
        {
            JavelinMeasurementItem measurementItem;
            try
            {
                measurementItem =
                    JavelinMeasurementItemDao.selectById(database, deleteItemList.get(cnt));
                String id = measurementItem.itemName;

                measurementItemNames[cnt] = id;
            }
            catch (SQLException ex)
            {
                System.out.println("SQL exception");
                ex.printStackTrace();
            }

        }
        idList.setObjItemValueArr(measurementItemNames);
        responseTelegram.setObjHeader(responseHeader);

        Body[] responseBodys = {idList};

        responseTelegram.setObjBody(responseBodys);

        return responseTelegram;
    }

    private void insertMeasurementDataDirect(final String database,
        final ResourceData convertedResourceData)
        throws SQLException
    {
        long startTime = System.currentTimeMillis();
        InsertResult result =
            ResourceDataDaoUtil.insertDirect(database, convertedResourceData,
                                             config_.getBatchSize(), config_.getItemIdCacheSize());
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        if (result.getInsertCount() != 0)
        {
            // IEDC0022=データベースに測定値を登録しました。 
            // データベース名:{0}、経過時間:{1}、登録件数:{2}、キャッシュヒット件数:{3}、キャッシュあふれ回数:{4}
            int cacheHitCount = result.getInsertCount() - result.getCacheMissCount();
            LOGGER.log("IEDC0022", database, elapsedTime, result.getInsertCount(), cacheHitCount,
                       result.getCacheOverflowCount());
        }
    }

    /**
     * 直接割合値を計測している値に対して一定値をかけ、保存する。
     * @param database 
     * 
     * @param resourceData 返還対象の値。
     */
    private void convertJmxRatioData(final String database, final ResourceData resourceData)
        throws SQLException
    {
        // 変換対象の値を特定する。
        Set<String> jmxTypeSet = new HashSet<String>();
        for (String itemName : resourceData.getMeasurementMap().keySet())
        {
            // jmxの測定種別を判別する。
            if (itemName.indexOf("/jmx/") >= 0)
            {
                jmxTypeSet.add(itemName);
            }
        }

        // jmxがなければ何もしない。
        if (jmxTypeSet.size() == 0)
        {
            return;
        }

        // 変換対象の値を取り出す。
        // 一定値をかける。
        for (String itemName : jmxTypeSet)
        {
            Map<String, MeasurementDetail> detailMap = getMultiDetailValue(resourceData, itemName);
            if (detailMap == null)
            {
                continue;
            }

            // それぞれのkey値に対して割合を示すものがあるか捜査する。
            Set<Entry<String, MeasurementDetail>> datailEntrySet = detailMap.entrySet();
            for (Entry<String, MeasurementDetail> detailEntry : datailEntrySet)
            {
                String name = detailEntry.getKey();
                boolean isRatio = false;
                for (String ratioKey : JMXManager.JMX_RATIO_ITEMNAME_ARRAY)
                {
                    if (!name.contains(ratioKey))
                    {
                        continue;
                    }

                    isRatio = true;
                    break;
                }

                if (!isRatio)
                {
                    continue;
                }

                MeasurementDetail detail = detailMap.get(name);
                detail.value =
                    String.valueOf(Double.valueOf(detail.value) * ResourceDataUtil.PERCENT_CONST
                        * ResourceDataUtil.PERCENTAGE_DATA_MAGNIFICATION);
            }
        }
    }

    private void notifyResource(final ResourceData convertedResourceData)
    {
        Telegram resourceTelegram =
            ResourceNotifyAccessor.getResourceTelgram(convertedResourceData);

        String clientId = convertedResourceData.clientId;
        if (clientId == null || clientId.equals(""))
        {
            clientId =
                JavelinClient.createClientId(convertedResourceData.ipAddress,
                                             convertedResourceData.portNum);
        }

        this.clientRepository_.sendTelegramToClient(clientId, resourceTelegram);
    }

    /**
     * 指定されたリソースデータの値を用いてカバレッジを計算し、それをそのデータに追加します。<br />
     *
     * @param database データベース名
     * @param resourceData 登録するデータ
     * @throws SQLException
     */
    private void
        calculateAndAddCoverageData(final String database, final ResourceData resourceData)
            throws SQLException
    {
        // カバレッジの計算に必要な値を取得する。
        long calledMethodCount =
            getSingleDetailValue(resourceData, Constants.ITEMNAME_CALLEDMETHODCOUNT);
        long convertedMethodCount =
            getSingleDetailValue(resourceData, Constants.ITEMNAME_CONVERTEDMETHOD);

        // 値が取得できない場合、データの追加は行わない。
        if (calledMethodCount < 0 || convertedMethodCount < 0)
        {
            return;
        }

        double coverage = 0.0;
        if (convertedMethodCount > 0)
        {
            coverage =
                (double)calledMethodCount / convertedMethodCount * ResourceDataUtil.PERCENT_CONST;
        }

        Map<String, MeasurementData> measurementMap = resourceData.getMeasurementMap();
        String subKey = "";
        String[] temp = null;
        for (String key : measurementMap.keySet())
        {
            temp = key.split("/");
            subKey = "/" + temp[CLUSTER_INDEX] + "/" + temp[SERVER_INDEX] + "/" + temp[AGENT_INDEX];
            break;
        }
        // カバレッジの値が入ったデータを作成する。
        MeasurementData coverageData =
            calcCpuUsage(subKey + Constants.ITEMNAME_COVERAGE, coverage, database);

        // 作成したデータを、他のデータの入ったMapに追加する。
        if (!subKey.equals(""))
        {
            measurementMap.put(subKey + Constants.ITEMNAME_COVERAGE, coverageData);
        }
    }

    /**
     * 指定されたリソースデータの値を用いてCPU使用率を計算し、それをそのデータに追加します。<br />
     *
     * @param database データベース名
     * @param resourceData 登録するデータ
     * @throws SQLException
     */
    private void
        calculateAndAddCpuUsageData(final String database, final ResourceData resourceData)
            throws SQLException
    {
        Map<String, MeasurementData> measurementMap = resourceData.getMeasurementMap();
        String subKey = null;
        String[] temp = null;
        // CPU使用率の計算に必要な値を取得する。
        long processorCount =
            getSingleDetailValue(resourceData, Constants.ITEMNAME_SYSTEM_CPU_PROCESSOR_COUNT);
        for (String key : measurementMap.keySet())
        {
            temp = key.split("/");
            subKey = "/" + temp[CLUSTER_INDEX] + "/" + temp[SERVER_INDEX] + "/" + temp[AGENT_INDEX];
            break;
        }
        long resourceInterval = this.config_.getResourceInterval();
        String itemName = subKey + Constants.ITEMNAME_SYSTEM_CPU_USERMODE_TIME;
        long sysCputimeUser = getSingleDetailValue(resourceData, itemName);
        long sysCputimeSys =
            getSingleDetailValue(resourceData, subKey + Constants.ITEMNAME_SYSTEM_CPU_SYSTEM_TIME);
        long sysCputimeIoWait =
            getSingleDetailValue(resourceData, subKey + Constants.ITEMNAME_SYSTEM_CPU_IOWAIT_TIME);
        long sysCputimeTotal = sysCputimeUser + sysCputimeSys + sysCputimeIoWait;

        long procCputimeTotal =
            getSingleDetailValue(resourceData, subKey + Constants.ITEMNAME_PROCESS_CPU_TOTAL_TIME);
        long procCputimeSys =
            getSingleDetailValue(resourceData, subKey + Constants.ITEMNAME_PROCESS_CPU_SYSTEM_TIME);
        long procCputimeIoWait =
            getSingleDetailValue(resourceData, subKey + Constants.ITEMNAME_PROCESS_CPU_IOWAIT_TIME);
        long procCputimeUser = procCputimeTotal - procCputimeSys - procCputimeIoWait;

        if (-1 < sysCputimeUser && -1 < sysCputimeSys && -1 < processorCount)
        {
            double sysCpuusageUser =
                CPUConverter.calcCPUUsage(sysCputimeUser, resourceInterval, processorCount);
            double sysCpuusageSys =
                CPUConverter.calcCPUUsage(sysCputimeSys, resourceInterval, processorCount);
            double sysCpuusageIoWait =
                CPUConverter.calcCPUUsage(sysCputimeIoWait, resourceInterval, processorCount);
            double sysCpuusageTotal =
                CPUConverter.calcCPUUsage(sysCputimeTotal, resourceInterval, processorCount);

            MeasurementData sysCpuusageUserData =
                calcCpuUsage(subKey + Constants.ITEMNAME_SYSTEM_CPU_USER_USAGE, sysCpuusageUser,
                             database);
            MeasurementData sysCpuusageSysData =
                calcCpuUsage(subKey + Constants.ITEMNAME_SYSTEM_CPU_SYSTEM_USAGE, sysCpuusageSys,
                             database);
            MeasurementData sysCpuusageIoWaitData =
                calcCpuUsage(subKey + Constants.ITEMNAME_SYSTEM_CPU_IOWAIT_USAGE,
                             sysCpuusageIoWait, database);
            MeasurementData sysCpuusageTotalData =
                calcCpuUsage(subKey + Constants.ITEMNAME_SYSTEM_CPU_TOTAL_USAGE, sysCpuusageTotal,
                             database);

            measurementMap.put(subKey + Constants.ITEMNAME_SYSTEM_CPU_USER_USAGE,
                               sysCpuusageUserData);
            measurementMap.put(subKey + Constants.ITEMNAME_SYSTEM_CPU_SYSTEM_USAGE,
                               sysCpuusageSysData);
            measurementMap.put(subKey + Constants.ITEMNAME_SYSTEM_CPU_IOWAIT_USAGE,
                               sysCpuusageIoWaitData);
            measurementMap.put(subKey + Constants.ITEMNAME_SYSTEM_CPU_TOTAL_USAGE,
                               sysCpuusageTotalData);
        }

        // プロセスのCPU使用率の計算に必要な値が取得できている場合、そのデータを追加する。
        if (-1 < procCputimeTotal && -1 < procCputimeSys && -1 < processorCount)
        {
            double procCpuusageUser =
                CPUConverter.calcCPUUsage(procCputimeUser, resourceInterval, processorCount);
            double procCpuusageSys =
                CPUConverter.calcCPUUsage(procCputimeSys, resourceInterval, processorCount);
            double procCpuusageIoWait =
                CPUConverter.calcCPUUsage(procCputimeIoWait, resourceInterval, processorCount);
            double procCpuusageTotal =
                CPUConverter.calcCPUUsage(procCputimeTotal, resourceInterval, processorCount);

            MeasurementData procCpuusageUserData =
                calcCpuUsage(subKey + Constants.ITEMNAME_PROCESS_CPU_USER_USAGE, procCpuusageUser,
                             database);
            MeasurementData procCpuusageSysData =
                calcCpuUsage(subKey + Constants.ITEMNAME_PROCESS_CPU_SYSTEM_USAGE, procCpuusageSys,
                             database);
            MeasurementData procCpuusageIoWaitData =
                calcCpuUsage(subKey + Constants.ITEMNAME_PROCESS_CPU_IOWAIT_USAGE,
                             procCpuusageIoWait, database);
            MeasurementData procCpuusageTotalData =
                calcCpuUsage(subKey + Constants.ITEMNAME_PROCESS_CPU_TOTAL_USAGE,
                             procCpuusageTotal, database);

            measurementMap.put(subKey + Constants.ITEMNAME_PROCESS_CPU_USER_USAGE,
                               procCpuusageUserData);
            measurementMap.put(subKey + Constants.ITEMNAME_PROCESS_CPU_SYSTEM_USAGE,
                               procCpuusageSysData);
            measurementMap.put(subKey + Constants.ITEMNAME_PROCESS_CPU_IOWAIT_USAGE,
                               procCpuusageIoWaitData);
            measurementMap.put(subKey + Constants.ITEMNAME_PROCESS_CPU_TOTAL_USAGE,
                               procCpuusageTotalData);
        }
    }

    /**
     * 指定されたリソースデータの、指定されたmeasurementTypeを持つデータの値を返します。<br />
     * ただし、データが入っていない場合や、複数系列のものを指定した場合は、-1を返します。<br />
     *
     * @param resourceData リソースデータ
     * @param itemName itemName
     * @return 指定されたデータの値
     */
    private long getSingleDetailValue(final ResourceData resourceData, final String itemName)
    {
        long value = -1;
        Map<String, MeasurementData> measurementMap = resourceData.getMeasurementMap();
        for (String key : measurementMap.keySet())
        {
            if (key.endsWith(itemName))
            {
                MeasurementData measurementData = measurementMap.get(key);
                if (measurementData != null)
                {
                    Map<String, MeasurementDetail> measurementDetailMap;
                    measurementDetailMap = measurementData.getMeasurementDetailMap();
                    MeasurementDetail measurementDetail;
                    measurementDetail = measurementDetailMap.get(MeasurementData.SINGLE_DETAIL_KEY);
                    if (measurementDetail != null)
                    {
                        value = Long.valueOf(measurementDetail.value).longValue();
                    }
                }
                break;

            }

        }

        return value;
    }

    /**
     * 指定されたリソースデータの、指定されたmeasurementTypeを持つデータの値をdouble型で返します。<br />
     * ただし、データが入っていない場合や、複数系列のものを指定した場合は、-1を返します。<br />
     *
     * @param resourceData リソースデータ
     * @param measurementTypeMap item_nameとmeasurement_typeの対応を表すマップ
     * @param itemName itemName
     * @return 指定されたデータの値
     */
    private Map<String, MeasurementDetail> getMultiDetailValue(final ResourceData resourceData,
        final String itemName)
    {
        Map<String, MeasurementData> measurementMap = resourceData.getMeasurementMap();
        MeasurementData measurementData = measurementMap.get(itemName);
        Map<String, MeasurementDetail> measurementDetailMap = null;
        if (measurementData != null)
        {
            measurementDetailMap = measurementData.getMeasurementDetailMap();
        }
        return measurementDetailMap;
    }

    /**
     * CPU使用率のデータに対して、指定されたitemNameとvalueをMeasurementDetailとして持つ、<br />
     * MeasurementDataを生成し、返します。<br />
     * @param itemName itemName
     * @param cpuUsage データの値
     * @param database 
     *
     * @return MeasurementData
     */
    private MeasurementData calcCpuUsage(final String itemName, final double cpuUsage,
        final String database)
    {
        MeasurementDetail measurementDetail = new MeasurementDetail();
        // 小数点以下の値も保持するため、一定の倍率を掛ける。
        measurementDetail.value = String.valueOf(cpuUsage);
        measurementDetail.displayName = "";

        MeasurementData measurementData = new MeasurementData();
        measurementData.itemName = itemName;
        measurementData.measurementType =
            ResourceDataDaoUtil.getItemId(database, itemName, config_.getItemIdCacheSize());
        measurementData.valueType = TelegramConstants.BYTE_ITEMMODE_KIND_STRING;
        measurementData.getMeasurementDetailMap().put(MeasurementData.SINGLE_DETAIL_KEY,
                                                      measurementDetail);

        return measurementData;
    }

    /**
     * 閾値の超過、復旧でアラームを出します。
     * @param database DB名
     * @param currentResourceData 取得したリソース値（積算値は差分値に変換済みとする）
     * @param prevResourceData 前回取得したリソース値（積算値は差分値に変換済みとする）
     */
    private void alarmThresholdExceedance(final String database,
        final ResourceData currentResourceData, final ResourceData prevResourceData)
    {
        SignalStateManager signalStateManager = SignalStateManager.getInstance();
        SummarySignalStateManager summarySignalStateManager =
            SummarySignalStateManager.getInstance();

        List<AlarmEntry> alarmEntryList = new ArrayList<AlarmEntry>();

        Map<Long, SignalDefinitionDto> signalDefinitionMap =
            signalStateManager.getSignalDeifinitionMap();

        // クラスタ名、IPアドレス、エージェント名を取得する。
        // クラスタ名、IPアドレス、エージェント名が取得できない場合は閾値判定ができないため、処理を終了する。
        String domain = getDomain(currentResourceData);
        if (domain == null)
        {
            return;
        }

        for (Entry<Long, SignalDefinitionDto> signalDefinitionEntry : signalDefinitionMap
            .entrySet())

        {
            SignalDefinitionDto signalDefinition = signalDefinitionEntry.getValue();
            String itemName = signalDefinition.getMatchingPattern();
            long signalId = signalDefinition.getSignalId();

            //現在のアラーム通知状況を取得
            AlarmData currentAlarmData = signalStateManager.getAlarmData(signalId);
            if (currentAlarmData == null)
            {
                currentAlarmData = new AlarmData();
                signalStateManager.addAlarmData(signalId, currentAlarmData);
            }
            AlarmProcessor processor = getAlarmProcessor();

            if (processor == null)
            {
                continue;
            }
            AlarmEntry alarmEntry =
                processor.calculateAlarmLevel(currentResourceData, prevResourceData,
                                              signalDefinition, currentAlarmData);
            if (alarmEntry == null)
            {
                continue;
            }
            // DataCollectorが参照する情報をセットする
            alarmEntry.setIpAddress(currentResourceData.ipAddress);
            alarmEntry.setPort(currentResourceData.portNum);
            alarmEntry.setDatabaseName(database);
            signalStateManager.addAlarmData(signalId, currentAlarmData);
            alarmEntry.setDefinition(signalDefinition);
            // アラーム通知処理
            if (alarmEntry.isSendAlarm())
            {
                alarmEntryList.add(alarmEntry);
            }
        }

        notifyClient(currentResourceData, summarySignalStateManager, alarmEntryList);
    }

    private void notifyClient(final ResourceData currentResourceData,
        final SummarySignalStateManager summarySignalStateManager,
        final List<AlarmEntry> alarmEntryList)
    {
        // 閾値超過アラームをクライアントに通知する。
        String clientId = currentResourceData.clientId;

        if (alarmEntryList != null && alarmEntryList.size() != 0)
        {
            if (clientId == null || clientId.equals(""))
            {
                clientId =
                    JavelinClient.createClientId(currentResourceData.ipAddress,
                                                 currentResourceData.portNum);
            }
            Telegram alarmTelegram = CollectorTelegramUtil.createAlarmTelegram(alarmEntryList);
            this.clientRepository_.sendTelegramToClient(clientId, alarmTelegram);
            for (AlarmEntry alarmEntry : alarmEntryList)
            {
                addSignalStateChangeEvent(alarmEntry);
                List<AlarmObserver> observerList =
                    alarmObserverFactory_.getAlarmObserverList(alarmEntry);
                for (AlarmObserver observer : observerList)
                {
                    observer.send(alarmEntry);
                }
            }
        }

        Map<Long, SummarySignalDefinitionDto> summarySignalDefinitionMap =
            summarySignalStateManager.getSummarySignalDefinitionMap();
        List<SummarySignalDefinitionDto> updatedSummarySignals =
            new ArrayList<SummarySignalDefinitionDto>();
        for (Entry<Long, SummarySignalDefinitionDto> entry : summarySignalDefinitionMap.entrySet())
        {
            SummarySignalDefinitionDto summarySignalDefinitionDto = entry.getValue();

            //サマリシグナルを構成するシグナルに更新が無い場合は何もしない
            if (!hasSignal(summarySignalDefinitionDto, alarmEntryList))
            {
                return;
            }

            int summaryLevel =
                summarySignalStateManager.getSummaryLevel(summarySignalDefinitionDto);

            Long summarySignalId = summarySignalDefinitionDto.summarySignalId_;
            int previousLevel = summarySignalStateManager.getSignalLevel(summarySignalId);
            if (previousLevel != summaryLevel)
            {
                summarySignalDefinitionDto.setSummarySignalStatus(summaryLevel);
                summarySignalDefinitionDto.setPriority(0);
                updatedSummarySignals.add(summarySignalDefinitionDto);
            }
            summarySignalStateManager.setSignalLevel(summarySignalId, summaryLevel);
        }
        if (updatedSummarySignals.size() != 0)
        {
            Telegram alarmSummaryTelegram =
                CollectorTelegramUtil
                    .createSummarySignalResponseTelegram(updatedSummarySignals,
                                                         TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_CHANGE_STATE);
            this.clientRepository_.sendTelegramToClient(clientId, alarmSummaryTelegram);
        }
    }

    /**
     * サマリシグナルを構成するシグナル郡に更新が発生したものが含まれるか判定する
     * @param dto サマリシグナルのDTO
     * @param entries 更新通知のリスト
     * @return 含まれるか否か
     */
    private boolean hasSignal(final SummarySignalDefinitionDto dto, final List<AlarmEntry> entries)
    {
        for (AlarmEntry entry : entries)
        {
            if (!dto.signalList_.contains(entry.getSignalName()))
            {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * リソース情報からエージェントの監視対象のクラスタ名、IPアドレス、エージェント名を取得する。<br />
     * クラスタ名、IPアドレス、エージェント名はスラッシュ区切りとする。
     * @param resourceData リソース情報
     * @return リソース情報から取得したエージェントの監視対象のクラスタ名、IPアドレス、エージェント名(スラッシュ区切り)
     */
    private String getDomain(final ResourceData resourceData)
    {
        if (resourceData == null)
        {
            return null;
        }
        Map<String, MeasurementData> resourceMap = resourceData.getMeasurementMap();
        if (resourceMap.size() == 0)
        {
            return null;
        }
        for (String key : resourceMap.keySet())
        {
            int firstKeyPosition = key.indexOf(KEY_SEPARETOR);
            if (firstKeyPosition < 0)
            {
                break;
            }
            int secondKeyPosition = key.indexOf(KEY_SEPARETOR, firstKeyPosition + 1);
            if (secondKeyPosition < 0)
            {
                break;
            }
            int thirdKeyPosition = key.indexOf(KEY_SEPARETOR, secondKeyPosition + 1);
            if (thirdKeyPosition < 0)
            {
                break;
            }
            int forthKeyPosition = key.indexOf(KEY_SEPARETOR, thirdKeyPosition + 1);
            if (forthKeyPosition < 0)
            {
                break;
            }
            String domain = key.substring(0, forthKeyPosition + 1);
            return domain;
        }

        return null;
    }

    /**
     * 閾値超過・回復イベントを発生させ、PerformanceDoctor診断結果テーブルに追加します。
     * @param alarmEntry 閾値超過・回復の情報
     */
    private void addSignalStateChangeEvent(final AlarmEntry alarmEntry)
    {
        PerfDoctorResultDto signalStateChangeEvent = new PerfDoctorResultDto();
        String level = "INFO";
        String exceededLevel = "INFO";
        double threshold = 0;
        String description = "";
        if (alarmEntry.getAlarmType().equals(AlarmType.FAILURE))
        {
            if (alarmEntry.getSignalValue() != 0)
            {
                threshold =
                    alarmEntry.getDefinition().getThresholdMaping()
                        .get(alarmEntry.getSignalValue());
            }
            if (alarmEntry.getSignalLevel() == SIGNAL_LEVEL_3)
            {
                if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_1)
                {
                    level = "WARN";
                    exceededLevel = "WARNING";
                }
                else if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_2)
                {
                    level = "ERROR";
                    exceededLevel = "CRITICAL";
                }
            }
            else if (alarmEntry.getSignalLevel() == SIGNAL_LEVEL_5)
            {
                if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_1)
                {
                    level = "WARN";
                    exceededLevel = "INFO";
                }
                else if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_2)
                {
                    level = "WARN";
                    exceededLevel = "WARNING";
                }
                else if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_3)
                {
                    level = "ERROR";
                    exceededLevel = "ERROR";
                }
                else if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_4)
                {
                    level = "ERROR";
                    exceededLevel = "CRITICAL";
                }
            }

            // 警告の概要を取得
            description =
                PerfDoctorMessages.getMessage(EXCEEDS_TAT_MESSAGES, new Object[]{threshold,
                    alarmEntry.getAlarmValue(), exceededLevel});
        }
        else
        {
            // 回復した閾値の内、最も小さいものを取得
            threshold =
                alarmEntry.getDefinition().getThresholdMaping()
                    .get(alarmEntry.getSignalValue() + 1);

            // 警告の概要を取得
            description =
                PerfDoctorMessages.getMessage(FALLS_TAT_MESSAGES, new Object[]{threshold,
                    alarmEntry.getAlarmValue(), exceededLevel});
        }

        signalStateChangeEvent.setLevel(level);
        signalStateChangeEvent.setOccurrenceTime(new Timestamp(System.currentTimeMillis()));
        signalStateChangeEvent.setDescription(description);
        signalStateChangeEvent.setMeasurementItemName(alarmEntry.getSignalName());
        signalStateChangeEvent.setClassName(alarmEntry.getSignalName());
        try
        {
            PerfDoctorResultDao.insert(alarmEntry.getDatabaseName(), signalStateChangeEvent);
        }
        catch (SQLException ex)
        {
            LOGGER.log(ex.getMessage());
        }

        ElasticSearchUtil.sendElasticSearch(this.config_, signalStateChangeEvent);
    }

    /**
     * 閾値判定処理を行うオブジェクトを取得する。
     * @return 閾値判定処理を行うオブジェクト
     */
    private AlarmProcessor getAlarmProcessor()
    {
        // 現在は、閾値超過のアラーム判定しかないが、判定ロジックを追加するために、
        // 引数にはSignalDefinitionDtoを設定する。
        String key = "default";
        AlarmProcessor processor = this.processorMap_.get(key);

        if (processor == null)
        {
            processor = new AlarmThresholdProcessor();
            this.processorMap_.put(key, processor);
        }
        return processor;
    }

    /**
     * JVNログデータを保存します。<br />
     *
     * @param database データベース名
     * @param logData {@link JavelinLogData}オブジェクト
     */
    private void logJavelinLogData(final String database, final JavelinLogData logData)
    {
        RotateConfig rotateConfig = this.rotateConfigMap_.get(database);
        if (rotateConfig == null)
        {
            rotateConfig = defaultRotateConfig_;
        }
        String clientId = logData.getClientId();

        JavelinLog javelinLog = createJavelinLog(logData);
        try
        {
            JavelinLogDao.insert(database, javelinLog, true);
            Telegram telegram = createThreadDumpResponseTelegram();
            this.clientRepository_.sendTelegramToClient(clientId, telegram);
        }
        catch (SQLException ex)
        {
            LOGGER.log(DATABASE_ACCESS_ERROR, ex, ex.getMessage());
        }
        StreamUtil.closeStream(javelinLog.javelinLog);

        // 一時ファイルがある場合は削除しておく
        logData.deleteFile();
    }

    /**
     * this is createTheadDumpResponseTelegram
     * @return telegram data
     */
    private Telegram createThreadDumpResponseTelegram()
    {
        Header responseHeader = new Header();

        responseHeader.setByteTelegramKind(TelegramConstants.BYTE_TELEGRAM_KIND_THREAD_DUMP);
        responseHeader.setByteRequestKind(TelegramConstants.BYTE_REQUEST_KIND_NOTIFY);

        Telegram responseTelegram = new Telegram();

        responseTelegram.setObjHeader(responseHeader);

        Body[] responseBodys = {};
        responseTelegram.setObjBody(responseBodys);

        return responseTelegram;

    }

    /**
     * Javelin ログを、データベースに書き込む形に変換します。<br />
     *
     * @param javelinLogData Javelin ログ
     * @return データベースに書き込む Javelin ログオブジェクト。
     *         ホスト情報が取得できない場合は <code>hostId</code> が <code>-1</code>
     */
    protected JavelinLog createJavelinLog(final JavelinLogData javelinLogData)
    {
        JavelinLog javelinLog = new JavelinLog();
        javelinLog.logFileName = javelinLogData.getLogFileName();
        javelinLog.javelinLog = createContentInputStream(javelinLogData);
        javelinLog.measurementItemName = javelinLogData.getAgentName();
        BufferedReader reader =
            StreamUtil.getBufferedReader(createContentInputStream(javelinLogData));
        try
        {
            String line = reader.readLine();
            List<String> elemList = JavelinLogUtil.csvTokenizeHeader(line);

            // ファイルの1行目を解析して各種属性を設定する
            JavelinLogUtil.parse(javelinLog, elemList);

            // durationを読み込む。
            while ((line = reader.readLine()) != null)
            {
                int beginIndex = line.indexOf(JavelinLogUtil.DURATION_KEY);
                if (beginIndex != 0 || line.length() < JavelinLogUtil.DURATION_KEY.length())
                {
                    continue;
                }
                String durationStr = line.substring(JavelinLogUtil.DURATION_KEY.length());
                JavelinLogUtil.setDuration(javelinLog, durationStr);
                break;
            }
        }
        catch (IOException ex)
        {
            LOGGER.log(IO_EXCEPTION_OCCURED, ex, ex.getMessage());
        }
        finally
        {
            StreamUtil.closeStream(reader);
        }

        return javelinLog;
    }

    /**
     * Javelin ログ内容を {@link InputStream} で返します。<br />
     *
     * @param javelinLogData Javelin ログ
     * @return ログ内容の {@link InputStream}
     */
    protected InputStream createContentInputStream(final JavelinLogData javelinLogData)
    {
        InputStream is = null;
        String contents = javelinLogData.getContents();
        if (contents != null)
        {
            // Javelin ログを文字列で保持している場合
            is = new ByteArrayInputStream(contents.getBytes());
        }
        else
        {
            // Javelin ログをファイルで保持している場合
            is = StreamUtil.getStream(javelinLogData.getFile());
        }
        return is;
    }

    /**
     * データベース名を生成します。<br />
     *
     * @param data Javelin ログ
     * @return データベース名
     */
    protected String createDatabaseName(final JavelinData data)
    {
        final int BUFFER = 32;
        StringBuilder builder = new StringBuilder(BUFFER);
        builder.append("javelin_");
        builder.append(data.getHost());
        builder.append("_");
        builder.append(data.getIpAddress());
        builder.append("_");
        builder.append(data.getPort());
        return builder.toString();
    }

    /**
     * 積算値で入っている値を差分値に変換したものを返す
     * @param prevResourceData 前回の計測値
     * @param resourceData 今回の計測値
     *
     * @return 積算値は差分値に変え、そうでな値はそのままにコピーしたResourceData
     */
    private static ResourceData accumulatedValueParser(final ResourceData prevResourceData,
        final ResourceData resourceData)
    {
        ResourceData newResourceData = new ResourceData();
        newResourceData.measurementTime = resourceData.measurementTime;
        newResourceData.hostName = resourceData.hostName;
        newResourceData.ipAddress = resourceData.ipAddress;
        newResourceData.portNum = resourceData.portNum;
        newResourceData.clientId = resourceData.clientId;

        // ResourceData を、積算値は差分値に変え、そうでな値はそのままにコピーする
        for (MeasurementData measurementData : resourceData.getMeasurementMap().values())
        {
            MeasurementData newMeasurementData = new MeasurementData();
            newMeasurementData.measurementType = measurementData.measurementType;
            newMeasurementData.itemName = measurementData.itemName;
            newMeasurementData.measurementTime = measurementData.measurementTime;
            newMeasurementData.valueType = measurementData.valueType;
            newMeasurementData.displayName = measurementData.displayName;

            // MeasurementData を、積算値は差分値に変え、そうでな値はそのままにコピーする
            for (MeasurementDetail detail : measurementData.getMeasurementDetailMap().values())
            {
                MeasurementDetail newMeasurementDetail = new MeasurementDetail();
                newMeasurementDetail.value = detail.value;
                newMeasurementDetail.displayName = detail.displayName;
                newMeasurementDetail.itemId = detail.itemId;
                newMeasurementDetail.itemName = detail.itemName;
                newMeasurementDetail.itemNum = detail.itemNum;
                newMeasurementDetail.type = detail.type;
                newMeasurementDetail.typeItemName = detail.typeItemName;
                newMeasurementDetail.valueId = detail.valueId;

                // 積算値は差分値に変える
                if (AccumulatedValuesDefinition.isAccumulatedValue(measurementData.itemName,
                                                                   detail.displayType))
                {
                    long prevMeasurementValue =
                        getPrevValue(prevResourceData, measurementData, detail);
                    long resultValue =
                        Long.valueOf(detail.value).longValue() - prevMeasurementValue;
                    if (resultValue < 0)
                    {
                        newMeasurementDetail.value = "0";
                    }
                    else
                    {
                        newMeasurementDetail.value = String.valueOf(resultValue);
                    }
                }
                newMeasurementData.addMeasurementDetail(newMeasurementDetail);
            }
            newResourceData.addMeasurementData(newMeasurementData);
        }
        return newResourceData;
    }

    /**
     * measurementData.measurementType に対応する1つ前の計測値を得る
     * @param prevResourceData 1つ前の計測値
     * @param measurementData 計測値
     * @param defaultValue 1つ前の計測値が存在しなかった場合の値
     * @return
     */
    private static long getPrevValue(final ResourceData prevResourceData,
        final MeasurementData measurementData, final MeasurementDetail defaultValue)
    {
        if (prevResourceData == null)
        {
            return Long.valueOf(defaultValue.value).longValue();
        }
        Map<String, MeasurementData> measurementMap = prevResourceData.getMeasurementMap();
        if (measurementMap == null)
        {
            return Long.valueOf(defaultValue.value).longValue();
        }
        MeasurementData prevMeasurementData = measurementMap.get(measurementData.itemName);
        if (prevMeasurementData == null)
        {
            return Long.valueOf(defaultValue.value).longValue();
        }
        Map<String, MeasurementDetail> measurementDetailMap =
            prevMeasurementData.getMeasurementDetailMap();
        if (measurementDetailMap == null)
        {
            return Long.valueOf(defaultValue.value).longValue();
        }
        MeasurementDetail measurementDetail = measurementDetailMap.get(defaultValue.displayName);
        if (measurementDetail == null)
        {
            return Long.valueOf(defaultValue.value).longValue();
        }
        long prevMeasurementValue = Long.valueOf(measurementDetail.value).longValue();
        return prevMeasurementValue;
    }

    /**
     * メールの送信を行う。
     * @param alarmEntry {@link AlarmEntry}
     */
    private void sendMail(final AlarmEntry alarmEntry)
    {

    }

}
