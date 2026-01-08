package org.distributed.stumatchdistributed.process;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;
import java.util.*;

/**
 * Manages processes (tasks) on a storage node.
 *
 * Simulates OS process scheduler with states:
 * - NEW: Process created
 * - READY: Ready to execute
 * - RUNNING: Currently executing
 * - WAITING: Waiting for I/O or resource
 * - TERMINATED: Completed or killed
 *
 * @author Your Name
 * @version 1.0
 */
public class ProcessManager {
    private static final Logger log = LoggerFactory.getLogger(ProcessManager.class);

    private final String nodeId;
    private final Map<Long, ManagedProcess> processes;
    private final BlockingQueue<ManagedProcess> readyQueue;
    private final ExecutorService processExecutor;
    private final ScheduledExecutorService scheduler;

    private volatile boolean running;

    public ProcessManager(String nodeId, int maxThreads) {
        this.nodeId = nodeId;
        this.processes = new ConcurrentHashMap<>();
        this.readyQueue = new LinkedBlockingQueue<>();
        this.processExecutor = Executors.newFixedThreadPool(maxThreads);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.running = false;

        log.info("🔧 Process Manager initialized for: {}", nodeId);
        log.info("   Max concurrent processes: {}", maxThreads);
    }

    /**
     * Starts the process scheduler.
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;

        // Start scheduler thread
        scheduler.scheduleAtFixedRate(this::scheduleProcesses,
                0, 100, TimeUnit.MILLISECONDS);

        log.info("▶️  Process scheduler started for: {}", nodeId);
    }

    /**
     * Submits a new process for execution.
     */
    public long submitProcess(String processName, Runnable task, int priority) {
        ManagedProcess process = new ManagedProcess(
                generateProcessId(),
                processName,
                task,
                priority
        );

        processes.put(process.getPid(), process);

        // Add to ready queue
        process.transitionTo(ProcessState.READY);
        readyQueue.offer(process);

        log.info("➕ Process submitted: {} (PID: {})", processName, process.getPid());

        return process.getPid();
    }

    /**
     * Scheduler: Picks processes from ready queue and executes them.
     */
    private void scheduleProcesses() {
        while (!readyQueue.isEmpty() && running) {
            ManagedProcess process = readyQueue.poll();

            if (process == null) {
                break;
            }

            // Transition to RUNNING
            process.transitionTo(ProcessState.RUNNING);

            // Execute
            processExecutor.submit(() -> executeProcess(process));
        }
    }

    /**
     * Executes a process.
     */
    private void executeProcess(ManagedProcess process) {
        log.info("▶️  Executing process: {} (PID: {})",
                process.getProcessName(), process.getPid());

        try {
            // Run the task
            process.getTask().run();

            // Mark as terminated
            process.transitionTo(ProcessState.TERMINATED);

            log.info("✅ Process completed: {} (PID: {})",
                    process.getProcessName(), process.getPid());

        } catch (Exception e) {
            log.error("❌ Process failed: {} (PID: {})",
                    process.getProcessName(), process.getPid(), e);
            process.transitionTo(ProcessState.TERMINATED);
        }
    }

    /**
     * Puts a process in WAITING state (simulates I/O wait).
     */
    public boolean waitProcess(long pid) {
        ManagedProcess process = processes.get(pid);
        if (process != null) {
            process.transitionTo(ProcessState.WAITING);
            return true;
        }
        return false;
    }

    /**
     * Resumes a WAITING process (returns to READY).
     */
    public boolean resumeProcess(long pid) {
        ManagedProcess process = processes.get(pid);
        if (process != null && process.getState() == ProcessState.WAITING) {
            process.transitionTo(ProcessState.READY);
            readyQueue.offer(process);
            return true;
        }
        return false;
    }

    /**
     * Kills a process.
     */
    public boolean killProcess(long pid) {
        ManagedProcess process = processes.remove(pid);
        if (process != null) {
            process.transitionTo(ProcessState.TERMINATED);
            log.warn("💀 Process killed: {} (PID: {})", process.getProcessName(), pid);
            return true;
        }
        return false;
    }

    /**
     * Gets process information.
     */
    public ProcessInfo getProcessInfo(long pid) {
        ManagedProcess process = processes.get(pid);
        return process != null ? process.getInfo() : null;
    }

    /**
     * Lists all processes.
     */
    public List<ProcessInfo> listProcesses() {
        List<ProcessInfo> infos = new ArrayList<>();
        for (ManagedProcess process : processes.values()) {
            infos.add(process.getInfo());
        }
        return infos;
    }

    /**
     * Stops the process manager.
     */
    public void shutdown() {
        running = false;
        scheduler.shutdown();
        processExecutor.shutdown();

        try {
            if (!processExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                processExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processExecutor.shutdownNow();
        }

        log.info("🛑 Process Manager stopped for: {}", nodeId);
    }

    private long generateProcessId() {
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);
    }
}

/**
 * Process states (like OS processes).
 */
enum ProcessState {
    NEW,
    READY,
    RUNNING,
    WAITING,
    TERMINATED
}

/**
 * Represents a managed process.
 */
class ManagedProcess {
    private final long pid;
    private final String processName;
    private final Runnable task;
    private final int priority;
    private volatile ProcessState state;
    private final long createdTime;
    private long startTime;
    private long endTime;

    public ManagedProcess(long pid, String processName, Runnable task, int priority) {
        this.pid = pid;
        this.processName = processName;
        this.task = task;
        this.priority = priority;
        this.state = ProcessState.NEW;
        this.createdTime = System.currentTimeMillis();
    }

    public void transitionTo(ProcessState newState) {
        ProcessState oldState = this.state;
        this.state = newState;

        if (newState == ProcessState.RUNNING) {
            startTime = System.currentTimeMillis();
        } else if (newState == ProcessState.TERMINATED) {
            endTime = System.currentTimeMillis();
        }
    }

    public ProcessInfo getInfo() {
        return new ProcessInfo(
                pid,
                processName,
                state,
                priority,
                createdTime,
                startTime,
                endTime
        );
    }

    // Getters
    public long getPid() { return pid; }
    public String getProcessName() { return processName; }
    public Runnable getTask() { return task; }
    public int getPriority() { return priority; }
    public ProcessState getState() { return state; }
}

