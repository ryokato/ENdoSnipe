/*
 * Copyright (c) 2004-2013 Acroquest Technology Co., Ltd. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 *
 * THE  SOFTWARE IS  PROVIDED BY  Acroquest Technology Co., Ltd., WITHOUT  WARRANTY  OF
 * ANY KIND,  EXPRESS  OR IMPLIED,  INCLUDING BUT  NOT LIMITED  TO THE
 * WARRANTIES OF  MERCHANTABILITY,  FITNESS FOR A  PARTICULAR  PURPOSE
 * AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
 * CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package jp.co.acroquest.endosnipe.web.explorer.service;

import static jp.co.acroquest.endosnipe.data.TableNames.REPORT_EXPORT_RESULT;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import jp.co.acroquest.endosnipe.collector.config.DataCollectorConfig;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.data.dao.JavelinMeasurementItemDao;
import jp.co.acroquest.endosnipe.report.ReporterThread;
import jp.co.acroquest.endosnipe.util.ResourceDataDaoUtil;
import jp.co.acroquest.endosnipe.web.explorer.config.DataBaseConfig;
import jp.co.acroquest.endosnipe.web.explorer.constants.LogMessageCodes;
import jp.co.acroquest.endosnipe.web.explorer.dao.PropertySettingDao;
import jp.co.acroquest.endosnipe.web.explorer.dao.ReportDefinitionDao;
import jp.co.acroquest.endosnipe.web.explorer.dao.SchedulingReportDefinitionDao;
import jp.co.acroquest.endosnipe.web.explorer.dto.ReportDefinitionDto;
import jp.co.acroquest.endosnipe.web.explorer.dto.SchedulingReportDefinitionDto;
import jp.co.acroquest.endosnipe.web.explorer.entity.PropertySettingDefinition;
import jp.co.acroquest.endosnipe.web.explorer.entity.ReportDefinition;
import jp.co.acroquest.endosnipe.web.explorer.entity.SchedulingReportDefinition;
import jp.co.acroquest.endosnipe.web.explorer.manager.DatabaseManager;
import jp.co.acroquest.endosnipe.web.explorer.util.ReportUtil;

import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * レポート出力機能のサービスクラス。
 * 
 * @author miyasaka
 *
 */
@Service
public class ReportService
{
    /**constant integer value is 7*/
    private static final int NUMBER_SEVEN = 7;

    /** レポートの出力先ディレクトリ名。 */
    private static final String REPORT_PATH = "report";

    /** ロガー。 */
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger.getLogger(DashboardService.class);

    /** レポート情報Dao */
    @Autowired
    protected ReportDefinitionDao reportDefinitionDao_;

    /** バッファのサイズ */
    private static final int BUFFER_SIZE = 1024;

    /** ファイルのセパレータ */
    private static final String FOLDER_SEPARATOR = File.separator;

    /**
     * Format of Time
     */
    private static final String TIME_FORMAT = "HH:mm";

    /** Scheduling レポート情報Dao */
    @Autowired
    protected SchedulingReportDefinitionDao schedulingReportDefinitionDao_;

    /** Property Setting Dao */
    @Autowired
    protected PropertySettingDao propertySettingDao_;

    /**
     * Set day into schedulingdto
     */
    private final Map<String, Integer> dayMap_ = new HashMap<String, Integer>();

    /**
     * declare thread List
     */

    private final List<ReporterThread> reportThreadList_ = new ArrayList<ReporterThread>();

    /**constants integer value is 4*/
    private static final int NUMBER_FOUR = 4;

    /**
     * デフォルトコンストラクタ。
     */
    public ReportService()
    {
        this.dayMap_.put("Sunday", Calendar.SUNDAY);
        this.dayMap_.put("Monday", Calendar.MONDAY);
        this.dayMap_.put("Tuesday", Calendar.TUESDAY);
        this.dayMap_.put("Wednesday", Calendar.WEDNESDAY);
        this.dayMap_.put("Thursday", Calendar.THURSDAY);
        this.dayMap_.put("Friday", Calendar.FRIDAY);
        this.dayMap_.put("Saturday", Calendar.SATURDAY);
    }

