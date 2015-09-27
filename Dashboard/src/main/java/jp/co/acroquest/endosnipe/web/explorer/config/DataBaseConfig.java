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
package jp.co.acroquest.endosnipe.web.explorer.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.co.acroquest.endosnipe.data.db.DatabaseType;

/**
 * ENdoSnipe DataBase の設定/定数を保持するクラスです。<br />
 * 
 * @author fujii
 */
public class DataBaseConfig
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

    /** データベース名。 */
    private String databaseName_ = DEF_DATABASE_NAME;

    /** データベースログインユーザ名 */
    private String databaseUserName_ = DEF_DATABASE_USER;

    /** データベースログインパスワード */
    private String databasePassword_ = DEF_DATABASE_PASSWORD;

    /** リソースモニタリングの設定ファイル名 */
    private String resourceMonitoringConf_ = DEF_RESOURCE_MONITORING_CONF;

    /** 接続モード */
    private String connectionMode_ = DEF_CONNECTION_MODE;

    /** サーバモードの設定 */
    private AgentSetting serverModeAgentSetting_ = new AgentSetting();

    /** エージェントの設定リスト */
    private final List<AgentSetting> agentSettingList_ = new ArrayList<AgentSetting>();

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

    /** データベースの種類のデフォルト値 */
    private static final DatabaseType DEF_DATABASE_TYPE = DatabaseType.H2;

    /** データベース保存先ベースディレクトリのデフォルト値 */
    private static final String DEF_DATABASE_DIR = "../data";

    /** データベースのホストアドレスのデフォルト値 */
    private static final String DEF_DATABASE_HOST = "localhost";

    /** データベースのポート番号のデフォルト値 */
    private static final String DEF_DATABASE_PORT = "5432";

    /** データベース名。 */
    private static final String DEF_DATABASE_NAME = null;

    /** データベースログインユーザ名のデフォルト値 */
    private static final String DEF_DATABASE_USER = "";

    /** データベースログインパスワードのデフォルト値 */
    private static final String DEF_DATABASE_PASSWORD = "";

    /** Javelinログ分割保存を行うかどうかのデフォルト値 */
    private static final boolean DEF_IS_LOG_SPLIT = false;

    /** Javelinログを分割保存する場合の1レコード辺りの最大サイズのデフォルト値 */
    private static final int DEF_LOG_SPLIT_SIZE = 300;

    /** Javelinログを分割保存する場合の閾値のデフォルト値 */
    private static final int DEF_LOG_SPLIT_THRESHOLD = 1024;

    /** サーバのリソース間隔（共通） */
    private long resourceInterval_ = DEF_RESOURCE_INTERVAL;

    /** リソース取得間隔のデフォルト値(ミリ秒) */
    private static final int DEF_RESOURCE_INTERVAL = 5000;

    /** リソースモニタリングの設定ファイル名のデフォルト値 */
    private static final String DEF_RESOURCE_MONITORING_CONF = "../conf/resource_monitoring.conf";

    /** 接続モードのデフォルト値 */
    private static final String DEF_CONNECTION_MODE = "client";

    /**
     * コンストラクタ。
     */
    public DataBaseConfig()
    {
    }

    /**
     * データベースの種類を返します。<br />
     * @return データベースの種類
     */
    public DatabaseType getDatabaseType()
    {
        return this.databaseType_;
    }

    /**
     * データベースの種類を設定します。<br />
     * @param type データベースの種類
     */
    public void setDatabaseType(final DatabaseType type)
    {
        this.databaseType_ = type;
    }

    /**
     * データベースディレクトリを返します。<br />
     * @return データベースディレクトリ
     */
    public String getBaseDir()
    {
        return baseDir_;
    }

    /**
     * データベースディレクトリを設定します。<br />
     * @param baseDir データベースディレクトリ
     */
    public void setBaseDir(final String baseDir)
    {
        baseDir_ = baseDir;
    }

    /**
     * データベースのホストアドレスを返します。<br />
     * @return ホストアドレスまたはホスト名
     */
    public String getDatabaseHost()
    {
        return this.databaseHost_;
    }

    /**
     * データベースのホストアドレスを設定します。<br />
     * @param host ホストアドレス（ホスト名でも可）
     */
    public void setDatabaseHost(final String host)
    {
        this.databaseHost_ = host;
    }

    /**
     * データベースのポート番号を返します。<br />
     * @return ポート番号
     */
    public String getDatabasePort()
    {
        return this.databasePort_;
    }

    /**
     * データベースのポート番号を設定します。<br />
     * @param port ポート番号
     */
    public void setDatabasePort(final String port)
    {
        this.databasePort_ = port;
    }

    /**
     * データベース名を取得します。
     * @return データベース名。
     */
    public String getDatabaseName()
    {
        return databaseName_;
    }

    /**
     * データベース名を設定します。
     * @param databaseName データベース名。
     */
    public void setDatabaseName(final String databaseName)
    {
        this.databaseName_ = databaseName;
    }

    /**
     * データベースログインユーザ名を返します。<br />
     * @return ユーザ名
     */
    public String getDatabaseUserName()
    {
        return this.databaseUserName_;
    }

    /**
     * データベースログインユーザ名を設定します。<br />
     * @param user ユーザ名
     */
    public void setDatabaseUserName(final String user)
    {
        this.databaseUserName_ = user;
    }

    /**
     * データベースログインパスワードを返します。<br />
     * @return パスワード
     */
    public String getDatabasePassword()
    {
        return this.databasePassword_;
    }

    /**
     * データベースログインパスワードを設定します。<br />
     * @param password パスワード
     */
    public void setDatabasePassword(final String password)
    {
        this.databasePassword_ = password;
    }

    /**
     * リソース取得間隔を設定します。<br />
     * @param interval リソース取得間隔
     */
    public void setResourceInterval(final long interval)
    {
        this.resourceInterval_ = interval;
    }

    /**
     * リソース取得間隔を返します。<br />
     * @return Interval リソース取得間隔
     */
    public long getResourceInterval()
    {
        return this.resourceInterval_;
    }

    /**
     * {@link AgentSetting} を追加します。<br />
     * @param agentSetting {@link AgentSetting} オブジェクト
     */
    public void addAgentSetting(final AgentSetting agentSetting)
    {
        agentSettingList_.add(agentSetting);
    }

    /**
     * {@link AgentSetting} のリストを返します。<br />
     * @return {@link AgentSetting} のリスト
     */
    public List<AgentSetting> getAgentSettingList()
    {
        return Collections.unmodifiableList(agentSettingList_);
    }

    /**
     * Javelinログを分割するかどうか。<br />
     * @return Javelinログを分割する場合、<code>true</code>
     */
    public boolean isLogSplit()
    {
        return this.isLogSplit_;
    }

    /**
     * Javelinログを分割するかどうかを設定します。<br />
     * @param isLogSplit Javelinログを分割する場合、<code>true</code>
     */
    public void setLogSplit(final boolean isLogSplit)
    {
        this.isLogSplit_ = isLogSplit;
    }

    /**
     * Javelinログを分割保存する場合の1レコード当たりの最大サイズを返します。<br />
     * @return Javelinログを分割保存する場合の1レコード当たりの最大サイズ
     */
    public int getLogSplitSize()
    {
        return this.logSplitSize_;
    }

    /**
     * Javelinログを分割保存する場合の1レコード当たりの最大サイズを設定します。<br />
     * @param logSplitSize Javelinログを分割保存する場合の1レコード当たりの最大サイズ
     */
    public void setLogSplitSize(final int logSplitSize)
    {
        this.logSplitSize_ = logSplitSize;
    }

    /**
     * Javelinログを分割保存する場合の閾値を返します。<br />
     * @return Javelinログを分割保存する場合の閾値
     */
    public int getLogSplitThreshold()
    {
        return this.logSplitThreshold_;
    }

    /**
     * Javelinログを分割保存する場合の閾値を設定します。<br />
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
     * @return connectionMode
     */
    public String getConnectionMode()
    {
        return connectionMode_;
    }

    /**
     * @param connectionMode セットする connectionMode
     */
    public void setConnectionMode(final String connectionMode)
    {
        connectionMode_ = connectionMode;
    }

    /**
     * @return serverModeAgentSetting
     */
    public AgentSetting getServerModeAgentSetting()
    {
        return serverModeAgentSetting_;
    }

    /**
     * @param serverModeAgentSetting セットする serverModeAgentSetting
     */
    public void setServerModeAgentSetting(final AgentSetting serverModeAgentSetting)
    {
        serverModeAgentSetting_ = serverModeAgentSetting;
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
}