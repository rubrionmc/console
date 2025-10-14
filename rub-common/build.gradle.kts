dependencies {
    compileOnly(libs.jetanno)
    implementation(libs.common)

    compileOnly("org.apache.logging.log4j:log4j-api:2.21.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.21.0")
}

tasks.register<Copy>("exportResources") {
    from("src/main/resources")
    into("${buildDir}/exportedResources")
}
