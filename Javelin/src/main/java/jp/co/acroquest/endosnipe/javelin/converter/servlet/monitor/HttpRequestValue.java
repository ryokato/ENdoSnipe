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
package jp.co.acroquest.endosnipe.javelin.converter.servlet.monitor;

import java.util.Map;

/**
 * HttpRequestValueクラス
 * @author acroquest
 *
 */
public class HttpRequestValue
{
    /** パス情報 */
    private String pathInfo_;

    /** コンテキストパス */
    private String contextPath_;

    /** サーブレットパス */
    private String servletPath_;

    /** リモートホスト */
    private String remoteHost_;

    /** リモートポート */
    private int remotePort_;

    /** メソッド名 */
    private String method_;

    /** クエリストリング */
    private String queryString_;

    /** エンコード文字列 */
    private String characterEncoding_;

    /** パラメータのMap */
    @SuppressWarnings("rawtypes")
    private Map parameterMap_;

    /** セッションID */
    private String sessionId_;

    /** IPアドレス */
    private String ipAddress_;

    /** Cookieから取得した値 */
    private String cookieValue_;

    /**
     * パス情報を取得します。
     * @return パス情報
     */
    public String getPathInfo()
    {
        return pathInfo_;
    }

    /**
     * パス情報を設定します。
     * @param pathInfo パス情報
     */
    public void setPathInfo(String pathInfo)
    {
        pathInfo_ = pathInfo;
    }

    /**
     * コンテキストパスを取得します。
     * @return コンテキストパス
     */
    public String getContextPath()
    {
        return contextPath_;
    }

    /**
     * コンテキストパスを設定します。
     * @param contextPath コンテキストパス
     */
    public void setContextPath(String contextPath)
    {
        contextPath_ = contextPath;
    }

    /**
     * サーブレットパスを取得します。
     * @return サーブレットパス
     */
    public String getServletPath()
    {
        return servletPath_;
    }

    /**
     * サーブレットパスを設定します。
     * @param servletPath サーブレットパス
     */
    public void setServletPath(String servletPath)
    {
        servletPath_ = servletPath;
    }

    /**
     * リモートホスト名を取得します。
     * @return リモートホスト名
     */
    public String getRemoteHost()
    {
        return remoteHost_;
    }

    /**
     * リモートホスト名を設定します。
     * @param remoteHost リモートホスト名
     */
    public void setRemoteHost(String remoteHost)
    {
        remoteHost_ = remoteHost;
    }

    /**
     * リモートポート番号を取得します。
     * @return リモートポート番号
     */
    public int getRemotePort()
    {
        return remotePort_;
    }

    /**
     * リモートポート番号を設定します。
     * @param remotePort リモートポート番号
     */
    public void setRemotePort(int remotePort)
    {
        remotePort_ = remotePort;
    }

    /**
     * メソッド名を取得します。
     * @return メソッド名
     */
    public String getMethod()
    {
        return method_;
    }

    /**
     * メソッド名を設定します。
     * @param method メソッド名
     */
    public void setMethod(String method)
    {
        method_ = method;
    }

    /**
     * クエリストリングを取得します。
     * @return クエリストリング
     */
    public String getQueryString()
    {
        return queryString_;
    }

    /**
     * クエリストリングを設定します。
     * @param queryString クエリストリング
     */
    public void setQueryString(String queryString)
    {
        queryString_ = queryString;
    }

    /**
     * エンコード文字列を取得します。
     * @return エンコード文字列
     */
    public String getCharacterEncoding()
    {
        return characterEncoding_;
    }

    /**
     * エンコード文字列を設定します。
     * @param characterEncoding エンコード文字列
     */
    public void setCharacterEncoding(String characterEncoding)
    {
        characterEncoding_ = characterEncoding;
    }

    /**
     * パラメータのMapを取得します。
     * @return パラメータのMap
     */
    @SuppressWarnings("rawtypes")
    public Map getParameterMap()
    {
        return parameterMap_;
    }

    /**
     * パラメータのMapを設定します。
     * @param parameterMap パラメータのMap
     */
    @SuppressWarnings("rawtypes")
    public void setParameterMap(Map parameterMap)
    {
        parameterMap_ = parameterMap;
    }

    /**
     * セッションIDを取得する。
     * @return セッションID
     */
    public String getSessionId()
    {
        return sessionId_;
    }

    /**
     * セッションIDを設定する。
     * @param sessionId セッションID
     */
    public void setSessionId(String sessionId)
    {
        sessionId_ = sessionId;
    }

    /**
     * IPアドレスを取得する。
     * @return IPアドレス
     */
    public String getIpAddress()
    {
        return ipAddress_;
    }

    /**
     * IPアドレスを設定する。
     * @param ipAddress IPアドレス
     */
    public void setIpAddress(String ipAddress)
    {
        ipAddress_ = ipAddress;
    }

    /**
     * Cookieの値を取得する。
     * @return Cookieの値
     */
    public String getCookieValue()
    {
        return cookieValue_;
    }

    /**
     * Cookieの値を設定する。
     * @param cookieValue Cookieの値
     */
    public void setCookieValue(String cookieValue)
    {
        cookieValue_ = cookieValue;
    }

}
