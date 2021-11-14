package com.gabrielleeg1.debuglog

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName

class DebugLogIrGenerationExtension(val messageCollector: MessageCollector) :
  IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    moduleFragment.transform(DebugLogTransformer(pluginContext), null)
  }
}

class DebugLogTransformer(val pluginContext: IrPluginContext) :
  IrElementTransformerVoidWithContext() {
  val typeNullableAny = pluginContext.irBuiltIns.anyNType
  val typeUnit = pluginContext.irBuiltIns.unitType

  val printlnFn = pluginContext
    .referenceFunctions(FqName("kotlin.io.println"))
    .single { symbol ->
      symbol.owner.valueParameters.size == 1 &&
        symbol.owner.valueParameters.first().type == typeNullableAny
    }

  val debugAnnotation = pluginContext.referenceClass(FqName("Debug"))!!

  override fun visitFunctionNew(declaration: IrFunction): IrStatement {
    val body = declaration.body

    if (body != null && declaration.hasAnnotation(debugAnnotation)) {
      declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol)
        .irBlockBody {
          +irDebugEnter(declaration)

          for (stmt in body.statements) {
            +stmt
          }
         
          if (declaration.returnType == typeUnit) {
            +irDebugExit(declaration)
          }
        }
        .transform(object : IrElementTransformerVoidWithContext() {
          override fun visitReturn(expression: IrReturn): IrExpression {
            return DeclarationIrBuilder(pluginContext, declaration.symbol).irBlock {
              val temp = irTemporary(expression.value)
              +irDebugExit(declaration)
              +expression.apply {
                value = irGet(temp)
              }
            }
          }
        }, null)
    }

    return super.visitFunctionNew(declaration)
  }

  private fun IrBuilderWithScope.irDebugExit(function: IrFunction): IrCall {
    return irCall(printlnFn).apply {
      putValueArgument(0, irString("$LightRed DEBUG EXIT$LightGray  : $Reset${function.name}"))
    }
  }

  private fun IrBuilderWithScope.irDebugEnter(function: IrFunction): IrCall {
    val message = irConcat().apply {
      addArgument(irString("$LightRed DEBUG ENTER $LightGray: $Reset${function.name}$LightGray("))
      function.valueParameters.forEachIndexed { index, irValueParameter ->
        if (index > 0) {
          addArgument(irString(", "))
        }
        addArgument(irString("$LightYellow${irValueParameter.name}$Reset=$Blue"))
        addArgument(irGet(irValueParameter))
      }
      addArgument(irString("$LightGray)$Reset"))
    }

    return irCall(printlnFn).apply { putValueArgument(0, message) }
  }
}
