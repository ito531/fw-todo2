package com.example.todoapp2.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.todoapp2.Todo;

import lombok.Data;

@Data
public class TodoDto {

    private Long id;
    private String title;
    private String detail;
    private String category;
    private Integer priority;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TodoDto from(Todo todo) {
        TodoDto dto = new TodoDto();
        dto.setId(todo.getId());
        dto.setTitle(todo.getTitle());
        dto.setDetail(todo.getDetail());
        dto.setCategory(todo.getCategory());
        dto.setPriority(todo.getPriority());
        dto.setDueDate(todo.getDueDate());
        dto.setDueTime(todo.getDueTime());
        dto.setCompleted(todo.getCompleted());
        dto.setCreatedAt(todo.getCreatedAt());
        dto.setUpdatedAt(todo.getUpdatedAt());
        return dto;
    }
}
