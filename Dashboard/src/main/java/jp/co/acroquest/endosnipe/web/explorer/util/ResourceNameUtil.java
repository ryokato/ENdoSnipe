/*******************************************************************************
 * ENdoSnipe 6.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Acroquest Technology Co.,Ltd.
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
package jp.co.acroquest.endosnipe.web.explorer.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * システムリソースファイルの名称を管理するユーティリティ
 * @author fujii
 *
 */
public class ResourceNameUtil
{
    private static Map<String, String> regularNamePattern__ = new HashMap<String, String>();

    /**
     * インスタンス化を防止するprivateコンストラクタ
     */
    private ResourceNameUtil()
    {
        // Do Nothing.
    }

    /**
     * LIKE検索が行えるかを判定する。
     * 
     * @param dataGroupIdList 検索条件
     * @return LIKE検索が行えるかどうか。
     */

    public static boolean isLikeSearch(final List<String> dataGroupIdList)
    {
        if (dataGroupIdList == null || dataGroupIdList.size() > 1)
        {
            return false;
        }

        String str = dataGroupIdList.get(0).replace(".*", "");
        return str.replaceAll("[\\[\\]{}.+*^$|\\\\?]", "").length() == str.length();
    }

    /**
     * 正規表現上の名称を返す。
     * @param resourceName リソース名
     * @return　正規表現上の名称
     */
    public static String getRegularName(final String resourceName)
    {
        synchronized (regularNamePattern__)
        {
            String regularName = regularNamePattern__.get(resourceName);
            if (regularName == null)
            {
                regularName = resourceName.replaceAll("([\\[\\](){}.+*^$|\\\\?])", "\\\\$1");
                // ".*"となっている箇所は置換しない。
                regularName = regularName.replaceAll("\\\\.\\\\\\*", "\\.\\*");
                regularNamePattern__.put(resourceName, regularName);
            }
            return regularName;
        }
    }
}
