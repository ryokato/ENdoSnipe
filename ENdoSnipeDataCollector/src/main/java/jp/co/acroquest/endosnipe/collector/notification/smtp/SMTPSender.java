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
package jp.co.acroquest.endosnipe.collector.notification.smtp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import jp.co.acroquest.endosnipe.collector.LogMessageCodes;
import jp.co.acroquest.endosnipe.collector.config.ConfigurationReader;
import jp.co.acroquest.endosnipe.collector.config.DataCollectorConfig;
import jp.co.acroquest.endosnipe.collector.config.MailTemplateEntity;
import jp.co.acroquest.endosnipe.collector.exception.InitializeException;
import jp.co.acroquest.endosnipe.collector.notification.AlarmEntry;
import jp.co.acroquest.endosnipe.collector.notification.AlarmObserver;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.common.util.KeywordConverter;
import jp.co.acroquest.endosnipe.common.util.KeywordConverterFactory;
import jp.co.acroquest.endosnipe.communicator.impl.CommunicatorMessages;

import org.apache.commons.lang.StringUtils;

/**
 * DataCollectorからSMTPでメール通知を送信するクラス。
 * 
 * @author fujii
 */
public class SMTPSender implements AlarmObserver
{
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger.getLogger(SMTPSender.class);

    /** 現在時刻を日付に変換するフォーマッタ。(時刻は含まない) */
    private final SimpleDateFormat dateFormatter_ = new SimpleDateFormat("yyyy/MM/dd");

    /** 現在時刻を時刻に変換するフォーマッタ。(日付は含まない) */
    private final SimpleDateFormat timeFormatter_ = new SimpleDateFormat("HH:mm:ss");

    /** 現在時刻を時刻(ミリ秒まで)に変換するフォーマッタ。(日付は含まない) */
    private final SimpleDateFormat millisFormatter_ = new SimpleDateFormat("HH:mm:ss.SSS");

    /** SMTPの設定値を保存するオブジェクト */
    private final DataCollectorConfig config_;

    /** 改行文字。 */
    private static final String LS = System.getProperty("line.separator");

    /** JavaMailに渡すSMTPサーバのキー名。 */
    private static final String SMTP_HOST_KEY = "mail.smtp.host";

    /** JavaMailに渡すSMTPサーバのポート番号のキー名。 */
    private static final String SMTP_PORT_KEY = "mail.smtp.port";

    /** JavaMailに渡す認証有無のキー名。 */
    private static final String SMTP_AUTH_KEY = "mail.smtp.auth";

    /** メーラーを表す名前。 */
    private static final String X_MAILER = "ENdoSnipe Mail Sender.";

    /** シグナル名を取得する為の置換キーワード。 */
    private static final String KEYWORD_SIGNAL_NAME = "signalName";

    /** IPアドレスを取得する為の置換キーワード。 */
    private static final String KEYWORD_IP_ADDRESS = "ipAddress";

    /** ポート番号を取得する為の置換キーワード。 */
    private static final String KEYWORD_PORT_NUMBER = "portNumber";

    /** DB名を取得する為の置換キーワード。 */
    private static final String KEYWORD_DATABASE_NAME = "databaseName";

    /** 処理が実行されたスレッドを取得するための置換キーワード。 */
    public static final String KEYWORD_THREAD_ID = "threadId";

    /** 対象の系列名を取得する為の置換キーワード。 */
    public static final String KEYWORD_ITEM_ID = "itemID";

    /** アラームしきい値を取得する為の置換キーワード。 */
    public static final String KEYWORD_ALARM_THRESHOLD = "alarmThreshold";

    /** 状態値を取得する為の置換キーワード。 */
    public static final String KEYWORD_ALARM_VALUE = "alarmValue";

    /** 計測間隔(単位:ミリ秒)を取得する為の置換キーワード。 */
    public static final String KEYWORD_ALARM_INTERVAL = "alarmInterval";

    /** アラームのレベルを取得する為の置換キーワード。 */
    public static final String KEYWORD_ALARM_LEVEL = "alarmLevel";

