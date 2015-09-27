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
package jp.co.acroquest.endosnipe.collector.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.acroquest.endosnipe.data.db.DatabaseType;

/**
 * ENdoSnipe DataCollector の設定/定数を保持するクラスです。<br />
 * 
 * @author y-komori
 */
public class DataCollectorConfig
{
    /** Javelinログを分割するかどうか */
    private boolean isLogSplit_ = DEF_IS_LOG_SPLIT;

    /** Javelinログを分割保存する場合の１レコードあたりの最大サイズ */
    private int logSplitSize_ = DEF_LOG_SPLIT_SIZE;

    /** Javelinログを分割する場合の閾値 */
    private int logSplitThreshold_ = DEF_LOG_SPLIT_THRESHOLD;

    /** Javelinログ最大蓄積件数のキー */
    public static final String JAVELIN_LOG_MAX_KEY = "javelin.log.max.record";

    /** Javelinログ最大蓄積件数のデフォルト値 */
    public static final int DEF_JAVELIN_LOG_MAX = 260000;

    /** Javelinログ最大蓄積件数の最小値 */
    public static final int MIN_JAVELIN_LOG_MAX = 1;

    /** 計測データ最大蓄積件数のキー */
    public static final String MEASUREMENT_LOG_MAX_KEY = "measurement.log.max.record";

    /** 計測データ最大蓄積件数のデフォルト値 */
    public static final int DEF_MEASUREMENT_LOG_MAX = 870000;

    /** 計測データ最大蓄積件数の最小値 */
    public static final int MIN_MEASUREMENT_LOG_MAX = 1;

    /** データベースの種類 */
    private DatabaseType databaseType_ = DEF_DATABASE_TYPE;

    /** データベースディレクトリ */
    private String baseDir_ = DEF_DATABASE_DIR;

    /** データベースのホストアドレス */
    private String databaseHost_ = DEF_DATABASE_HOST;

    /** データベースのポート番号 */
    private String databasePort_ = DEF_DATABASE_PORT;

    /** データベース名 */
    private String databaseName_ = DEF_DATABASE_NAME;

    /** データベースログインユーザ名 */
    private String databaseUserName_ = DEF_DATABASE_USER;

    /** データベースログインパスワード */
    private String databasePassword_ = DEF_DATABASE_PASSWORD;

    /** リソースモニタリングの設定ファイル名 */
    private String resourceMonitoringConf_ = DEF_RESOURCE_MONITORING_CONF;

    /** レポートを出力するディレクトリ。 */
    private String reportOutputPath_ = DEF_REPORT_OUTPUT_PATH;

    /** 接続モード */
    private String connectionMode_ = DEF_MODE;

    /** 待ち受けホスト */
    private String acceptHost_ = DEF_ACCEPT_HOST;

    /** 待ち受けポート */
    private int acceptPort_ = DEF_ACCEOT_PORT;

    /** SSL通信：SSLを行うかどうか（Javelin） */
    private boolean sslEnable_ = false;

    /** SSL通信：keyStoreのパス */
    private String sslKeyStore_ = "";

    /** SSL通信：keyStoreのパスワード */
    private String sslKeyStorePass_ = "";

    /** SSL通信：trustStoreのパス */
    private String sslTrustStore_ = "";

    /** SSL通信：trustStoreのパスワード */
    private String sslTrustStorePass_ = "";

    /** Javelinログの最大蓄積期間 */
    private String jvnLogStoragePeriod_ = DEF_MEASUREMENT_LOG_STORAGE_PERIOD;

    /** 計測データの最大蓄積期間 */
    private String measurementLogStoragePeriod_ = DEF_MEASUREMENT_LOG_STORAGE_PERIOD;

    /** 同一SQL判定フラグ */
    private boolean judgeSimilarSql_ = DEF_JUDGE_SIMILAR_SQL;

    /** Agent毎の設定を保持するリスト */
    private final List<AgentSetting> agentSttingList_ = new ArrayList<AgentSetting>();

    /** データベースの種類のデフォルト値 */
    private static final DatabaseType DEF_DATABASE_TYPE = DatabaseType.H2;

    /** データベース保存先ベースディレクトリのデフォルト値 */
    private static final String DEF_DATABASE_DIR = "../data";

