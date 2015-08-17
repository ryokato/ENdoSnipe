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
package jp.co.acroquest.endosnipe.communicator.entity;

/**
 * 電文に関する定数インターフェースです。<br />
 * 
 * @author y-komori
 */
public interface TelegramConstants
{
    /** 電文種別（状態通知） */
    byte BYTE_TELEGRAM_KIND_ALERT = 0;

    /** 電文種別（状態取得） */
    byte BYTE_TELEGRAM_KIND_GET = 1;

    /** 電文種別（リセット） */
    byte BYTE_TELEGRAM_KIND_RESET = 2;

    /** 電文種別（リソース通知） */
    byte BYTE_TELEGRAM_KIND_RESOURCENOTIFY = 3;

    /** 電文種別（設定変更） */
    byte BYTE_TELEGRAM_KIND_CONFIGCHANGE = 4;

    /** 電文種別（機能呼び出し） */
    byte BYTE_TELEGRAM_KIND_FUNCTIONCALL = 5;

    /** 電文種別(JVNログダウンロード) */
    byte BYTE_TELEGRAM_KIND_JVN_FILE = 6;

    /** 電文種別(サーバプロパティ設定取得) */
    byte BYTE_TELEGRAM_KIND_GET_PROPERTY = 7;

    /** 電文種別(サーバプロパティ設定更新) */
    byte BYTE_TELEGRAM_KIND_UPDATE_PROPERTY = 8;

    /** 電文種別(JVNログ一覧取得) */
    byte BYTE_TELEGRAM_KIND_JVN_FILE_LIST = 9;

    /** 電文種別(計測対象更新) */
    byte BYTE_TELEGRAM_KIND_UPDATE_TARGET = 10;

    /** 電文種別(ダンプ取得) */
    byte BYTE_TELEGRAM_KIND_GET_DUMP = 11;

    /** 電文種別(クラス削除) */
    byte BYTE_TELEGRAM_KIND_REMOVE_CLASS = 12;

    /** 電文種別(閾値超過アラーム/復旧アラーム通知) */
    byte BYTE_TELEGRAM_SIGNAL_STATE_CHANGE = 13;

    /** 電文種別(接続通知) */
    byte BYTE_TELEGRAM_KIND_CONNECT_NOTIFY = 14;

    /** 電文種別(JMX項目通知) */
    byte BYTE_TELEGRAM_KIND_JMX = 15;

    /** 電文種別(DB名増加通知) */
    byte BYTE_TELEGRAM_KIND_ADD_DATABASE_NAME = 16;

    /** 電文種別(DB名減少通知) */
    byte BYTE_TELEGRAM_KIND_DEL_DATABASE_NAME = 17;

    /** 電文種別(シグナル状態取得) */
    byte BYTE_TELEGRAM_KIND_SIGNAL_STATE = 21;

    /** 電文種別(シグナル定義更新) */
    byte BYTE_TELEGRAM_KIND_SIGNAL_DEFINITION = 22;

    /** byte telegram for tree node adding */
    byte BYTE_TELEGRAM_KIND_TREE_ADD_DEFINITION = 23;

    /** byte telegram for tree node deleting */
    byte BYTE_TELEGRAM_KIND_TREE_DELETE_DEFINITION = 24;

    /** 電文種別(ツリー定義更新) */
    byte BYTE_TELEGRAM_KIND_MUL_RES_GRAPH_DEFINITION = 25;

    /** 電文種別(ツリー定義更新) */
    byte BYTE_TELEGRAM_KIND_ADD_MUL_RES_GRAPH_DEFINITION = 27;

    /** 電文種別(SQL実行計画取得通知) */
    byte BYTE_TELEGRAM_KIND_SQL_PLAN = 28;

    /**
     * threadDump response telegram
     */
    byte BYTE_TELEGRAM_KIND_THREAD_DUMP = 31;

    /** Summary Signal の　telegram kind*/

    byte BYTE_TELEGRAM_KIND_SUMMARYSIGNAL_DEFINITION = 29;

    /** Summary Signal の　telegram kind*/
    byte BYTE_TELEGRAM_KIND_ADD_STATE_CHANGE_SUMMARYSIGNAL_DEFINITION = 30;

    /** 最後の電文以外 */
    byte HALFWAY_TELEGRAM = 0;

    /** 最後の電文 */
    byte FINAL_TELEGRAM = 1;

    /** 要求応答種別（通知） */
    byte BYTE_REQUEST_KIND_NOTIFY = 0;

    /** 要求応答種別（要求） */
    byte BYTE_REQUEST_KIND_REQUEST = 1;

    /** 要求応答種別（応答） */
    byte BYTE_REQUEST_KIND_RESPONSE = 2;

    /** オブジェクト名（リソース通知でのリソース値） */
    String OBJECTNAME_RESOURCE = "resources";

