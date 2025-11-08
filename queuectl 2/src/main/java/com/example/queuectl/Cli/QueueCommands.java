package com.example.queuectl.Cli;

import com.example.queuectl.Configuration.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.queuectl.Entity.Job;
import com.example.queuectl.Service.JobService;
import com.example.queuectl.Service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class QueueCommands {

    private final JobService jobService;
    private final WorkerService workerService;

    @Autowired
    private Config config;

    @ShellMethod(value = "Add a job: queuectl enqueue '{\"id\":\"job1\",\"command\":\"sleep 2\"}'", key = "queuectl enqueue")
    public void enqueue(@ShellOption String jsonInput) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonInput);

            String id = node.get("id").asText();
            String command =node.get("command").asText();

            Job job = new Job();
            job.setId(id);
            job.setCommand(command);
            job.setMaxRetries(config.getMaxRetries());
            jobService.add(job);
            System.out.println(id+" "+"added to queue successfully");
    }

    @ShellMethod(
            value = "Start worker: queuectl worker start <count>",
            key = "queuectl worker start"
    )
    public void start(@ShellOption(help = "Number of workers") int count) {
        workerService.start(count);
    }


    @ShellMethod(value = "Stop workers: queuectl worker stop", key = "queuectl worker stop")
    public void Stop() {
        workerService.stop();
    }

    @ShellMethod(value = "List jobs by state: queuectl list <state>", key = "queuectl list")
    public void listOfJobs(String state) {
        List<Job> jobs=jobService.findTheState(state);
        for(int i=0;i<jobs.size();i++){
            System.out.println(jobs.get(i));
        }
    }

    @ShellMethod(value = "List DLQ jobs: queuectl dlq list", key = "queuectl dlq list")
    public void listDLQJobs() {
        List<Job> dlqJobs = jobService.getDLQJobs();

        if (dlqJobs.isEmpty()) {
            System.out.println("No jobs in DLQ");
        } else {
           for(int i=0;i<dlqJobs.size();i++){
               System.out.println(dlqJobs.get(i));
           }
        }
    }

    @ShellMethod(value = "Retry a DLQ job: queuectl dlq retry <jobId>", key = "queuectl dlq retry")
    public void retryDLQJob(String jobId) {
        boolean success = jobService.retryDLQJob(jobId);
        if (success) {
            System.out.println("Job " + jobId + " moved from DLQ to pending state");
        } else {
            System.out.println("Failed to retry job " + jobId + ". Job not found in DLQ");
        }
    }

    @ShellMethod(key = "queuectl config set", value = "Set a configuration value in application.properties")
    public String set(String key,String value) {
        String fullKey = "queuectl." + key;
        config.setProperty(fullKey, value);
        return "Updated " + fullKey + " " + value;
    }

    @ShellMethod(key="queuectl status",value = "Show all jobs and their states")
    public void getAll(){
        List<Job> jobs=jobService.get_All();
        for(int i=0;i<jobs.size();i++){
            System.out.println(jobs.get(i));
        }
    }

}
