package com.demo.be.repository;

import com.demo.be.model.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {

    List<ThongBao> findByRecipientUsernameOrderByThoiGianTaoDesc(String recipientUsername);

    long countByRecipientUsernameAndDaDocFalse(String recipientUsername);

    @Modifying
    @Query("UPDATE ThongBao t SET t.daDoc = true WHERE t.recipientUsername = :username AND t.daDoc = false")
    void markAllAsReadByUsername(String username);
}