    /** データベースのホストアドレスのデフォルト値 */
    private static final String DEF_DATABASE_HOST = "localhost";

    /** データベースのポート番号のデフォルト値 */
    private static final String DEF_DATABASE_PORT = "5432";

    /** データベース名のデフォルト値 */
    private static final String DEF_DATABASE_NAME = "endosnipedb";

    /** データベースログインユーザ名のデフォルト値 */
    private static final String DEF_DATABASE_USER = "";

    /** データベースログインパスワードのデフォルト値 */
    private static final String DEF_DATABASE_PASSWORD = "";

    /** Javelinログ分割保存を行うかどうかのデフォルト値 */
    private static final boolean DEF_IS_LOG_SPLIT = false;

    /** Javelinログを分割保存する場合の1レコード辺りの最大サイズのデフォルト値 */
    public static final int DEF_LOG_SPLIT_SIZE = 300;

    /** Javelinログを分割保存する場合の閾値のデフォルト値 */
    public static final int DEF_LOG_SPLIT_THRESHOLD = 1024;

    /** サーバのリソース間隔（共通） */
    private long resourceInterval_ = DEF_RESOURCE_INTERVAL;

    /** リソース取得間隔のデフォルト値(ミリ秒) */
    public static final int DEF_RESOURCE_INTERVAL = 5000;

    /** リソースモニタリングの設定ファイル名のデフォルト値 */
    private static final String DEF_RESOURCE_MONITORING_CONF = "../conf/resource_monitoring.conf";

    /** レポート出力先ディレクトリのデフォルト値 */
    private static final String DEF_REPORT_OUTPUT_PATH = null;

    /** 接続方式 Client */
    public static final String MODE_CLIENT = "client";

    /** 接続方式 Server */
    public static final String MODE_SERVER = "server";

    /** 接続方式のデフォル値 */
    private static final String DEF_MODE = MODE_CLIENT;

    /** サーバホスト名のデフォルト値 */
    private static final String DEF_ACCEPT_HOST = "localhost";

    /** 待ち受けポートのデフォルト値 */
    private static final int DEF_ACCEOT_PORT = 19000;

    /** 計測データの最大蓄積期間のデフォルト値 */
    private static final String DEF_MEASUREMENT_LOG_STORAGE_PERIOD = "7d";

    /** 同一SQL判定フラグの初期値 */
    private static final boolean DEF_JUDGE_SIMILAR_SQL = false;

    //--------------------
    // SMTP settings(default)
    //--------------------
    /** メール通知を送信するかどうかのデフォルト値。 */
    public static final boolean DEF_SEND_MAIL = false;

    /** メールサーバのデフォルト値。 */
    private static final String DEF_SMTP_SERVER = "mail.example.com";

    /** メールポートのデフォルト値。 */
    private static final int DEF_SMTP_PORT = 587;

    /** メールのエンコーディング設定デフォルト値。 */
    private static final String DEF_SMTP_ENCODING = "iso-2022-jp";

    /** 送信元メールアドレス設定デフォルト値。 */
    private static final String DEF_SMTP_FROM = "endosnipe@example.com";

    /** 送信先メールアドレス設定デフォルト値。 */
    private static final String DEF_SMTP_TO = "endosnipe@example.com";

    //--------------------
    // SMTP settings
    //--------------------
    /** メール通知を送信するかどうか */
    private boolean sendMail_ = DEF_SEND_MAIL;

    /** メールサーバ */
    private String smtpServer_ = DEF_SMTP_SERVER;

    /** メールサーバのポート番号 */
    private int smtpPort_ = DEF_SMTP_PORT;

    /** メールサーバのユーザ名 */
    private String smtpUser_ = "";

    /** メールサーバのパスワード */
    private String smtpPassword_ = "";

    /** メールのエンコーディング */
    private String smtpEncoding_ = DEF_SMTP_ENCODING;

    /** 送信元メールアドレス */
    private String smtpFrom_ = DEF_SMTP_FROM;

    /** 送信先メールアドレス */
    private String smtpTo_ = DEF_SMTP_TO;

    /** メールテンプレート（キー：設定項目名、値：テンプレートファイル名） */
    private final Map<String, Map<String, MailTemplateEntity>> smtpTemplateMap_ =
        new HashMap<String, Map<String, MailTemplateEntity>>();

