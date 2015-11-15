package jp.co.acroquest.endosnipe.report.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.co.acroquest.endosnipe.common.util.SQLUtil;
import jp.co.acroquest.endosnipe.data.TableNames;
import jp.co.acroquest.endosnipe.data.dao.AbstractDao;
import jp.co.acroquest.endosnipe.data.db.DBManager;
import jp.co.acroquest.endosnipe.report.entity.ReportItemValue;

/**
 * レポート出力用のDao
 * 
 * @author eriguchi
 * 
 */
public class ReportDao extends AbstractDao implements TableNames
{
	/** 最大件数 */
	public static final int ITEM_COUNT = 200;

	/** 平均値計算用のSQL */
	private static final String SQL_AVERAGE = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    , ji.measurement_item_name measurement_item_name"
		+ "    , avg(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , max(cast(mv.measurement_value as double precision)) max_value"
		+ "    , min(cast(mv.measurement_value as double precision)) min_value"
		+ "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time) - date_part('epoch', ?::timestamp)) "
		+ "    / ?"
		+ ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "//
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?" //
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ "GROUP BY measurement_index, ji.measurement_item_name " + "ORDER BY"
		+ "    ji.measurement_item_name, measurement_time";

	/** 合計値計算用のSQL */
	private static final String SQL_SUM = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    , ji.measurement_item_name item_name"
		+ "    , sum(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) max_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) min_value"
		+ "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time) - date_part('epoch', ?::timestamp)) "
		+ "    / ?"
		+ ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?"
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ "GROUP BY measurement_index, ji.measurement_item_name " + "ORDER BY"
		+ "    ji.measurement_item_name, measurement_time";

	/** 平均値計算用のSQL(H2) */
	private static final String SQL_AVERAGE_ALL_H2 = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    , case when substring(ji.measurement_item_name, 0, 1) = '/' then '/' "
		+ "           when substring(ji.measurement_item_name, 0, 5) = 'jdbc:' then 'jdbc:' "
		+ "           else 'java' end item_name_head"
		+ "    , avg(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , max(cast(mv.measurement_value as double precision)) max_value"
		+ "    , min(cast(mv.measurement_value as double precision)) min_value" + "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time) - date_part('epoch', ?::timestamp)) "
		+ "    / ?" + ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "//
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?" //
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ " GROUP BY measurement_index, item_name_head" + " ORDER BY" + "    measurement_time";

	/** 平均値計算用のSQL(postgres) */
	private static final String SQL_AVERAGE_ALL_POSTGRES = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    , case when substring(ji.measurement_item_name, 0, 2) = '/' then '/' "
		+ "           when substring(ji.measurement_item_name, 0, 6) = 'jdbc:' then 'jdbc:' "
		+ "           else 'java' end item_name_head"
		+ "    , avg(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , max(cast(mv.measurement_value as double precision)) max_value"
		+ "    , min(cast(mv.measurement_value as double precision)) min_value" + "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time)  - date_part('epoch', ?::timestamp)) "
		+ "    / ?" + ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "//
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?" //
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ " GROUP BY measurement_index, item_name_head" + " ORDER BY" + "    measurement_time";

	/** 合計値計算用のSQL(H2) */
	private static final String SQL_SUM_ALL_H2 = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    , case when substring(ji.measurement_item_name, 0, 1) = '/' then '/' "
		+ "           when substring(ji.measurement_item_name, 0, 5) = 'jdbc:' then 'jdbc:' "
		+ "           else 'java' end item_name_head"
		+ "    , sum(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) max_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) min_value"
		+ "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time) - date_part('epoch', ?::timestamp)) "
		+ "    / ?"
		+ ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?"
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ "GROUP BY measurement_index, item_name_head " //
		+ "ORDER BY " + "    measurement_time";

	/** 合計値計算用のSQL(postgres) */
	private static final String SQL_SUM_ALL_POSTGRES = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    , case when substring(ji.measurement_item_name, 0, 2) = '/' then '/' "
		+ "           when substring(ji.measurement_item_name, 0, 6) = 'jdbc:' then 'jdbc:' "
		+ "           else 'java' end item_name_head"
		+ "    , sum(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) max_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) min_value"
		+ "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time) - date_part('epoch', ?::timestamp)) "
		+ "    / ?"
		+ ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?"
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ "GROUP BY measurement_index, item_name_head " //
		+ "ORDER BY " + "    measurement_time";

