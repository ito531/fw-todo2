package com.example.todoapp2.api;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.todoapp2.Todo;
import com.example.todoapp2.TodoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class TodoApiController {

    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos")
    public List<TodoDto> todos(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (!"desc".equals(order)) {
            order = "asc";
        }
        return todoService.search(keyword, category, order, from, to).stream()
                .map(TodoDto::from)
                .toList();
    }

    @GetMapping(value = "/api/todos.csv", produces = "text/csv")
    public ResponseEntity<byte[]> todosCsv(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "false") boolean includeCompleted,
            @RequestParam(required = false, defaultValue = "0") int trash) {
        if (!"desc".equals(order)) {
            order = "asc";
        }
        boolean showTrash = trash == 1;
        List<Todo> todos = todoService.search(keyword, category, order, showTrash || includeCompleted, showTrash);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("やること,ジャンル,優先度,期限,状態\r\n");
        for (Todo todo : todos) {
            csv.append(csvValue(todo.getTitle())).append(',')
                    .append(csvValue(todo.getCategory())).append(',')
                    .append(csvValue(priorityLabel(todo.getPriority()))).append(',')
                    .append(csvValue(todo.getDueDate() == null ? "" : todo.getDueDate().toString())).append(',')
                    .append(csvValue(Boolean.TRUE.equals(todo.getCompleted()) ? "完了" : "未完了"))
                    .append("\r\n");
        }
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todos.csv\"")
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/api/todos/{id}")
    public ResponseEntity<?> todo(@PathVariable Long id, HttpServletRequest request) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound(request));
        }
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @PostMapping("/api/todos")
    public ResponseEntity<TodoDto> create(@Valid @RequestBody TodoRequest todoRequest) {
        Todo todo = toTodo(todoRequest);
        todoService.create(todo);
        Todo created = todoService.findById(todo.getId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(TodoDto.from(created));
    }

    @PutMapping("/api/todos/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TodoRequest todoRequest,
            HttpServletRequest request) {
        Todo existing = todoService.findById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound(request));
        }
        Todo todo = toTodo(todoRequest);
        todo.setId(id);
        todoService.update(todo);
        Todo updated = todoService.findById(id);
        return ResponseEntity.ok(TodoDto.from(updated));
    }

    @DeleteMapping("/api/todos/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound(request));
        }
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ProblemDetail notFound(HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "\u6307\u5b9a\u3055\u308c\u305fToDo\u306f\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationError(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::error)
                .toList();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "入力に誤りがあります");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    private Map<String, String> error(FieldError fieldError) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("field", fieldError.getField());
        error.put("message", fieldError.getDefaultMessage());
        return error;
    }

    private String priorityLabel(Integer priority) {
        if (priority == 1) {
            return "高";
        }
        if (priority == 2) {
            return "中";
        }
        return "低";
    }

    private String csvValue(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.startsWith("=") || safeValue.startsWith("+") || safeValue.startsWith("-") || safeValue.startsWith("@")) {
            safeValue = "'" + safeValue;
        }
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private Todo toTodo(TodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDetail(request.getDetail());
        todo.setCategory(request.getCategory());
        todo.setPriority(request.getPriority());
        todo.setDueDate(request.getDueDate());
        todo.setDueTime(request.getDueTime());
        todo.setCompleted(request.getCompleted());
        return todo;
    }
}