    /** 閾値超過を表す定数 */
    public static final String SMTP_FAILURE = "failure";

    /** 閾値回復を表す定数 */
    public static final String SMTP_RECOVER = "recover";

    /** テンプレートのデフォルト名 */
    private static final String SMTP_TEMPLATE_DEFAULT = "default";

    //--------------------
    // SNMP settings(default)
    //--------------------
    /** SNMPTrapを送信するかどうかのデフォルト値true(=送信する) */
    public static final boolean DEF_SEND_TRAP = false;

    /** マネージャリストのデフォルト値localhost */
    private static final String DEF_MANAGERS = "localhost";

    /** SNMP Trapポート番号のデフォルト値162 */
    public static final int DEF_TRAP_PORT = 162;

    /** Trapコミュニティ名のデフォルト値public */
    private static final String DEF_TRAP_COMMUNITY = "public";

    /** SNMP Version: v1 */
    public static final String VERSION_V1 = "v1";

    /** SNMP Version: v2c */
    public static final String VERSION_V2C = "v2c";

    /** SNMP Version: v3 */
    public static final String VERSION_V3 = "v3";

    /** SNMP Versionのデフォルト値v2c */
    private static final String DEF_VERSION = VERSION_V2C;

    //--------------------
    // SNMP settings
    //--------------------
    /** SNMPTrapを送信するかどうか。true:送信する、false:送信しない */
    private boolean sendTrap_ = DEF_SEND_TRAP;

    /** マネージャリスト */
    private String managers_ = DEF_MANAGERS;

    /** SNMP Trapポート番号 */
    private int trapPort_ = DEF_TRAP_PORT;

    /** SNMP Version */
    private String version_ = DEF_VERSION;

    /** Trapコミュニティ名 */
    private String trapCommunity_ = DEF_TRAP_COMMUNITY;

    /** バッチサイズ */
    private int batchSize_ = DEF_BATCH_SIZE;

    /** バッチ更新 */
    public static final int DEF_BATCH_SIZE = 100;

    /** itemIdキャッシュ */
    private int itemIdCacheSize_ = DEF_CACHE_SIZE;

    /** itemIdキャッシュ */
    public static final int DEF_CACHE_SIZE = 50000;

    //--------------------
    // Language settings
    //--------------------
    /** デフォルトの言語 */
    private static final String DEF_LANGUAGE = "";

    /** 使用言語 */
    private String language_ = DEF_LANGUAGE;

    /** ホスト名(ElasticSearch) */
    private String esHostName_;

    /** ホストポート(ElasticSearch) */
    private int esHostPort_;

    /** クラスタ名(ElasticSearch) */
    private String esClusterName_;

    /** インデックス(ElasticSearch:接頭辞) */
    private String esIndexPrefix_;

    /** インデックス(ElasticSearch:接尾辞) */
    private String esIndexPostfix_;

    /** 種別 (ElasticSearch)*/
    private String esType_;

    /** 利用可能かどうか(ElasticSearch) */
    private boolean esAvailable_;

    /**
     * データベースの種類を返します。<br />
     *
     * @return データベースの種類
     */
    public DatabaseType getDatabaseType()
    {
        return this.databaseType_;
    }

    /**
     * データベースの種類を設定します。<br />
     *
     * @param type データベースの種類
     */
    public void setDatabaseType(final DatabaseType type)
    {
        this.databaseType_ = type;
    }

    /**
     * データベースディレクトリを返します。<br />
     * 
     * @return データベースディレクトリ
     */
    public String getBaseDir()
    {
        return baseDir_;
    }

    /**
     * データベースディレクトリを設定します。<br />
     * 
     * @param baseDir データベースディレクトリ
     */
    public void setBaseDir(final String baseDir)
    {
        baseDir_ = baseDir;
    }

    /**
     * データベースのホストアドレスを返します。<br />
     *
     * @return ホストアドレスまたはホスト名
     */
    public String getDatabaseHost()
    {
        return this.databaseHost_;
    }

    /**
     * データベースのホストアドレスを設定します。<br />
     *
     * @param host ホストアドレス（ホスト名でも可）
     */
    public void setDatabaseHost(final String host)
    {
        this.databaseHost_ = host;
    }

