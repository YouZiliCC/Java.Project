package com.paper.repository;

import com.paper.model.JournalMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 期刊指标Repository - 使用JPA进行数据访问
 * 安全、高效，避免SQL注入风险
 */
@Repository
public interface JournalMetricsRepository extends JpaRepository<JournalMetrics, Long> {
    
    /**
     * 查询所有期刊的最新年份数据
     */
    @Query("""
        SELECT j FROM JournalMetrics j
        WHERE j.year = (
            SELECT MAX(j2.year) FROM JournalMetrics j2
            WHERE j2.journal = j.journal
        )
        ORDER BY j.journal
        """)
    List<JournalMetrics> findAllLatestYears();
    
    /**
     * 根据期刊名称查询所有年份数据
     */
    List<JournalMetrics> findByJournalOrderByYearDesc(String journal);
    
    /**
     * 查询所有不同的期刊名称
     */
    @Query("SELECT DISTINCT j.journal FROM JournalMetrics j ORDER BY j.journal")
    List<String> findAllJournalNames();
    
    /**
     * 查询指定期刊的最新年份数据
     */
    @Query("""
        SELECT j FROM JournalMetrics j
        WHERE j.journal = :journal
        AND j.year = (
            SELECT MAX(j2.year) FROM JournalMetrics j2
            WHERE j2.journal = :journal
        )
        """)
    Optional<JournalMetrics> findLatestByJournal(@Param("journal") String journal);
    
    /**
     * 查询两个期刊的最新年份数据
     */
    @Query("""
        SELECT j FROM JournalMetrics j
        WHERE j.journal IN (:journal1, :journal2)
        AND j.year = (
            SELECT MAX(j2.year) FROM JournalMetrics j2
            WHERE j2.journal = j.journal
        )
        """)
    List<JournalMetrics> findLatestByTwoJournals(
        @Param("journal1") String journal1,
        @Param("journal2") String journal2
    );
    
    /**
     * 检查期刊是否存在
     */
    boolean existsByJournal(String journal);
    
    /**
     * 统计总期刊数量
     */
    @Query("SELECT COUNT(DISTINCT j.journal) FROM JournalMetrics j")
    long countDistinctJournals();
}
