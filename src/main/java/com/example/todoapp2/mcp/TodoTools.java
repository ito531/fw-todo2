package com.example.todoapp2.mcp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.todoapp2.Todo;
import com.example.todoapp2.TodoService;
import com.example.todoapp2.api.HolidayClient;
import com.example.todoapp2.api.TodoDto;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class TodoTools {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "デザイン", "マーケティング", "プログラミング", "資格", "就職活動");

    private final TodoService todoService;
    private final HolidayClient holidayClient;

    public TodoTools(TodoService todoService, HolidayClient holidayClient) {
        this.todoService = todoService;
        this.holidayClient = holidayClient;
    }

    @McpTool(name = "list_todos", description = "やることの一覧を返す（期間・ジャンルで絞れる）")
    public List<TodoDto> listTodos(
            @McpToolParam(description = "キーワード", required = false) String keyword,
            @McpToolParam(description = "ジャンル", required = false) String category,
            @McpToolParam(description = "開始日（yyyy-MM-dd）", required = false) String from,
            @McpToolParam(description = "終了日（yyyy-MM-dd）", required = false) String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);
        return todoService.search(keyword, category, "asc", fromDate, toDate).stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "get_todo", description = "やることを1件返す")
    public TodoDto getTodo(@McpToolParam(description = "やることのID", required = true) Long id) {
        return TodoDto.from(todoService.findById(id));
    }

    @McpTool(name = "add_todo", description = "やることを1件足す")
    public TodoDto addTodo(
            @McpToolParam(description = "やることの題名", required = true) String title,
            @McpToolParam(description = "メモ", required = false) String detail,
            @McpToolParam(description = "ジャンル", required = true) String category,
            @McpToolParam(description = "優先度", required = true) Integer priority,
            @McpToolParam(description = "期限（yyyy-MM-dd）", required = false) String dueDate) {
        validateCategory(category);

        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(dueDate == null ? null : LocalDate.parse(dueDate));
        todo.setCompleted(false);
        todoService.create(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "update_todo", description = "やることを1件直す（期限を変えるのもこれ）")
    public TodoDto updateTodo(
            @McpToolParam(description = "やることのID", required = true) Long id,
            @McpToolParam(description = "やることの題名", required = false) String title,
            @McpToolParam(description = "メモ", required = false) String detail,
            @McpToolParam(description = "ジャンル", required = false) String category,
            @McpToolParam(description = "優先度", required = false) Integer priority,
            @McpToolParam(description = "期限（yyyy-MM-dd）", required = false) String dueDate) {
        Todo todo = todoService.findById(id);

        if (title != null) {
            todo.setTitle(title);
        }
        if (detail != null) {
            todo.setDetail(detail);
        }
        if (category != null) {
            validateCategory(category);
            todo.setCategory(category);
        }
        if (priority != null) {
            todo.setPriority(priority);
        }
        if (dueDate != null) {
            todo.setDueDate(LocalDate.parse(dueDate));
        }

        todoService.update(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "complete_todo", description = "やることを完了にする")
    public TodoDto completeTodo(@McpToolParam(description = "やることのID", required = true) Long id) {
        Todo todo = todoService.findById(id);
        todo.setCompleted(true);
        todoService.update(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "delete_todo", description = "やることを1件消す")
    public void deleteTodo(@McpToolParam(description = "やることのID", required = true) Long id) {
        todoService.delete(id);
    }

    @McpTool(name = "summarize_week", description = "期間内のやることを件数・ジャンル別内訳・未完了の期限切れ件数として要約して返す")
    public TodoSummary summarizeWeek(
            @McpToolParam(description = "開始日（yyyy-MM-dd）", required = false) String from,
            @McpToolParam(description = "終了日（yyyy-MM-dd）", required = false) String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);
        List<Todo> todos = todoService.search(null, null, "asc", fromDate, toDate);
        Map<String, Long> categoryCounts = todos.stream()
                .collect(Collectors.groupingBy(Todo::getCategory, LinkedHashMap::new, Collectors.counting()));
        long overdueCount = todos.stream()
                .filter(todo -> todo.getDueDate() != null)
                .filter(todo -> todo.getDueDate().isBefore(LocalDate.now()))
                .filter(todo -> !Boolean.TRUE.equals(todo.getCompleted()))
                .count();
        return new TodoSummary(todos.size(), categoryCounts, overdueCount);
    }

    @McpTool(name = "find_free_days", description = "期間の中で、期限のやることが無く、土日でも祝日でもない「空いている日」を返す。やることの期限を動かす先を決めるのに使う")
    public List<String> findFreeDays(
            @McpToolParam(description = "開始日（yyyy-MM-dd）", required = true) String from,
            @McpToolParam(description = "終了日（yyyy-MM-dd）", required = true) String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        Set<LocalDate> busyDays = todoService.search(null, null, "asc", fromDate, toDate).stream()
                .map(Todo::getDueDate)
                .filter(date -> date != null)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> holidays = holidayClient.getHolidays(fromDate, toDate).holidays().keySet();

        return fromDate.datesUntil(toDate.plusDays(1))
                .filter(date -> !busyDays.contains(date))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .filter(date -> !holidays.contains(date.toString()))
                .map(LocalDate::toString)
                .toList();
    }

    private void validateCategory(String category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("ジャンルは、デザイン・マーケティング・プログラミング・資格・就職活動のいずれかにしてください。");
        }
    }

    public record TodoSummary(long totalCount, Map<String, Long> categoryCounts, long overdueCount) {
    }
}
