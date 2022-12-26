package com.wafflytime.config

import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskDecorator
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor


@Configuration
@EnableAsync
class AsyncConfig {

    private val poolSize: Int = 4

    @Bean
    fun mailExecutor() : ThreadPoolTaskExecutor {
        val taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.setThreadNamePrefix("MailTask-")
        taskExecutor.corePoolSize = poolSize
        taskExecutor.maxPoolSize = poolSize * 2
        taskExecutor.queueCapacity = poolSize * 5
        taskExecutor.setTaskDecorator(LoggingTaskDecorator())
        taskExecutor.setRejectedExecutionHandler(AsyncRejectedExecutionHandler())
        return taskExecutor
    }
}

class LoggingTaskDecorator : TaskDecorator {

    override fun decorate(task: Runnable): Runnable {
        val callerThreadContext = MDC.getCopyOfContextMap()

        return Runnable {
            callerThreadContext?.let {
                MDC.setContextMap(it)
            }
            task.run()
        }
    }
}

class AsyncRejectedExecutionHandler : RejectedExecutionHandler {

    override fun rejectedExecution(r: Runnable?, executor: ThreadPoolExecutor?) {
        // 가용 쓰레드가 없을 경우 처리 로직 작성
        // 아무 것도 작성하지 않으면 TaskRejectedException 예외를 무시
    }
}