    /** オブジェクト名（リソース通知での時刻） */
    String TIME_RESOURCE = "resource-time";

    /** オブジェクト名（機能呼び出しでの接続管理） */
    String OBJECTNAME_CONNECTIONMANAGER = "connectionManager";

    /** オブジェクト名（機能呼び出しでの表示操作） */
    String OBJECTNAME_VIEWOPERATION = "viewOperation";

    /** オブジェクト名（機能呼び出しでの強制 Full GC 実行） */
    String OBJECTNAME_FORCEFULLGC = "forceFullGC";

    /** オブジェクト名（JVNファイル） */
    String OBJECTNAME_JVN_FILE = "jvnFile";

    /** オブジェクト名(ダンプ取得) */
    String OBJECTNAME_DUMP = "dump";

    /** オブジェクト名(計測アラーム) */
    String OBJECTNAME_RESOURCEALARM = "resourceAlarm";

    /** オブジェクト名(シグナル定義変更) */
    String OBJECTNAME_SIGNAL_CHANGE = "signalDefinitionChange";

    /** Summary Signal Telegram object name to notify change state*/
    String OBJECTNAME_SUMMARY_SIGNAL_CHANGE = "summarySignalDefinitionChange";

    /** オブジェクト名(ツリー定義変更) */
    String OBJECTNAME_TREE_CHANGE = "treeDefinitionChange";

    /** オブジェクト名（接続情報通知での接続情報） */
    String OBJECTNAME_CONNECTINFO = "connectInformation";

    /** オブジェクト名(JMXオブジェクト名) */
    String OBJECTNAME_JMX = "jmx";

    /** オブジェクト名(DB名) */
    String OBJECTnAME_DATABASE_NAME = "databaseName";

    /** オブジェクト名(SQL文) */
    String OBJECTNAME_SQL_STATEMENT = "sqlplan";

    /** オブジェクト名(SQL実行計画) */
    String OBJECTNAME_SQL_EXECUTION_PLAN = "sqlexecution";

    /** オブジェクト名(SQL実行計画取得時間) */
    String OBJECTNAME_GETTING_PLAN_TIME = "gettingplantime";

    /** オブジェクト名(スタックとレース) */
    String OBJECTNAME_STACK_TRACE = "stacktrace";

    // ////////////////
    // 制御用項目名 //
    // ////////////////

    /** 項目名（初期化処理） */
    String ITEMNAME_INITIALIZE = "/initialize";

    /** 項目名(ヒープダンプ取得) */
    String ITEMNAME_HEAPDUMP = "/heapDump";

    /** 項目名(スレッドダンプ取得) */
    String ITEMNAME_THREADDUMP = "threadDump";

    /** 項目名(クラスヒストグラム取得) */
    String ITEMNAME_CLASSHISTOGRAM = "classHistogramDump";

    /** 項目名(セッションダンプ取得) */
    String ITEMNAME_SESSIONDUMP = "sessionDump";

    /** 項目名(機能呼び出しでの強制 Full GC) */
    String ITEMNAME_FORCEFULLGC = "forceFullGC";

    // ////////////////
    // 取得用項目名 //
    // ////////////////
    // 項目名(DBから取得する系列を指定するキー文字列)

    // -----------------------------------------------------
    // 基礎項目

    /** 項目名（時刻） */
    String ITEMNAME_TIME = "/common/fundamental/time/current";

    /** 項目名（リソース値での取得時刻） */
    String ITEMNAME_ACQUIREDTIME = "/common/fundamental/time/acquired";

    /** 項目名(呼び出し回数) */
    String ITEMNAME_CALL_COUNT = "/common/fundamental/callCount";

    /** 項目名(今回の処理時間) */
    String ITEMNAME_CURRENT_INTERVAL = "/common/fundamental/interval/current";

    /** 項目名(合計処理時間) */
    String ITEMNAME_TOTAL_INTERVAL = "/common/fundamental/interval/total";

    /** 項目名(最大処理時間) */
    String ITEMNAME_MAXIMUM_INTERVAL = "/common/fundamental/interval/max";

    /** 項目名(最小処理時間) */
    String ITEMNAME_MINIMUM_INTERVAL = "/common/fundamental/interval/min";

    /** 項目名(合計CPU時間) */
    String ITEMNAME_TOTAL_CPU_INTERVAL = "/common/fundamental/cpuInterval/total";

    /** 項目名(最大CPU時間) */
    String ITEMNAME_MAXIMUM_CPU_INTERVAL = "/common/fundamental/cpuInterval/max";

    /** 項目名(最小CPU時間) */
    String ITEMNAME_MINIMUM_CPU_INTERVAL = "/common/fundamental/cpuInterval/min";

    /** 項目名(合計ユーザ時間) */
    String ITEMNAME_TOTAL_USER_INTERVAL = "/common/fundamental/userInterval/total";

