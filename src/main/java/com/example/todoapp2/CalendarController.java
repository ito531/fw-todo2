package com.example.todoapp2;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month, Model model) {
        YearMonth now = YearMonth.now();
        int displayYear = year == null ? now.getYear() : year;
        int displayMonth = month == null ? now.getMonthValue() : month;
        YearMonth currentMonth = YearMonth.of(displayYear, displayMonth);

        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("previousMonth", currentMonth.minusMonths(1));
        model.addAttribute("nextMonth", currentMonth.plusMonths(1));
        model.addAttribute("weeks", createWeeks(currentMonth));
        return "calendar";
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
