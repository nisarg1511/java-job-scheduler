#Failure Modes

1. What if **the process crashes while writing to disk**?
   > |If the process crashes while executing, all in-memory state is lost. On restart, the system relies on persisted logs or metadata to determine the last consistent state. Tasks that were not durably marked as completed may be re-executed safely. The system does not attempt to resume execution mid-task, but instead recovers at well-defined boundaries.
2. What if **a task is executed twice**?
   > |The scheduler does not attempt to prevent duplicate task execution. Instead, tasks are designed to be idempotent, ensuring that re-execution produces the same final state. This avoids complex coordination while remaining robust to crashes and retries.
3. What if **the scheduler crashes after assigning a task but before recording it**?
   > |If the system crashes during a disk write, any partially written state must be treated as invalid. The system must assume the write did not succeed. To handle this safely, writes should be designed to be atomic, so that after a crash the system sees either the old state or the fully written new state. Tasks whose completion was not durably recorded are treated as failed and may be safely re-executed.
4. What if **the scheduler crashes after starting a task, but before persisting that the task is RUNNING**?
   > |The scheduler and task execution are co-located within the same process. A scheduler crash terminates all in-flight tasks. Therefore, if a crash occurs before task state is durably recorded, the task is treated as not executed and may be safely retried on restart.
5. What if **the task completes successfully, but the scheduler crashes before persisting COMPLETED**?
   > |A task is considered completed only when its effects have been durably applied. If a task completes execution but the scheduler crashes before persisting the COMPLETED state, the task will be retried on restart. Therefore, tasks must be designed with idempotent effects so that re-execution does not violate system correctness.

#How does the scheduler handles and addresses different kind of failures:
| Scenario | Behavior |
| ---------------------- | ----------------------- |
| Scheduler crash | All tasks stop |
| Crash before RUNNING | Task retried |
| Crash during execution | Task retried |
| Crash before COMPLETED | Task retried |
| Duplicate execution | Safe due to idempotency |
| Partial disk write | Treated as failure |
