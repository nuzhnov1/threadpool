package com.nuzhnov.threadpool.app

import com.nuzhnov.threadpool.app.network.currencyService
import com.nuzhnov.threadpool.app.network.models.Response
import com.nuzhnov.threadpool.threadpool.ThreadManager
import com.nuzhnov.threadpool.threadpool.Task
import com.nuzhnov.threadpool.threadpool.ThreadItem
import java.io.File
import java.io.PrintWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter


private class CustomTask(
    private val callback: () -> Unit
) : Task {

    override fun run() = callback()
}


private val logFile = File("threadpool-log").also { it.createNewFile() }
private val writer = PrintWriter(logFile)
private val testCallback = { Thread.sleep(1L) }

private val workCallback = {
    val threadID = Thread.currentThread().id

    try {
        Thread.sleep(10_000)

        val response = currencyService
            .getCurrencies()
            .execute()
        val time = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)

        when (val responseBody = response.body()) {
            null -> writer.println("Поток №$threadID: ошибка получения списка валют. ($time)")

            is Response.Success -> writer.println("Поток №$threadID: cписок валют получен успешно. ($time)")

            is Response.Failed -> {
                writer.println("Поток №$threadID: ошибка получения списка валют. ($time)")
                writer.println("\tКод ошибки: ${responseBody.error.code}.")
                writer.println("\tСообщение: ${responseBody.error.message}.")
            }

            is Response.Unknown -> writer.println("Поток №$threadID: неизвестный ответ сервера. ($time)")
        }
    } catch (e: Exception) {
        val time = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        writer.println("Поток №$threadID: ${e.localizedMessage}. ($time)")
    }
}


private fun ThreadItem.printInfo(number: Int) {
    println("Поток №$number:")
    println("\tЗапущен: ${if (isRunning) "да" else "нет"}.")
    println("\tВыполняет задачу: ${if (task != null) "да" else "нет"}.")
}

private fun ThreadManager.printInfo() {
    println("Информация о всех потоках менеджера:")
    threadList.zip(threadList.indices).forEach { (thread, number) ->
        thread.printInfo(number)
    }
}

private fun testThreadPool(taskCount: Int, threadCount: Int) {
    val manager = ThreadManager(threadCount)
    repeat(taskCount) { manager.enqueueTask(CustomTask(testCallback)) }

    val before = System.currentTimeMillis()

    // Создаём пул потоков из 'threadCount' потоков и запускаем его на 'taskCount' задачах
    manager.runAll()
    manager.awaitExecutionAllTasks()

    val after = System.currentTimeMillis()
    val duration = (after - before) / 1000.0

    manager.clearThreads()

    println("Время выполнения $taskCount задач в $threadCount потоках с использованием пула потоков: " +
            "$duration секунд."
    )
}

private fun testManualCreatedThreads(taskCount: Int) {
    val before = System.currentTimeMillis()

    // Создаём для каждой задачи свой поток, запускаем их и ждём, пока они все не завершаться
    val threads = List(taskCount) { Thread(CustomTask(callback = testCallback)).also { it.start() } }
    threads.forEach { it.join() }

    val after = System.currentTimeMillis()
    val duration = (after - before) / 1000.0

    println("Время выполнения $taskCount задач, для каждой из которых создавался свой поток: " +
            "$duration секунд."
    )
}

private fun printMenu() {
    println("-".repeat(100))
    println("Меню программы")
    println("\t1: Добавить n потоков.")
    println("\t2: Добавить n задач.")
    println("\t3: Удалить n потоков.")
    println("\t4: Удалить n задач.")
    println("\t5: Вывести текущее число потоков и задач.")
    println("\t6: Вывести информацию об отдельном потоке.")
    println("\t7: Вывести информацию об всех потоках.")
    println("\t8: Запустить отдельный поток.")
    println("\t9: Запустить все потоки.")
    println("\t10: Остановить отдельный поток.")
    println("\t11: Остановить все потоки.")
    println("\t12: Сравнительное выполнение задач.")
    println("\t13: Выйти из программы.")
    println("-".repeat(100))
}