    /**
     * データベースのポート番号を返します。<br />
     *
     * @return ポート番号
     */
    public String getDatabasePort()
    {
        return this.databasePort_;
    }

    /**
     * データベースのポート番号を設定します。<br />
     *
     * @param port ポート番号
     */
    public void setDatabasePort(final String port)
    {
        this.databasePort_ = port;
    }

    /**
     * データベース名を取得します。<br />
     * 
     * @return DataCollectorが書き込みに使用するデータベース名。
     */
    public String getDatabaseName()
    {
        return databaseName_;
    }

    /**
     * データベース名を設定します。<br />
     * 
     * @param dbname DataCollectorが書き込みに使用するデータベース名。
     */
    public void setDatabaseName(final String dbname)
    {
        this.databaseName_ = dbname;
    }

    /**
     * データベースログインユーザ名を返します。<br />
     *
     * @return ユーザ名
     */
    public String getDatabaseUserName()
    {
        return this.databaseUserName_;
    }

    /**
     * データベースログインユーザ名を設定します。<br />
     *
     * @param user ユーザ名
     */
    public void setDatabaseUserName(final String user)
    {
        this.databaseUserName_ = user;
    }

    /**
     * データベースログインパスワードを返します。<br />
     *
     * @return パスワード
     */
    public String getDatabasePassword()
    {
        return this.databasePassword_;
    }

    /**
     * データベースログインパスワードを設定します。<br />
     *
     * @param password パスワード
     */
    public void setDatabasePassword(final String password)
    {
        this.databasePassword_ = password;
    }

    /**
     * リソース取得間隔を設定します。<br />
     *
     * @param interval リソース取得間隔
     */
    public void setResourceInterval(final long interval)
    {
        this.resourceInterval_ = interval;
    }

    /**
     * リソース取得間隔を返します。<br />
     *
     * @return Interval リソース取得間隔
     */
    public long getResourceInterval()
    {
        return this.resourceInterval_;
    }

    /**
     * {@link AgentSetting} を追加します。<br />
     * 
     * @param agentSetting {@link AgentSetting} オブジェクト
     */
    public void addAgentSetting(final AgentSetting agentSetting)
    {
        agentSttingList_.add(agentSetting);
    }

    /**
     * {@link AgentSetting} のリストを返します。<br />
     * 
     * @return {@link AgentSetting} のリスト
     */
    public List<AgentSetting> getAgentSettingList()
    {
        return Collections.unmodifiableList(agentSttingList_);
    }

    /**
     * Javelinログを分割するかどうか。<br />
     * 
     * @return Javelinログを分割する場合、<code>true</code>
     */
    public boolean isLogSplit()
    {
        return this.isLogSplit_;
    }

    /**
     * Javelinログを分割するかどうかを設定します。<br />
     * 
     * @param isLogSplit Javelinログを分割する場合、<code>true</code>
     */
    public void setLogSplit(final boolean isLogSplit)
    {
        this.isLogSplit_ = isLogSplit;
    }

    /**
     * Javelinログを分割保存する場合の1レコード当たりの最大サイズを返します。<br />
     * 
     * @return Javelinログを分割保存する場合の1レコード当たりの最大サイズ
     */
    public int getLogSplitSize()
    {
        return this.logSplitSize_;
    }

    /**
     * Javelinログを分割保存する場合の1レコード当たりの最大サイズを設定します。<br />
     * 
     * @param logSplitSize Javelinログを分割保存する場合の1レコード当たりの最大サイズ
     */
    public void setLogSplitSize(final int logSplitSize)
    {
        this.logSplitSize_ = logSplitSize;
    }

    /**
     * Javelinログを分割保存する場合の閾値を返します。<br />
     * 
     * @return Javelinログを分割保存する場合の閾値
     */
    public int getLogSplitThreshold()
    {
        return this.logSplitThreshold_;
    }

    /**
     * Javelinログを分割保存する場合の閾値を設定します。<br />
     * 
     * @param logSplitThreshold Javelinログを分割保存する場合の閾値
     */
    public void setLogSplitThreshold(final int logSplitThreshold)
    {
        this.logSplitThreshold_ = logSplitThreshold;
    }

