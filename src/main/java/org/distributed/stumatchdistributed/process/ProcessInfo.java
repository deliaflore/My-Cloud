package org.distributed.stumatchdistributed.process;

/**
 * Process information snapshot.
 */
public class ProcessInfo {
    private final long pid;
    private final String processName;
    private final ProcessState state;
    private final int priority;
    private final long createdTime;
    private final long startTime;
    private final long endTime;

    public ProcessInfo(long pid, String processName, ProcessState state, int priority,
                       long createdTime, long startTime, long endTime) {
        this.pid = pid;
        this.processName = processName;
        this.state = state;
        this.priority = priority;
        this.createdTime = createdTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public long getPid() { return pid; }
    public String getProcessName() { return processName; }
    public ProcessState getState() { return state; }
    public int getPriority() { return priority; }
    public long getCreatedTime() { return createdTime; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }

    public long getExecutionTime() {
        if (startTime == 0) return 0;
        if (endTime == 0) return System.currentTimeMillis() - startTime;
        return endTime - startTime;
    }
}
