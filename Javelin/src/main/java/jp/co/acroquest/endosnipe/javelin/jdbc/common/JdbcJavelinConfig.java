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
package jp.co.acroquest.endosnipe.javelin.jdbc.common;

import jp.co.acroquest.endosnipe.common.config.JavelinConfig;
import jp.co.acroquest.endosnipe.common.config.JavelinConfigUtil;

/**
 * JDBCJavelinの設定を保持するクラス。
 * @author eriguchi
 */
public class JdbcJavelinConfig extends JavelinConfig
{
    /** 実行計画を取得するか否かを設定するキー。 */
    public static final String RECORDEXECPLAN_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.recordExecPlan";

    /** SQL文のFull Scanを監視するか否かを設定するキー。 */
    public static final String FULLSCAN_MONITOR_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.fullScan.monitor";

    /** JDBC呼出し重複出力を行うか否かを設定するキー。 */
    public static final String RECORDDUPLJDBCCALL_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.recordDuplJdbcCall";

    /** バインド変数を取得するか否かを設定するキー。 */
    public static final String RECORDBINDVAL_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.recordBindVal";

    /** 実行計画を取得する閾値を設定するキー。 */
    public static final String EXECPLANTHRESHOLD_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.execPlanThreshold";

    /** バインド変数出力での文字列長制限を設定するキー。 */
    public static final String STRINGLIMITLENGTH_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.stringLimitLength";

    /** 同一トランザクション内の同一SQL呼び出し回数超過の閾値を監視するか否かを設定するキー。 */
    public static final String SQLCOUNT_MONITOR_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.sqlcount.monitor";

    /** 同一トランザクション内の同一SQL呼び出し回数超過の閾値。 */
    public static final String SQLCOUNT_KEY = JavelinConfig.JAVELIN_PREFIX + "jdbc.sqlcount";

    /** SQLトレース出力を行うか否かを設定するキー。 */
    public static final String ORACLE_ALLOW_SQL_TRACE_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.oracle.allowSqlTrace";

    /** PostgreSQLで詳細な実行計画を取得するか否かを設定するキー。 */
    public static final String POSTGRES_VERBOSE_PLAN_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.postgres.verbosePlan";

    /** 最大クエリ保存数を設定するキー。 */
    public static final String RECORD_STATEMENT_NUM_MAXIMUM_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.record.statement.num.maximum";

    /** JDBCJavelinでスタックトレース出力のON/OFFを設定するキー。 */
    public static final String RECORD_STACKTRACE_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.record.stackTrace";

    /** JDBCJavelinでスタックトレースを出力するための閾値。 */
    public static final String RECORD_STACKTRACE_THREADHOLD_KEY = JavelinConfig.JAVELIN_PREFIX
        + "jdbc.record.stacktraceThreashold";

    /** JDBCJavelinを有効にするかどうかのキー */
    public static final String JDBC_JAVELIN_ENABLED_KEY = JAVELIN_PREFIX + "jdbc.enable";

    /** JDBCJavelinをLightweightモードで動作させるかどうかのキー */
    public static final String JDBC_JAVELIN_LIGHTWEIGHT_KEY = JAVELIN_PREFIX + "jdbc.lightweight";

    /** 全SQLをログ出力させるかどうかのキー */
    public static final String LOG_ALL_SQL_KEY = JAVELIN_PREFIX + "jdbc.log.all.sql";

    /** SQLログの出力先フォルダのキー */
    public static final String JDBC_LOG_DIR_KEY = JAVELIN_PREFIX + "jdbc.log.dir";

    /** 実行計画出力ON/OFFフラグのデフォルト値 */
    private static final boolean DEFAULT_RECORDEXECPLAN = false;

    /** SQL文のFull Scanを監視するか否かを設定する値のデフォルト値 */
    private static final boolean DEFAULT_FULLSCAN_MONITOR = true;

    /** JDBC呼出し重複出力フラグのデフォルト値 */
    private static final boolean DEFAULT_RECORDDUPLJDBCCALL = false;

    /** バインド変数出力フラグのデフォルト値 */
    private static final boolean DEFAULT_RECORDBINDVAL = true;

    /** SQLの実行計画を記録する際の閾値のデフォルト値 */
    private static final long DEFAULT_EXECPLANTHRESHOLD = 0;

    /** バインド変数出力での文字列長制限のデフォルト値 */
    private static final long DEFAULT_STRINGLIMITLENGTH = 102400;

