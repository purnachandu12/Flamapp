package com.example.queuectl.Service;

import com.example.queuectl.Entity.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final JobService jobService;
    private  ExecutorService executor;
    private volatile boolean running = false;

    public  synchronized void start(int count) {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newFixedThreadPool(count);
        }
        running = true;
        for (int i = 0; i < count; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Loop();
                }
            });
        }
        System.out.println("Started " + count + " workers.");
    }

    public void stop() {
        running = false;
        executor.shutdown();
        System.out.println("Workers stopped");
    }

    private void Loop() {
        while (running) {
            Optional<Job> jobOpt = jobService.findNextJobToProcess();
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                jobService.markJob(job);
                execute(job);
            }
            try {
                Thread.sleep(1000);
            }
            catch (Exception ignored) {}
        }
    }

    private void execute(Job job) {
        try {
            Process process = new ProcessBuilder("bash", "-c", job.getCommand())
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            if (exitCode == 0){
                System.out.println("success");
                jobService.Finished(job);
            }
            else {
                System.out.println("failed");
                jobService.FailedProcess(job, "Exit code: " + exitCode);
            }
        }
        catch (Exception e) {
            jobService.FailedProcess(job, e.getMessage());
        }
    }
}