    /** 項目名(最大ユーザ時間) */
    String ITEMNAME_MAXIMUM_USER_INTERVAL = "/common/fundamental/userInterval/max";

    /** 項目名(最小ユーザ時間) */
    String ITEMNAME_MINIMUM_USER_INTERVAL = "/common/fundamental/userInterval/min";

    // -----------------------------------------------------
    // 累積値

    /** 項目名(合計処理時間) */
    String ITEMNAME_ACCUMULATED_TOTAL_INTERVAL = "/common/accumulated/interval/total";

    /** 項目名(最大処理時間) */
    String ITEMNAME_ACCUMULATED_MAXIMUM_INTERVAL = "/common/accumulated/interval/max";

    /** 項目名(最小処理時間) */
    String ITEMNAME_ACCUMULATED_MINIMUM_INTERVAL = "/common/accumulated/interval/min";

    /** 項目名(合計CPU時間) */
    String ITEMNAME_ACCUMULATED_TOTAL_CPU_INTERVAL = "/common/accumulated/cpuInterval/total";

    /** 項目名(最大CPU時間) */
    String ITEMNAME_ACCUMULATED_MAXIMUM_CPU_INTERVAL = "/common/accumulated/cpuInterval/max";

    /** 項目名(最小CPU時間) */
    String ITEMNAME_ACCUMULATED_MINIMUM_CPU_INTERVAL = "/common/accumulated/cpuInterval/min";

    /** 項目名(合計CPU時間) */
    String ITEMNAME_ACCUMULATED_TOTAL_USER_INTERVAL = "/common/accumulated/userInterval/total";

    /** 項目名(最大CPU時間) */
    String ITEMNAME_ACCUMULATED_MAXIMUM_USER_INTERVAL = "/common/accumulated/userInterval/max";

    /** 項目名(最小CPU時間) */
    String ITEMNAME_ACCUMULATED_MINIMUM_USER_INTERVAL = "/common/accumulated/userInterval/min";

    // -----------------------------------------------------
    // システム項目: 基礎値

    /** 項目名（リソース値でのプロセッサ数） */
    String ITEMNAME_SYSTEM_CPU_PROCESSOR_COUNT = "/system/cpu/processor/number";

    /** 項目名（ユーザモードでのCPU使用量） */
    String ITEMNAME_SYSTEM_CPU_USERMODE_TIME = "/system/cpu/time/user(d)";

    /** 項目名（システムモードでのCPU使用量） */
    String ITEMNAME_SYSTEM_CPU_SYSTEM_TIME = "/system/cpu/time/system(d)";

    /** 項目名（iowaitでのCPU使用量） */
    String ITEMNAME_SYSTEM_CPU_IOWAIT_TIME = "/system/cpu/time/iowait(d)";

    // -----------------------------------------------------
    // システム項目: CPU(算出値)

    /** 項目名（CPU使用率（システム）の合計） */
    String ITEMNAME_SYSTEM_CPU_TOTAL_USAGE = "/system/cpu/usage/total:%";

    /** 項目名（CPU使用率（システム）のうちのシステムの使用率） */
    String ITEMNAME_SYSTEM_CPU_SYSTEM_USAGE = "/system/cpu/usage/system:%";

    /** 項目名（CPU使用率（システム）のうちのI/O Waitの使用率） */
    String ITEMNAME_SYSTEM_CPU_IOWAIT_USAGE = "/system/cpu/usage/iowait:%";

    /** 項目名（CPUごとの負荷：後ろにCPUコア番号(1～N)を付ける） */
    String ITEMNAME_CPU_ARRAY = "/system/cpu/processor";

    // -----------------------------------------------------
    // システム項目: メモリ(算出値)

    /** 項目名（システム全体のメモリ最大値） */
    String ITEMNAME_SYSTEM_MEMORY_PHYSICAL_MAX = "/system/memory/physical/max:bytes";

    /** 項目名（システム全体の空きメモリ） */
    String ITEMNAME_SYSTEM_MEMORY_PHYSICAL_FREE = "/system/memory/physical/free:bytes";

    /** 項目名（システム全体の空きメモリ） */
    String ITEMNAME_SYSTEM_MEMORY_PHYSICAL_USED = "/system/memory/physical/used:bytes";

    /** 項目名（リソース値での仮想メモリ容量） */
    String ITEMNAME_SYSTEM_MEMORY_VIRTUAL_USED = "/system/memory/virtual/used:bytes";

    /** 項目名（リソース値でのスワップ領域容量） */
    String ITEMNAME_SYSTEM_MEMORY_SWAP_MAX = "/system/memory/swap/max:bytes";

    /** 項目名（リソース値でのスワップ領域空き容量） */
    String ITEMNAME_SYSTEM_MEMORY_SWAP_FREE = "/system/memory/swap/free:bytes";

