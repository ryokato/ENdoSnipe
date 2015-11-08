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
package jp.co.acroquest.endosnipe.web.explorer.config;

/**
 * ENdoSnipe DashBoard の設定/定数を保持するクラスです。<br />
 *
 * @author kato
 */
public class DashBoardConfig
{
    /** ダッシュボードの系列数 */
    private int seriesNumber_ = DEFAULT_SERIES_NUM;

    /** ダッシュボードのシステムデフォルト系列数 */
    public static final int DEFAULT_SERIES_NUM = 10;

    /**
     * コンストラクタ。
     */
    public DashBoardConfig()
    {
    }

    /**
     * ダッシュボードの系列数を返します。<br />
     * @return ダッシュボードの系列数
     */
    public int getSeriesNumber()
    {
        return seriesNumber_;
    }

    /**
     * ダッシュボードの系列数を設定します。<br />
     * @param number ダッシュボードの系列数
     */
    public void setSeriesNumber(final int number)
    {
        this.seriesNumber_ = number;
    }
}