    /** テンプレートから、発生日を取得する為の置換キーワード。 */
    private static final String KEYWORD_DATE = "date";

    /** テンプレートから、発生時刻を取得する為の置換キーワード。 */
    private static final String KEYWORD_TIME = "time";

    /** テンプレートから、発生時刻(ミリ秒まで)を取得する為の置換キーワード。 */
    private static final String KEYWORD_TIMEMILLIS = "timeMillis";

    /** 認証オブジェクト */
    private Authenticator authenticator_;

    /** シグナルのレベル1 */
    private static final int SIGNAL_LEVEL_1 = 1;

    /** シグナルのレベル2 */
    private static final int SIGNAL_LEVEL_2 = 2;

    /** シグナルのレベル3 */
    private static final int SIGNAL_LEVEL_3 = 3;

    /** シグナルのレベル4 */
    private static final int SIGNAL_LEVEL_4 = 4;

    /** シグナルのレベル5 */
    private static final int SIGNAL_LEVEL_5 = 5;

    /**
     * DataCollectorConfigから設定情報を読み込み、SMTPSenderを初期化する。
     * @throws IOException 入出力エラーが発生した場合
     * @throws InitializeException 初期化例外が発生した場合
     */
    public SMTPSender()
        throws IOException,
            InitializeException
    {
        this.config_ = ConfigurationReader.load("");
        this.printConfig();
        String username = this.config_.getSmtpUser();
        if (StringUtils.isNotEmpty(username))
        {
            String password = this.config_.getSmtpPassword();
            authenticator_ = new SMTPAuthenticator(username, password);
        }
    }

    /**
     * リソース監視のアラームを通知する。
     * @param entry 通知処理を行うデータ
     */
    public void send(final AlarmEntry entry)
    {
        if (entry == null)
        {
            // 送信データがnullの場合は何もせずに終了する
            LOGGER.log(LogMessageCodes.NO_SEND_INFORMATION_MESSAGE);
            return;
        }
        if (!this.config_.isSendMail())
        {
            return;
        }

        try
        {
            // メールオブジェクトを作成する
            MimeMessage message = createMailMessage(this.config_, entry);

            // メールを送信する
            Transport.send(message);
        }
        catch (Exception exception)
        {
            String message = exception.getMessage();
            LOGGER.log(LogMessageCodes.SENDING_MAIL_ERROR_MESSAGE, exception, message);
        }
    }

    /**
     * メールを作成する。
     * 
     * @param config javelin.propertiesで設定した値。
     * @param entry 通知処理を行うデータ
     * @return 作成したメッセージ。
     * @throws MessagingException メッセージ作成中にエラーが発生した場合。
     */
    protected MimeMessage
        createMailMessage(final DataCollectorConfig config, final AlarmEntry entry)
            throws MessagingException
    {
        // JavaMailに渡すプロパティを設定する
        Properties props = System.getProperties();
        String smtpServer = this.config_.getSmtpServer();
        if (smtpServer == null || smtpServer.length() == 0)
        {
            LOGGER.log(LogMessageCodes.SMTP_SERVER_NOT_SPECIFIED);
            String detailMessageKey = "collector.notification.smtp.SMTPSender.SMTPNotSpecified";
            String messageDetail = CommunicatorMessages.getMessage(detailMessageKey);

            throw new MessagingException(messageDetail);
        }
        props.setProperty(SMTP_HOST_KEY, smtpServer);
        int smtpPort = config.getSmtpPort();
        props.setProperty(SMTP_PORT_KEY, String.valueOf(smtpPort));

        // メールサーバに対するセッションキーとなるオブジェクトを作成する
        Session session = null;
        if (authenticator_ == null)
        {
            // パスワード認証を行わない場合
            session = Session.getDefaultInstance(props);
        }
        else
        {
            // パスワード認証を行う場合
            props.setProperty(SMTP_AUTH_KEY, "true");
            session = Session.getDefaultInstance(props, authenticator_);
        }

        // MIMEメッセージを作成する
        MimeMessage message = new MimeMessage(session);

        // メッセージのプロパティを設定する
        // :ヘッダ
        Date date = new Date();
        String encoding = this.config_.getSmtpEncoding();
        message.setHeader("X-Mailer", X_MAILER);
        message.setHeader("Content-Type", "text/plain; charset=\"" + encoding + "\"");
        message.setSentDate(date);

        // :from
        String from = this.config_.getSmtpFrom();
        InternetAddress fromAddr = new InternetAddress(from);
        message.setFrom(fromAddr);

        // :to
        String[] recipients = this.config_.getSmtpTo().split(",");
        for (String toStr : recipients)
        {
            InternetAddress toAddr = new InternetAddress(toStr);
            message.addRecipient(Message.RecipientType.TO, toAddr);
        }

        // :body, subject
        String subject;
        String body;
        subject = createSubject(entry, date);
        try
        {
            body = createBody(entry, date);
        }
        catch (IOException ex)
        {
            LOGGER.log(LogMessageCodes.FAIL_READ_MAIL_TEMPLATE, "");
            body = createDefaultBody(entry, date);
        }

        message.setSubject(subject, encoding);
        message.setText(body, encoding);

        return message;
    }