private fun readNaturalNumber(): Int {
    val number = readLine()?.toIntOrNull()

    return when {
        number == null || number < 0 -> throw RuntimeException("Ошибка: неверный ввод.")
        else -> number
    }
}

private fun mainLoop() {
    val manager = ThreadManager(0)

    while (true) {
        printMenu()
        print("Введите пункт меню: ")

        try {
            when (readNaturalNumber()) {
                1 -> {
                    print("Введите число потоков: ")
                    val number = readNaturalNumber()
                    manager.addNewThreads(number)
                }

                2 -> {
                    print("Введите число задач: ")
                    val number = readNaturalNumber()
                    repeat(number) { manager.enqueueTask(CustomTask(callback = workCallback)) }
                }

                3 -> {
                    print("Введите число удаляемых потоков: ")
                    val number = readNaturalNumber()
                    manager.removeThreads(number)
                }

                4 -> {
                    print("Введите число удаляемых задач: ")
                    val number = readNaturalNumber()
                    repeat(number) { manager.dequeueTask() }
                }

                5 -> {
                    println("Текущее число потоков: ${manager.threadList.size}.")
                    println("Текущее число задач: ${manager.tasksQueue.size}.")
                }

                6 -> {
                    print("Введите номер потока: ")
                    val threadNumber = readNaturalNumber()
                    val thread = manager.threadList.getOrNull(threadNumber)

                    if (thread == null) {
                        println("Ошибка: потока с номером $threadNumber не существует!")
                    } else {
                        thread.printInfo(threadNumber)
                    }
                }

                7 -> manager.printInfo()

                8 -> {
                    print("Введите номер потока: ")
                    val threadNumber = readNaturalNumber()
                    val thread = manager.threadList.getOrNull(threadNumber)

                    if (thread == null) {
                        println("Ошибка: потока с номером $threadNumber не существует!")
                    } else {
                        thread.isRunning = true
                    }
                }

                9 -> manager.threadList.forEach { thread -> thread.isRunning = true }

                10 -> {
                    print("Введите номер потока: ")
                    val threadNumber = readNaturalNumber()
                    val thread = manager.threadList.getOrNull(threadNumber)

                    if (thread == null) {
                        println("Ошибка: потока с номером $threadNumber не существует!")
                    } else {
                        thread.isRunning = false
                    }
                }

                11 -> manager.threadList.forEach { thread -> thread.isRunning = false }

                12 -> {
                    print("Введите число задач: ")
                    val taskCount = readNaturalNumber()
                    print("Введите число потоков: ")
                    val threadCount = readNaturalNumber()

                    println("Начинаю сравнительное тестирование...")
                    testThreadPool(taskCount, threadCount)
                    testManualCreatedThreads(taskCount)
                    println("Сравнительное тестирование закончено")
                }

                13 -> {
                    manager.clearThreads()
                    println("Завершаю выполнение программы...")
                }

                else -> println("Ошибка: неверный пункт меню.")
            }
        } catch (e: RuntimeException) {
            println(e.localizedMessage)
        }
    }
}

fun main() {
    println("-".repeat(100))
    println("Начальные тесты")
    println("-".repeat(100))
    println("Сравнительный тест №1.")
    println("Число задач - 100, число потоков - 10.")
    println()
    testThreadPool(100, 10)
    testManualCreatedThreads(100)
    println("-".repeat(100))

    println("-".repeat(100))
    println("Сравнительный тест №2.")
    println("Число задач - 1000, число потоков - 100.")
    println()
    testThreadPool(1000, 100)
    testManualCreatedThreads(1000)
    println("-".repeat(100))

    println("-".repeat(100))
    println("Сравнительный тест №3.")
    println("Число задач - 10000, число потоков - 100.")
    println()
    testThreadPool(10000, 100)
    testManualCreatedThreads(10000)
    println("-".repeat(100))

    mainLoop()
}