    /** 同一トランザクション内の同一SQL呼び出し回数超過の閾値を監視するか否かのデフォルト値 */
    private static final boolean DEFAULT_SQLCOUNT_MONITOR = true;

    /** 閾値のデフォルト */
    private static final int DEFAULT_SQLCOUNT = 20;

    /** SQLトレース出力フラグのデフォルト値 */
    private static final boolean DEFAULT_ORACLE_ALLOW_SQL_TRACE = false;

    /** PostgreSQL用詳細実行計画出力フラグのデフォルト値 */
    private static final boolean DEFAULT_POSTGRES_VERBOSE_PLAN = false;

    /** 最大クエリ保存数のデフォルト値 */
    private static final int DEFAULT_MAX_RECORD_STATEMENT_NUM_MAXIMUM = 256;

    /** JDBCJavelinでスタックトレース出力のデフォルト値 */
    private static final boolean DEF_RECORD_STACKTRACE = true;

    /** JDBCJavelinでスタックトレース出力のSQL実行時間のデフォルト値 */
    private static final int DEF_RECORD_STACKTRACE_THRESHOLD = 0;

    /** JDBCJavelinを有効にするかどうかのデフォルト値 */
    private static final boolean DEF_JDBC_JAVELIN_ENABLED = true;

    /** JDBCJavelinをLightweightモードで動作させるかどうかのデフォルト値 */
    private static final boolean DEF_JDBC_JAVELIN_LIGHTWEIGHT = false;

    /** 実行計画を取得するか否かを設定するキー。 */
    private static boolean isRecordExecPlan__;

    /** SQL文のFull Scanを監視するか否かを設定するキー。*/
    private static boolean isFullScanMonitor__;

    /** JDBC呼出し重複出力を行うか否かを設定するキー。 */
    private static boolean isRecordDuplJdbcCall__;

    /** バインド変数を取得するか否かを設定するキー。 */
    private static boolean isRecordBindVal__;

    /** 実行計画を取得する閾値を設定するキー。 */
    private static long execPlanThreshold__;

    /** バインド変数出力での文字列長制限を設定するキー。 */
    private static long stringLimitLength__;

    /** 同一トランザクション内の同一SQL呼び出し回数超過の閾値を監視するか否か。 */
    private static boolean isSqlcountMonitor__;

    /**  同一トランザクション内の同一SQL呼び出し回数超過の閾値。 */
    private static long sqlcount__;

    /** SQLトレース出力を行うか否かを設定するキー。 */
    private static boolean isOracleAllowSqlTrace__;

    /** PostgreSQLで詳細な実行計画を取得するか否かを設定するキー。 */
    private static boolean isPostgresVerbosePlan__;

    /** 最大クエリ保存数を設定するキー。 */
    private static int recordStatementNumMaximum__;

    /** JDBCJavelinでスタックトレース出力のON/OFFを設定するキー。 */
    private static boolean isRecordStackTrace__;

    /** JDBCJavelinでスタックトレースを出力するための閾値。 */
    private static int recordStacktraceThreashold__;

    /**
     * Oracleの実行計画の出力オプション。
     * "BASIC","SERIAL","TYPICAL","ALL"の何れかを指定する。
     * デフォルト値は"SERIAL"
     */
    private final String outputOption_;

    /** JDBC Javelinを使用するかどうか */
    private static boolean isJdbcJavelinEnabled__;

    /**
     * JDBCJavelinの記録モードをLightweightモードにするか。<br/>
     * trueの場合、監視に必要のない実行計画等を取得しない。デフォルト値はfalse。
     */
    private static boolean isJdbcJavelinLightweightMode__;

    /** 全SQLをログ出力させるかどうか */
    private static boolean logAllSql__;

    /** SQLログの出力先フォルダ */
    private static String logDir__;
    static
    {
        initialize();
    }