    /**
     * 設定値のSubjectを読み込み、文字列置換する。
     * 
     * @param data 通知処理を行うデータ
     * @param date 日時
     * @return 変換後のメールSubject。
     */
    private String createSubject(final AlarmEntry entry, final Date date)
    {
        String name = getSimpleSignalName(entry);
        String type = entry.getAlarmType().name().toLowerCase();
        MailTemplateEntity entity = this.config_.getSmtpTemplate(name, type, true);
        String subjectTemplate = entity.subject;
        return convertTemplate(subjectTemplate, entry, date);
    }

    /**
     * メールテンプレートを読み込み、文字列置換する。
     * 
     * @param data 通知処理を行うデータ
     * @param date 日時
     * @return 変換後のメール本文。
     * @throws IOException テンプレートの読み込みに失敗した場合。
     */
    private String createBody(final AlarmEntry entry, final Date date)
        throws IOException
    {
        String name = entry.getSignalName();
        String type = entry.getAlarmType().name().toLowerCase();
        MailTemplateEntity entity = this.config_.getSmtpTemplate(name, type, true);
        String bodyTemplate = entity.body;
        if (bodyTemplate == null)
        {
            return "";
        }
        return convertTemplate(bodyTemplate, entry, date);
    }

    /**
     * テンプレートに対して文字列置換を実施する。
     * 
     * @param template テンプレート文字列。
     * @param data 通知処理を行うデータ
     * @param date 日時
     * @return 変換後の文字列。
     */
    private String convertTemplate(final String template, final AlarmEntry data, final Date date)
    {
        // 発生 ／復旧閾値、イベント名
        double alarmThreshold = data.getSignalValue();

        // 文字列置換を行う
        KeywordConverter conv = KeywordConverterFactory.createDollarBraceConverter();
        String signalName = getSimpleSignalName(data);
        conv.addConverter(KEYWORD_SIGNAL_NAME, signalName);
        conv.addConverter(KEYWORD_IP_ADDRESS, data.getIpAddress());
        conv.addConverter(KEYWORD_PORT_NUMBER, data.getPort());
        conv.addConverter(KEYWORD_DATABASE_NAME, data.getDatabaseName());
        conv.addConverter(KEYWORD_ALARM_THRESHOLD, alarmThreshold);
        conv.addConverter(KEYWORD_ALARM_VALUE, data.getAlarmValue());
        conv.addConverter(KEYWORD_ALARM_INTERVAL, String.valueOf(data.getEscalationPeriod()));
        conv.addConverter(KEYWORD_ALARM_LEVEL, getSignalValue(data));
        conv.addConverter(KEYWORD_DATE, this.dateFormatter_.format(date));
        conv.addConverter(KEYWORD_TIME, this.timeFormatter_.format(date));
        conv.addConverter(KEYWORD_TIMEMILLIS, this.millisFormatter_.format(date));

        return conv.convert(template);
    }