    /**
     * Run Thread to export report
     */
    public void runThread()
    {
        PropertySettingDefinition simulExecution =
                propertySettingDao_.selectByKey("simulExecutionNum");
        int simulExecutionNum = 0;
        if (simulExecution != null && simulExecution.value_ != null)
        {
            simulExecutionNum = Integer.parseInt(simulExecution.value_);
        }
        DatabaseManager manager = DatabaseManager.getInstance();
        DataBaseConfig dbConfig = manager.getDataBaseConfig();
        if (dbConfig != null)
        {
            if (simulExecutionNum == 0)
            {
                simulExecutionNum = 1;
            }
            if (simulExecutionNum > 0)
            {
                DataCollectorConfig dataCollectorConfig = this.convertDataCollectorConfig(dbConfig);
                for (int threadNum = 0; threadNum < simulExecutionNum; threadNum++)
                {
                    ReporterThread report = new ReporterThread(dataCollectorConfig);
                    Thread reportThread = new Thread(report);
                    reportThreadList_.add(report);
                    report.isRunningThread = true;
                    reportThread.start();
                }
            }
        }
        else
        {
            LOGGER.log(LogMessageCodes.CANNOT_FIND_REPORT_SETTINGS);
        }
    }

    /**
     * Stop thread to export report
     */
    public void stopThread()
    {
        for (int indexThread = 0; indexThread < reportThreadList_.size(); indexThread++)
        {
            reportThreadList_.get(indexThread).stopThread();
        }
    }

    /**
     * レポートを作成する。
     * 
     * @param reportDefinitionDto レポート出力の定義
     */
    public void createReport(final ReportDefinitionDto reportDefinitionDto)
    {

        ReportUtil.createReport(reportDefinitionDto);

    }

    /**
     * 
     * @param dataBaseConfig configuration for database
     * @return DataCollectorConfig
     */

    private DataCollectorConfig convertDataCollectorConfig(final DataBaseConfig dataBaseConfig)
    {
        DataCollectorConfig dataCollectorConfig = new DataCollectorConfig();
        dataCollectorConfig.setDatabaseHost(dataBaseConfig.getDatabaseHost());
        dataCollectorConfig.setDatabaseName(dataBaseConfig.getDatabaseName());
        dataCollectorConfig.setDatabasePassword(dataBaseConfig.getDatabasePassword());
        dataCollectorConfig.setDatabasePort(dataBaseConfig.getDatabasePort());
        dataCollectorConfig.setDatabaseType(dataBaseConfig.getDatabaseType());
        dataCollectorConfig.setDatabaseUserName(dataBaseConfig.getDatabaseUserName());

        return dataCollectorConfig;
    }

    /**
     * すべてのシグナルデータを返す。
     * 
     * @return シグナルデータ一覧
     */
    public List<ReportDefinitionDto> getAllReport()
    {
        List<ReportDefinition> reportList = null;
        try
        {
            reportList = reportDefinitionDao_.selectAll();
        }
        catch (PersistenceException pEx)
        {
            Throwable cause = pEx.getCause();
            if (cause instanceof SQLException)
            {
                SQLException sqlEx = (SQLException)cause;
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
            }
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, pEx, pEx.getMessage());

            return new ArrayList<ReportDefinitionDto>();
        }

        List<ReportDefinitionDto> definitionDtoList = new ArrayList<ReportDefinitionDto>();

        for (ReportDefinition reportDefinitioin : reportList)
        {
            ReportDefinitionDto reportDto =
                    ReportUtil.convertReportDifinitionDto(reportDefinitioin);
            definitionDtoList.add(reportDto);
        }