    /**
     * 設定値を初期化します。<br />
     *
     * このメソッドは、 JUnit コードからも呼ばれます。<br />
     * （ {@link jp.co.acroquest.test.util.JavelinTestUtil#camouflageJavelinConfig(Class, String)} ）
     */
    private static void initialize()
    {
        JavelinConfigUtil configUtil = JavelinConfigUtil.getInstance();
        isRecordExecPlan__ = configUtil.getBoolean(RECORDEXECPLAN_KEY, DEFAULT_RECORDEXECPLAN);
        isFullScanMonitor__ = configUtil.getBoolean(FULLSCAN_MONITOR_KEY, DEFAULT_FULLSCAN_MONITOR);
        isRecordDuplJdbcCall__ =
            configUtil.getBoolean(RECORDDUPLJDBCCALL_KEY, DEFAULT_RECORDDUPLJDBCCALL);
        isRecordBindVal__ = configUtil.getBoolean(RECORDBINDVAL_KEY, DEFAULT_RECORDBINDVAL);
        execPlanThreshold__ = configUtil.getLong(EXECPLANTHRESHOLD_KEY, DEFAULT_EXECPLANTHRESHOLD);
        stringLimitLength__ = configUtil.getLong(STRINGLIMITLENGTH_KEY, DEFAULT_STRINGLIMITLENGTH);
        isSqlcountMonitor__ = configUtil.getBoolean(SQLCOUNT_MONITOR_KEY, DEFAULT_SQLCOUNT_MONITOR);
        sqlcount__ = configUtil.getLong(SQLCOUNT_KEY, DEFAULT_SQLCOUNT);
        isOracleAllowSqlTrace__ =
            configUtil.getBoolean(ORACLE_ALLOW_SQL_TRACE_KEY, DEFAULT_ORACLE_ALLOW_SQL_TRACE);
        isPostgresVerbosePlan__ =
            configUtil.getBoolean(POSTGRES_VERBOSE_PLAN_KEY, DEFAULT_POSTGRES_VERBOSE_PLAN);
        recordStatementNumMaximum__ =
            configUtil.getInteger(RECORD_STATEMENT_NUM_MAXIMUM_KEY,
                                  DEFAULT_MAX_RECORD_STATEMENT_NUM_MAXIMUM);
        isRecordStackTrace__ = configUtil.getBoolean(RECORD_STACKTRACE_KEY, DEF_RECORD_STACKTRACE);
        recordStacktraceThreashold__ =
            configUtil
                .getInteger(RECORD_STACKTRACE_THREADHOLD_KEY, DEF_RECORD_STACKTRACE_THRESHOLD);
        isJdbcJavelinEnabled__ =
            configUtil.getBoolean(JDBC_JAVELIN_ENABLED_KEY, DEF_JDBC_JAVELIN_ENABLED);
        isJdbcJavelinLightweightMode__ =
            configUtil.getBoolean(JDBC_JAVELIN_LIGHTWEIGHT_KEY, DEF_JDBC_JAVELIN_LIGHTWEIGHT);
        logAllSql__ = configUtil.getBoolean(LOG_ALL_SQL_KEY, false);
        logDir__ = configUtil.getString(JDBC_LOG_DIR_KEY, "../traces");
    }

    /**
     * Javelinの設定オブジェクトを作成する。
     */
    public JdbcJavelinConfig()
    {
        this.outputOption_ = "SERIAL";
    }

    /** 
     * SQLの実行計画を記録する際の閾値を返す。
     * 値（ミリ秒）を下回る処理時間の呼び出し情報は記録しない。
     * デフォルト値は0。
     *
     * @return 閾値（ミリ秒）
     */
    public long getExecPlanThreshold()
    {
        return execPlanThreshold__;
    }

    /**
     * SQLの実行計画を記録する際の閾値を設定する。
     * 
     * @param execPlanThreshold 閾値（ミリ秒）
     */
    public void setExecPlanThreshold(final long execPlanThreshold)
    {
        execPlanThreshold__ = execPlanThreshold;
    }

    /** 
     * JDBC呼出し重複出力フラグを返す。
     * trueの場合はアプリからのStatement呼び出し及びDBMSに依存した
     * 内部的なStatement呼び出しの両者をログに出力。
     * デフォルトはfalse。
     *
     * @return JDBC呼出し重複出力フラグ
     */
    public boolean isRecordDuplJdbcCall()
    {
        return isRecordDuplJdbcCall__;
    }

    /**
     * JDBC呼出し重複出力フラグを設定する。
     * 
     * @param recordDuplJdbcCall JDBC呼出し重複出力フラグ
     */
    public void setRecordDuplJdbcCall(final boolean recordDuplJdbcCall)
    {
        isRecordDuplJdbcCall__ = recordDuplJdbcCall;
    }

    /** 
     * 実行計画出力ON/OFFフラグを返す。
     *
     * @return 実行計画取得フラグ
     */
    public boolean isRecordExecPlan()
    {
        return isRecordExecPlan__;
    }