    /**
     * シグナル名の末尾部分を取得する。
     * @param entry {@link AlarmEntry}
     */
    private String getSimpleSignalName(final AlarmEntry entry)
    {
        String signalName = entry.getSignalName();
        int position = signalName.lastIndexOf("/");
        if (position > 0)
        {
            signalName = signalName.substring(position + 1);
        }
        return signalName;
    }

    /**
     * シグナルのアラームレベルを返す。
     * @param alarmEntry {@link AlarmEntry}
     * @return　シグナルのアラームレベル
     */
    private String getSignalValue(final AlarmEntry alarmEntry)
    {
        if (alarmEntry.getSignalLevel() == SIGNAL_LEVEL_3)
        {
            if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_1)
            {
                return "WARNING";
            }
            else if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_2)
            {
                return "CRITICAL";
            }
        }
        else if (alarmEntry.getSignalLevel() == SIGNAL_LEVEL_5)
        {
            if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_1)
            {
                return "INFO";
            }
            else if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_2)
            {
                return "WARNING";
            }
            else if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_3)
            {
                return "ERROR";
            }
            else if (alarmEntry.getSignalValue() == SIGNAL_LEVEL_4)
            {
                return "CRITICAL";
            }
        }
        return "";
    }

    /**
     * エラー時のデフォルトメッセージ文字列を作成する。
     * テンプレートを利用せずに作成する。
     * 
     * @param data 通知処理を行うデータ
     * @param date 日時
     * @return 作成したメッセージ文字列。
     */
    private String createDefaultBody(final AlarmEntry data, final Date date)
    {
        // 発生 ／復旧閾値、イベント名
        double alarmThreshold = data.getSignalValue();

        StringBuilder builder = new StringBuilder();

        builder.append(KEYWORD_ALARM_THRESHOLD + " = " + alarmThreshold + LS);
        builder.append(KEYWORD_ALARM_VALUE + " = " + data.getAlarmValue() + LS);
        builder.append(KEYWORD_ALARM_INTERVAL + " = " + String.valueOf(data.getEscalationPeriod())
            + LS);
        builder.append(KEYWORD_ALARM_LEVEL + " = " + data.getSignalLevel());
        builder.append(KEYWORD_DATE + " = " + this.dateFormatter_.format(date) + LS);
        builder.append(KEYWORD_TIMEMILLIS + " = " + this.millisFormatter_.format(date) + LS);

        return builder.toString();
    }

    /**
     * SMTPの設定値を表示する。
     */
    public void printConfig()
    {
        // SMTPの設定値を呼び出す。
        Boolean smtpSendMail = this.config_.isSendMail();
        String smtpServer = this.config_.getSmtpServer();
        int smtpPort = this.config_.getSmtpPort();
        String smtpUser = this.config_.getSmtpUser();
        String smtpEncoding = this.config_.getSmtpEncoding();
        String smtpFrom = this.config_.getSmtpFrom();
        String[] smtpTo = this.config_.getSmtpTo().split(",");

        StringBuilder builder = new StringBuilder();

        // SMTPの設定値を標準出力する。
        builder.append(LS);
        builder.append("\tcollector.smtp.sendMail             : " + smtpSendMail).append(LS);
        builder.append("\tcollector.smtp.server               : " + smtpServer).append(LS);
        builder.append("\tcollector.smtp.port                 : " + smtpPort).append(LS);
        builder.append("\tcollector.smtp.user                 : " + smtpUser).append(LS);
        builder.append("\tcollector.smtp.from                 : " + smtpFrom).append(LS);
        for (int index = 0; index < smtpTo.length; index++)
        {
            builder.append("\tcollector.smtp.to                   : " + smtpTo[index]).append(LS);
        }
        builder.append("\tcollector.smtp.encoding             : " + smtpEncoding).append(LS);
        builder.append("<<<<");
        LOGGER.log(LogMessageCodes.OUTPUT_SMTP_SETTINGS, builder.toString());
    }

}
