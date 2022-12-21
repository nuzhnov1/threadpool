plugins {
    id("com.nuzhnov.threadpool.kotlin-common-conventions")
    application
}

dependencies {
    implementation(project(":threadpool"))
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation("com.squareup.moshi:moshi-adapters:1.14.0")

    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
}

application {
    mainClass.set("com.nuzhnov.threadpool.app.AppKt")
}

tasks.distZip { archiveBaseName.set("threadpool") }
tasks.distTar { archiveBaseName.set("threadpool") }
