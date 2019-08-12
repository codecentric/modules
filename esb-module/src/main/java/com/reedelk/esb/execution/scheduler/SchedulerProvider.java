package com.reedelk.esb.execution.scheduler;

import reactor.core.scheduler.Scheduler;

public class SchedulerProvider {

    public static Scheduler flow() {
        return FlowScheduler.INSTANCE;
    }

    public static Scheduler fork(int threads) {
        return ForkSchedulerProvider.get(threads);
    }
}
