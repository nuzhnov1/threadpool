package com.nuzhnov.threadpool.threadpool

/**
 * Класс потока для пула потоков [ThreadPool].
 *
 * @property pool ссылка на объект пула потоков [ThreadPool].
 */
internal class ThreadItem(private var pool: ThreadPool? = null) : Thread() {

    internal var isRunning = false      // Запущен ли поток в данный момент
        private set
    private var task: Runnable? = null  // Текущая исполняемая задача данным потоком


    /**
     * Метод засыпания потока. Поток спит, пока его не разбудит менеджер потоков (пул поток).
     */
    private fun sleep() {
        while (!isRunning) {
            // Пора баиньки...
            sleep(SLEEP_TIME)
        }
    }

    /**
     * Основной цикл работы потока. Работает до тех пор, пока менеджер не прервёт его,
     * иными словами не вызовет метод interrupt.
     */
    override fun run() {
        try {
            while (true) {
                sleep()
                task?.run()
                isRunning = false
                pool?.notify(this)
            }
        } catch (_: InterruptedException) {}
    }

    /**
     * Метод для начала работы потока, вызываемый объектом [ThreadPool].
     * Устанавливает код работы потока и пробуждает его.
     *
     * @param onExecute код работы потока
     *
     * @throws IllegalStateException если объект пула потока [pool] до этого не был установлен.
     */
    internal fun doWork(onExecute: Runnable) {
        if (pool != null) {
            this.task = onExecute
            isRunning = true
        } else {
            throw IllegalStateException("thread pool isn't set to this thread")
        }
    }


    companion object {
        const val SLEEP_TIME = 100L  // ms
    }
}
