package com.nuzhnov.threadpool.threadpool

/**
 * Класс потока для менеджера потоков [ThreadManager].
 *
 * @property manager ссылка на объект менеджера потоков [ThreadManager].
 */
class ThreadItem(val manager: ThreadManager) : Thread() {

    /** Запущен ли поток в данный момент или он спит. */
    var isRunning = false
    /** Текущая задача, выполняемая потоком. */
    var task: Task? = null


    /**
     * Основной цикл работы потока. Работает до тех пор, пока он не будет прерван, т.н.
     * для него не будет вызван метод interrupt.
     */
    override fun run() {
        try {
            while (true) {
                sleepLoop()
                task?.run()  // Выполняем задачу, если она есть
                task = null  // После выполнения удаляем задачу

                // Если поток ранее не был остановлен:
                if (isRunning) {
                    task = manager.dequeueTask()

                    // Если задач нет
                    if (task == null) {
                        // Пора спать...
                        sleep(SLEEP_DELTA_TIME)
                    }
                }
            }
        } catch (_: InterruptedException) {}
    }

    /** Метод с циклом засыпания потока. Поток спит, пока его не разбудит другой поток. */
    private fun sleepLoop() {
        while (!isRunning) {
            // Пора спать...
            sleep(SLEEP_DELTA_TIME)
        }
    }


    private companion object {
        const val SLEEP_DELTA_TIME = 10L
    }
}
