package com.example.employeecheckingplatform.repository;

import com.example.employeecheckingplatform.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment,Long> {

    Optional<Attachment> findByHashIdAndDeleted(String hashId, boolean deleted);

}