    /** 項目名（page in） */
    String ITEMNAME_SYSTEM_MEMORY_PAGEIN_COUNT = "/system/memory/pagein(d)";

    /** 項目名（page out） */
    String ITEMNAME_SYSTEM_MEMORY_PAGEOUT_COUNT = "/system/memory/pageout(d)";

    // -----------------------------------------------------
    // システム項目: ファイル

    /** 項目名（リソース値での、ファイル入力量） */
    String ITEMNAME_FILEINPUTSIZEOFSYSTEM = "/system/file/read:bytes(d)";

    /** 項目名（リソース値での、ファイル出力量） */
    String ITEMNAME_FILEOUTPUTSIZEOFSYSTEM = "/system/file/write:bytes(d)";

    /** 項目名（FD/ハンドル数） */
    String ITEMNAME_SYSTEM_HANDLE_TOTAL_NUMBER = "/system/file/handle/number";

    // -----------------------------------------------------
    // プロセス項目: CPU(基礎値)

    /** 項目名（CPU使用時間:total） */
    String ITEMNAME_PROCESS_CPU_TOTAL_TIME = "/process/cpu/time/total(d)";

    /** 項目名（CPU使用時間:system） */
    String ITEMNAME_PROCESS_CPU_SYSTEM_TIME = "/process/cpu/time/system(d)";

    /** 項目名（CPU使用時間:iowait） */
    String ITEMNAME_PROCESS_CPU_IOWAIT_TIME = "/process/cpu/time/iowait(d)";

    /** 項目名（リソース値でのJava稼働時間） */
    String ITEMNAME_JAVAUPTIME = "/process/fundamental/uptime";

    // -----------------------------------------------------
    // プロセス項目: CPU(算出値)

    /** 項目名（CPU使用率（プロセス）の合計） */
    String ITEMNAME_PROCESS_CPU_TOTAL_USAGE = "/process/cpu/usage/total:%";

    /** 項目名（CPU使用率（プロセス）のうちのシステムの使用率） */
    String ITEMNAME_PROCESS_CPU_SYSTEM_USAGE = "/process/cpu/usage/system:%";

    /** 項目名（CPU使用率（プロセス）のうちのIOWAITの使用率） */
    String ITEMNAME_PROCESS_CPU_IOWAIT_USAGE = "/process/cpu/usage/iowait:%";

    /** jdbc node under response */
    String PREFIX_PROCESS_RESPONSE_JDBC = "/process/response/jdbc/";

    /** servlet node under response */
    String PREFIX_PROCESS_RESPONSE_SERVLET = "/process/response/servlet";

    /** method node under response */
    String PREFIX_PROCESS_RESPONSE_METHOD = "/process/response/method/";

    /** max (Maximum turn around time)*/
    String POSTFIX_MAX = "/max";

    /** min (Minimum turn around time) */
    String POSTFIX_MIN = "/min";

    /** average (Average turn around time) */
    String POSTFIX_AVERAGE = "/average";

    /** count (Number of accessing) */
    String POSTFIX_COUNT = "/count";

    /** event (Event node when an event is detected) */
    String POSTFIX_EVENT = "/event/";

    /**  error (Error node when an error is happened) */
    String POSTFIX_ERROR = "/error/";

    /** stalled */
    String POSTFIX_STALLED = "/stalled";

    // -----------------------------------------------------
    // プロセス項目: メモリ(算出値)

    /** 項目名（リソース値での物理メモリ容量） */
    String ITEMNAME_PROCESS_MEMORY_PHYSICAL_MAX = "/process/memory/physical/max:bytes";

    /** 項目名（物理メモリ使用量（プロセス）） */
    String ITEMNAME_PROCESS_MEMORY_PHYSICAL_USED = "/process/memory/physical/used:bytes";

    /** 項目名（リソース値での物理メモリ空き容量） */
    String ITEMNAME_PROCESS_MEMORY_PHYSICAL_FREE = "/process/memory/physical/free:bytes";

    /** 項目名（リソース値での仮想マシンメモリ容量） */
    String ITEMNAME_PROCESS_MEMORY_VIRTUALMACHINE_MAX = "/process/memory/virtual/max:bytes";

    /** 項目名（仮想メモリ使用量（プロセス）） */
    String ITEMNAME_PROCESS_MEMORY_VIRTUAL_USED = "/process/memory/virtual/used:bytes";

    /** 項目名（リソース値での仮想マシンメモリ空き容量） */
    String ITEMNAME_PROCESS_MEMORY_VIRTUALMACHINE_FREE = "/process/memory/virtual/free:bytes";

    /** 項目名（リソース値でのヒープメモリコミット容量） */
    String ITEMNAME_JAVAPROCESS_MEMORY_HEAP_COMMIT = "/process/heap/commit:bytes";

