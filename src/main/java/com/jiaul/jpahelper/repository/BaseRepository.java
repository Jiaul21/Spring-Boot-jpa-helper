package com.jiaul.jpahelper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<E, ID> extends JpaRepository<E, ID> {

    @Query("select e from #{#entityName} e where id(e) = :id")
    <T> Optional<T> findByID(@Param("id") ID id, Class<T> type);

    @Query("select e from #{#entityName} e where id(e) in :ids")
    <T> List<T> findByIDsIn(@Param("ids") Collection<ID> ids, Class<T> tClass);

    @Query("select id(e) from #{#entityName} e where id(e) in :ids")
    List<ID> findExistingIDs(@Param("ids") Collection<ID> ids);

}
