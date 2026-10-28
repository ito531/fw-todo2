package com.example.todoapp2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword, @Param("category") String category, @Param("order") String order,
            @Param("from") LocalDate from, @Param("to") LocalDate to,
            @Param("includeCompleted") boolean includeCompleted,
            @Param("trash") boolean trash,
            @Param("limit") Integer limit, @Param("offset") Integer offset);

    int count(@Param("keyword") String keyword, @Param("category") String category,
            @Param("includeCompleted") boolean includeCompleted, @Param("trash") boolean trash);

    void insert(Todo todo);

    Todo findById(@Param("id") Long id);

    Todo findDeletedById(@Param("id") Long id);

    void update(Todo todo);

    void softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    void restore(@Param("id") Long id);
}
