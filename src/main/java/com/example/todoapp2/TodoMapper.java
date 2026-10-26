package com.example.todoapp2;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword, @Param("category") String category, @Param("order") String order,
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    void insert(Todo todo);

    Todo findById(@Param("id") Long id);

    void update(Todo todo);

    void deleteById(@Param("id") Long id);
}
