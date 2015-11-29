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
package jp.co.acroquest.endosnipe.communicator.accessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import jp.co.acroquest.endosnipe.common.config.JavelinConfig;
import jp.co.acroquest.endosnipe.common.entity.ItemType;
import jp.co.acroquest.endosnipe.common.entity.MeasurementData;
import jp.co.acroquest.endosnipe.common.entity.MeasurementDetail;
import jp.co.acroquest.endosnipe.common.entity.ResourceData;
import jp.co.acroquest.endosnipe.communicator.TelegramUtil;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.MeasurementConstants;
import jp.co.acroquest.endosnipe.communicator.entity.RequestBody;
import jp.co.acroquest.endosnipe.communicator.entity.ResponseBody;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramItemNameIdMap;

/**
 * リソース通知電文のためのアクセサクラスです。<br />
 * @author fujii
 */
public class ResourceNotifyAccessor implements TelegramConstants, MeasurementConstants
{
    /** グラフの系列が１つ */
    private static final int SINGLE_RESOURCE = 1;

    /** グラフの系列が２つ以上 */
    private static final int MULTI_RESOURCE = 2;

    /** グラフ系列の電文を表す項目名の接尾辞 */
    private static final String NAME_POSTFIX = "-name";

    /** Javelinの設定。 */
    private static final JavelinConfig CONFIG = new JavelinConfig();

    /** 表示名変換マップ */
    private static Map<String, String> convMap__ = new HashMap<String, String>(0);

    /** 置換変数を特定するための正規表現文字列。 */
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{[A-z0-9][A-z0-9_.-]*\\}");

    /** 計測項目名(ID)の接頭辞。定義ファイルから取得した文字列。 */
    private static String prefixTemplate__;

    /** 計測項目名(ID)に接頭辞を付与しない項目の前方一致パターンリスト。 */
    private static List<String> noPrefixPatternList__ = new ArrayList<String>();

    static
    {
        prefixTemplate__ = CONFIG.getItemNamePrefix();
        String tmpNoPrefixPatternStr = CONFIG.getItemNameNoPrefixList();
        if (tmpNoPrefixPatternStr != null)
        {
            String[] tmpNoPrefixPatternArr = tmpNoPrefixPatternStr.split(",");
            for (String pattern : tmpNoPrefixPatternArr)
            {
                // 空文字列、空白のみは除外する
                if (pattern.trim().length() == 0)
                {
                    continue;
                }
                noPrefixPatternList__.add(pattern.trim());
            }
        }
    }

    /**
     * プライベートコンストラクタ
     */
    private ResourceNotifyAccessor()
    {
        // Do Nothing.
    }

    /**
     * {@link ResourceData}オブジェクトからリソース通知の電文を作成します。<br />
     *
     * @param resourceData {@link ResourceData}オブジェクト
     * @return 電文内容
     */
    public static Telegram getResourceTelgram(final ResourceData resourceData)
    {
        List<Body> responseBodyList = new ArrayList<Body>();

        // 時刻を追加する。
        Long currentTime = resourceData.measurementTime;
        ResponseBody timeBody = makeTimeBody(currentTime);
        responseBodyList.add(timeBody);

        Map<String, MeasurementData> measurementDataMap = resourceData.getMeasurementMap();
        for (MeasurementData measurementData : measurementDataMap.values())
        {
            String itemName = measurementData.itemName;
            byte itemType = measurementData.valueType;

            // measurementDataの詳細を追加する。
            Map<String, MeasurementDetail> measurementDetailMap =
                measurementData.getMeasurementDetailMap();

            List<String> nameList = new ArrayList<String>();
            List<Object> valueList = new ArrayList<Object>();
            for (MeasurementDetail detail : measurementDetailMap.values())
            {
                String value = detail.value;

                if ("".equals(detail.displayName) == false)
                {
                    nameList.add(detail.displayName);
                    valueList.add(value);
                }
                else
                {
                    ResponseBody body =
                        makeResourceResponseBody(itemName, value, ItemType.getItemType(itemType));
                    responseBodyList.add(body);
                }
            }

            if (nameList.size() > 0)
            {
                ResponseBody valueBody =
                    makeResourceResponseBody(itemName, valueList, ItemType.getItemType(itemType));
                responseBodyList.add(valueBody);

                // DisplayName対応
                List<String> convNameList = new ArrayList<String>(nameList.size());
                for (String name : nameList)
                {
                    // DisplayNameが個別設定されていたら適用
                    if (convMap__.containsKey(name))
                    {
                        convNameList.add(convMap__.get(name));
                    }
                    else
                    {
                        convNameList.add(name);
                    }
                }

                ResponseBody nameBody =
                    makeResourceResponseBody(itemName + "-name", convNameList,
                                             ItemType.ITEMTYPE_STRING);
                responseBodyList.add(nameBody);
            }
        }

        Telegram responseTelegram = makeResponseTelegram(responseBodyList);

        return responseTelegram;
    }