    /**
     * @param resourceMonitoringConf セットする resourceMonitoringConf
     */
    public void setResourceMonitoringConf(final String resourceMonitoringConf)
    {
        resourceMonitoringConf_ = resourceMonitoringConf;
    }

    /**
     * @return resourceMonitoringConf
     */
    public String getResourceMonitoringConf()
    {
        return resourceMonitoringConf_;
    }

    /**
     * レポートを出力するディレクトリを取得する。
     * 
     * @return reportOutputPath レポートを出力するディレクトリ。
     */
    public String getReportOutputPath()
    {
        return reportOutputPath_;
    }

    /**
     * レポートを出力するディレクトリを設定する。
     * 
     * @param reportOutputPath レポートを出力するディレクトリ。
     */
    public void setReportOutputPath(final String reportOutputPath)
    {
        reportOutputPath_ = reportOutputPath;
    }

    /**
     * メール通知を送信するかどうかを取得する。
     * 
     * @return メール通知を送信するかどうかの設定
     */
    public boolean isSendMail()
    {
        return sendMail_;
    }

    /**
     *　メール通知を送信するかどうかを設定する。
     * 
     * @param sendMail メール通知を送信するかどうかの設定
     */
    public void setSendMail(final boolean sendMail)
    {
        sendMail_ = sendMail;
    }

    /**
     * メールのエンコーディングを取得する。
     * 
     * @return メールのエンコーディングの設定
     */
    public String getSmtpEncoding()
    {
        return smtpEncoding_;
    }

    /**
     *　メールのエンコーディングを設定する。
     * 
     * @param smtpEncoding メールのエンコーディングの設定
     */
    public void setSmtpEncoding(final String smtpEncoding)
    {
        smtpEncoding_ = smtpEncoding;
    }

    /**
     * メールサーバを取得する。
     * 
     * @return メールサーバの設定
     */
    public String getSmtpServer()
    {
        return smtpServer_;
    }

    /**
     *　メールサーバを設定する。
     * 
     * @param smtpServer メールサーバの設定
     */
    public void setSmtpServer(final String smtpServer)
    {
        smtpServer_ = smtpServer;
    }

    /**
     * メールサーバのポート番号を取得する。
     * @return ポート番号
     */
    public int getSmtpPort()
    {
        return smtpPort_;
    }

    /**
     * メールサーバのポート番号を設定する。
     * @param smtpPort ポート番号
     */
    public void setSmtpPort(final int smtpPort)
    {
        smtpPort_ = smtpPort;
    }

    /**
     * メールサーバのユーザ名を取得する。
     * @return ユーザ名
     */
    public String getSmtpUser()
    {
        return smtpUser_;
    }

    /**
     * メールサーバのユーザ名を設定する。
     * @param smtpUser ユーザ名
     */
    public void setSmtpUser(final String smtpUser)
    {
        smtpUser_ = smtpUser;
    }

    /**
     * メールサーバのパスワードを取得する。
     * @return パスワード
     */
    public String getSmtpPassword()
    {
        return smtpPassword_;
    }

    /**
     * メールサーバのパスワードを設定する。
     * @param smtpPassword パスワード
     */
    public void setSmtpPassword(final String smtpPassword)
    {
        smtpPassword_ = smtpPassword;
    }

    /**
     * 送信元メールアドレスを取得する。
     * 
     * @return 送信元メールアドレスの設定
     */
    public String getSmtpFrom()
    {
        return smtpFrom_;
    }

    /**
     *　送信元メールアドレスを設定する。
     * 
     * @param smtpFrom 送信元メールアドレスの設定
     */
    public void setSmtpFrom(final String smtpFrom)
    {
        smtpFrom_ = smtpFrom;
    }

    /**
     * 送信先メールアドレスを取得する。
     * 
     * @return 送信先メールアドレスの設定
     */
    public String getSmtpTo()
    {
        return smtpTo_;
    }

    /**
     *　送信先メールアドレスを設定する。
     * 
     * @param smtpTo 送信先メールアドレスの設定
     */
    public void setSmtpTo(final String smtpTo)
    {
        smtpTo_ = smtpTo;
    }