    /**
     * JDBC呼出し重複出力フラグを設定する。
     * 
     * @param recordExecPlan 実行計画取得フラグ
     */
    public void setRecordExecPlan(final boolean recordExecPlan)
    {
        isRecordExecPlan__ = recordExecPlan;
    }

    /** 
     * SQL文のFull Scanを監視するかどうのフラグを返す。
     *
     * @return 実行計画取得フラグ
     */
    public boolean isFullScanMonitor()
    {
        return isFullScanMonitor__;
    }

    /** 
     * SQL文のFull Scanを監視するかどうのフラグをセットする。
     * 
     * @param isFullScanMonitor Full Scan監視フラグ
     */
    public void setIsFullScanMonitor(boolean isFullScanMonitor)
    {
        JdbcJavelinConfig.isFullScanMonitor__ = isFullScanMonitor;
    }

    /** 
     * バインド変数出力フラグを返す。
     * trueの場合はPreparedStatementのバインド変数をログに出力。
     * デフォルトはtrue。
     *
     * @return バインド変数出力フラグ
     */
    public boolean isRecordBindVal()
    {
        return isRecordBindVal__;
    }

    /**
     * バインド変数出力フラグを設定する。
     * 
     * @param recordBindVal 実行計画取得フラグ
     */
    public void setRecordBindVal(final boolean recordBindVal)
    {
        isRecordBindVal__ = recordBindVal;
    }

    /**
     * Oracleの実行計画の出力オプションを返す。
     *
     * @return 出力オプション
     */
    public String getOutputOption()
    {
        return this.outputOption_;
    }

    /**
     * バインド変数出力における文字列長制限を返す。
     * setStringおよびsetObjectメソッドで指定されたバインド変数を文字列化する
     * 際の最大文字列長。
     * デフォルト値は64。
     *
     * @return 最大文字列長
     */
    public long getJdbcStringLimitLength()
    {
        return stringLimitLength__;
    }

    /**
     * バインド変数出力における文字列長制限を設定する。
     * 
     * @param jdbcStringLimitLength 最大文字列長
     */
    public void setJdbcStringLimitLength(final long jdbcStringLimitLength)
    {
        stringLimitLength__ = jdbcStringLimitLength;
    }

    /**
     * 同一トランザクション内の同一SQL呼び出し回数超過の閾値を監視するか否かを返す。
     * 
     * @return 同一トランザクション内の同一SQL呼び出し回数超過の閾値を監視するか否か
     */
    public boolean isSqlcountMonitor()
    {
        return isSqlcountMonitor__;
    }

    /**
     * 同一トランザクション内の同一SQL呼び出し回数超過の閾値を監視するか否かを設定する。
     * 
     * @param sqlcountMonitor 同一トランザクション内の同一SQL呼び出し回数超過の閾値を監視するか否か
     */
    public void setSqlcountMonitor(final boolean sqlcountMonitor)
    {
        isSqlcountMonitor__ = sqlcountMonitor;
    }

    /**
     * 同一トランザクション内の同一SQL呼び出し回数超過の閾値を返す。
     *
     * @return 同一トランザクション内の同一SQL呼び出し回数超過の閾値
     */
    public long getSqlcount()
    {
        return sqlcount__;
    }

    /**
     * 同一トランザクション内の同一SQL呼び出し回数超過の閾値を設定する。
     * 
     * @param sqlcount 同一トランザクション内の同一SQL呼び出し回数超過の閾値
     */
    public void setSqlcount(final long sqlcount)
    {
        sqlcount__ = sqlcount;
    }

    /** 
     * Oracleに対するSQLトレースの出力指示フラグ。
     * trueの場合はスレッドの開始-終了間のSQLトレースを出力する。
     * デフォルトはfalse(出力しない)。
     *
     * @return SQLトレースの出力指示フラグ
     */
    public boolean isAllowSqlTraceForOracle()
    {
        return isOracleAllowSqlTrace__;
    }

    /**
     * Oracleに対するSQLトレースの出力指示フラグを設定する。
     * 
     * @param allowSqlTraceForOracle SQLトレースの出力指示フラグ
     */
    public void setAllowSqlTraceForOracle(final boolean allowSqlTraceForOracle)
    {
        isOracleAllowSqlTrace__ = allowSqlTraceForOracle;
    }

