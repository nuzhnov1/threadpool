plugins {
    id("com.nuzhnov.threadpool.kotlin-application-conventions")
}

dependencies {
    implementation(project(":threadpool"))
}

application {
    // Define the main class for the application.
    mainClass.set("com.nuzhnov.threadpool.app.AppKt")
}