    /**
     * メールテンプレートファイル名を取得する。
     *
     * @param name テンプレート名
     * @param type 閾値超過、回復を表す文字列
     * @param useDefault デフォルトの値を使うかどうか
     * @return テンプレートファイル名
     */
    public MailTemplateEntity getSmtpTemplate(final String name, final String type,
        final boolean useDefault)
    {
        if (name == null)
        {
            return null;
        }
        Map<String, MailTemplateEntity> templateMap = this.smtpTemplateMap_.get(name);
        if (useDefault)
        {
            templateMap = this.smtpTemplateMap_.get(SMTP_TEMPLATE_DEFAULT);
        }
        if (templateMap == null)
        {
            return null;
        }
        return templateMap.get(type);
    }

    /**
     * メールテンプレートファイル名を設定する。
     *
     * @param name テンプレート名
     * @param type 閾値超過、回復を表す文字列
     * @param template テンプレートファイル名
     */
    public void addSmtpTemplate(final String name, final String type,
        final MailTemplateEntity template)
    {
        Map<String, MailTemplateEntity> mailTemplateMap = this.smtpTemplateMap_.get(name);
        if (mailTemplateMap == null)
        {
            mailTemplateMap = new HashMap<String, MailTemplateEntity>();
            this.smtpTemplateMap_.put(name, mailTemplateMap);
        }
        mailTemplateMap.put(type, template);
    }

    /**
     * SNMPTrapを送信するかどうかを取得する。
     * 
     * @return SNMPTrapを送信するかどうか
     */
    public boolean isSendTrap()
    {
        return sendTrap_;
    }

    /**
     *　SNMPTrapを送信するかどうかを設定する。
     * 
     * @param sendTrap SNMPTrapを送信するかどうか
     */
    public void setSendTrap(final boolean sendTrap)
    {
        sendTrap_ = sendTrap;
    }

    /**
     * マネージャリストを取得する。
     * 
     * @return マネージャリスト
     */
    public String getManagers()
    {
        return managers_;
    }

    /**
     *　マネージャリストを設定する。
     * 
     * @param managers マネージャリスト
     */
    public void setManagers(final String managers)
    {
        managers_ = managers;
    }

    /**
     * SNMP Trapポート番号を取得する。
     * 
     * @return SNMP Trapポート番号
     */
    public int getTrapPort()
    {
        return trapPort_;
    }

    /**
     *　SNMP Trapポート番号を設定する。
     * 
     * @param trapPort SNMP Trapポート番号
     */
    public void setTrapPort(final int trapPort)
    {
        trapPort_ = trapPort;
    }

    /**
     * Trapコミュニティ名を取得する。
     * 
     * @return Trapコミュニティ名
     */
    public String getTrapCommunity()
    {
        return trapCommunity_;
    }

    /**
     *　Trapコミュニティ名を設定する。
     * 
     * @param trapCommunity Trapコミュニティ名
     */
    public void setTrapCommunity(final String trapCommunity)
    {
        trapCommunity_ = trapCommunity;
    }

    /**
     * SNMP Versionを取得する。
     * 
     * @return SNMP Version
     */
    public String getVersion()
    {
        return version_;
    }

    /**
     *　SNMP Versionを設定する。
     * 
     * @param version SNMP Version
     */
    public void setVersion(final String version)
    {
        version_ = version;
    }

    /**
     * 接続モードを取得する。
     * @return connectionMode 接続モード
     */
    public String getConnectionMode()
    {
        return connectionMode_;
    }

    /**
     * 接続モードを設定する。
     * @param connectionMode 接続モード
     */
    public void setConnectionMode(final String connectionMode)
    {
        connectionMode_ = connectionMode;
    }

    /**
     * 待ち受けホストを取得する。
     * @return acceptHost 待ち受けホスト
     */
    public String getAcceptHost()
    {
        return acceptHost_;
    }

    /**
     * 待ち受けホストを設定する。
     * @param acceptHost 待ち受けホスト
     */
    public void setAcceptHost(final String acceptHost)
    {
        acceptHost_ = acceptHost;
    }

    /**
     * 待ち受けポートを取得する。
     * @return acceptPort 待ち受けポート
     */
    public int getAcceptPort()
    {
        return acceptPort_;
    }

    /**
     * 待ち受けポートを設定する。
     * @param acceptPort 待ち受けポート
     */
    public void setAcceptPort(final int acceptPort)
    {
        acceptPort_ = acceptPort;
    }