        return definitionDtoList;
    }

    /**
     * 指定したレポート名をキーに、レポート定義の一覧を取得する。
     * 
     * @param reportName レポート名
     * @return レポート定義の一覧
     */
    public List<ReportDefinitionDto> getReportByReportName(final String reportName)
    {
        List<ReportDefinitionDto> reportDefinitionDtos = new ArrayList<ReportDefinitionDto>();
        List<ReportDefinition> definitionDtos = reportDefinitionDao_.selectByName(reportName);

        int definitionDtosLength = definitionDtos.size();

        for (int index = 0; index < definitionDtosLength; index++)
        {
            ReportDefinition definition = definitionDtos.get(index);
            ReportDefinitionDto definitionDto = ReportUtil.convertReportDifinitionDto(definition);
            reportDefinitionDtos.add(definitionDto);
        }

        return reportDefinitionDtos;
    }

    /**
     * 指定したレポートIDをキーに、レポート定義を取得する。
     * 
     * @param reportId レポートID
     * @return レポート定義
     */
    public ReportDefinitionDto getReportByReportId(final int reportId)
    {
        ReportDefinition reportDefinition = reportDefinitionDao_.selectById(reportId);
        ReportDefinitionDto reportDefinitionDto =
                ReportUtil.convertReportDifinitionDto(reportDefinition);

        return reportDefinitionDto;
    }

    /**
     * レポート出力の定義をDBに登録する。
     * 
     * @param reportDefinition レポート出力の定義
     * @return レポート出力の定義
     */
    public ReportDefinitionDto insertReportDefinition(final ReportDefinition reportDefinition)
    {
        try
        {
            reportDefinitionDao_.insert(reportDefinition);
        }
        catch (DuplicateKeyException dkEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, dkEx, dkEx.getMessage());
            return new ReportDefinitionDto();
        }

        ReportDefinitionDto reportDefinitionDto =
                ReportUtil.convertReportDifinitionDto(reportDefinition);

        return reportDefinitionDto;
    }

    /**
     * Check same report Name
     * @param reportId got from database
     * @param reportName got from database
     * @return duplicate or not.
     */
    public boolean hasSameReportName(final long reportId, final String reportName)
    {
        SchedulingReportDefinition schedulingReportDefinition =
                this.schedulingReportDefinitionDao_.selectByName(reportName);
        if (schedulingReportDefinition == null)
        {
            // 同一schedulingReportル名を持つ閾値判定定義情報が存在しない場合
            return false;
        }
        else if (schedulingReportDefinition.reportId_ == reportId)
        {
            // schedulingReport名が一致する閾値判定定義情報が更新対象自身の場合
            return false;
        }
        return true;
    }

    /**
     * ReportDefinitionDtoオブジェクトをReportDefinitionオブジェクトに変換する。
     * 
     * @param definitionDto
     *            ReportDefinitionDtoオブジェクト
     * 
     * @return ReportDefinitionオブジェクト
     */
    public ReportDefinition convertReportDefinition(final ReportDefinitionDto definitionDto)
    {

        ReportDefinition reportDefinition = new ReportDefinition();

        reportDefinition.reportId_ = definitionDto.getReportId();
        reportDefinition.reportName_ = definitionDto.getReportName();
        reportDefinition.targetMeasurementName_ = definitionDto.getTargetMeasurementName();

        Calendar fmTimeCal = definitionDto.getReportTermFrom();
        Calendar toTimeCal = definitionDto.getReportTermTo();

        reportDefinition.fmTime_ = fmTimeCal.getTime();
        reportDefinition.toTime_ = toTimeCal.getTime();
        reportDefinition.status_ = definitionDto.getStatus();
        Date toDate = toTimeCal.getTime();
        String tableName = ResourceDataDaoUtil.getTableNameToInsert(toDate, REPORT_EXPORT_RESULT);
        reportDefinition.tableName_ = tableName;
        return reportDefinition;
    }

    /**
     * クライアントからのレポート出力ダウンロードを受信するためのサーブレットです。
     * 
     * @param response {@link HttpServletResponse}オブジェクト
     * @param fileName ファイル名
     */
    public void doRequest(final HttpServletResponse response, final String fileName)
    {
        if (fileName == null)
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_FILE_NAME);
            String message = "filename is null.";
            handleException(response, message);
            return;
        }

        String filePath = this.getFilePath(fileName);

        OutputStream outputStream = null;

        try
        {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            BufferedInputStream buffInputStream = new BufferedInputStream(fileInputStream);

            String nonSpaceFileName = fileName.replaceAll("[ 　]", "");

            response.setHeader("Content-Disposition", "attachment; filename=" + nonSpaceFileName);

            outputStream = response.getOutputStream();

            int len = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((len = fileInputStream.read(buffer, 0, buffer.length)) != -1)
            {
                outputStream.write(buffer, 0, len);
            }

            buffInputStream.close();
        }
        catch (IOException ioEx)
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_REPORT_FILE_NAME);
            handleException(response, ioEx.getMessage());
        }
        finally
        {

            if (outputStream != null)
            {
                try
                {
                    outputStream.close();
                }
                catch (IOException ioEx)
                {
                    LOGGER.log(LogMessageCodes.UNEXPECTED_ERROR);
                    handleException(response, ioEx.getMessage());
                }
                finally
                {
                    outputStream = null;
                }
            }
        }
    }

    /**
     * レポートファイルを削除する。
     * 
     * @param fileName ファイル名
     * @return ファイル削除に成功した場合はtrue
     */
    public boolean deleteReportFile(final String fileName)
    {
        if (fileName == null)
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_FILE_NAME);
            return false;
        }

        // ファイル削除に成功した場合にtrueになるフラグ
        boolean isDeleteSuccess = false;

        String filePath = this.getFilePath(fileName);

        File file = new File(filePath);

        if (file.exists())
        {
            // ファイルを削除する
            isDeleteSuccess = file.delete();
        }
        else
        {
            LOGGER.log(LogMessageCodes.UNKNOWN_FILE_NAME);
            return false;
        }

        return isDeleteSuccess;
    }

    /**
     * レポート名に該当するレポート定義を削除する。
     * 
     * @param reportName レポート名
     */
    public void deleteReportDefinitionByName(final String reportName)
    {
        try
        {
            // レポート名に該当するレポート定義を削除する
            reportDefinitionDao_.deleteByName(reportName);
        }
        catch (PersistenceException pEx)
        {
            Throwable cause = pEx.getCause();
            if (cause instanceof SQLException)
            {
                SQLException sqlEx = (SQLException)cause;
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
            }
            else
            {
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, pEx, pEx.getMessage());
            }
        }
    }

    /**
     * レポートIDに該当するレポート定義を削除する。
     * 
     * @param reportId レポートID
     */
    public void deleteReportDefinitionById(final int reportId)
    {
        try
        {
            // レポートIDに該当するレポート定義を削除する
            reportDefinitionDao_.deleteById(reportId);
        }
        catch (PersistenceException pEx)
        {
            Throwable cause = pEx.getCause();
            if (cause instanceof SQLException)
            {
                SQLException sqlEx = (SQLException)cause;
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
            }
            else
            {
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, pEx, pEx.getMessage());
            }
        }
    }

    /**
     * ファイルが存在しないことをクライアント側に表示する。
     * 
     * @param responce {@link HttpServletResponse}オブジェクト
     * @param message エラーメッセージ
     */
    private void handleException(final HttpServletResponse responce, final String message)
    {
        try
        {
            // ContentTypeを指定
            responce.setContentType("text/csv;charset=UTF-8");
            String fileName = new String("error.txt".getBytes("UTF-8"), "ISO-8859-1");
            // Headerを設定
            responce.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            // 内容を出力
            PrintWriter writer = responce.getWriter();
            writer.write(message);
            writer.close();

        }
        catch (IOException ioEx)
        {
            LOGGER.log(LogMessageCodes.UNEXPECTED_ERROR);
        }
    }

    /**
     * ファイルの絶対パスを取得する。
     * 
     * @param fileName ファイル名
     * @return ファイルの絶対パス
     */
    private String getFilePath(final String fileName)
    {
        String currentDir = new File(".").getAbsolutePath();
        DatabaseManager dbMmanager = DatabaseManager.getInstance();
        String dbName = dbMmanager.getDataBaseName(1);

        String filePath =
                currentDir + FOLDER_SEPARATOR + REPORT_PATH + FOLDER_SEPARATOR + dbName
                        + FOLDER_SEPARATOR + fileName;

        return filePath;
    }

    /**
     * get all schedule data.
     * @param nodeName fullpath of nodeName
     * @return all schedule data
     */
    public List<SchedulingReportDefinitionDto> getAllSchedule(final String nodeName)
    {

        List<SchedulingReportDefinition> reportList = null;
        try
        {
            if (nodeName != null && !nodeName.trim().equals(""))
            {
                String agentName = getAgentName(nodeName) + "%";
                reportList = schedulingReportDefinitionDao_.selectAllByAgentName(agentName);
            }
            else
            {
                reportList = schedulingReportDefinitionDao_.selectAll();
            }
        }
        catch (PersistenceException pEx)
        {
            Throwable cause = pEx.getCause();
            if (cause instanceof SQLException)
            {
                SQLException sqlEx = (SQLException)cause;
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
            }
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, pEx, pEx.getMessage());

            return new ArrayList<SchedulingReportDefinitionDto>();
        }

        List<SchedulingReportDefinitionDto> definitionDtoList =
                new ArrayList<SchedulingReportDefinitionDto>();

        for (SchedulingReportDefinition reportDefinitioin : reportList)
        {
            SchedulingReportDefinitionDto reportDto =
                    this.convertSchedulingReportDifinitionDto(reportDefinitioin);
            definitionDtoList.add(reportDto);
        }
        return definitionDtoList;
    }

    /**
     * Substract agent Name from node name
     * @param nodeName full node path name
     * @return agentName
     */
    private String getAgentName(final String nodeName)
    {
        String[] agentSplit = nodeName.split("/");

        String agentName = nodeName;
        if (agentSplit.length >= NUMBER_FOUR)
        {
            agentName = "/" + agentSplit[1];
            for (int index = 2; index < NUMBER_FOUR; index++)
            {
                agentName += "/" + agentSplit[index];
            }
        }

        return agentName;
    }

    /**
     * insert scheuling report.
     * @param schedulingReportDefinition is used
     * @return is used
     */
    public SchedulingReportDefinitionDto insertSchedulingReport(
            final SchedulingReportDefinition schedulingReportDefinition)
    {
        try
        {
            schedulingReportDefinitionDao_.insert(schedulingReportDefinition);
        }
        catch (DuplicateKeyException dkEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, dkEx, dkEx.getMessage());
            return new SchedulingReportDefinitionDto();
        }

        SchedulingReportDefinitionDto schedulingReportDefinitionDto =
                this.convertSchedulingReportDifinitionDto(schedulingReportDefinition);

        return schedulingReportDefinitionDto;
    }

    /**
     * get scheduling info.
     * @param reportId got from database
     * @return schedulingreport
     */
    public SchedulingReportDefinitionDto getSchedulingInfo(final int reportId)
    {
        SchedulingReportDefinition schedulingReportDefinition =
                schedulingReportDefinitionDao_.selectById(reportId);
        SchedulingReportDefinitionDto schedulingReportDefinitionDto =
                this.convertSchedulingReportDifinitionDto(schedulingReportDefinition);
        return schedulingReportDefinitionDto;
    }

    /**
     * update scheduling data
     * @param schedulingReportDefinition got from database
     * @param beforeSchedulingReportName oldSchedulingreportName
     * @return scheduling report
     */
    public SchedulingReportDefinitionDto updateSchedulingInfo(
            final SchedulingReportDefinition schedulingReportDefinition,
            final String beforeSchedulingReportName)
    {
        try
        {
            SchedulingReportDefinition beforeSignalInfo =
                    schedulingReportDefinitionDao_.selectById(schedulingReportDefinition.reportId_);
            if (beforeSignalInfo == null)
            {
                return new SchedulingReportDefinitionDto();
            }

            schedulingReportDefinitionDao_.update(schedulingReportDefinition);

            String beforeItemName = beforeSignalInfo.reportName_;
            String afterItemName = schedulingReportDefinition.reportName_;

            this.updateMeasurementItemName(beforeItemName, afterItemName);
        }
        catch (PersistenceException pEx)
        {
            Throwable cause = pEx.getCause();
            if (cause instanceof SQLException)
            {
                SQLException sqlEx = (SQLException)cause;
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
            }
            else
            {
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, pEx, pEx.getMessage());
            }

            return new SchedulingReportDefinitionDto();
        }
        catch (SQLException sqlEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
            return new SchedulingReportDefinitionDto();
        }

        SchedulingReportDefinitionDto schedulingReportDefinitionDto =
                this.convertSchedulingReportDifinitionDto(schedulingReportDefinition);
        // 各クライアントにシグナル定義の変更を送信する。
        if (!schedulingReportDefinitionDto.getReportName().equals(beforeSchedulingReportName))
        {
            ReportUtil.sendSchedulingReportDefinition(schedulingReportDefinitionDto, "add");
            if (!checkReportIsExist(beforeSchedulingReportName))
            {
                SchedulingReportDefinitionDto oldSchedule = schedulingReportDefinitionDto;
                oldSchedule.setReportName(beforeSchedulingReportName);
                ReportUtil.sendSchedulingReportDefinition(oldSchedule, "delete");
            }

        }

        return schedulingReportDefinitionDto;
    }

    /**
     * 
     * @param schedulingDefinitionDto is used
     * @return is used
     */
    public SchedulingReportDefinition convertSchedulingReportDefinition(
            final SchedulingReportDefinitionDto schedulingDefinitionDto)
    {
        SchedulingReportDefinition schedulingReportDefinition = new SchedulingReportDefinition();
        schedulingReportDefinition.reportId_ = schedulingDefinitionDto.getReportId();
        schedulingReportDefinition.reportName_ = schedulingDefinitionDto.getReportName();
        schedulingReportDefinition.targetMeasurementName_ =
                schedulingDefinitionDto.getTargetMeasurementName();
        schedulingReportDefinition.term_ = schedulingDefinitionDto.getTerm();
        schedulingReportDefinition.day_ = schedulingDefinitionDto.getDay();
        Calendar time = schedulingDefinitionDto.getTime();
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        schedulingReportDefinition.time_ = timeFormat.format(time.getTime());
        schedulingReportDefinition.date_ = schedulingDefinitionDto.getDate();

        Calendar currentTimeCalendar = Calendar.getInstance();
        currentTimeCalendar.set(Calendar.SECOND, 0);
        currentTimeCalendar.set(Calendar.MILLISECOND, 0);
        Calendar planExportedTimeCalendar = Calendar.getInstance();
        planExportedTimeCalendar.set(Calendar.SECOND, 0);
        planExportedTimeCalendar.set(Calendar.MILLISECOND, 0);
        planExportedTimeCalendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        planExportedTimeCalendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE));

        int date = 0;

        if ("DAILY".equals(schedulingReportDefinition.term_))
        {
            long scheduleTimeLong = planExportedTimeCalendar.getTimeInMillis();
            long currentTimeLong = currentTimeCalendar.getTimeInMillis();

            if (scheduleTimeLong <= currentTimeLong)
            {
                planExportedTimeCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        else if ("WEEKLY".equals(schedulingReportDefinition.term_))
        {
            if (schedulingReportDefinition.day_ != null)
            {
                dayMap_.get(schedulingReportDefinition.day_);
                int planExportedDay =
                        (dayMap_.get(schedulingReportDefinition.day_))
                                - planExportedTimeCalendar.get(Calendar.DAY_OF_WEEK);
                planExportedTimeCalendar.add(Calendar.DAY_OF_MONTH, planExportedDay);
                long scheduleTimeLong = planExportedTimeCalendar.getTimeInMillis();
                long currentTimeLong = currentTimeCalendar.getTimeInMillis();

                if (scheduleTimeLong <= currentTimeLong)
                {
                    planExportedTimeCalendar.add(Calendar.DAY_OF_MONTH, NUMBER_SEVEN);
                }
            }
        }
        else if ("MONTHLY".equals(schedulingReportDefinition.term_))
        {
            if (schedulingReportDefinition.date_ != null)
            {
                date = Integer.parseInt(schedulingReportDefinition.date_);
                if (Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) < date)
                {
                    planExportedTimeCalendar.set(Calendar.DAY_OF_MONTH,
                                                 Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
                }
                else
                {
                    planExportedTimeCalendar.set(Calendar.DAY_OF_MONTH, date);
                }

                long scheduleTimeLong = planExportedTimeCalendar.getTimeInMillis();
                long currentTimeLong = currentTimeCalendar.getTimeInMillis();
                if (currentTimeLong >= scheduleTimeLong)
                {
                    planExportedTimeCalendar.add(Calendar.MONTH, 1);
                }
            }
        }

        if (date > planExportedTimeCalendar.get(Calendar.DAY_OF_MONTH)
                && (planExportedTimeCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) >= date)

        {

            planExportedTimeCalendar.set(Calendar.DAY_OF_MONTH, date);

        }

        //        planExportedTimeCalendar.set(Calendar.SECOND, 0);
        Timestamp planExportedTime = new Timestamp(planExportedTimeCalendar.getTimeInMillis());
        schedulingReportDefinition.planExportedTime_ = planExportedTime;
        return schedulingReportDefinition;
    }

    /**
     * check report name and report term exist or not in Database
     * @param reportDefinitionDto get the report data
     * from controller class
     * @return true or false
     */
    public boolean checkReportName(final ReportDefinitionDto reportDefinitionDto)
    {
        ReportDefinition reportDefinition = null;
        String reportName = reportDefinitionDto.getReportName();
        Calendar fmTimeCal = reportDefinitionDto.getReportTermFrom();
        Calendar toTimeCal = reportDefinitionDto.getReportTermTo();

        Date startTime = fmTimeCal.getTime();
        Date endTime = toTimeCal.getTime();
        reportDefinition = reportDefinitionDao_.selectName(reportName, startTime, endTime);
        if (reportDefinition != null)
        {
            return true;
        }

        return false;

    }

    /**
    * convert into scheduling report dto.
    * @param schedulingReportDefinition is used
    * @return is used
    */
    private SchedulingReportDefinitionDto convertSchedulingReportDifinitionDto(
            final SchedulingReportDefinition schedulingReportDefinition)
    {
        SchedulingReportDefinitionDto schedulingDefinitionDto = new SchedulingReportDefinitionDto();
        schedulingDefinitionDto.setReportId(schedulingReportDefinition.reportId_);
        schedulingDefinitionDto.setReportName(schedulingReportDefinition.reportName_);
        schedulingDefinitionDto.setTargetMeasurementName(schedulingReportDefinition.targetMeasurementName_);
        schedulingDefinitionDto.setTerm(schedulingReportDefinition.term_);
        schedulingDefinitionDto.setDay(schedulingReportDefinition.day_);
        schedulingDefinitionDto.setDate(schedulingReportDefinition.date_);

        DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

        Calendar time = Calendar.getInstance();
        try
        {
            time.setTime(timeFormat.parse(schedulingReportDefinition.time_));
        }
        catch (ParseException ex)
        {
            LOGGER.log(LogMessageCodes.UNSUPPORTED_REPORT_FILE_DURATION_FORMAT);
        }

        schedulingDefinitionDto.setTime(time);

        return schedulingDefinitionDto;
    }

    /**
     * delete scheduling report.
     * @param reportId got from database
     */
    public void deleteSchduleReportById(final int reportId)
    {
        try
        {
            SchedulingReportDefinition scheduleReport =
                    schedulingReportDefinitionDao_.selectById(reportId);
            // レポートIDに該当するレポート定義を削除する
            schedulingReportDefinitionDao_.deleteById(reportId);
            if (!checkReportIsExist(scheduleReport.reportName_))
            {
                SchedulingReportDefinitionDto scheduleReportDto =
                        this.convertSchedulingReportDifinitionDto(scheduleReport);
                ReportUtil.sendSchedulingReportDefinition(scheduleReportDto, "delete");
            }

        }
        catch (PersistenceException pEx)
        {
            Throwable cause = pEx.getCause();
            if (cause instanceof SQLException)
            {
                SQLException sqlEx = (SQLException)cause;
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
            }
            else
            {
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, pEx, pEx.getMessage());
            }
        }
    }

    /**
         * update measurement item.
         * @param beforeItemName before item
         * @param afterItemName after item
         * @throws SQLException is used
         */
    private void updateMeasurementItemName(final String beforeItemName, final String afterItemName)
        throws SQLException
    {
        DatabaseManager dbMmanager = DatabaseManager.getInstance();
        String dbName = dbMmanager.getDataBaseName(1);

        JavelinMeasurementItemDao.updateMeasurementItemName(dbName, beforeItemName, afterItemName);
    }

    private boolean checkReportIsExist(final String reportName)
    {
        List<ReportDefinitionDto> reportDefinitionDto = this.getReportByReportName(reportName);
        if (reportDefinitionDto != null && reportDefinitionDto.size() > 0)
        {
            return true;
        }
        return false;
    }
}