    /** 項目名（リソース値でのヒープメモリ最大） */
    String ITEMNAME_JAVAPROCESS_MEMORY_HEAP_MAX = "/process/heap/max:bytes";

    /** 項目名（リソース値でのヒープメモリ使用量） */
    String ITEMNAME_JAVAPROCESS_MEMORY_HEAP_USED = "/process/heap/used:bytes";

    /** 項目名（リソース値でのヒープ以外のメモリコミット容量） */
    String ITEMNAME_JAVAPROCESS_MEMORY_NONHEAP_COMMIT = "/process/nonheap/commit:bytes";

    /** 項目名（リソース値でのヒープ以外のメモリ最大） */
    String ITEMNAME_JAVAPROCESS_MEMORY_NONHEAP_MAX = "/process/nonheap/max:bytes";

    /** 項目名（リソース値でのヒープ以外のメモリ使用量） */
    String ITEMNAME_JAVAPROCESS_MEMORY_NONHEAP_USED = "/process/nonheap/used:bytes";

    /** 項目名（メジャーフォールト数） */
    String ITEMNAME_PROCESS_MEMORY_MAJORFAULT_COUNT = "/process/memory/majorfault/count(d)";

    // -----------------------------------------------------
    // プロセス項目: ネットワーク

    /** 項目名（リソース値での、プロセス全体のネットワークデータ受信量） */
    String ITEMNAME_NETWORKINPUTSIZEOFPROCESS = "/process/network/read:bytes(d)";

    /** 項目名（リソース値での、プロセス全体のネットワークデータ送信量） */
    String ITEMNAME_NETWORKOUTPUTSIZEOFPROCESS = "/process/network/write:bytes(d)";

    // -----------------------------------------------------
    // プロセス項目: ファイル

    /** 項目名（リソース値での、ファイル入力量） */
    String ITEMNAME_FILEINPUTSIZEOFPROCESS = "/process/file/read:bytes(d)";

    /** 項目名（リソース値での、ファイル出力量） */
    String ITEMNAME_FILEOUTPUTSIZEOFPROCESS = "/process/file/write:bytes(d)";

    // -----------------------------------------------------
    // プロセス項目: スレッド

    /** 項目名（スレッド数） */
    String ITEMNAME_PROCESS_THREAD_TOTAL_COUNT = "/process/thread/native";

    /** 項目名（FD/ハンドル数） */
    String ITEMNAME_PROCESS_HANDLE_TOTAL_NUMBER = "/process/file/handle/number";

    /** 項目名（リソース値での、スレッド数） */
    String ITEMNAME_JAVAPROCESS_THREAD_TOTAL_COUNT = "/process/thread/java/total";

    /** 項目名(RUNNBLEなスレッド数) */
    String ITEMNAME_RUNNABLE_THREAD_COUNT = "/process/thread/java/runnable";

    /** 項目名(RUNNBLEなスレッド数) */
    String ITEMNAME_BLOCKED_THREAD_COUNT = "/process/thread/java/blocked";

    // -----------------------------------------------------
    // プロセス情報: コレクション情報

    /** 項目名（リソース値での、コレクションの数） */
    String ITEMNAME_JAVAPROCESS_COLLECTION_LIST_COUNT = "/process/collection/list";

    /** 項目名（リソース値での、コレクションの数） */
    String ITEMNAME_JAVAPROCESS_COLLECTION_QUEUE_COUNT = "/process/collection/queue";

    /** 項目名（リソース値での、コレクションの数） */
    String ITEMNAME_JAVAPROCESS_COLLECTION_SET_COUNT = "/process/collection/set";

    /** 項目名（リソース値での、コレクションの数） */
    String ITEMNAME_JAVAPROCESS_COLLECTION_MAP_COUNT = "/process/collection/map";

    /** 項目名（リソース値での、クラスヒストグラムから取得したオブジェクトのサイズ） */
    String ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_SIZE = "/process/histogram/"
        + "object/size:bytes";

    /** 項目名（リソース値での、クラスヒストグラムから取得したオブジェクトの数） */
    String ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_COUNT = "/process/histogram/object/number";

    // -----------------------------------------------------
    // プロセス情報: レスポンス情報

    /** 項目名（リソース値での、Turn Around Timeの平均値） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_AVERAGE = "/process/response/total/average";

    /** 項目名（リソース値での、Turn Around Timeの最大値） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_MAX = "/process/response/total/max";

    /** 項目名（リソース値での、Turn Around Timeの最小値） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_MIN = "/process/response/total/min";

    /** 項目名（リソース値での、Turn Around Timeの呼び出し回数） */
    String ITEMNAME_PROCESS_RESPONSE_TOTAL_COUNT = "/process/response/total/count";

