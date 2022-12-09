package com.nuzhnov.threadpool.threadpool

import kotlin.collections.ArrayDeque

/**
 * Класс пула потоков, он же менеджер потоков.
 * Представляет собой хранилище потоков и задач, которые потоки выполняют.
 * По мере поступления задач, пул потоков даёт эти задачи потокам.
 * Если все потоки заняты, то задача ожидает своего выполнения в очереди.
 *
 * @param size размер пула потоков
 */
class ThreadPool(size: Int = DEFAULT_POOL_SIZE) {

    private val threads = List(size) { ThreadItem(this).also { it.start() } }
    private val queue = ArrayDeque<Runnable>()


    /**
     * Помещает задачу в очередь или же отдаёт на выполнение потоку, если имеется
     * свободный таковой.
     *
     * @param task код задачи
     */
    fun enqueueTask(task: Runnable) = threads.find { thread -> !thread.isRunning }
        ?.doWork(task)
        ?: queue.addLast(task)

    /**
     * Очищает очередь задач. Однако при этом уже запущенные задачи
     * продолжают своё выполнение!
     */
    fun clearTasks() = queue.clear()

    /**
     * Завершает работу всех потоков.
     * Обязательное действие перед уничтожением объекта [ThreadPool]!
     */
    fun shutdown() = threads
        .forEach { thread -> thread.interrupt() }
        .also { while (threads.all { it.isAlive }) { Thread.sleep(10) } }

    /**
     * Ожидаем, пока все текущие потоки не завершат все свои задачи.
     */
    fun awaitExecutionAllTasks() {
        while (threads.all { it.isRunning } && queue.isNotEmpty()) {
            Thread.sleep(AWAIT_DELTA_TIME)
        }
    }

    /**
     * Вызывается потоком.
     * Поток с помощью этого метода уведомляет данный менеджер, что он завершил
     * свою задачу и готов к исполнению новой.
     *
     * @param currentThread поток, который вызывает данный метод
     */
    internal fun notify(currentThread: ThreadItem) = queue.removeFirstOrNull()?.let { currentThread.doWork(it) }

    companion object {
        const val DEFAULT_POOL_SIZE = 16
        const val AWAIT_DELTA_TIME = 10L
    }
}
