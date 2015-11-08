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
package jp.co.acroquest.endosnipe.web.explorer.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.common.util.MessageUtil;
import jp.co.acroquest.endosnipe.web.explorer.config.ConfigurationReader;
import jp.co.acroquest.endosnipe.web.explorer.config.DashBoardConfig;
import jp.co.acroquest.endosnipe.web.explorer.constants.LogMessageCodes;
import jp.co.acroquest.endosnipe.web.explorer.constants.ResponseConstants;
import jp.co.acroquest.endosnipe.web.explorer.dao.DashboardInfoDao;
import jp.co.acroquest.endosnipe.web.explorer.dto.ResponseDto;
import jp.co.acroquest.endosnipe.web.explorer.entity.DashboardInfo;
import jp.co.acroquest.endosnipe.web.explorer.exception.InitializeException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * Dashboard用サービスクラス。
 *
 * @author fujii
 */
@Service
public class DashboardService
{
    /** ロガー */
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger.getLogger(DashboardService.class);

    /**
     * ダッシュボード情報Dao
     */
    @Autowired
    protected DashboardInfoDao dashboardInfoDao_;

    /**
     * コンストラクタ
     */
    public DashboardService()
    {

    }

    /**
     * 全てのダッシュボードデータを返す。
     *
     * @return ダッシュボードデータ
     */
    public List<Map<String, String>> getAllDashboard()
    {
        List<DashboardInfo> dashboardList = null;
        try
        {
            dashboardList = dashboardInfoDao_.selectAll();
        }
        catch (PersistenceException pe)
        {
            Throwable cause = pe.getCause();
            if (cause instanceof SQLException)
            {
                SQLException sqlEx = (SQLException)cause;
                LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
            }
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION);
            return new ArrayList<Map<String, String>>();
        }

        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        for (DashboardInfo dashboardInfo : dashboardList)
        {
            Map<String, String> dataMap = this.convertDataMap(dashboardInfo);
            resultList.add(dataMap);
        }
        return resultList;
    }

    /**
     * ダッシュボードの名前の一覧を取得する
     * @return ダッシュボードの名前の一覧
     */
    public List<String> getNames()
    {
        return dashboardInfoDao_.selectName();
    }

    /**
     * ダッシュボードを登録する。
     * @param dashboardInfo 登録するダッシュボード情報
     * @return 登録結果電文
     */
    public ResponseDto insert(final DashboardInfo dashboardInfo)
    {
        ResponseDto responseDto = new ResponseDto();
        if (getByName(dashboardInfo.name) != null)
        {
            String errorMessage = MessageUtil.getMessage("WEWD0160", new Object[] {});
            responseDto.setMessage(errorMessage);
            responseDto.setResult(ResponseConstants.RESULT_FAILURE);
            return responseDto;
        }

        // 最終更新日時を設定
        dashboardInfo.lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());
        int count = 0;
        try
        {
            count = dashboardInfoDao_.insert(dashboardInfo);
            if (count > 0)
            {
                responseDto.setResult(ResponseConstants.RESULT_SUCCESS);
                // Dashboard名の一覧を取得する
                List<String> dashboardNameList = dashboardInfoDao_.selectName();
                responseDto.setData(dashboardNameList);
            }
            else
            {
                String errorMessage = MessageUtil.getMessage("WEWD0161", new Object[] {});
                responseDto.setResult(ResponseConstants.RESULT_FAILURE);
                responseDto.setMessage(errorMessage);
            }

            return responseDto;
        }
        catch (DuplicateKeyException dkEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, dkEx, dkEx.getMessage());
            String errorMessage = MessageUtil.getMessage("WEWD0160", new Object[] {});
            responseDto.setMessage(errorMessage);
            responseDto.setResult(ResponseConstants.RESULT_FAILURE);
            return responseDto;
        }
    }

    /**
     * ダッシュボードを更新する。
     *
     * @param dashboardInfo ダッシュボード情報
     * @return 更新結果電文
     */
    public ResponseDto update(final DashboardInfo dashboardInfo)
    {
        return update(dashboardInfo, false);
    }

    /**
     * ダッシュボードを更新する。
     *
     * @param dashboardInfo ダッシュボード情報
     * @param auto 自動生成したマップかどうか。（true：自動生成したマップである、false：自動生成したマップでない。）
     * @return 更新結果電文
     */
    public ResponseDto update(final DashboardInfo dashboardInfo, final boolean auto)
    {
        // 最終更新日時を設定
        dashboardInfo.lastUpdate = new Timestamp(Calendar.getInstance().getTimeInMillis());
        int count = 0;
        ResponseDto responseDto = new ResponseDto();
        try
        {
            if (auto == true)
            {
                count = dashboardInfoDao_.updateByName(dashboardInfo);
            }
            else
            {
                count = dashboardInfoDao_.update(dashboardInfo);
            }
            if (count > 0)
            {
                responseDto.setResult(ResponseConstants.RESULT_SUCCESS);
            }
            else
            {
                String errorMessage = MessageUtil.getMessage("WEWD0162", new Object[] {});
                responseDto.setResult(ResponseConstants.RESULT_FAILURE);
                responseDto.setMessage(errorMessage);
            }

            return responseDto;
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

            String errorMessage = MessageUtil.getMessage("WEWD0163", new Object[] {});
            responseDto.setMessage(errorMessage);
            responseDto.setResult(ResponseConstants.RESULT_FAILURE);
            return responseDto;
        }
    }

    /**
     * ダッシュボードを取得する。
     * @param dashboardId Target dashboardId
     * @return 取得結果
     */
    public ResponseDto getById(final long dashboardId)
    {
        ResponseDto responseDto = new ResponseDto();
        try
        {
            DashboardInfo dashboardInfo = dashboardInfoDao_.selectById(dashboardId);
            if (dashboardInfo == null)
            {
                String errorMessage = MessageUtil.getMessage("WEWD0164", new Object[] {});
                responseDto.setMessage(errorMessage);
                responseDto.setResult(ResponseConstants.RESULT_FAILURE);
                return responseDto;
            }

            Map<String, String> convertData = this.convertDataMap(dashboardInfo);
            responseDto.setResult(ResponseConstants.RESULT_SUCCESS);
            responseDto.setData(convertData);

            return responseDto;
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

            String errorMessage = MessageUtil.getMessage("WEWD0165", new Object[] {});
            responseDto.setMessage(errorMessage);
            responseDto.setResult(ResponseConstants.RESULT_FAILURE);
            return responseDto;
        }
    }

    /**
     * ダッシュボードを取得する。
     * @param name ダッシュボード名
     * @return 取得結果
     */
    public DashboardInfo getByName(final String name)
    {
        try
        {
            return dashboardInfoDao_.selectByName(name);
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
            return null;
        }
    }

    /**
     * ダッシュボードを削除する。
     * @param dashboardId ダッシュボードID
     * @return 削除結果
     */
    public ResponseDto removeDashboardById(final long dashboardId)
    {
        int count = 0;
        ResponseDto responseDto = new ResponseDto();
        try
        {
            count = dashboardInfoDao_.deleteById(dashboardId);
            if (count == 0)
            {
                String errorMessage = MessageUtil.getMessage("WEWD0166", new Object[] {});
                responseDto.setMessage(errorMessage);
                responseDto.setResult(ResponseConstants.RESULT_FAILURE);
                return responseDto;
            }

            responseDto.setResult(ResponseConstants.RESULT_SUCCESS);

            // Dashboard名の一覧を取得する
            List<String> dashboardNameList = dashboardInfoDao_.selectName();
            responseDto.setData(dashboardNameList);

            return responseDto;
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

            String errorMessage = MessageUtil.getMessage("WEWD0167", new Object[] {});
            responseDto.setMessage(errorMessage);
            responseDto.setResult(ResponseConstants.RESULT_FAILURE);
            return responseDto;
        }
    }

    /**
     * ダッシュボード情報をMap形式に変換する。
     * @param dashboardInfo ダッシュボード情報
     * @return Map形式のダッシュボード情報
     */
    private Map<String, String> convertDataMap(final DashboardInfo dashboardInfo)
    {
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("id", String.valueOf(dashboardInfo.dashboardId));
        dataMap.put("parentTreeId", "");
        dataMap.put("data", dashboardInfo.name);
        dataMap.put("treeId", String.valueOf(dashboardInfo.dashboardId));
        dataMap.put("dashboardData", dashboardInfo.data);
        return dataMap;
    }

    /**
     * 背景画像のリストを取得する。
     * @param directoryPath 背景画像格納フォルダ
     * @param relativePath 相対パス
     * @return 背景画像のリスト取得結果
     */
    public ResponseDto getImageList(final String directoryPath, final String relativePath)
    {
        Map<String, String> imageMap = new HashMap<String, String>();
        File directory = new File(directoryPath);
        if (directory.isDirectory())
        {
            File[] imageFile = directory.listFiles();

            for (File image : imageFile)
            {
                String imageName = image.getName();
                if (imageName.endsWith(".png"))
                {
                    imageMap.put(imageName, relativePath + "/" + imageName);
                }
            }
        }

        ResponseDto responseDto = new ResponseDto();
        responseDto.setResult(ResponseConstants.RESULT_SUCCESS);
        responseDto.setData(imageMap);
        return responseDto;
    }

    /**
     * デフォルトの系列数を設定ファイルから取得する。
     * @param filePath 設定ファイルのパス
     * @return デフォルトの系列数
     */
    public int getDefautlSeriesNumber(final String filePath)
    {
        int defautlSeriesNumber = DashBoardConfig.DEFAULT_SERIES_NUM;
        try
        {
            DashBoardConfig config = ConfigurationReader.loadDashBoardConfig(filePath);
            defautlSeriesNumber = config.getSeriesNumber();
        }
        catch (InitializeException ex)
        {
            LOGGER.log(LogMessageCodes.CANNOT_FIND_PROPERTY, ex, ex.getMessage(), filePath);
        }
        catch (IOException ex)
        {
            LOGGER.log(LogMessageCodes.CANNOT_FIND_PROPERTY, ex, ex.getMessage(), filePath);
        }

        return defautlSeriesNumber;
    }
}
