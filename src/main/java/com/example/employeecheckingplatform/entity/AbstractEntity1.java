package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractEntity1 {

    @Id
    Long id;

    @Column(updatable = false, nullable = false)
    @JsonIgnore
    @CreationTimestamp
    Date createdAt;

    @Column(nullable = false)
    @JsonIgnore
    @UpdateTimestamp
    Date updatedAt;

    @Column(updatable = false, nullable = false)
    @JsonIgnore
    @CreatedBy
    Long createdBy;

    @Column(nullable = false)
    @JsonIgnore
    @LastModifiedBy
    Long updatedBy;

    @Column(nullable = false)
    @JsonIgnore
    Boolean deleted = false;


}
