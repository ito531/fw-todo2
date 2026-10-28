package com.example.todoapp.mcp;

import java.time.LocalDate;
import java.util.List;

import com.example.todoapp2.TodoService;
import com.example.todoapp2.api.TodoDto;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class TodoTools {

    private final TodoService todoService;

    public TodoTools(TodoService todoService) {
        this.todoService = todoService;
    }

    @McpTool(name = "list_todos", description = "\u3084\u308b\u3053\u3068\u306e\u4e00\u89a7\u3092\u8fd4\u3059\uff08\u671f\u9593\u30fb\u30b8\u30e3\u30f3\u30eb\u3067\u7d5e\u308c\u308b\uff09")
    public List<TodoDto> listTodos(
            @McpToolParam(description = "keyword", required = false) String keyword,
            @McpToolParam(description = "category", required = false) String category,
            @McpToolParam(description = "from date (yyyy-MM-dd)", required = false) String from,
            @McpToolParam(description = "to date (yyyy-MM-dd)", required = false) String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);
        return todoService.search(keyword, category, "asc", fromDate, toDate).stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "get_todo", description = "\u3084\u308b\u3053\u3068\u30921\u4ef6\u8fd4\u3059")
    public TodoDto getTodo(
            @McpToolParam(description = "todo ID", required = true) Long id) {
        return TodoDto.from(todoService.findById(id));
    }
}
