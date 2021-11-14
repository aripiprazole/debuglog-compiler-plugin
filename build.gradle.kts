import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.30"
  id("com.github.gmazzo.buildconfig") version "3.0.3"
}

val pluginId = "com.gabrielleeg1.debuglog.plugin"

allprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "com.github.gmazzo.buildconfig")
  
  group = "com.gabrielleeg1.debuglog"
  version = "1.0-SNAPSHOT"

  repositories {
    mavenCentral()
  }

  dependencies {
  }

  tasks {
    test {
      useJUnitPlatform()
    }

    withType<KotlinCompile> {
      kotlinOptions.jvmTarget = "1.8"
    }
    
    buildConfig {
      val project = project(":debuglog-compiler-plugin")
      packageName(project.group.toString())

      buildConfigField(
        "String",
        "KOTLIN_PLUGIN_ID",
        "\"$pluginId\""
      )
      buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
      buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
      buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
    }
  }
}
