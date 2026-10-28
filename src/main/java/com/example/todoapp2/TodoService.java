package com.example.todoapp2;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order) {
        return search(keyword, category, order, null, null);
    }

    public List<Todo> search(String keyword, String category, String order, boolean includeCompleted) {
        return search(keyword, category, order, includeCompleted, false);
    }

    public List<Todo> search(String keyword, String category, String order, boolean includeCompleted, boolean trash) {
        return todoMapper.search(keyword, category, order, null, null, includeCompleted, trash, null, null);
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to) {
        return todoMapper.search(keyword, category, order, from, to, true, false, null, null);
    }

    public List<Todo> searchPage(String keyword, String category, String order, boolean includeCompleted, boolean trash, int pageSize, int page) {
        return todoMapper.search(keyword, category, order, null, null, includeCompleted, trash, pageSize, (page - 1) * pageSize);
    }

    public int count(String keyword, String category, boolean includeCompleted, boolean trash) {
        return todoMapper.count(keyword, category, includeCompleted, trash);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public Todo findDeletedById(Long id) {
        return todoMapper.findDeletedById(id);
    }

    public void create(Todo todo) {
        todoMapper.insert(todo);
    }

    public void update(Todo todo) {
        todoMapper.update(todo);
    }

    public void delete(Long id) {
        todoMapper.softDelete(id, LocalDateTime.now());
    }

    public void restore(Long id) {
        todoMapper.restore(id);
    }
}