    /**
     * SSL通信：SSLを行うかどうかを取得する。
     * @return SSL通信：SSLを行うかどうか
     */
    public boolean isSslEnable()
    {
        return sslEnable_;
    }

    /**
     * SSL通信：SSLを行うかどうかを設定する。
     * @param sslEnable SSL通信：SSLを行うかどうか
     */
    public void setSslEnable(final boolean sslEnable)
    {
        sslEnable_ = sslEnable;
    }

    /**
     * SSL通信：keyStoreのパスを取得する。
     * @return SSL通信：keyStoreのパス
     */
    public String getSslKeyStore()
    {
        return sslKeyStore_;
    }

    /**
     * SSL通信：keyStoreのパスを設定する。
     * @param sslKeyStore SSL通信：keyStoreのパス
     */
    public void setSslKeyStore(final String sslKeyStore)
    {
        sslKeyStore_ = sslKeyStore;
    }

    /**
     * SSL通信：keyStoreのパスワードを設定する。
     * @return sslKeyStore SSL通信：keyStoreのパスワード
     */
    public String getSslKeyStorePass()
    {
        return sslKeyStorePass_;
    }

    /**
     * SSL通信：keyStoreのパスワードを設定する。
     * @param sslKeyStorePass SSL通信：keyStoreのパスワード
     */
    public void setSslKeyStorePass(final String sslKeyStorePass)
    {
        sslKeyStorePass_ = sslKeyStorePass;
    }

    /**
     * SSL通信：trustStoreのパスを取得する。
     * @return SSL通信：trustStoreのパス
     */
    public String getSslTrustStore()
    {
        return sslTrustStore_;
    }

    /**
     * SSL通信：trustStoreのパスを設定する。
     * @param sslTrustStore SSL通信：trustStoreのパス
     */
    public void setSslTrustStore(final String sslTrustStore)
    {
        sslTrustStore_ = sslTrustStore;
    }

    /**
     * SSL通信：trustStoreのパスワードを取得する。
     * @return SSL通信：trustStoreのパスワード
     */
    public String getSslTrustStorePass()
    {
        return sslTrustStorePass_;
    }

    /**
     * SSL通信：trustStoreのパスワードを設定する。
     * @param sslTrustStorePass SSL通信：trustStoreのパスワード
     */
    public void setSslTrustStorePass(final String sslTrustStorePass)
    {
        sslTrustStorePass_ = sslTrustStorePass;
    }

    /**
     * Javelinログの最大蓄積期間を取得する。
     * @return Javelinログの最大蓄積期間
     */
    public String getJvnLogStoragePeriod()
    {
        return jvnLogStoragePeriod_;
    }

    /**
     * Javelinログの最大蓄積期間を設定する。
     * @param jvnLogStoragePeriod Javelinログの最大蓄積期間
     */
    public void setJvnLogStoragePeriod(final String jvnLogStoragePeriod)
    {
        this.jvnLogStoragePeriod_ = jvnLogStoragePeriod;
    }

    /**
     * 計測データの最大蓄積期間を取得する。
     * @return 計測データの最大蓄積期間
     */
    public String getMeasurementLogStoragePeriod()
    {
        return measurementLogStoragePeriod_;
    }

    /**
     * 計測データの最大蓄積期間を設定する。
     * @param measurementLogStoragePeriod 計測データの最大蓄積期間
     */
    public void setMeasurementLogStoragePeriod(final String measurementLogStoragePeriod)
    {
        this.measurementLogStoragePeriod_ = measurementLogStoragePeriod;
    }

    /**
     * 同一SQL判定フラグを取得する。
     * @return 同一SQL判定フラグ
     */
    public boolean isJudgeSimilarSql()
    {
        return judgeSimilarSql_;
    }

    /**
     * 同一SQL判定フラグを設定する。
     * @param judgeSimilarSql 同一SQL判定フラグ
     */
    public void setJudgeSimilarSql(final boolean judgeSimilarSql)
    {
        this.judgeSimilarSql_ = judgeSimilarSql;
    }

    /**
     * 使用言語を取得する。
     * 
     * @return 使用言語
     */
    public String getLanguage()
    {
        return language_;
    }

