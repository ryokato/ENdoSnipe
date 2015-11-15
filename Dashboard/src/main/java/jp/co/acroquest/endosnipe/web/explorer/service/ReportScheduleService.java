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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.co.acroquest.endosnipe.util.ResourceDataDaoUtil;
import jp.co.acroquest.endosnipe.web.explorer.dao.ReportDefinitionDao;
import jp.co.acroquest.endosnipe.web.explorer.dao.SchedulingReportDefinitionDao;
import jp.co.acroquest.endosnipe.web.explorer.dto.ReportDefinitionDto;
import jp.co.acroquest.endosnipe.web.explorer.dto.SchedulingReportDefinitionDto;
import jp.co.acroquest.endosnipe.web.explorer.entity.ReportDefinition;
import jp.co.acroquest.endosnipe.web.explorer.entity.SchedulingReportDefinition;
import jp.co.acroquest.endosnipe.web.explorer.util.ReportUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * 
 * @author khinlay
 *
 */
@Service
public class ReportScheduleService
{
    /** 先月 */
    private static final int LAST_MONTH = -1;

    /** 一週間前 */
    private static final int SEVEN_DAYS_AGO = -7;

    /** 昨日 */
    private static final int YESTERDAY = LAST_MONTH;

    /** マンスリー出力 */
    private static final String TERM_MONTHRY = "MONTHLY";

    /** ウィークリー出力 */
    private static final String TERM_WEEKLY = "WEEKLY";

    /** デイリー出力 */
    private static final String TERM_DAILY = "DAILY";

    /** 時間のフォーマット */
    private static final String TIME_FORMAT = "HH:mm";

    /** 来月 */
    private static final int NEXT_MONTH = 1;

    /** 来週 */
    private static final int NEXT_WEEK = 7;

    /** 明日 */
    private static final int TOMORROW = 1;

    /** スケジュール定義のDAO */
    @Autowired
    protected SchedulingReportDefinitionDao schedulingReportDefinitionDao_;

    /** レポート出力結果のDAO */
    @Autowired
    protected ReportDefinitionDao reportDefinitionDao_;

    /**
     * デフォルトコンストラクタ。
     */
    public ReportScheduleService()
    {
    }

    /**
     * スレッドを実行する
     * @param context アプリケーションコンテキスト
     */
    public void run(final ApplicationContext context)
    {
        Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());

        List<SchedulingReportDefinition> definitions =
                schedulingReportDefinitionDao_.selectPreviousSchedule(currentTime);

        for (SchedulingReportDefinition definition : definitions)
        {
            SchedulingReportDefinitionDto dto = convert(definition);
            ReportDefinition reportDefinition = convertScheduleToReport(dto);
            reportDefinition.status_ = "creating";
            ReportDefinitionDto definitionDto =
                    ReportUtil.convertReportDifinitionDto(reportDefinition);

            //put the report to the queue for export
            ReportUtil.createReport(definitionDto);

            //既にエクスポートされているレポートが存在しない場合は新規に追加する
            SchedulingReportDefinition exportedDefinition =
                    schedulingReportDefinitionDao_.selectByName(reportDefinition.reportName_);
            if (exportedDefinition == null)
            {
                ReportUtil.sendSchedulingReportDefinition(dto, "add");
            }

            reportDefinitionDao_.insert(reportDefinition);

            Calendar nextExportCal = createNextTerm(reportDefinition, definition);
            Timestamp timestamp = new Timestamp(nextExportCal.getTimeInMillis());
            definition.planExportedTime_ = timestamp;

            schedulingReportDefinitionDao_.update(definition);
        }
    }

    /**
     * convert to report definition.
     * @param scheduling scheduling report dto
     * @return definiton
     */
    private ReportDefinition convertScheduleToReport(final SchedulingReportDefinitionDto scheduling)
    {
        ReportDefinition definition = new ReportDefinition();
        definition.reportId_ = scheduling.getReportId();
        definition.reportName_ = scheduling.getReportName();
        definition.targetMeasurementName_ = scheduling.getTargetMeasurementName();

        Timestamp planExportedTime = scheduling.getPlanExportedTime();
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);
        fromCalendar.setTimeInMillis(planExportedTime.getTime());

        if (TERM_DAILY.equals(scheduling.getTerm()))
        {
            fromCalendar.add(Calendar.DAY_OF_MONTH, YESTERDAY);
        }
        else if (TERM_WEEKLY.equals(scheduling.getTerm()))
        {
            fromCalendar.add(Calendar.DAY_OF_MONTH, SEVEN_DAYS_AGO);
        }
        else if (TERM_MONTHRY.equals(scheduling.getTerm()))
        {
            fromCalendar.add(Calendar.MONTH, LAST_MONTH);
        }

        Date date = new Date(planExportedTime.getTime());
        definition.toTime_ = date;
        definition.fmTime_ = fromCalendar.getTime();
        String tableName = ResourceDataDaoUtil.getTableNameToInsert(date, REPORT_EXPORT_RESULT);
        definition.tableName_ = tableName;

        return definition;
    }

    /**
     * DefinitionをDTOへ変換する
     * @param definition SchedulingReportDefinition
     * @return SchedulingReportDefinitionDto
     */
    private SchedulingReportDefinitionDto convert(final SchedulingReportDefinition definition)
    {
        SchedulingReportDefinitionDto dto = new SchedulingReportDefinitionDto();
        dto.setReportId(definition.reportId_);
        dto.setReportName(definition.reportName_);
        dto.setTargetMeasurementName(definition.targetMeasurementName_);
        dto.setTerm(definition.term_);
        dto.setPlanExportedTime(definition.planExportedTime_);
        dto.setDay(definition.day_);
        dto.setDate(definition.date_);

        DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        Calendar time = Calendar.getInstance();
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        try
        {
            time.setTime(timeFormat.parse(definition.time_));
            dto.setTime(time);
        }
        catch (ParseException ex)
        {
            return null;
        }

        return dto;
    }

    /**
     * 次にレポート出力する日時のカレンダーを作成する
     * @param reportDefinition ReportDefinition
     * @param schedulingDefinition SchedulingReportDefinition
     * @return 次にレポート出力する日時
     */
    private Calendar createNextTerm(final ReportDefinition reportDefinition,
            final SchedulingReportDefinition schedulingDefinition)
    {
        DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        Calendar tmpCalendar = Calendar.getInstance();
        try
        {
            tmpCalendar.setTime(timeFormat.parse(schedulingDefinition.time_));
        }
        catch (ParseException ex)
        {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        //時、分を今回スケジュールが実行された時間にセットする
        calendar.set(Calendar.HOUR_OF_DAY, tmpCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, tmpCalendar.get(Calendar.MINUTE));

        //設定に応じて次回起動時刻を設定する
        if (schedulingDefinition.term_.equals(TERM_MONTHRY))
        {
            calendar.add(Calendar.MONTH, NEXT_MONTH);
        }
        else if (schedulingDefinition.term_.equals(TERM_WEEKLY))
        {
            calendar.add(Calendar.DAY_OF_MONTH, NEXT_WEEK);
        }
        else if (schedulingDefinition.term_.equals(TERM_DAILY))
        {
            calendar.add(Calendar.DAY_OF_MONTH, TOMORROW);
        }

        return calendar;
    }
}
