package com.gabrielleeg1.debuglog

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class IrPluginTest {
  @Test
  fun `ir plugin test`() {
    val result = compile(
      SourceFile.kotlin(
        "main.kt",
        """
        annotation class Debug

        fun main() {
            println(greet("josé"))
        }
        
        @Debug
        fun greet(name: String): String {
            Thread.sleep(15)
            return "hello ${'$'}name"
        }
        """.trimIndent(),
      ),
    )

    result.classLoader
      .loadClass("MainKt")
      .declaredMethods
      .single { f -> f.name == "main" && f.parameterCount == 0 }
      .invoke(null)

    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
  }
}

fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
  return KotlinCompilation()
    .apply {
      sources = sourceFiles.toList()
      inheritClassPath = true
      messageOutputStream = System.out
      compilerPlugins = listOf(DebugLogComponentRegistrar())
    }
    .compile()
}