    /**
     * 使用言語を設定する。
     * 
     * @param language 使用言語
     */
    public void setLanguage(final String language)
    {
        language_ = language;
    }

    /**
     * バッチサイズを取得します。
     * @return バッチサイズ
     */
    public int getBatchSize()
    {
        return this.batchSize_;
    }

    /**
     * バッチサイズを設定します。
     * @param batchSize バッチサイズ
     */
    public void setBatchSize(final int batchSize)
    {
        batchSize_ = batchSize;
    }

    /**
     * 項目IDのキャッシュサイズを取得します。
     * @return キャッシュサイズ
     */
    public int getItemIdCacheSize()
    {
        return itemIdCacheSize_;
    }

    /**
     * 項目IDのキャッシュサイズを設定します。
     * @param itemIdCacheSize 項目IDのキャッシュサイズ
     */
    public void setItemIdCacheSize(final int itemIdCacheSize)
    {
        itemIdCacheSize_ = itemIdCacheSize;
    }

    /**
     * ホスト名(ElasticSearch)を取得する。
     * @return ホスト名(ElasticSearch)
     */
    public String getEsHostName()
    {
        return esHostName_;
    }

    /**
     * ホスト名(ElasticSearch)を取得する。
     * @param esHostName ホスト名(ElasticSearch)
     */
    public void setEsHostName(final String esHostName)
    {
        esHostName_ = esHostName;
    }

    /**
     * ホストポート番号(ElasticSearch)を取得する。
     * @return ホストポート番号(ElasticSearch)
     */
    public int getEsHostPort()
    {
        return esHostPort_;
    }

    /**
     * ホストポート番号(ElasticSearch)を取得する。
     * @param esHostPort ホストポート番号(ElasticSearch)
     */
    public void setEsHostPort(final int esHostPort)
    {
        esHostPort_ = esHostPort;
    }

    /**
     * クラスタ名(ElasticSearch)を取得する。
     * @return クラスタ名(ElasticSearch)
     */
    public String getEsClusterName()
    {
        return esClusterName_;
    }

    /**
     * クラスタ名(ElasticSearch)を設定する。
     * @param esClusterName クラスタ名(ElasticSearch)
     */
    public void setEsClusterName(final String esClusterName)
    {
        this.esClusterName_ = esClusterName;
    }

    /**
     * インデックス(接頭辞:ElasticSearch)を取得する。
     * @return インデックス(接頭辞:ElasticSearch)
     */
    public String getEsIndexPrefix()
    {
        return esIndexPrefix_;
    }

    /**
     * インデックス(接頭辞:ElasticSearch)を設定する。
     * @param esIndexPrefix インデックス(接頭辞:ElasticSearch)
     */
    public void setEsIndexPrefix(final String esIndexPrefix)
    {
        this.esIndexPrefix_ = esIndexPrefix;
    }

    /**
     * インデックス(接尾辞:ElasticSearch）を設定する。
     * @return インデックス(接尾辞:ElasticSearch）
     */
    public String getEsIndexPostfix()
    {
        return esIndexPostfix_;
    }

    /**
     * インデックス(接尾辞:ElasticSearch）を取得する。
     * @param esIndexPostifx インデックス(接尾辞:ElasticSearch）
     */
    public void setEsIndexPostfix(final String esIndexPostifx)
    {
        esIndexPostfix_ = esIndexPostifx;
    }

    /**
     * 種別(ElasticSearch)を取得する。
     * @return 種別(ElasticSearch)
     */
    public String getEsType()
    {
        return esType_;
    }

    /**
     * 種別(ElasticSearch)を設定する。
     * @param esType 種別(ElasticSearch)
     */
    public void setEsType(final String esType)
    {
        this.esType_ = esType;
    }

    /**
     * 利用か可能かどうか(ElasticSearch)を取得する。
     * @return 利用可能かどうか(ElasticSearch)。
     */
    public boolean isEsAvailable()
    {
        return esAvailable_;
    }

    /**
     * 利用か可能かどうか(ElasticSearch)を設定する。
     * @param esAvailable 利用可能かどうか(ElasticSearch)。
     */
    public void setEsAvailable(final boolean esAvailable)
    {
        this.esAvailable_ = esAvailable;
    }

}
