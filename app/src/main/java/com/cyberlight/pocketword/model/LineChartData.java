package com.cyberlight.pocketword.model;

import java.time.LocalDate;

public class LineChartData {
    public int num;
    public LocalDate date;

    public LineChartData(int num, LocalDate date) {
        this.num = num;
        this.date = date;
    }
}