	/** 例外計算用のSQL(H2) */
	private static final String SQL_EXCEPTION_H2 = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    , case when substring(ji.measurement_item_name, 0, 1) = '/' then '/' "
		+ "           when substring(ji.measurement_item_name, 0, 5) = 'jdbc:' then 'jdbc:' "
		+ "           else 'java' end item_name_head"
		+ "    , sum(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) max_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) min_value"
		+ "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time) - date_part('epoch', ?::timestamp)) "
		+ "    / ?"
		+ ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?"
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ "GROUP BY measurement_index, ji.measurement_item_name " + "ORDER BY"
		+ "    item_name_head, measurement_time";

	/** 例外計算用のSQL(postgres) */
	private static final String SQL_EXCEPTION_POSTGRES = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    , case when substring(ji.measurement_item_name, 0, 2) = '/' then '/' "
		+ "           when substring(ji.measurement_item_name, 0, 6) = 'jdbc:' then 'jdbc:' "
		+ "           else 'java' end item_name_head"
		+ "    , sum(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) max_value"
		+ "    , sum(cast(mv.measurement_value as double precision)) min_value"
		+ "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time) - date_part('epoch', ?::timestamp)) "
		+ "    / ?"
		+ ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?"
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ "GROUP BY measurement_index, ji.measurement_item_name " + "ORDER BY"
		+ "    item_name_head, measurement_time";

	/**
	 * CallTreeNodo生成数のデータを取得するためのSQL文。
	 */
	private static final String SQL_CALLTREE_AVERAGE = "SELECT"
		+ "    min(mv.measurement_time) measurement_time"
		+ "    ,ji.measurement_item_name item_name"
		+ "    , avg(cast(mv.measurement_value as double precision)) summary_value"
		+ "    , max(cast(mv.measurement_value as double precision)) max_value"
		+ "    , min(cast(mv.measurement_value as double precision)) min_value"
		+ "    ,floor( "
		+ ITEM_COUNT
		+ " * (date_part('epoch', measurement_time) - date_part('epoch', ?::timestamp)) "
		+ "    / ?"
		+ ") measurement_index "
		+ "FROM" //
		+ "    measurement_value mv" //
		+ "    ,javelin_measurement_item ji "//
		+ "WHERE" //
		+ "    ji.measurement_item_name = ?" //
		+ "    AND mv.measurement_item_id = ji.measurement_item_id"
		+ "    AND" //
		+ "    (" //
		+ "        mv.measurement_time BETWEEN ?" + "        AND ?" + "    ) "
		+ "GROUP BY measurement_index, ji.measurement_item_name " + "ORDER BY"
		+ "    ji.measurement_item_name, measurement_time";

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。
	 * 
	 * サマリとして平均値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static List<ReportItemValue> selectSum(String database, Timestamp startTime,
		Timestamp endTime, String itemName) throws SQLException
	{
		List<ReportItemValue> result = select(database, startTime, endTime, itemName, SQL_SUM);

		return result;
	}

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。
	 * 
	 * サマリとして平均値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static List<ReportItemValue> selectAverage(String database, Timestamp startTime,
		Timestamp endTime, String itemName) throws SQLException
	{
		List<ReportItemValue> result = select(database, startTime, endTime, itemName, SQL_AVERAGE);

		return result;
	}

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。 このメソッドで取得できるのは、CallTreeNode生成数のデータのみである。
	 * 
	 * サマリとして平均値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static List<ReportItemValue> selectCallTreeAverage(String database, Timestamp startTime,
		Timestamp endTime, String itemName) throws SQLException
	{
		List<ReportItemValue> result = select(database, startTime, endTime, itemName,
			SQL_CALLTREE_AVERAGE);

		return result;
	}

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。
	 * 
	 * サマリとして平均値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static Map<String, List<ReportItemValue>> selectSumAll(String database,
		Timestamp startTime, Timestamp endTime, String itemName) throws SQLException
	{
		Map<String, Map<Integer, ReportItemValue>> resultMap;

		if (DBManager.isDefaultDb() == true)
		{
			resultMap = selectMap(database, startTime, endTime, itemName, SQL_SUM_ALL_H2);
		}
		else
		{
			resultMap = selectMap(database, startTime, endTime, itemName, SQL_SUM_ALL_POSTGRES);
		}

		Map<String, List<ReportItemValue>> result;
		if (resultMap.isEmpty())
		{
			result = createZeroMapData(startTime, endTime, itemName);
		}
		else
		{
			result = convertToReportItemListMap(startTime, endTime, resultMap);
		}

		return result;
	}

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。
	 * 
	 * サマリとして平均値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static Map<String, List<ReportItemValue>> selectAverageAll(String database,
		Timestamp startTime, Timestamp endTime, String itemName) throws SQLException
	{
		Map<String, Map<Integer, ReportItemValue>> resultMap;

		if (DBManager.isDefaultDb() == true)
		{
			resultMap = selectMap(database, startTime, endTime, itemName, SQL_AVERAGE_ALL_H2);
		}
		else
		{
			resultMap = selectMap(database, startTime, endTime, itemName, SQL_AVERAGE_ALL_POSTGRES);
		}

		Map<String, List<ReportItemValue>> result;
		if (resultMap.isEmpty())
		{
			result = createZeroMapData(startTime, endTime, itemName);
		}
		else
		{
			result = convertToReportItemListMap(startTime, endTime, resultMap);
		}
		return result;
	}