    /** 接頭辞（リソース値での、eventの発生回数） */
    String PREFIX_PROCESS_RESPONSE_EVENT = "/process/response/page/event";

    /** 項目名（リソース値での、Turn Around Timeの平均値(SQL以外)） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_AVERAGE_EXCL_SQL = "/process/response/nosql/average";

    /** 項目名（リソース値での、Turn Around Timeの最大値(SQL以外)） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_MAX_EXCL_SQL = "/process/response/nosql/max";

    /** 項目名（リソース値での、Turn Around Timeの最小値(SQL以外)） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_MIN_EXCL_SQL = "/process/response/nosql/min";

    /** 項目名（リソース値での、Turn Around Timeの呼び出し回数(SQL以外)） */
    String ITEMNAME_PROCESS_RESPONSE_TOTAL_COUNT_EXCL_SQL = "/process/response/nosql/count";

    /** 項目名（リソース値での、Turn Around Timeの平均値(SQLのみ)） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_AVERAGE_ONLY_SQL = "/process/response/sql/average";

    /** 項目名（リソース値での、Turn Around Timeの最大値(SQLのみ)） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_MAX_ONLY_SQL = "/process/response/sql/max";

    /** 項目名（リソース値での、Turn Around Timeの最小値(SQLのみ)） */
    String ITEMNAME_PROCESS_RESPONSE_TIME_MIN_ONLY_SQL = "/process/response/sql/min";

    /** 項目名（リソース値での、Turn Around Timeの呼び出し回数(SQLのみ)） */
    String ITEMNAME_PROCESS_RESPONSE_TOTAL_COUNT_ONLY_SQL = "/process/response/sql/count";

    /** 項目名(HTTP例外) */
    String ITEMNAME_JAVAPROCESS_HTTP_EXCEPTION = "/process/response/http/thrown";

    // -----------------------------------------------------
    // プロセス情報: VM

    /** 項目名(Java 仮想マシンが実行を開始してからロードされたクラスの合計数) */
    String ITEMNAME_JAVAPROCESS_CLASSLOADER_CLASS_TOTAL = "/process/classloader/class/total";

    /** 項目名(Java 仮想マシンに現在ロードされているクラスの数) */
    String ITEMNAME_JAVAPROCESS_CLASSLOADER_CLASS_CURRENT = "/process/classloader/"
        + "class/current";

    /** 項目名（リソース値での、トータルのガベージコレクションの時間） */
    String ITEMNAME_JAVAPROCESS_GC_TIME_TOTAL = "/process/gc/time/total(d)";

    /** 項目名（リソース値での、ファイナライズ待ちオブジェクト数） */
    String ITEMNAME_JAVAPROCESS_GC_FINALIZEQUEUE_COUNT = "/process/gc/finalizequeue/number";

    /** 項目名(例外発生回数) */
    String ITEMNAME_JAVAPROCESS_EXCEPTION_OCCURENCE_COUNT = "/process/response/java/error";

    /** 項目名(ストール発生回数) */
    String ITEMNAME_JAVAPROCESS_STALL_OCCURENCE_COUNT = "/process/response/java/stalled";

    // -----------------------------------------------------
    // プロセス情報: APサーバ情報

    /** 項目名（APサーバのワーカースレッドプール(最大数,稼動数)） */
    String ITEMNAME_SERVER_POOL = "/process/apserver/worker/number";

    /** 項目名（プール(最大数,稼動数)） */
    String ITEMNAME_POOL_SIZE = "/process/commons/poolsize";

    /** 項目名（リソース値での、HttpSession数） */
    String ITEMNAME_HTTPSESSION_NUM = "/process/httpsession/instance/number";

    /** 項目名（リソース値での、HttpSession総サイズ） */
    String ITEMNAME_HTTPSESSION_TOTALSIZE = "/process/httpsession/size/total:bytes";

    // -----------------------------------------------------
    // Agent情報

    /** 項目名（カバレッジ） */
    String ITEMNAME_COVERAGE = "/javelin/converter/coverage:%";

    /** 項目名（イベント種別毎のイベント発生回数） */
    String ITEMNAME_EVENT_COUNT = "/javelin/event/occured/count";

    /** 項目名(CallNodeTree数) */
    String ITEMNAME_NODECOUNT = "/javelin/calltreenode/generated/current";

    /** 項目名(最大CallTreeNode数) */
    String ITEMNAME_MAX_NODECOUNT = "/javelin/calltreenode/generated/max";

    /** 項目名(合計CallTreeNode数) */
    String ITEMNAME_ALL_NODECOUNT = "/javelin/calltreenode/generated/all";

    /** 項目名(CallTree数) */
    String ITEMNAME_CALLTREECOUNT = "/javelin/calltree/generated/current";

    /** 項目名(JavelinConverterで変換したメソッド数) */
    String ITEMNAME_CONVERTEDMETHOD = "/javelin/converter/methods/converted";

