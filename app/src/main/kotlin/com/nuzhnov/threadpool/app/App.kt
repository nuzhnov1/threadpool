package com.nuzhnov.threadpool.app

import com.nuzhnov.threadpool.threadpool.ThreadPool
import kotlin.math.sqrt


class CustomTask : Runnable {
    override fun run() {
        sqrt(1234567890.0)
    }
}


fun testThreadPool(taskCount: Int, threadCount: Int) {
    val before = System.currentTimeMillis()

    // Создаём пул потоков из 'threadCount' потоков и запускаем его на 'taskCount' задачах
    val pool = ThreadPool(threadCount)
    repeat(taskCount) { pool.enqueueTask(CustomTask()) }
    pool.awaitExecutionAllTasks()
    pool.shutdown()

    val after = System.currentTimeMillis()
    val duration = (after - before) / 1000.0

    println("Время выполнения $taskCount задач в $threadCount потоках с использованием пула потоков: " +
            "$duration секунд."
    )
}

fun testManualCreatedThreads(taskCount: Int) {
    val before = System.currentTimeMillis()

    // Создаём для каждой задачи свой поток, запускаем их и ждём, пока они все не завершаться
    val threads = List(taskCount) { Thread(CustomTask()).also { it.start() } }
    threads.forEach { it.join() }

    val after = System.currentTimeMillis()
    val duration = (after - before) / 1000.0

    println("Время выполнения $taskCount задач, для каждой из которых создавался свой поток: " +
            "$duration секунд."
    )
}

fun main() {
    println("-".repeat(100))
    println("Сравнительный тест №1.")
    println("Число задач - 100, число потоков - 10.")
    println()
    testThreadPool(100, 10)
    testManualCreatedThreads(100)
    println("-".repeat(100))

    println("-".repeat(100))
    println("Сравнительный тест №2.")
    println("Число задач - 1000, число потоков - 50.")
    println()
    testThreadPool(1000, 50)
    testManualCreatedThreads(1000)
    println("-".repeat(100))

    println("-".repeat(100))
    println("Сравнительный тест №3.")
    println("Число задач - 10000, число потоков - 300.")
    println()
    testThreadPool(10000, 300)
    testManualCreatedThreads(10000)
    println("-".repeat(100))
}
