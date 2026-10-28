package com.example.todoapp2;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false, defaultValue = "month") String view,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        if (!"week".equals(view)) {
            view = "month";
        }
        if ("week".equals(view)) {
            LocalDate currentDate = date == null ? LocalDate.now() : date;
            LocalDate weekStart = currentDate.minusDays(currentDate.getDayOfWeek().getValue() % 7);
            LocalDate weekEnd = weekStart.plusDays(6);
            model.addAttribute("view", view);
            model.addAttribute("weekStart", weekStart);
            model.addAttribute("weekEnd", weekEnd);
            model.addAttribute("previousWeek", weekStart.minusWeeks(1));
            model.addAttribute("nextWeek", weekStart.plusWeeks(1));
            model.addAttribute("from", weekStart);
            model.addAttribute("to", weekEnd);
            model.addAttribute("weeks", createWeek(weekStart));
            return "calendar";
        }
        YearMonth now = YearMonth.now();
        int displayYear = year == null ? now.getYear() : year;
        int displayMonth = month == null ? now.getMonthValue() : month;
        YearMonth currentMonth = YearMonth.of(displayYear, displayMonth);

        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("view", view);
        model.addAttribute("previousMonth", currentMonth.minusMonths(1));
        model.addAttribute("nextMonth", currentMonth.plusMonths(1));
        model.addAttribute("weeks", createWeeks(currentMonth));
        model.addAttribute("from", currentMonth.atDay(1));
        model.addAttribute("to", currentMonth.atEndOfMonth());
        return "calendar";
    }

    private List<List<LocalDate>> createWeek(LocalDate weekStart) {
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(weekStart.plusDays(i));
        }
        return List.of(days);
    }

    private List<List<LocalDate>> createWeeks(YearMonth yearMonth) {
        List<LocalDate> days = new ArrayList<>();
        int emptyCellsBefore = yearMonth.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < emptyCellsBefore; i++) {
            days.add(null);
        }
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            days.add(yearMonth.atDay(day));
        }
        while (days.size() % 7 != 0) {
            days.add(null);
        }

        List<List<LocalDate>> weeks = new ArrayList<>();
        for (int index = 0; index < days.size(); index += 7) {
            weeks.add(days.subList(index, index + 7));
        }
        return weeks;
    }
}
