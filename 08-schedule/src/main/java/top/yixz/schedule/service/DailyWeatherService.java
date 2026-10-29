package top.yixz.schedule.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author yxzhang
 * @date 2026/10/21
 * @description 定时拉取和风天气数据并邮件推送
 **/
@Slf4j
@Service
public class DailyWeatherService {

    /**
     * 全局复用一个 OkHttpClient，它内部自带连接池，每次 new 会浪费连接
     */
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    /**
     * 和风天气约定的成功状态码
     */
    private static final String SUCCESS_CODE = "200";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Resource
    private JavaMailSender mailSender;

    @Value("${weather.api.key}")
    private String weatherApiKey;
    @Value("${weather.api.city}")
    private String cityId;
    @Value("${weather.api.city-name:南京}")
    private String cityName;
    @Value("${weather.api.url}")
    private String weatherApiUrl;
    @Value("${weather.mail.recipients}")
    private String recipients;
    @Value("${weather.mail.subject-prefix:【今日天气】}")
    private String subjectPrefix;
    @Value("${spring.mail.username}")
    private String fromMail;

    /**
     * 定时规则写在配置里，改时间不用动代码；默认每天 08:30:00 推送
     */
    @Scheduled(cron = "${weather.cron:0 30 8 * * ?}")
    public void sendDailyWeather() {
        // 0、没配 KEY 就别发请求了，直接提示去配，避免打出一堆看不懂的 401
        if (weatherApiKey == null || weatherApiKey.isBlank()) {
            log.warn("【天气推送】未配置和风天气 KEY，请设置环境变量 WEATHER_API_KEY 后再试");
            return;
        }
        if (recipients == null || recipients.isBlank()) {
            log.warn("【天气推送】未配置收件人，请设置环境变量 WEATHER_MAIL_RECIPIENTS");
            return;
        }

        try {
            // 1、请求和风天气 API
            String weatherResponse = getWeatherData();
            if (weatherResponse == null) {
                log.error("【天气推送】获取天气数据失败");
                return;
            }

            // 2、解析 JSON：daily 数组的第 0 项就是今天
            JSONObject weatherJson = JSONObject.parseObject(weatherResponse);
            String code = weatherJson.getString("code");
            if (!SUCCESS_CODE.equals(code)) {
                log.error("【天气推送】和风天气返回异常 code={}，请检查 KEY 是否有效、city 是否正确", code);
                return;
            }

            JSONArray daily = weatherJson.getJSONArray("daily");
            if (daily == null || daily.isEmpty()) {
                log.error("【天气推送】返回结果里没有 daily 数据");
                return;
            }
            JSONObject todayWeather = daily.getJSONObject(0);

            String fxDate = todayWeather.getString("fxDate");
            String tempMax = todayWeather.getString("tempMax");
            String tempMin = todayWeather.getString("tempMin");
            String textDay = todayWeather.getString("textDay");
            String textNight = todayWeather.getString("textNight");
            String windDirDay = todayWeather.getString("windDirDay");
            String windScaleDay = todayWeather.getString("windScaleDay");
            String humidity = todayWeather.getString("humidity");

            // 3、组装邮件主题与内容
            String subject = String.format("%s%s %s天气", subjectPrefix, LocalDate.now().format(DATE_FORMATTER), cityName);
            String content = String.format("""
                    %s %s 天气实况：

                    白天天气：%s
                    夜间天气：%s
                    气温区间：%s℃ ~ %s℃
                    风向风力：%s %s 级
                    相对湿度：%s%%

                    —— 本邮件由定时任务自动发送，请勿回复 ——
                    """, fxDate, cityName, textDay, textNight, tempMin, tempMax, windDirDay, windScaleDay, humidity);

            // 4、发送
            sendMail(subject, content);
            log.info("【天气推送】邮件已发送，收件人：{}", recipients);
        } catch (Exception e) {
            // 定时任务里的异常必须自己兜住，否则线程池里抛出去会被静默吞掉
            log.error("【天气推送】执行失败", e);
        }
    }

    /**
     * 调用和风天气 3 天预报接口
     *
     * @return 接口返回的 JSON 字符串，失败返回 null
     */
    private String getWeatherData() throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(weatherApiUrl), "天气接口地址不合法")
                .newBuilder()
                .addQueryParameter("location", cityId)
                .addQueryParameter("key", weatherApiKey)
                .build();

        Request request = new Request.Builder().url(url).get().build();

        // try-with-resources 保证响应体被关掉，否则连接池会一直被占着
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.error("【天气推送】请求失败，HTTP 状态码 {}", response.code());
                return null;
            }
            return response.body().string();
        }
    }

    /**
     * 发送纯文本邮件，收件人支持逗号分隔多个
     *
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    private void sendMail(String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(recipients.split("\\s*,\\s*"));
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