    /** 項目名(JavelinConverterで変換対象から除外したメソッド数) */
    String ITEMNAME_EXCLUDEDMETHOD = "/javelin/converter/methods/excluded";

    /** 項目名(JavelinConverterで変換を行ったメソッドのうち、呼び出されたメソッド数) */
    String ITEMNAME_CALLEDMETHODCOUNT = "/javelin/converter/methods/executed";

    /** 項目名（JVNファイル JVNファイル名） */
    String ITEMNAME_JVN_FILE_NAME = "/javelin/jvnfile/name";

    /** 項目名（JVNファイル JVNファイル内容） */
    String ITEMNAME_JVN_FILE_CONTENT = "/javelin/jvnfile/contents";

    /** 項目名（アイテム名） */
    String ITEMNAME_JVN_ITEM_NAME = "/javelin/jvnfile/itemname";

    // -----------------------------------------------------
    // その他

    /** 項目名（jmx） */
    String ITEMNAME_JMX = "/jmx";

    // -----------------------------------------------------
    // 監視情報

    /** 項目名（接続通知：接続元種別） */
    String ITEMNAME_CONNECTNOTIFY_KIND = "kind";

    /** 項目名（接続通知：DB名） */
    String ITEMNAME_CONNECTNOTIFY_DBNAME = "dbName";

    /** 項目名(接続通知：接続目的) */
    String ITEMNAME_CONNECTNOTIFY_PURPOSE = "purpose";

    /** 項目名(全ての呼び出し元の名前) */
    String ITEMNAME_ALL_CALLER_NAMES = "/allCallerNames";

    /** 項目名(ルート) */
    String ITEMNAME_ROOT_NODE = "/rootNode";

    /** 項目名(計測対象) */
    String ITEMNAME_TARGET = "/target";

    /** 項目名(トランザクショングラフ出力対象) */
    String ITEMNAME_TRANSACTION_GRAPH = "/transactionGraph";

    /** 項目名(TATアラーム閾値) */
    String ITEMNAME_ALARM_THRESHOLD = "/alarmThreshold";

    /** 項目名(CPUアラーム閾値) */
    String ITEMNAME_ALARM_CPU_THRESHOLD = "/alarmCpuThreshold";

    /** 項目名(クラス名) */
    String ITEMNAME_CLASSTOREMOVE = "/classToRemove";

    /** 項目名(HTTPエラー発生回数) */
    String ITEMNAME_HTTP_ERRCOUNT = "httpErrorCount";

    /** 項目名(Hadoop:NameNode) */
    String ITEMNAME_HADOOP_NAMENODE = "/hadoop/NameNode";

    /** 項目名(Hadoop:JobTracker) */
    String ITEMNAME_HADOOP_JOBTRACKER = "/hadoop/JobTracker";

    /** 項目名(Infinispan:mapreduce) */
    String ITEMNAME_INFINISPAN_MAPREDUCE = "/infinispan/MapReduce";

    /** 項目名(追加測定項目) */
    String ITEMNAME_OPTIONAL_RESOURCE = "/resource";

    /** 項目名(Hadoop:DataNode) */
    String ITEMNAME_HADOOP_DATANODE = "/hadoop/DataNode";

    /** 項目名(Hadoop:TaskTracker) */
    String ITEMNAME_HADOOP_TASKTRACKER = "/hadoop/TaskTracker";

    /** 項目名(HBase:HMaster) */
    String ITEMNAME_HBASE_HMASTER = "/hbase/HMaster";

    /** 項目名(HBase:HRegionServer) */
    String ITEMNAME_HBASE_HREGIONSERVER = "/hbase/HRegionServer";

    /** 項目名（計測ID） */
    String ITEMNAME_MEASUREMENT_TYPE = "measurementType";

    /** 項目名（閾値判定定義情報のID） */
    String ITEMNAME_SIGNAL_ID = "signalId";

    /** 項目名（シグナル名） */
    String ITEMNAME_SIGNAL_NAME = "signalName";

    /** 項目名（閾値超過・復旧判定する時間） */
    String ITEMNAME_ESCALATION_PERIOD = "escalationPeriod";

    /** 項目名（アラームの種類） */
    String ITEMNAME_ALARM_TYPE = "alarmType";

    /** 項目名（アラーム発生時の障害状態） */
    String ITEMNAME_SIGNAL_VALUE = "signalValue";

    /** 項目名（閾値判定定義情報のレベル） */
    String ITEMNAME_SIGNAL_LEVEL = "signalLevel";

    /** 項目名（各レベル毎の閾値） */
    String ITEMNAME_PATTERN_VALUE = "patternValue";

    /** 項目名（閾値判定パターン） */
    String ITEMNAME_MATCHING_PATTERN = "matchingPattern";