	/**
	 * 値が0のMapを生成する。
	 * 
	 * @param startTime
	 *            開始時刻。
	 * @param endTime
	 *            終了時刻。
	 * @param itemName
	 *            項目名。
	 * @return　生成したMap。
	 */
	private static Map<String, List<ReportItemValue>> createZeroMapData(Timestamp startTime,
		Timestamp endTime, String itemName)
	{
		Map<String, List<ReportItemValue>> result;
		long startMillis = startTime.getTime();
		long endMillis = endTime.getTime();
		List<ReportItemValue> reportItemList = new ArrayList<ReportItemValue>();
		for (int index = 0; index < ITEM_COUNT; index++)
		{
			ReportItemValue reportItemValue = new ReportItemValue();
			reportItemValue.itemName = itemName;
			reportItemValue.measurementTime = new Timestamp(startMillis + (endMillis - startMillis)
				* index / ITEM_COUNT);
			reportItemValue.summaryValue = 0;
			reportItemValue.maxValue = 0;
			reportItemValue.minValue = 0;
			reportItemList.add(reportItemValue);
		}
		result = new HashMap<String, List<ReportItemValue>>();
		result.put("", reportItemList);
		return result;
	}

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。
	 * 
	 * サマリとして平均値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static Map<String, List<ReportItemValue>> selectSumMap(String database,
		Timestamp startTime, Timestamp endTime, String itemName) throws SQLException
	{
		Map<String, Map<Integer, ReportItemValue>> resultMap = selectMap(database, startTime,
			endTime, itemName, SQL_SUM);

		Map<String, List<ReportItemValue>> result = convertToReportItemListMap(startTime, endTime,
			resultMap);

		return result;
	}

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。
	 * 
	 * サマリとして合計値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static Map<String, List<ReportItemValue>> selectExceptionSumMap(String database,
		Timestamp startTime, Timestamp endTime, String itemName) throws SQLException
	{
		Map<String, Map<Integer, ReportItemValue>> resultMap;

		if (DBManager.isDefaultDb() == true)
		{
			resultMap = selectMap(database, startTime, endTime, itemName, SQL_EXCEPTION_H2);
		}
		else
		{
			resultMap = selectMap(database, startTime, endTime, itemName, SQL_EXCEPTION_POSTGRES);
		}

		Map<String, List<ReportItemValue>> result = convertToReportItemListMap(startTime, endTime,
			resultMap);

		return result;
	}

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。
	 * 
	 * サマリとして合計値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static Map<String, List<ReportItemValue>> selectStallSumMap(String database,
		Timestamp startTime, Timestamp endTime, String itemName) throws SQLException
	{
		Map<String, Map<Integer, ReportItemValue>> resultMap;

		// 使用するSQLは例外データと同じものを使用する。
		if (DBManager.isDefaultDb() == true)
		{
			resultMap = selectMap(database, startTime, endTime, itemName, SQL_EXCEPTION_H2);
		}
		else
		{
			resultMap = selectMap(database, startTime, endTime, itemName, SQL_EXCEPTION_POSTGRES);
		}

		Map<String, List<ReportItemValue>> result = convertToReportItemListMap(startTime, endTime,
			resultMap);

		return result;
	}

