package com.gabrielleeg1.debuglog

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

@AutoService(CommandLineProcessor::class)
class DebugLogCommandLineProcessor : CommandLineProcessor {
  override val pluginId = BuildConfig.KOTLIN_PLUGIN_ID
  override val pluginOptions = emptyList<AbstractCliOption>()
}
