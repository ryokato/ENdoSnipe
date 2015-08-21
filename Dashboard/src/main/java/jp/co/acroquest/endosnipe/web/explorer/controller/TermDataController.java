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
package jp.co.acroquest.endosnipe.web.explorer.controller;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.co.acroquest.endosnipe.data.dao.JavelinLogDao;
import jp.co.acroquest.endosnipe.data.entity.JavelinLog;
import jp.co.acroquest.endosnipe.perfdoctor.WarningUnit;
import jp.co.acroquest.endosnipe.web.explorer.dto.MeasurementValueDto;
import jp.co.acroquest.endosnipe.web.explorer.dto.PerfDoctorTableDto;
import jp.co.acroquest.endosnipe.web.explorer.form.PerfDoctorTermDataForm;
import jp.co.acroquest.endosnipe.web.explorer.form.TermDataForm;
import jp.co.acroquest.endosnipe.web.explorer.manager.DatabaseManager;
import jp.co.acroquest.endosnipe.web.explorer.service.JvnFileEntryJudge;
import jp.co.acroquest.endosnipe.web.explorer.service.MeasurementValueService;
import jp.co.acroquest.endosnipe.web.explorer.util.ResourceNameUtil;
import net.arnx.jsonic.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wgp.util.DataConvertUtil;

/**
 * 期間データを取り扱うコントローラークラス。
 *
 * @author miyasaka
 *
 */
@Controller
@RequestMapping("/termData")
public class TermDataController
{
    /** PerformanceDoctorページのID。 */
    private static final String PERFDOCTOR_POSTFIX_ID = "/performanceDoctor";

    /** MeasurementValueの取得用のインタフェースを提供するクラスのオブジェクト。 */
    @Autowired
    protected MeasurementValueService measurementValueService;

    /**
     * コンストラクタ。
     */
    public TermDataController()
    {

    }

    /**
     * 機関データを取得する。
     *
     * @param data 取得する期間データに関する情報
     * @return 期間データ
     */
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, List<Map<String, String>>> getTermData(
            @RequestParam(value = "data") final String data)
    {
        Map<String, List<Map<String, String>>> responseDataList =
                new HashMap<String, List<Map<String, String>>>();
        TermDataForm termDataForm = JSON.decode(data, TermDataForm.class);
        List<String> dataGroupIdList = termDataForm.getDataGroupIdList();
        if (dataGroupIdList == null)
        {
            // if no dataGroupId, return empty map.
            return new HashMap<String, List<Map<String, String>>>();
        }

        StringBuilder patternStr = new StringBuilder();
        Map<String, List<MeasurementValueDto>> measurementValueMap;
        if (ResourceNameUtil.isLikeSearch(dataGroupIdList))
        {
            // 検索用のlikeパターンを作成する。
            for (String dataId : dataGroupIdList)
            {
                patternStr.append(dataId.replace(".*", "%"));
            }

            long startTime = Long.valueOf(termDataForm.getStartTime());
            long endTime = Long.valueOf(termDataForm.getEndTime());
            Date startDate = new Date(startTime);
            Date endDate = new Date(endTime);
            measurementValueMap =
                    measurementValueService.getMeasurementValueListLike(startDate, endDate,
                                                                        patternStr.toString());
        }
        else
        {
            // 検索用の正規表現パターンを作成する。
            for (String dataId : dataGroupIdList)
            {
                patternStr.append(ResourceNameUtil.getRegularName(dataId));
            }

            long startTime = Long.valueOf(termDataForm.getStartTime());
            long endTime = Long.valueOf(termDataForm.getEndTime());
            Date startDate = new Date(startTime);
            Date endDate = new Date(endTime);
            measurementValueMap =
                    measurementValueService.getMeasurementValueList(startDate, endDate,
                                                                    patternStr.toString());
        }

        if (measurementValueMap.size() > 1)
        {
            // 各のキーのmeasurementValueListを一つのListに結合する
            List<MeasurementValueDto> combinedMeasurementValueList =
                    new ArrayList<MeasurementValueDto>();
            for (Entry<String, List<MeasurementValueDto>> measurementValueEntry : measurementValueMap.entrySet())
            {
                for (MeasurementValueDto measurementValueDto : measurementValueEntry.getValue())
                {
                    combinedMeasurementValueList.add(measurementValueDto);
                }
            }
            List<Map<String, String>> measurementValueList =
                    createTreeMenuData(combinedMeasurementValueList);
            responseDataList.put(dataGroupIdList.get(0), measurementValueList);
        }
        else
        {
            for (Entry<String, List<MeasurementValueDto>> measurementValueEntry : measurementValueMap.entrySet())
            {
                List<Map<String, String>> measurementValueList =
                        createTreeMenuData(measurementValueEntry.getValue());
                responseDataList.put(dataGroupIdList.get(0), measurementValueList);
            }
        }
        if (responseDataList.size() == 0 && dataGroupIdList.size() > 0)
        {
            for (String dataGroupId : dataGroupIdList)
            {
                responseDataList.put(dataGroupId, new ArrayList<Map<String, String>>());
            }
        }

        return responseDataList;
    }

    /**
     * PerformanceDoctorで表示するための期間データを返す。
     *
     * @param data
     *            取得する期間データの定義
     * @return PerformanceDoctorにおける期間データ
     */
    @RequestMapping(value = "/getPerfDoctor", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, List<PerfDoctorTableDto>> getTermPerfDoctorData(
            @RequestParam(value = "data") final String data)
    {
        Map<String, List<PerfDoctorTableDto>> responceDataList =
                new HashMap<String, List<PerfDoctorTableDto>>();
        DatabaseManager dbManager = DatabaseManager.getInstance();
        String dbName = dbManager.getDataBaseName(1);

        PerfDoctorTermDataForm termDataForm = JSON.decode(data, PerfDoctorTermDataForm.class);
        List<String> dataGroupIdList = termDataForm.getDataGroupIdList();

        for (String dataGroupId : dataGroupIdList)
        {
            List<JavelinLog> list = new ArrayList<JavelinLog>();

            try
            {
                list = JavelinLogDao.selectByTermAndName(dbName, null, null, dataGroupId, true);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            JvnFileEntryJudge judge = new JvnFileEntryJudge();
            List<WarningUnit> warnings = judge.judge(list, true, true);
            List<PerfDoctorTableDto> dtoList = new ArrayList<PerfDoctorTableDto>();
            for (WarningUnit warning : warnings)
            {
                PerfDoctorTableDto dto = new PerfDoctorTableDto();
                dto.setDate(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(
                                                                                        warning.getStartTime())));
                dto.setDescription(warning.getDescription());
                dto.setLevel(warning.getLevel());
                dto.setClassName(warning.getClassName());
                dto.setMethodName(warning.getMethodName());
                dto.setDetailResult(warning.getLogFileName());
                dtoList.add(dto);
            }

            list.clear();
            responceDataList.put(dataGroupId + PERFDOCTOR_POSTFIX_ID, dtoList);
        }

        return responceDataList;
    }

    /**
     * ツリーメニューのデータを作成する。
     *
     * @param treeMenuDtoList ツリーメニューのDtoのリスト
     * @return ツリーメニューのデータ
     */
    private List<Map<String, String>> createTreeMenuData(final List<?> treeMenuDtoList)
    {
        List<Map<String, String>> bufferDtoList = new ArrayList<Map<String, String>>();
        for (Object treeMenuData : treeMenuDtoList)
        {
            bufferDtoList.add(DataConvertUtil.getPropertyList(treeMenuData));
        }
        return bufferDtoList;
    }
}