	/**
	 * 指定した期間から、サマリ計算を行い、結果を取得する。
	 * 
	 * サマリとして平均値を計算するSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static Map<String, List<ReportItemValue>> selectAverageMap(String database,
		Timestamp startTime, Timestamp endTime, String itemName) throws SQLException
	{
		Map<String, Map<Integer, ReportItemValue>> resultMap = selectMap(database, startTime,
			endTime, itemName, SQL_AVERAGE);

		Map<String, List<ReportItemValue>> result = convertToReportItemListMap(startTime, endTime,
			resultMap);
		return result;
	}

	/**
	 * 指定した期間から、指定したSQLを用いてサマリ計算を行い、結果を取得する。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @param sqlBase
	 *            SQL
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	public static Map<String, Map<Integer, ReportItemValue>> selectMap(String database,
		Timestamp startTime, Timestamp endTime, String itemName, String sqlBase)
		throws SQLException
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		Map<String, Map<Integer, ReportItemValue>> result = null;
		try
		{
			conn = getConnection(database, true);
			pstmt = conn.prepareStatement(sqlBase);
			// CHECKSTYLE:OFF
			long startSeconds = startTime.getTime() / 1000;
			long endSeconds = endTime.getTime() / 1000;
			pstmt.setTimestamp(1, startTime);
			pstmt.setLong(2, endSeconds - startSeconds);
			pstmt.setString(3, itemName);
			pstmt.setTimestamp(4, startTime);
			pstmt.setTimestamp(5, endTime);
			// CHECKSTYLE:ON
			rs = pstmt.executeQuery();
			result = getReportItemMapFromResultSet(rs);
		}
		finally
		{
			SQLUtil.closeResultSet(rs);
			SQLUtil.closeStatement(pstmt);
			SQLUtil.closeConnection(conn);
		}

		return result;
	}

	/**
	 * 指定した期間の計測情報のサマリ計算を行い、結果を取得する。
	 * 
	 * 引数で指定したSQLで問い合わせ、結果をリストにして返す。 必ず、ITEM_COUNTのサイズのリストを返す。
	 * 
	 * @param database
	 *            データベース
	 * @param startTime
	 *            開始時刻
	 * @param endTime
	 *            終了時刻
	 * @param itemName
	 *            item_name
	 * @param sql
	 *            SQL
	 * @return レポート出力に使用するReportItemValueのリスト。
	 * @throws SQLException
	 *             DBアクセス時にエラーが発生した場合
	 */
	private static List<ReportItemValue> select(String database, Timestamp startTime,
		Timestamp endTime, String itemName, String sql) throws SQLException
	{
		Map<String, Map<Integer, ReportItemValue>> resultMap = selectMap(database, startTime,
			endTime, itemName, sql);

		long startMillis = startTime.getTime();
		long endMillis = endTime.getTime();

		List<ReportItemValue> result = new ArrayList<ReportItemValue>(ITEM_COUNT);
		if (resultMap.isEmpty())
		{
			for (int index = 0; index < ITEM_COUNT; index++)
			{
				ReportItemValue reportItemValue = new ReportItemValue();
				reportItemValue.itemName = itemName;
				reportItemValue.measurementTime = new Timestamp(startMillis
					+ (endMillis - startMillis) * index / ITEM_COUNT);
				reportItemValue.summaryValue = 0;
				reportItemValue.maxValue = 0;
				reportItemValue.minValue = 0;
				result.add(reportItemValue);
			}
		}
		for (Map<Integer, ReportItemValue> reportItemMap : resultMap.values())
		{
			for (int index = 0; index < ITEM_COUNT; index++)
			{
				ReportItemValue reportItemValue = reportItemMap.get(Integer.valueOf(index));
				if (reportItemValue == null)
				{
					// 値がなければ0で補間する
					reportItemValue = new ReportItemValue();
					reportItemValue.itemName = itemName;
					reportItemValue.measurementTime = new Timestamp(startMillis
						+ (endMillis - startMillis) * index / ITEM_COUNT);
					reportItemValue.summaryValue = 0;
					reportItemValue.maxValue = 0;
					reportItemValue.minValue = 0;
				}
				if (reportItemValue != null)
				{
					result.add(reportItemValue);
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * itemNameがキー、ReportItemValueのリストが値のマップに変換する。
	 * 
	 * @param startTime
	 *            開示時刻
	 * @param endTime
	 *            終了時刻
	 * @param inputMap
	 *            変換対象のマップ
	 * @return
	 */
	private static Map<String, List<ReportItemValue>> convertToReportItemListMap(
		Timestamp startTime, Timestamp endTime, Map<String, Map<Integer, ReportItemValue>> inputMap)
	{
		long startMillis = startTime.getTime();
		long endMillis = endTime.getTime();

		Map<String, List<ReportItemValue>> result = new LinkedHashMap<String, List<ReportItemValue>>();

		for (Map.Entry<String, Map<Integer, ReportItemValue>> entry : inputMap.entrySet())
		{
			String entryItemName = entry.getKey();
			Map<Integer, ReportItemValue> reportItemMap = entry.getValue();
			List<ReportItemValue> list = new ArrayList<ReportItemValue>(ITEM_COUNT);
			for (int index = 0; index < ITEM_COUNT; index++)
			{
				ReportItemValue reportItemValue = reportItemMap.get(Integer.valueOf(index));
				if (reportItemValue == null)
				{
					// 値がなければ0で補間する
					reportItemValue = new ReportItemValue();
					reportItemValue.itemName = entryItemName;
					reportItemValue.measurementTime = new Timestamp(startMillis
						+ (endMillis - startMillis) * index / ITEM_COUNT);
					reportItemValue.summaryValue = 0;
					reportItemValue.maxValue = 0;
					reportItemValue.minValue = 0;
				}
				if (reportItemValue != null)
				{
					list.add(reportItemValue);
				}
			}

			result.put(entryItemName, list);
		}
		return result;
	}

	/**
	 * {@link ResultSet}インスタンスから、{@link ReportValue}のリストを生成します。
	 * 
	 * @param rs
	 *            データが含まれている{@link ResultSet} インスタンス
	 * @throws SQLException
	 *             SQL 実行結果取得時に例外が発生した場合
	 */
	private static Map<String, Map<Integer, ReportItemValue>> getReportItemMapFromResultSet(
		final ResultSet rs) throws SQLException
	{
		Map<String, Map<Integer, ReportItemValue>> result = new LinkedHashMap<String, Map<Integer, ReportItemValue>>();

		while (rs.next() == true)
		{
			ReportItemValue reportItemValue = new ReportItemValue();
			// CHECKSTYLE:OFF
			reportItemValue.measurementTime = rs.getTimestamp(1);
			reportItemValue.itemName = rs.getString(2);
			reportItemValue.summaryValue = rs.getDouble(3);
			reportItemValue.maxValue = rs.getDouble(4);
			if (reportItemValue.maxValue == null)
			{
				reportItemValue.maxValue = reportItemValue.summaryValue;
			}
			reportItemValue.minValue = rs.getDouble(5);
			if (reportItemValue.minValue == null)
			{
				reportItemValue.minValue = reportItemValue.summaryValue;
			}
			reportItemValue.index = rs.getInt(6);
			// CHECKSTYLE:ON

			if (reportItemValue.itemName == null)
			{
				reportItemValue.itemName = "";
			}

			Map<Integer, ReportItemValue> map = result.get(reportItemValue.itemName);
			if (map == null)
			{
				map = new LinkedHashMap<Integer, ReportItemValue>();
				result.put(reportItemValue.itemName, map);
			}
			map.put(reportItemValue.index, reportItemValue);
		}
		return result;
	}

	/**
	 * @author khine wai
	 * @param dbName
	 *            dbName
	 * @param status
	 *            status
	 * @param targetItemName
	 *            targetItemName
	 * @param fmTime
	 *            fmTime
	 * @param toTime
	 *            toTime
	 */
	public void changeStatus(String dbName, String status, String targetItemName, Timestamp fmTime,
		Timestamp toTime)
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sqlBase = "UPDATE REPORT_EXPORT_RESULT"
			+ " SET STATUS = ? WHERE TARGET_MEASUREMENT_NAME= ? AND FM_TIME= ? AND TO_TIME= ?";
		try
		{
			conn = getConnection(dbName, true);
			pstmt = conn.prepareStatement(sqlBase);
			pstmt.setString(1, status);
			pstmt.setString(2, targetItemName);
			pstmt.setTimestamp(3, fmTime);
			pstmt.setTimestamp(4, toTime);
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			SQLUtil.closeResultSet(rs);
			SQLUtil.closeStatement(pstmt);
			SQLUtil.closeConnection(conn);
		}
	}
}
