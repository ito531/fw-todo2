package com.example.todoapp2;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private static final int PAGE_SIZE = 10;

    private final TodoService todoService;

    public HomeController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "やること管理");
        return "index";
    }

    @GetMapping("/todos")
    public String todos(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "false") boolean includeCompleted,
            @RequestParam(required = false, defaultValue = "0") int trash,
            @RequestParam(required = false, defaultValue = "1") int page, Model model) {
        if (!"desc".equals(order)) {
            order = "asc";
        }
        if (page < 1) {
            page = 1;
        }
        boolean showTrash = trash == 1;
        boolean effectiveIncludeCompleted = showTrash || includeCompleted;
        int totalPages = Math.max(1, (int) Math.ceil((double) todoService.count(keyword, category, effectiveIncludeCompleted, showTrash) / PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }
        List<Todo> todos = todoService.searchPage(keyword, category, order, effectiveIncludeCompleted, showTrash, PAGE_SIZE, page);
        model.addAttribute("todos", todos);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("order", order);
        model.addAttribute("includeCompleted", effectiveIncludeCompleted);
        model.addAttribute("trash", showTrash);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        return "todos";
    }

    @GetMapping("/todos/new")
    public String create(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirm(@Valid @ModelAttribute("todo") Todo todo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "create";
        }
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String rewrite(@ModelAttribute("todo") Todo todo) {
        return "create";
    }

    @PostMapping("/todos")
    public String insert(@ModelAttribute("todo") Todo todo, RedirectAttributes redirectAttributes) {
        todoService.create(todo);
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String editConfirm(@PathVariable("id") Long id, @Valid @ModelAttribute("todo") Todo todo, BindingResult bindingResult, Model model) {
        model.addAttribute("id", id);
        if (bindingResult.hasErrors()) {
            return "edit";
        }
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}/edit")
    public String editRewrite(@PathVariable("id") Long id, @ModelAttribute("todo") Todo todo, Model model) {
        model.addAttribute("id", id);
        return "edit";
    }

    @PostMapping("/todos/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute("todo") Todo todo, RedirectAttributes redirectAttributes) {
        todo.setId(id);
        todoService.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/delete")
    public String deleteConfirm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "delete";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "削除しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/restore")
    public String restore(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findDeletedById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos?trash=1";
        }
        todoService.restore(id);
        redirectAttributes.addFlashAttribute("message", "戻しました");
        return "redirect:/todos?trash=1";
    }
}
