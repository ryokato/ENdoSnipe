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
package jp.co.acroquest.endosnipe.common.util;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jp.co.acroquest.endosnipe.common.Constants;
import jp.co.acroquest.endosnipe.common.entity.ItemType;
import jp.co.acroquest.endosnipe.common.entity.MeasurementData;
import jp.co.acroquest.endosnipe.common.entity.MeasurementDetail;
import jp.co.acroquest.endosnipe.common.entity.ResourceData;

/**
 * システムリソースを扱うためのユーティリティクラス<br />
 * 
 * @author ochiai
 */
public class ResourceDataUtil
{
    /** パーセントにするための定数:100 */
    public static final int PERCENT_CONST = 100;

    /** nano と mili の変換のための定数：1000*1000 */
    public static final int NANO_TO_MILI = 1000 * 1000;

    /** CPU使用率（％）の最大値 */
    public static final double MAX_CPU_RATE = 100.0;

    /** CPU使用率（％）の最小値 */
    public static final double MIN_CPU_RATE = 0.0;

    /**
     * 割合を表すデータ値をDBに入れる時に掛ける値。<br />
     * DBに入れられるのは整数値なので、小数点以下も保持するために、この値を掛ける。<br />
     */
    public static final int PERCENTAGE_DATA_MAGNIFICATION = 1000;

    private ResourceDataUtil()
    {
        // do nothing.
    }

    /**
     * 指定されたデータを基にし、計測値を全て0に、計測時刻を指定されたものに変更した、別のデータを生成し、返します。<br>
     * @param srcData 元のデータ
     * @param measurementTime 変更後の計測時刻
     * @param connected 接続時かどうか
     * @return 計測値を0にし、計測時刻を変更したデータ
     */
    public static ResourceData createAllZeroResourceData(final ResourceData srcData,
        final long measurementTime, final boolean connected)
    {
        ResourceData dstData = new ResourceData();
        dstData.measurementTime = measurementTime;
        dstData.hostName = srcData.hostName;
        dstData.ipAddress = srcData.ipAddress;
        dstData.portNum = srcData.portNum;

        Set<Entry<String, MeasurementData>> measurementMapEntrySet =
            srcData.getMeasurementMap().entrySet();

        for (Map.Entry<String, MeasurementData> measurementMapEntry : measurementMapEntrySet)
        {
            MeasurementData srcMeasurementData = measurementMapEntry.getValue();
            MeasurementData dstMeasurementData = new MeasurementData();
            dstMeasurementData.measurementType = srcMeasurementData.measurementType;
            dstMeasurementData.itemName = srcMeasurementData.itemName;
            dstMeasurementData.measurementTime = new Timestamp(measurementTime);
            dstMeasurementData.valueType = ItemType.getItemTypeNumber(ItemType.ITEMTYPE_STRING);

            Set<Entry<String, MeasurementDetail>> detailMapEntrySet =
                srcMeasurementData.getMeasurementDetailMap().entrySet();

            for (Map.Entry<String, MeasurementDetail> measurementDetailMapEntry : detailMapEntrySet)
            {
                MeasurementDetail srcMeasurementDetail = measurementDetailMapEntry.getValue();
                MeasurementDetail dstMeasurementDetail = new MeasurementDetail();
                // JavaUpTimeの場合、0ではなく1をセットする。
                if (Constants.ITEMNAME_JAVAUPTIME.equals(dstMeasurementData.itemName))
                {
                    if (connected == true)
                    {
                        dstMeasurementDetail.value = "1";
                    }
                    else
                    {
                        dstMeasurementDetail.value = srcMeasurementDetail.value;
                    }
                }
                else
                {
                    dstMeasurementDetail.value = "0";
                }
                dstMeasurementDetail.displayName = srcMeasurementDetail.displayName;
                dstMeasurementDetail.itemId = srcMeasurementDetail.itemId;
                dstMeasurementDetail.itemName = srcMeasurementDetail.itemName;
                dstMeasurementDetail.itemNum = srcMeasurementDetail.itemNum;
                dstMeasurementDetail.type = srcMeasurementDetail.type;
                dstMeasurementDetail.typeItemName = srcMeasurementDetail.typeItemName;
                dstMeasurementDetail.valueId = srcMeasurementDetail.valueId;
                dstMeasurementData.addMeasurementDetail(dstMeasurementDetail);
            }

            dstData.addMeasurementData(dstMeasurementData);
        }

        return dstData;
    }

