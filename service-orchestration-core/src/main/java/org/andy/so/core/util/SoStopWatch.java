package org.andy.so.core.util;

import org.springframework.util.StopWatch;

import java.text.NumberFormat;

/**
 * @author: andy
 */
public class SoStopWatch extends StopWatch {
    /**
     * 指定 ID
     *
     * @param id string
     */
    public SoStopWatch(String id) {
        super(id);
    }

    /**
     * @return 毫秒打印输出
     */
    public String prettyPrintMillis() {
        String summary = "StopWatch '" + getId() + "': 执行时间 = " + getTotalTimeMillis() + " ms";
        StringBuilder sb = new StringBuilder(summary);
        sb.append('\n');

        sb.append("---------------------------------------------\n");
        sb.append("ms         %     Task name\n");
        sb.append("---------------------------------------------\n");
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumIntegerDigits(9);
        nf.setGroupingUsed(false);
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMinimumIntegerDigits(3);
        pf.setGroupingUsed(false);
        for (TaskInfo task : getTaskInfo()) {
            sb.append(nf.format(task.getTimeMillis())).append("  ");
            sb.append(pf.format((double) task.getTimeMillis() / getTotalTimeMillis())).append("  ");
            sb.append(task.getTaskName()).append('\n');
        }

        return sb.toString();
    }
}