    /** 項目名（メール送信有無) */
    String ITEMNAME_SEND_MAIL = "sendMail";

    /** 項目名(シグナル定義追加) */
    String ITEMNAME_SIGNAL_ADD = "signalAdd";

    /** 項目名(シグナル定義更新) */
    String ITEMNAME_SIGNAL_UPDATE = "signalUpdate";

    /** 項目名(シグナル定義削除) */
    String ITEMNAME_SIGNAL_DELETE = "signalDelete";

    /** Summary Signal Error Message for process*/
    String ITEMNAME_SUMMARY_SIGNAL_ERROR = "summarySignalErrorMessage";

    /** Summary Signal Status */
    String ITEMNAME_SUMMARY_SIGNAL_STATUS = "summarySignalStatus";

    /** Summary Signal child list */
    String ITEMNAME_SUMMARY_SIGNAL_CHILDLIST = "summarySignalChildList";

    /** Summary Signal Name */
    String ITEMNAME_SUMMARY_SIGNAL_NAME = "summarySignalName";

    /** Summary Signal Id */
    String ITEMNAME_SUMMARY_SIGNAL_ID = "summarySignalId";

    /** Summary Signal Type */
    String ITEMNAME_SUMMARY_SIGNAL_TYPE = "summarySignalDefinitionType";

    /**Summary Signal Add Process*/
    String ITEMNAME_SUMMARY_SIGNAL_ADD = "summarySignalAdd";

    /** Summary Signal Update Process**/
    String ITEMNAME_SUMMARY_SIGNAL_UPDATE = "summarySignalUpdate";

    /** Summary Signal Delete Process* */
    String ITEMNAME_SUMMARY_SIGNAL_DELETE = "summarySignalDelete";

    /** Summary Signal Get All Process**/
    String ITEMNAME_SUMMARY_SIGNAL_ALL = "summarySignalAll";

    /** Summary Signal Change State Notification* */
    String ITEMNAME_SUMMARY_SIGNAL_CHANGE_STATE = "summarySignalChangeState";

    /** 項目名(ツリー定義追加) */
    String ITEMNAME_TREE_ADD = "treeAdd";

    /** tree delete message */
    String ITEMNAME_TREE_DELETE = "treeDelete";

    /** 項目名（ツリー表示名） */
    String ITEMNAME_TREE_DATA = "data";

    /** 項目名（ツリーID） */
    String ITEMNAME_TREE_TREEID = "treeId";

    /** 項目名（親ツリーID） */
    String ITEMNAME_TREE_PARENTTREEID = "parentTreeId";

    /** 項目名（ID） */
    String ITEMNAME_TREE_ID = "id";

    /** 項目名（ツリーのタイプ） */
    String ITEMNAME_TREE_TYPE = "type";

    /** 項目名（アイコン） */
    String ITEMNAME_TREE_ICON = "icon";

    /** 項目名（計測単位） */
    String ITEMNAME_TREE_MEASUREMENTUNIT = "measurementUnit";

    /** 項目名（閾値判定定義情報のID） */
    String ITEMNAME_MUL_RES_GRAPH_ID = "mulResourceGraphId";

    /** 項目名(シグナル定義追加) */
    String ITEMNAME_MUL_RES_GRAPH_ADD = "mulResourceGraphAdd";

    /** 項目名(シグナル定義更新) */
    String ITEMNAME_MUL_RES_GRAPH_UPDATE = "mulResourceGraphUpdate";

    /** 項目名(シグナル定義削除) */
    String ITEMNAME_MUL_RES_GRAPH_DELETE = "mulResourceGraphDelete";

    /** int ⇔ byte[] 変換時に対応するバイト数 */
    int INT_BYTE_SWITCH_LENGTH = 4;

    /** long ⇔ byte[] 変換時に対応するバイト数 */
    int LONG_BYTE_SWITCH_LENGTH = 8;

    /** 項目型（１バイト符号付整数） */
    byte BYTE_ITEMMODE_KIND_BYTE = 0;

    /** 項目型（４バイト符号付整数） */
    byte BYTE_ITEMMODE_KIND_4BYTE_INT = 2;

    /** 項目型（８バイト符号付整数） */
    byte BYTE_ITEMMODE_KIND_8BYTE_INT = 3;

    /** 項目型（文字列） */
    byte BYTE_ITEMMODE_KIND_STRING = 6;

    /** ループ回数（1回） */
    int INT_LOOP_COUNT_SINGLE = 1;

    /** クラス名、メソッド名のセパレータ */
    String CLASSMETHOD_SEPARATOR = "###CLASSMETHOD_SEPARATOR###";

    /** 項目名(ストール検出回数) */
    String ITEMNAME_JAVAPROCESS_METHOD_STALL_COUNT = "javaprocess.method.stall.count";
}