    /**
     * 指定された、連続2回のデータを比べ、可変数系列について、後のものにしか無いものを探します。<br>
     * それらについて、グラフの始まりを表すために追加すべきデータ（値が0のもの）を作成し、返します。<br>
     * @param prevData 前回のデータ
     * @param currData 今回のデータ
     * @param prevData2 前々回のデータ
     * @return グラフの始まりを表すために追加すべきデータ。
     */
    public static ResourceData createAdditionalPreviousData(final ResourceData prevData,
        final ResourceData currData, final ResourceData prevData2)
    {
        ResourceData additionalData = new ResourceData();
        // 時刻は前回のものを用いる。
        additionalData.measurementTime = prevData.measurementTime;
        additionalData.clientId = currData.clientId;
        additionalData.hostName = currData.hostName;
        additionalData.ipAddress = currData.ipAddress;
        additionalData.portNum = currData.portNum;

        Set<Entry<String, MeasurementData>> measurementMapEntrySet =
            currData.getMeasurementMap().entrySet();

        for (Map.Entry<String, MeasurementData> measurementMapEntry : measurementMapEntrySet)
        {
            MeasurementData currMeasurementData = measurementMapEntry.getValue();
            MeasurementData prevMeasurementData =
                getPrevMeasurementData(prevData, prevData2, measurementMapEntry);
            MeasurementData additionalMeasurementData = new MeasurementData();
            additionalMeasurementData.measurementType = currMeasurementData.measurementType;
            additionalMeasurementData.itemName = currMeasurementData.itemName;
            // 時刻は前回のものを用いる。
            additionalMeasurementData.measurementTime = new Timestamp(prevData.measurementTime);
            additionalMeasurementData.valueType =
                ItemType.getItemTypeNumber(ItemType.ITEMTYPE_STRING);

            Set<Entry<String, MeasurementDetail>> detailMapEntry =
                currMeasurementData.getMeasurementDetailMap().entrySet();

            for (Map.Entry<String, MeasurementDetail> measurementDetailMapEntry : detailMapEntry)
            {
                MeasurementDetail currMeasurementDetail = measurementDetailMapEntry.getValue();
                String key = measurementDetailMapEntry.getKey();
                MeasurementDetail prevMeasurementDetail =
                    prevMeasurementData.getMeasurementDetailMap().get(key);

                if (prevMeasurementDetail == null)
                {
                    MeasurementDetail addtionalMeasurementDetail = new MeasurementDetail();
                    // 値は0を入れる。
                    addtionalMeasurementDetail.value = "0";
                    addtionalMeasurementDetail.displayName = currMeasurementDetail.displayName;
                    addtionalMeasurementDetail.itemId = currMeasurementDetail.itemId;
                    addtionalMeasurementDetail.itemName = currMeasurementDetail.itemName;
                    addtionalMeasurementDetail.itemNum = currMeasurementDetail.itemNum;
                    addtionalMeasurementDetail.type = currMeasurementDetail.type;
                    addtionalMeasurementDetail.typeItemName = currMeasurementDetail.typeItemName;
                    addtionalMeasurementDetail.valueId = currMeasurementDetail.valueId;
                    additionalMeasurementData.addMeasurementDetail(addtionalMeasurementDetail);
                }
            }

            additionalData.addMeasurementData(additionalMeasurementData);
        }

        return additionalData;
    }

    /**
     * 前回の計測値を返す。前回の計測値が見つからない場合は、前々回の値を返す。
     * @param prevData 前回の計測値
     * @param prevData2 前々回の計測値
     * @param measurementMapEntry
     * @return 前回の計測値。
     */
    private static MeasurementData getPrevMeasurementData(final ResourceData prevData,
        final ResourceData prevData2, final Map.Entry<String, MeasurementData> measurementMapEntry)
    {
        MeasurementData prevMeasurementData =
            prevData.getMeasurementMap().get(measurementMapEntry.getKey());
        if (prevMeasurementData != null)
        {
            return prevMeasurementData;
        }
        if (prevData2 != null)
        {
            prevMeasurementData = prevData2.getMeasurementMap().get(measurementMapEntry.getKey());
            if (prevMeasurementData != null)
            {
                return prevMeasurementData;
            }
        }
        return new MeasurementData();
    }

    /**
     * CPU使用率を計算する
     * @param cpuTime 計測期間中のCPU時間（ナノ秒）
     * @param measurementInterval 計測間隔(ミリ秒)
     * @param processorCount プロセッサ数
     * @return CPU使用率
     */
    public static double calcCPUUsage(final long cpuTime, final long measurementInterval,
        final long processorCount)
    {
        double cpuUsage = 0.0;
        if (measurementInterval * processorCount > 0)
        {
            cpuUsage =
                (double)cpuTime / (measurementInterval * NANO_TO_MILI * processorCount)
                    * PERCENT_CONST;
            // パフォーマンスカウンタの仕様上、CPU使用率が100％を超えることがあるため、
            // 最大100％に丸める。（#2006）
            cpuUsage = Math.min(cpuUsage, MAX_CPU_RATE);
        }
        return cpuUsage;
    }
}
