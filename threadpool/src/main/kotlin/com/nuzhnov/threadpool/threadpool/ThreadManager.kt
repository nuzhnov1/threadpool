package com.nuzhnov.threadpool.threadpool

import kotlin.collections.ArrayDeque
import kotlin.math.min

/**
 * Класс менеджера потоков.
 * Представляет собой хранилище потоков и задач, которые потоки выполняют,
 * а также набор методов для управления этим хранилищем.
 *
 * @param size исходный размер пула потоков.
 */
class ThreadManager(size: Int = DEFAULT_POOL_SIZE) {

    private val _threadList = MutableList(size) { ThreadItem(this).also { it.start() } }
    private val _tasksQueue = ArrayDeque<Task>()

    /** Список потоков. */
    val threadList: List<ThreadItem> = _threadList
    /** Список задач. */
    val tasksQueue: List<Task> = _tasksQueue


    /**
     * Помещает задачу [task] в очередь или же отдаёт её на выполнение свободному потоку, если
     * такой есть. Если задача отложенная, то просто помещаем её в стек.
     *
     * @param task задача.
     */
    fun enqueueTask(task: Task) = synchronized(_tasksQueue) {
        _tasksQueue.addLast(task)
    }

    /** Удаляет первую задачу из очереди.
     *  При этом если эта задача на данный момент выполняется потоком, то она будет выполнена.
     */
    fun dequeueTask(): Task? = synchronized(_tasksQueue) {
        _tasksQueue.removeFirstOrNull()
    }

    /** Удаляет все задачи из очереди.
     *  Задачи, выполняющиеся на данный момент потоками будут выполнены.
     */
    fun clearQueue() = synchronized(_tasksQueue) {
        _tasksQueue.clear()
    }

    /**
     * Добавить [count] новых потоков в пул.
     *
     * @param count количество добавляемых потоков.
     */
    fun addNewThreads(count: Int = 1) = repeat(count) {
        _threadList.add(ThreadItem(this).also { it.start() })
    }

    /**
     * Удалить [count] потоков из пула.
     *
     * @param count количество удаляемых потоков
     */
    fun removeThreads(count: Int = 1) = repeat(min(count, _threadList.size)) {
        _threadList.removeLast().interrupt()
    }

    /** Завершает работу всех потоков и удаляет их.
     *  Обязательное действие перед уничтожением менеджера потоков.
     */
    fun clearThreads() = _threadList.forEach { thread -> thread.interrupt() }.also { _threadList.clear() }

    /** Запускает все потоки. */
    fun runAll() = _threadList.forEach { thread -> thread.isRunning = true }

    /** Ожидает, пока все работающие потоки не завершат все задачи.
     *  Если работающих потоков нет, то возврат произойдёт сразу.
     */
    fun awaitExecutionAllTasks() {
        while (_threadList.any { it.isRunning } && _tasksQueue.isNotEmpty()) {
            Thread.sleep(AWAIT_DELTA_TIME)
        }
    }


    companion object {
        const val DEFAULT_POOL_SIZE = 16
        const val AWAIT_DELTA_TIME = 10L
    }
}
