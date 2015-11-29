package jp.co.acroquest.endosnipe.web.explorer.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.common.logger.SystemLogger;
import jp.co.acroquest.endosnipe.data.entity.JavelinLog;
import jp.co.acroquest.endosnipe.web.explorer.constants.LogMessageCodes;
import jp.co.acroquest.endosnipe.web.explorer.servlet.JvnDownloadServlet;
import jp.co.acroquest.endosnipe.web.explorer.util.DaoUtil;

import org.springframework.stereotype.Service;

/**
 * PerformanceDoctorService
 * @author acroquest
 *
 */
@Service
public class PerformanceDoctorService
{
    /** ロガー */
    private static final ENdoSnipeLogger LOGGER =
            ENdoSnipeLogger.getLogger(JvnDownloadServlet.class);

    /** バッファのサイズ */
    private static final int BUFFER_SIZE = 1024;

    /**
     * コンストラクタ
     */
    public PerformanceDoctorService()
    {

    }

    /**
     * クライアントからのjvnファイル出力ダウンロードを受信するためのサーブレットです。
     * 
     * @param response
     *            {@link HttpServletResponse}オブジェクト
     * @param fileName ファイル名
     */
    public void doRequest(final HttpServletResponse response, final String fileName)
    {
        try
        {
            // Javalinログを取得する
            JavelinLog jvnLog = DaoUtil.getJavelinLog("1", fileName);
            if (jvnLog == null)
            {
                LOGGER.log(LogMessageCodes.FAIL_GET_JVNLOG);
                return;
            }
            // Javalinログをクライアントに返す
            printOutFile(null, response, fileName, jvnLog.javelinLog);
        }
        catch (IOException ex)
        {
            // Do Nothing.
            SystemLogger.getInstance().warn(ex);
        }
    }

    /**
     * ファイルを出力します。
     * @param req {@link HttpServletRequest}オブジェクト
     * @param res {@link HttpServletResponse}オブジェクト
     * @param logFileName ログファイル名
     * @param inputStream 入力ストリーム
     * @throws IOException 入出力例外
     */
    private void printOutFile(final HttpServletRequest req, final HttpServletResponse res,
            final String logFileName, InputStream inputStream)
        throws IOException
    {
        OutputStream os = res.getOutputStream();
        try
        {
            // レスポンス設定
            res.setContentType("application/octet-stream");
            res.setHeader("Content-Disposition", "filename=\"" + logFileName + "\"");

            int len = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((len = inputStream.read(buffer)) >= 0)
            {
                os.write(buffer, 0, len);
            }

            inputStream.close();
        }
        catch (IOException ex)
        {
            printOutNotFound(res);
        }
        finally
        {

            if (os != null)
            {
                try
                {
                    os.close();
                    inputStream.close();
                }
                catch (IOException ex)
                {
                    LOGGER.log(LogMessageCodes.IO_ERROR);

                }
                finally
                {
                    os = null;
                    inputStream = null;
                }
            }
        }
    }

    /**
     * ファイルが見つからない場合
     * 
     * @param res
     *            {@link HttpServletResponse}
     */
    private void printOutNotFound(final HttpServletResponse res)
    {
        OutputStream toClient = null;
        try
        {
            toClient = res.getOutputStream();
            res.setContentType("text/html;charset=Shift_JIS");
            toClient.write("File not found".getBytes());
            toClient.close();
        }
        catch (IOException ex)
        {
            LOGGER.log(LogMessageCodes.IO_ERROR);
        }
        finally
        {
            if (toClient != null)
            {
                try
                {
                    toClient.close();
                }
                catch (IOException ex)
                {
                    LOGGER.log(LogMessageCodes.IO_ERROR);
                }
                finally
                {
                    toClient = null;
                }
            }
        }
    }

    /**
     * Get data according to fileName
     * 
     * @param fileName
     *           jvn log file name
     *  @return detailData
     */
    public String getPerfDoctorDetailData(final String fileName)
    {
        StringBuilder detailData = new StringBuilder();

        try
        {

            JavelinLog jvnLog = DaoUtil.getJavelinLog("1", fileName);
            if (jvnLog == null)
            {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(jvnLog.javelinLog));
            String line;
            while ((line = reader.readLine()) != null)
            {
                detailData.append(line + "%");
            }

            reader.close();

        }
        catch (IOException ex)
        {
            // Do Nothing.
            SystemLogger.getInstance().warn(ex);
        }
        return detailData.toString();
    }
}
