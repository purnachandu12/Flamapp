QueueCTL — Background Job Queue System

A **CLI-based background job management system** built with **Spring Boot (Java)** that manages job execution, retries, workers, and a Dead Letter Queue (DLQ).
Designed as part of a backend developer internship assignment.

---

## Overview

**QueueCTL** allows users to enqueue jobs, execute them asynchronously through worker processes, retry failed ones with exponential backoff, and persist job data across restarts.

It supports:

* Background job execution
* Multiple concurrent workers
* Automatic retries and exponential backoff
* Dead Letter Queue (DLQ)
* Persistent job storage
* CLI-based configuration and monitoring

---

##Tech Stack

* **Java 17**
* **Spring Boot**
* **H2 Database (persistent mode)**
* **Spring Shell CLI**
* **JPA / Hibernate**

---

## Core Features

- Enqueue background jobs  
- Multiple worker threads  
- Retry on failure (with exponential backoff)  
- Dead Letter Queue (DLQ)  
- Persistent storage  
- CLI interface for all operations  
- Configurable retry count and delay  
- Graceful worker shutdown


---

## Demonstration & Screenshots

Below are the test scenarios captured during execution — showing that all core functionalities work as expected.

---

### 1. Job Enqueued Successfully

Jobs were successfully added to the queue and listed under the **Pending** state.

🖼️
![Job Enqueued Successfully](https://github.com/purnachandu12/Flamapp/blob/main/Enqueue.png)

---

### 2. Worker Execution & Job Processing

A worker picked up the job and executed it successfully. The job moved to the **Completed** state.

🖼️
![Worker Executing Job](https://github.com/purnachandu12/Flamapp/blob/main/workers%20assigned.png)

---

### 3. Failed Job Retries and DLQ Movement

A failed job automatically retried multiple times with exponential backoff and eventually moved to the **Dead Letter Queue** after max attempts.

🖼️
![Job Failed and Moved to DLQ](https://github.com/purnachandu12/Flamapp/blob/main/failed%20and%20retry.png)

---

### 4. Dead Letter Queue Listing

The failed job was visible in the **DLQ List**, showing proper state and metadata.

🖼️
![DLQ Listing](https://github.com/purnachandu12/Flamapp/blob/main/dead%20list.png)

---

### 5. Retried Job from DLQ to Pending

The job in DLQ was successfully retried and moved back to the **Pending** state for reprocessing.

🖼️
![DLQ Job Retried to Pending](https://github.com/purnachandu12/Flamapp/blob/main/dead%20queue%20to%20pending.png)

---

### 6. Multiple Jobs Running Concurrently

Multiple jobs were processed simultaneously by different workers, demonstrating proper concurrency handling.

🖼️
![Multiple Jobs Processed](https://github.com/purnachandu12/Flamapp/blob/main/multiple%20jobs.png)

---

### 7. Config Management — Max Retries Updated

System configuration (e.g., `max-retries`) was successfully updated through CLI.

🖼️
![Config Updated](https://github.com/purnachandu12/Flamapp/blob/main/set%20max%20tries.png)

---

### 8. System Status Overview

The system status summary showed total jobs with counts of completed, failed, and pending states, along with active worker details.

🖼️
![Status Summary](https://github.com/purnachandu12/Flamapp/blob/main/status.png)

---

## Architecture Overview

* **CLI Layer:** Spring Shell handles user commands.
* **Service Layer:** Manages job queue operations (enqueue, process, retry).
* **Worker Threads:** Execute jobs concurrently with state tracking.
* **Persistence Layer:** H2 database for durable job storage.
* **DLQ Management:** Stores permanently failed jobs for inspection and reprocessing.

---

## Key Highlights

* Exponential retry mechanism
* Worker synchronization to avoid duplicate processing
* Persistent job data (survives restart)
* Clean and user-friendly CLI
* Easy configuration updates

---
