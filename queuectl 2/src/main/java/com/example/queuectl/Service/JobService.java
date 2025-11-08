package com.example.queuectl.Service;

import com.example.queuectl.Configuration.Config;
import com.example.queuectl.Entity.Job;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class JobService {

    @PersistenceContext
    private EntityManager entityManager;

    Config config;

    public JobService(Config config){
        this.config=config;
    }


    public Job add(Job job) {
        job.setNextRunAt(LocalDateTime.now());
        job.setMaxRetries(config.getMaxRetries());
        entityManager.persist(job);
        return job;
    }

    public List<Job> get_All(){
        TypedQuery<Job> query = entityManager.createQuery("SELECT j FROM Job j", Job.class);
        List<Job> jobs=query.getResultList();
        return jobs;
    }

    @Transactional
    public Optional<Job> findNextJobToProcess() {
        try {
            TypedQuery<Job> query = entityManager.createQuery(
                    "SELECT j FROM Job j " +
                            "WHERE j.state = 'pending' AND j.nextRunAt <= :now " +
                            "ORDER BY j.createdAt ASC",
                    Job.class
            );
            query.setParameter("now", LocalDateTime.now());
            query.setMaxResults(1);
            query.setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE); // 🔒 Prevents race conditions

            List<Job> jobs = query.getResultList();
            if (jobs.isEmpty()) {
                return Optional.empty();
            }

            Job job = jobs.get(0);
            job.setState("processing");
            job.setUpdatedAt(LocalDateTime.now());
            entityManager.merge(job);

            return Optional.of(job);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<Job> findTheState(String state) {
        TypedQuery<Job> query=entityManager.createQuery("SELECT j FROM Job j WHERE j.state = :s1", Job.class);
        query.setParameter("s1",state);
        List<Job> result=query.getResultList();
        return result;
    }

    public void markJob(Job job) {
        job.setState("processing");
        entityManager.merge(job);
    }

    public void Finished(Job job) {
        job.setState("completed");
        entityManager.merge(job);
    }

    public void FailedProcess(Job job, String error) {
        job.setAttempts(job.getAttempts() + 1);
        job.setLastError(error);

        if (job.getAttempts() >= job.getMaxRetries()) {
            job.setState("dead");
            System.out.println("Job " + job.getId() + " moved to DEAD state after " + job.getAttempts() + " attempts");
        } else {
            job.setState("pending"); // Change back to pending for retry, NOT "failed"
            long delayInSeconds = (long) Math.pow(2, job.getAttempts());
            job.setNextRunAt(LocalDateTime.now().plusSeconds(delayInSeconds));
            System.out.println("Job " + job.getId() + " failed, will retry in " + delayInSeconds + " seconds. Attempt " + job.getAttempts() + "/" + job.getMaxRetries());
        }
        entityManager.merge(job);
    }

    public List<Job> getDLQJobs() {
        TypedQuery<Job> query = entityManager.createQuery(
                "SELECT j FROM Job j WHERE j.state = 'dead' ORDER BY j.updatedAt DESC",
                Job.class);
        return query.getResultList();
    }

    public boolean retryDLQJob(String jobId) {
        List<Job> list=getDLQJobs();
        for(int i=0;i<list.size();i++){
            if(list.get(i).getId().equals(jobId)){
                Job job=list.get(i);
                job.setState("pending");
                job.setAttempts(0);
                job.setLastError(null);
                job.setNextRunAt(LocalDateTime.now());
                entityManager.merge(job);
                return true;
            }
        }
        System.out.println("not found");
        return false;
    }

}
