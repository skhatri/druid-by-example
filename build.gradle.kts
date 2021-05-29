plugins {
    scala
    `java-library`
    `idea`
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.2")
    implementation("com.typesafe:config:1.4.1")
    implementation("org.apache.kafka:kafka-streams-scala_2.13:2.7.0")
    implementation("com.opencsv:opencsv:5.4")
    implementation("com.fasterxml.jackson.module:jackson-module-scala_2.13:2.12.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.2")
    testImplementation("org.scalatest:scalatest_2.13:3.1.2")
    testImplementation("org.scalatestplus:junit-4-12_2.13:3.1.2.0")

    testRuntimeOnly("org.scala-lang.modules:scala-xml_2.13:1.2.0")
}

val topicName: String = if(project.hasProperty("topic.name")) "${project.ext["topic.name"]}" else "football_matches"
val logLines: String = if(project.hasProperty("log.lines")) "${project.ext["log.lines"]}" else "false"
val delay:String = if(project.hasProperty("season.delay")) "${project.ext["season.delay"]}"  else "0"

task("runApp", JavaExec::class) {
    main = "com.github.streams.playmatches.PlayMatchEventApp"
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xms256m", "-Xmx256m")
    args = listOf(topicName, logLines, delay)
}
