package com.firm.order.modules.base.dao;
import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.firm.order.modules.base.entity.BaseEntity;


@Repository
public interface BaseDao<T extends BaseEntity> extends JpaRepository<T, Serializable>, JpaSpecificationExecutor<T> {
}