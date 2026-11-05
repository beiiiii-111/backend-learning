package com.group6.campus.repository;

import com.group6.campus.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    java.util.Optional<Activity> findByIdAndIsDeleted(Long id, Integer isDeleted);

    /**
     * 名额扣减：条件更新保证原子性与不超卖（对应评审方案的核心 SQL）。
     * 返回影响行数：0 = 名额已满或活动不可报名。
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Activity a SET a.taken = a.taken + 1, a.updateTime = CURRENT_TIMESTAMP " +
           "WHERE a.id = :id AND a.taken < a.capacity AND a.status = 'approved' AND a.isDeleted = 0")
    int increaseTaken(@Param("id") Long id);

    /** 取消报名释放名额 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Activity a SET a.taken = CASE WHEN a.taken > 0 THEN a.taken - 1 ELSE 0 END, " +
           "a.updateTime = CURRENT_TIMESTAMP WHERE a.id = :id")
    int decreaseTaken(@Param("id") Long id);
}