    /**
     * PostgreSQLに対する実行計画詳細取得フラグ。
     * trueの場合は実行計画の要約ではなく、内部表現全てを取得する。
     * falseの場合は要約のみを取得する。
     * 
     * @return PostgreSQLで実行計画詳細を取得する場合は、<code>true</code>
     */
    public boolean isVerbosePlanForPostgres()
    {
        return isPostgresVerbosePlan__;
    }

    /**
     * PostgreSQLに対する実行計画詳細取得フラグを設定する。
     * 
     * @param verbosePlanForPostgres SQLトレースの出力指示フラグ
     */
    public void setVerbosePlanForPostgres(final boolean verbosePlanForPostgres)
    {
        isPostgresVerbosePlan__ = verbosePlanForPostgres;
    }

    /**
     * クエリの最大保存数を取得する。
     * 
     * デフォルト値は1000。
     *
     * @return 最大文字列長
     */
    public int getRecordStatementNumMax()
    {
        return recordStatementNumMaximum__;
    }

    /**
     * クエリの最大保存数を設定する。
     * 
     * @param recordMaxStatementNum クエリの最大保存数
     */
    public void setRecordStatementNumMax(final int recordMaxStatementNum)
    {
        recordStatementNumMaximum__ = recordMaxStatementNum;
    }

    /**
     * JDBCJavelinでスタックトレースを出力するかどうかを返します。<br />
     * 
     * @return JDBCJavelinでスタックトレースを出力する場合、<code>true</code>
     */
    public boolean isRecordStackTrace()
    {
        return isRecordStackTrace__;
    }

    /**
     * JDBCJavelinでスタックトレースを出力するかどうかを返します。<br />
     * 
     * @param isRecordThreashold JDBCJavelinでスタックトレースを出力する場合、<code>true</code>
     */
    public void setRecordStackTrace(boolean isRecordThreashold)
    {
        isRecordStackTrace__ = isRecordThreashold;
    }

    /**
     * JDBCJavelinでスタックトレースを出力するSQL実行時間の閾値を返します。<br />
     * 
     * @return JDBCJavelinでスタックトレースを出力するSQL実行時間の閾値
     */
    public int getRecordStackTraceThreshold()
    {
        return recordStacktraceThreashold__;
    }

    /**
     * JDBCJavelinでスタックトレースを出力するSQL実行時間の閾値を設定します。<br />
     * 
     * @param stackTraceThreshold JDBCJavelinでスタックトレースを出力するSQL実行時間の閾値
     */
    public void setRecordStackTraceThreshold(int stackTraceThreshold)
    {
        recordStacktraceThreashold__ = stackTraceThreshold;
    }

    /**
     * JDBC Javelinが有効かどうかを取得する
     * 
     * @return JDBC Javelinが有効かどうか
     */
    public boolean isJdbcJavelinEnabled()
    {
        return isJdbcJavelinEnabled__;
    }

    /**
     * JDBC Javelinを使用するかどうかを設定する。
     * 
     * @param isJdbcJavelinEnabled JDBC Javelinを使用するかどうか
     */
    public void setJdbcJavelinEnabled(boolean isJdbcJavelinEnabled)
    {
        isJdbcJavelinEnabled__ = isJdbcJavelinEnabled;
    }

    /**
     * JDBCJavelinをLightweightモードで動作させるかどうかを取得する。
     * 
     * @return Lightweightモードで動作させる場合はtrue、そうでない場合はfalseを返す。
     */
    public boolean isJdbcJavelinLightweightMode()
    {
        return isJdbcJavelinLightweightMode__;
    }

    /**
     * JDBCJavelinをLightweightモードで動作させるかどうかを取得する。
     * 
     * @param isJdbcJavelinLightweightMode Lightweightモードで動作させる場合はtrue、そうでない場合はfalse。
     */
    public void setJdbcJavelinLightweightMode(boolean isJdbcJavelinLightweightMode)
    {
        JdbcJavelinConfig.isJdbcJavelinLightweightMode__ = isJdbcJavelinLightweightMode;
    }

    /**
     * 全SQLをログ出力するかどうかを取得する。
     * @return 全SQLをログ出力するかどうか
     */
    public boolean isLogAllSql()
    {
        return logAllSql__;
    }

    /**
     * SQLログの出力先フォルダを取得する。
     * @return SQLログの出力先フォルダ
     */
    public String getLogDir()
    {
        return logDir__;
    }

}