    private static ResponseBody makeResourceResponseBody(String itemName, List<?> nameList,
        ItemType itemType)
    {
        // 値を追加する。
        ResponseBody valueBody = new ResponseBody();
        valueBody.setStrObjName(OBJECTNAME_RESOURCE);
        valueBody.setStrItemName(itemName);
        valueBody.setIntLoopCount(nameList.size());
        valueBody.setByteItemMode(itemType);
        valueBody.setObjItemValueArr(nameList.toArray(new Object[nameList.size()]));

        // DisplayNameが個別設定されていたら適用
        if (convMap__.containsKey(itemName))
        {
            valueBody.setStrObjDispName(convMap__.get(itemName));
        }

        return valueBody;
    }

    /**
     * 受信した電文からリソースデータを作成します。<br />
     * 電文種別がリソース通知電文でない場合や、要求応答種別が応答でない場合、<br />
     * 内容が不正である場合は<code>null</code>を返します。<br />
     *
     * @param telegram リソース通知電文
     * @param dbName データベース名
     * @param agentName エージェント名
     *
     * @return 電文から作成した{@link ResourceData}オブジェクト
     */
    public static ResourceData createResourceData(final Telegram telegram, String dbName,
        String agentName)
    {
        if (checkTelegramKind(telegram) == false || checkResponseKind(telegram) == false)
        {
            return null;
        }

        Logger logger = Logger.getLogger(ResourceNotifyAccessor.class);
        logger.debug("ResourceNotifyAccessor.createResourceData() begin");

        ResourceData resourceData = new ResourceData();

        Body[] bodies = telegram.getObjBody();

        List<TelegramItemNameIdMap> itemNameIdMapList =
            TelegramUtil.getTelegramItemNameIdMapList(bodies);

        int bodyMax = bodies.length;
        int bodyCnt = 0;
        while (bodyCnt < bodyMax)
        {
            Body body = bodies[bodyCnt];
            String objectName = body.getStrObjName();

            if (OBJECTNAME_ITEMNAME_ID_MAP.equals(objectName))
            {
                // 項目名とIDのマップは、圧縮/解凍に使うためのものであるため、パスする
                bodyCnt++;
                continue;
            }

            // 項目名が圧縮されている場合も考慮して、解凍後の項目名を取得する
            String itemName = TelegramUtil.getMeasurementItemName(itemNameIdMapList, body);

            if (TIME_RESOURCE.equals(objectName))
            {
                // 項目名が時刻の場合には時刻を設定し、ホストIDの時にはホストIDを設定する。
                // それ以外の場合には、計測値情報を表すとして、ResourceDataに値を設定する。
                // ※接頭辞が付く場合を想定して、後方一致で確認している
                if (itemName != null && itemName.endsWith(ITEMNAME_TIME))
                {
                    if (body.getIntLoopCount() != 1)
                    {
                        return null;
                    }

                    // サーバの時刻でグラフのデータを保存する。
                    resourceData.measurementTime = (Long)body.getObjItemValueArr()[0];
                }

                // 時間の項目が来ないと無限ループになるので、どちらにしてもカウントアップする
                bodyCnt++;
            }
            else if (OBJECTNAME_RESOURCE.equals(objectName))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("ResourceNotifyAccessor.createResourceData: "
                        + "call addMeasurementData([" + bodyCnt + "]:" + itemName + ")");
                }
                bodyCnt =
                    addMeasurementData(resourceData, bodies, bodyCnt, dbName, agentName,
                                       itemNameIdMapList);
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.info("ResourceNotifyAccessor.createResourceData: "
                        + "unknown ObjectName: [" + bodyCnt + "]=" + objectName);
                }
                bodyCnt++;
            }
        }
        return resourceData;
    }

    /**
     * {@link ResourceData}オブジェクトに計測情報を追加します。<br />
     *
     * @param resourceData {@link ResourceData}オブジェクト
     * @param bodies 電文本体の配列
     * @param cnt 読込中の電文位置
     * @param dbName データベース名
     * @param itemNameIdMapList 電文サイズを解凍する際に使う、項目名とIDのセット
     *
     * @return 次の電文位置
     */
    private static int addMeasurementData(ResourceData resourceData, Body[] bodies, int cnt,
        String dbName, String agentName, List<TelegramItemNameIdMap> itemNameIdMapList)
    {
        Logger logger = Logger.getLogger(ResourceNotifyAccessor.class);

        MeasurementData data = new MeasurementData();

        // 計測値情報を追加
        Body measurementBody = bodies[cnt];
        String measurementObjName = measurementBody.getStrObjName();

        // 項目名が圧縮されている場合も考慮して、解凍後の項目名を取得する
        String itemName = TelegramUtil.getMeasurementItemName(itemNameIdMapList, measurementBody);
        String measuremnetItemName = agentName + itemName;

        if (OBJECTNAME_RESOURCE.equals(measurementObjName))
        {
            // 計測値情報を決定する。
            data.measurementType = 0;
            data.itemName = measuremnetItemName;
            data.valueType = ItemType.getItemTypeNumber(measurementBody.getByteItemMode());

            data.displayName = measurementBody.getStrObjDispName();
            if (isSingleResource(bodies, cnt))
            {
                MeasurementDetail measurementDetail = new MeasurementDetail();
                measurementDetail.displayName = "";
                if (measurementBody.getIntLoopCount() == 1)
                {
                    Object[] valueArray = measurementBody.getObjItemValueArr();
                    data.valueType = ItemType.getItemTypeNumber(ItemType.ITEMTYPE_STRING);
                    measurementDetail.value = String.valueOf(valueArray[0]);
                }
                ItemType itemType = measurementBody.getByteItemMode();
                measurementDetail.valueId = ItemType.getItemTypeNumber(itemType);
                data.addMeasurementDetail(measurementDetail);

                if (logger.isDebugEnabled())
                {
                    logger.debug("ResourceNotifyAccessor.addMeasurementData: "
                        + "addMeasurementData(" + data + ")");
                }

                resourceData.addMeasurementData(data);
                return (cnt + SINGLE_RESOURCE);
            }
            else
            {
                // 計測値詳細(計測値、表示名)を追加
                Body nameBody = bodies[cnt + 1];

                Object[] valueArr = measurementBody.getObjItemValueArr();

                String nameObjName = nameBody.getStrObjName();
                Object[] nameArr = null;
                if (OBJECTNAME_RESOURCE.equals(nameObjName))
                {
                    nameArr = nameBody.getObjItemValueArr();
                }

                for (int num = 0; num < valueArr.length; num++)
                {
                    String name = (String)nameArr[num];
                    String value = String.valueOf(valueArr[num]);

                    MeasurementDetail detail = new MeasurementDetail();
                    detail.value = String.valueOf(value);
                    detail.displayName = name;
                    data.addMeasurementDetail(detail);
                }
                data.valueType = ItemType.getItemTypeNumber(ItemType.ITEMTYPE_STRING);

                if (logger.isDebugEnabled())
                {
                    logger.debug("ResourceNotifyAccessor.addMeasurementData: "
                        + "addMeasurementData(" + data + ")");
                }

                resourceData.addMeasurementData(data);
                return (cnt + MULTI_RESOURCE);
            }
        }
        return (cnt + SINGLE_RESOURCE);
    }

    /**
     * 読込中の電文タが、単一のデータ系列をあらわすか、複数のデータ系列をあらわすか。<br />
     * 単一データ系列である場合は、以下のいずれかを満たします。<br />
     * <ul>
     * <li>読込中の電文本体が、配列の最後である。</li>
     * <li>読込中の電文の次の電文のオブジェクト名が"resources"でない。</li>
     * <li>読込中の電文の次の電文の項目名が、"&lt;読込中電文の項目名&gt;-name"でない。</li>
     * </ul>
     *
     * @param bodies 電文本体の配列
     * @param cnt 読み込み中の電文の番号
     * @return 単一データ系列であれば、<code>true</code>
     */
    private static boolean isSingleResource(Body[] bodies, int cnt)
    {
        // 読込中の電文本体が、配列の最後である場合
        if (bodies.length <= (cnt + 1))
        {
            return true;
        }
        Body valueBody = bodies[cnt];
        Body nameBody = bodies[cnt + 1];

        // 読込中の電文の次の電文のオブジェクト名が"resources"であり、
        // 読込中の電文の次の電文の項目名が、"<読込中電文の項目名>-name"である場合
        if (OBJECTNAME_RESOURCE.equals(nameBody.getStrObjName()))
        {
            String valueItemName = valueBody.getStrItemName();
            String nameItemName = nameBody.getStrItemName();
            if ((valueItemName + NAME_POSTFIX).equals(nameItemName))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 電文種別がリソース通知であるかどうか<br />
     *
     * @param telegram 電文
     * @return リソース通知電文である場合、<code>true</code>
     */
    private static boolean checkTelegramKind(final Telegram telegram)
    {
        Header header = telegram.getObjHeader();
        if (BYTE_TELEGRAM_KIND_RESOURCENOTIFY == header.getByteTelegramKind())
        {
            return true;
        }
        return false;
    }

    /**
     * 要求応答種別が応答あるいは通知であるかどうか
     * @param telegram 電文
     * @return true:要求応答種別が応答あるいは通知である、false:要求応答種別が応答あるいは通知でない。
     */
    private static boolean checkResponseKind(final Telegram telegram)
    {
        Header header = telegram.getObjHeader();
        if (BYTE_REQUEST_KIND_RESPONSE == header.getByteRequestKind()
            || BYTE_REQUEST_KIND_NOTIFY == header.getByteRequestKind())
        {
            return true;
        }
        return false;
    }

    /**
     * 要求本体を作成する。
     *
     * @param itemName 項目名
     * @return 要求本体
     */
    public static RequestBody makeResourceRequestBody(final String itemName)
    {
        RequestBody requestBody = new RequestBody();
        requestBody.setStrObjName(OBJECTNAME_RESOURCE);
        requestBody.setStrItemName(itemName);
        return requestBody;
    }

    /**
     * 応答本体を作成する。
     *
     * @param itemName 項目名
     * @param value 値
     * @param itemType 項目の型
     * @return 要求本体
     */
    public static ResponseBody makeResourceResponseBody(String itemName, Object value,
        ItemType itemType)
    {
        ResponseBody responseBody = new ResponseBody();
        responseBody.setStrObjName(OBJECTNAME_RESOURCE);
        responseBody.setStrItemName(itemName);
        responseBody.setIntLoopCount(1);
        responseBody.setByteItemMode(itemType);
        responseBody.setObjItemValueArr(new Object[]{value});

        // DisplayNameが個別設定されていたら適用
        if (convMap__.containsKey(itemName))
        {
            responseBody.setStrObjDispName(convMap__.get(itemName));
        }

        return responseBody;
    }

    /**
     * 応答電文を作成する。
     * 
     * @param responseBodyList 応答電文に詰めるBodyのリスト。
     * @return 作成した応答電文(Telegram)。
     */
    public static Telegram makeResponseTelegram(List<Body> responseBodyList)
    {
        return makeTelegram(responseBodyList, BYTE_REQUEST_KIND_RESPONSE);
    }

    /**
     * 応答電文を作成する。
     * 
     * @param responseBodyList 応答電文に詰めるBodyのリスト。
     * @return 作成した応答電文(Telegram)。
     */
    public static Telegram makeNotifyTelegram(List<Body> responseBodyList)
    {
        return makeTelegram(responseBodyList, BYTE_REQUEST_KIND_NOTIFY);
    }

    /**
     * 応答電文を作成する。
     * 
     * @param responseBodyList 応答電文に詰めるBodyのリスト。
     * @param requestKind リクエスト種別
     * @return 作成した応答電文(Telegram)。
     */
    public static Telegram makeTelegram(List<Body> responseBodyList, byte requestKind)
    {
        Header responseHeader = new Header();
        responseHeader.setByteTelegramKind(BYTE_TELEGRAM_KIND_RESOURCENOTIFY);
        responseHeader.setByteRequestKind(requestKind);

        Telegram responseTelegram = new Telegram();
        responseTelegram.setObjHeader(responseHeader);

        Body[] objBodies = responseBodyList.toArray(new Body[responseBodyList.size()]);

        // strItemNameに、ホスト名などの接頭辞を付与する
        //  - クラスタ名は強制的に付与する。
        //  - 除外対象となる文字列で始まるものには付与しない
        //  - 付与する接頭辞はフォーマットを定義できる

        String prefix = getPrefixStr();

        for (Body body : objBodies)
        {
            String itemName = body.getStrItemName();
            boolean needPrefix = isPrefixNeeded(itemName);
            if (needPrefix == true)
            {
                String concreteItemName = appendPrefix(prefix, itemName);

                // 接頭辞と項目名の間にスラッシュが入らないケースを回避する
                body.setStrItemName(concreteItemName);
            }
            else
            {
                // 接頭辞と項目名の間にスラッシュが入らないケースを回避する
                body.setStrItemName(itemName);
            }
        }

        responseTelegram.setObjBody(objBodies);

        return responseTelegram;
    }

    private static String appendPrefix(String prefix, String itemName)
    {
        StringBuilder builder = new StringBuilder(prefix);

        if (prefix.length() > 0 && itemName.length() > 0 && prefix.endsWith("/") == false
            && itemName.startsWith("/") == false)
        {
            builder.append("/");
        }
        String concreteItemName = builder.append(itemName).toString();
        return concreteItemName;
    }

    /**
     * 接頭辞を付与する必要があるかどうか判定する。<br/>
     * javelin.propertiesに定義した、除外対象のItemNameに前方一致する場合は、
     * 接頭辞を付与しない。
     * 
     * @param itemName 判定対象の項目名称。
     * @return 接頭辞を付与する場合はtrue、付与しない場合はfalse。
     */
    private static boolean isPrefixNeeded(String itemName)
    {
        if (itemName == null)
        {
            return false;
        }

        boolean isPrefixNeeded = true;
        for (String pattern : noPrefixPatternList__)
        {
            if (itemName.startsWith(pattern) == true)
            {
                isPrefixNeeded = false;
                break;
            }
        }
        return isPrefixNeeded;
    }

    /**
     * 定義から取得した文字列を元に、接頭辞となる文字列を生成する。<br/>
     * <p>
     * 定義は、javelin.properties 内 javelin.resource.itemName.prefix に記述する。
     * </p>
     * <p>
     * 定義中には置換変数を含めることができる。<br/>
     * 置換変数の書式は ${(システムプロパティ名)} である。
     * ただし、システムプロパティ名に指定できる文字は英数字、
     * 半角ハイフン、半角アンダースコア、半角ピリオドのみとする。
     * 
     * @return 生成した接頭辞。
     */
    private static String getPrefixStr()
    {
        Matcher varMatcher = VAR_PATTERN.matcher(prefixTemplate__);

        // 定義から取得した文字列中に、変数が存在する場合は抽出する
        List<String> varList = new ArrayList<String>();
        int startIndex = 0;
        while (varMatcher.find(startIndex))
        {
            int beginIndex = varMatcher.start();
            int endIndex = varMatcher.end();
            if (endIndex <= beginIndex + 1)
            {
                startIndex = endIndex;
                continue;
            }
            String varStr = prefixTemplate__.substring(beginIndex + 2, endIndex - 1);
            varList.add(varStr);
            startIndex = endIndex;
        }

        // 変数がなければそのままの文字列を返す
        if (varList.size() == 0)
        {
            return prefixTemplate__;
        }

        // 変数部分は、システムプロパティから値を取得して置換する
        String tempPrefix = prefixTemplate__;
        for (String propName : varList)
        {
            String propValue = System.getProperty(propName);
            if (propValue != null)
            {
                tempPrefix = tempPrefix.replaceAll("\\$\\{" + propName + "\\}", propValue);
            }
        }

        return tempPrefix;
    }

    /**
     * 時間項目のBodyを作成します。
     * 
     * @param time 時刻。
     * @return 作成したBody。
     */
    public static ResponseBody makeTimeBody(long time)
    {
        Long currentTimeLong = Long.valueOf(time);
        ResponseBody timeBody = new ResponseBody();
        timeBody.setStrObjName(TIME_RESOURCE);
        timeBody.setStrItemName(ITEMNAME_TIME);
        timeBody.setIntLoopCount(1);
        timeBody.setByteItemMode(ItemType.ITEMTYPE_LONG);
        timeBody.setObjItemValueArr(new Object[]{currentTimeLong});
        return timeBody;
    }

    /**
     * 表示名変換マップを設定します。
     *
     * @param convMap 表示名変換マップ
     */
    public static void setConvMap(Map<String, String> convMap)
    {
        convMap__ = convMap;
    }

}
