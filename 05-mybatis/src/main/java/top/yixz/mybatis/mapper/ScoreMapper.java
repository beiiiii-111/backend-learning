package top.yixz.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.yixz.mybatis.entity.Score;
import top.yixz.mybatis.vo.ScoreSheetVO;

import java.util.List;
import java.util.Map;

/**
 * 成绩 Mapper：多表查询写在这里
 *
 * <p>单表 CRUD 从 BaseMapper 继承；跨表的查询用手写 SQL + @Select 注解，
 * 不用建 XML 文件。</p>
 *
 * @author yixz
 * @description ScoreMapper
 */
@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    /**
     * 一条 SQL 查出成绩单：成绩表 join 学生表取姓名，join 课程表取课程名
     *
     * <p>两个注意点：
     * 1. 别名要和 ScoreSheetVO 的属性名一致（AS studentName），否则查到了也是 null；
     * 2. 逻辑删除不是全局拦截，手写 SQL 必须自己写 deleted = 0，
     *    包括被 join 进来的学生表（st.deleted = 0），漏了赵雪就会回来。</p>
     */
    @Select("""
            SELECT s.id    AS scoreId,
                   st.name AS studentName,
                   c.name  AS courseName,
                   s.score AS score
            FROM t_score s
                     JOIN t_student st ON st.id = s.student_id AND st.deleted = 0
                     JOIN t_course c ON c.id = s.course_id
            WHERE s.deleted = 0
              AND s.student_id = #{studentId}
            ORDER BY s.id
            """)
    List<ScoreSheetVO> selectScoreSheet(@Param("studentId") Long studentId);

    /**
     * 动手练习 2：成绩单分页版
     *
     * <p>第一个参数写成 IPage，返回值也写 IPage，分页插件会自动补 COUNT 和 LIMIT。
     * 注意：MP 靠参数位置找 IPage，所以它必须是第一个参数。</p>
     */
    @Select("""
            SELECT s.id    AS scoreId,
                   st.name AS studentName,
                   c.name  AS courseName,
                   s.score AS score
            FROM t_score s
                     JOIN t_student st ON st.id = s.student_id AND st.deleted = 0
                     JOIN t_course c ON c.id = s.course_id
            WHERE s.deleted = 0
              AND s.student_id = #{studentId}
            ORDER BY s.id
            """)
    IPage<ScoreSheetVO> selectScoreSheetPage(IPage<ScoreSheetVO> page, @Param("studentId") Long studentId);

    /**
     * 动手练习 1：每门课的平均分，带课程名和选课人数
     */
    @Select("""
            SELECT c.name            AS courseName,
                   COUNT(s.id)       AS studentCount,
                   ROUND(AVG(s.score), 2) AS avgScore
            FROM t_score s
                     JOIN t_course c ON c.id = s.course_id
            WHERE s.deleted = 0
            GROUP BY s.course_id, c.name
            ORDER BY avgScore DESC
            """)
    List<Map<String, Object>> selectCourseAvg();

    /**
     * 动手练习 3：每个班的平均分
     *
     * <p>关键点是 st.deleted = 0 —— 赵雪已经被逻辑删除，她的两条成绩不该算进去。</p>
     */
    @Select("""
            SELECT st.class_name     AS className,
                   COUNT(s.id)       AS scoreCount,
                   ROUND(AVG(s.score), 2) AS avgScore
            FROM t_score s
                     JOIN t_student st ON st.id = s.student_id AND st.deleted = 0
            WHERE s.deleted = 0
            GROUP BY st.class_name
            ORDER BY avgScore DESC
            """)
    List<Map<String, Object>> selectClassAvg();
}
