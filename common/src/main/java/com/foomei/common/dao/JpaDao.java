package com.foomei.common.dao;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaDao<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T>, QueryDslPredicateExecutor<T> {

}
