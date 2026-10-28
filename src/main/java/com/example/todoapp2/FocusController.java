package com.example.todoapp2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 今日取り組むタスクを、期限と優先度から表示する画面。 */
@Controller
public class FocusController {

    private static final int FOCUS_LIMIT = 5;

    private final TodoService todoService;

    public FocusController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/focus")
    public String focus(Model model) {
        LocalDate today = LocalDate.now();
        List<Todo> todos = todoService.search(null, null, "asc", true);

        int todayTotal = 0;
        int todayCompleted = 0;
        int overdueCount = 0;
        List<Todo> incomplete = new ArrayList<>();
        for (Todo todo : todos) {
            if (today.equals(todo.getDueDate())) {
                todayTotal++;
                if (Boolean.TRUE.equals(todo.getCompleted())) {
                    todayCompleted++;
                }
            }
            if (!Boolean.TRUE.equals(todo.getCompleted())) {
                incomplete.add(todo);
                if (todo.getDueDate() != null && todo.getDueDate().isBefore(today)) {
                    overdueCount++;
                }
            }
        }

        incomplete.sort(Comparator
                .comparing((Todo todo) -> focusGroup(todo, today))
                .thenComparing(Todo::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Todo::getPriority, Comparator.nullsLast(Comparator.naturalOrder())));

        model.addAttribute("today", today);
        model.addAttribute("todayTotal", todayTotal);
        model.addAttribute("todayCompleted", todayCompleted);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("focusTodos", incomplete.stream().limit(FOCUS_LIMIT).toList());
        return "focus";
    }

    @PostMapping("/focus/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "タスクが見つかりませんでした");
            return "redirect:/focus";
        }
        todo.setCompleted(true);
        todoService.update(todo);
        redirectAttributes.addFlashAttribute("message", "完了にしました。おつかれさまです！");
        return "redirect:/focus";
    }

    private int focusGroup(Todo todo, LocalDate today) {
        if (todo.getDueDate() == null) {
            return 2;
        }
        return todo.getDueDate().isAfter(today) ? 1 : 0;
    }
}
