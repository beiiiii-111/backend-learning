package top.yixz.schedule.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yxzhang
 * @date 2026/10/21
 * @description cron 表达式定时任务示例
 **/
@Slf4j
@Service
public class CronTaskService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 3 秒任务的累计执行次数，用来在日志里对照有没有漏跑
     */
    private final AtomicInteger counter = new AtomicInteger();

    /**
     * 效果一：在指定时间触发一次。
     * <p>
     * cron 六位的含义（从左到右）：秒 分 时 日 月 周，第六位通常写 ? 表示不关心星期。
     * 这里取 expression 里写死的具体时刻，默认 每天 09:30:00 执行一次，
     * 分钟和小时位是固定值，所以一天只会在这一刻命中。
     */
    @Scheduled(cron = "${schedule.fixed-time.cron:0 14 9 * * ?}")
    public void runAtFixedTime() {
        log.info("[定点任务] 到点触发一次，当前时间 {}", LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 效果二：每隔 3 秒触发一次。
     * <p>
     * 秒字段写星号加步长（星号 / 3），表示从 0 秒开始每 3 秒走一格：
     * 00、03、06 …… 57 秒各触发一次，下一分钟重新从 00 秒开始。*/

   // @Scheduled(cron = "${schedule.period.cron:*/3 * * * * ?}")
   // public void runEveryThreeSeconds() {
     //   log.info("[3秒任务] 第 {} 次执行，当前时间 {}",
       //         counter.incrementAndGet(), LocalDateTime.now().format(FORMATTER));
  //  }

}
