package space.kscience.kmath.wasm.internal

import space.kscience.kmath.ast.MST
import space.kscience.kmath.ast.MST.*
import space.kscience.kmath.expressions.Expression
import space.kscience.kmath.expressions.StringSymbol
import space.kscience.kmath.internal.binaryen.*
import space.kscience.kmath.internal.webassembly.Instance
import space.kscience.kmath.operations.*
import space.kscience.kmath.internal.binaryen.Module as BinaryenModule
import space.kscience.kmath.internal.webassembly.Module as WasmModule

private val spreader = eval("(obj, args) => obj(...args)")

@Suppress("UnsafeCastFromDynamic")
internal sealed class WasmBuilder<T>(
    val binaryenType: Type,
    val algebra: Algebra<T>,
    val target: MST,
) where T : Number {
    val keys: MutableList<String> = mutableListOf()
    lateinit var ctx: BinaryenModule

    open fun visitSymbolic(mst: MST.Symbolic): ExpressionRef {
        try {
            algebra.bindSymbol(mst.value)
        } catch (ignored: Throwable) {
            null
        }?.let { return visitNumeric(Numeric(it)) }

        var idx = keys.indexOf(mst.value)

        if (idx == -1) {
            keys += mst.value
            idx = keys.lastIndex
        }

        return ctx.local.get(idx, binaryenType)
    }

    abstract fun visitNumeric(mst: Numeric): ExpressionRef

    open fun visitUnary(mst: Unary): ExpressionRef =
        error("Unary operation ${mst.operation} not defined in $this")

    open fun visitBinary(mst: Binary): ExpressionRef =
        error("Binary operation ${mst.operation} not defined in $this")

    open fun createModule(): BinaryenModule = js("new \$module\$binaryen.Module()")

    fun visit(mst: MST): ExpressionRef = when (mst) {
        is Symbolic -> visitSymbolic(mst)
        is Numeric -> visitNumeric(mst)

        is Unary -> when {
            algebra is NumericAlgebra && mst.value is Numeric -> visitNumeric(
                Numeric(algebra.unaryOperationFunction(mst.operation)(algebra.number(mst.value.value))))

            else -> visitUnary(mst)
        }

        is Binary -> when {
            algebra is NumericAlgebra && mst.left is Numeric && mst.right is Numeric -> visitNumeric(Numeric(
                algebra
                    .binaryOperationFunction(mst.operation)
                    .invoke(algebra.number(mst.left.value), algebra.number(mst.right.value))
            ))

            else -> visitBinary(mst)
        }
    }

    val instance by lazy {
        val c = WasmModule(with(createModule()) {
            ctx = this
            val expr = visit(target)

            addFunction(
                "executable",
                createType(Array(keys.size) { binaryenType }),
                binaryenType,
                arrayOf(),
                expr
            )

            setOptimizeLevel(3)
            optimizeFunction("executable")
            addFunctionExport("executable", "executable")
            val res = emitBinary()
            dispose()
            res
        })

        val i = Instance(c, js("{}") as Any)
        val symbols = keys.map(::StringSymbol)
        keys.clear()

        Expression<T> { args ->
            val params = symbols.map(args::getValue).toTypedArray()
            spreader(i.exports.asDynamic().executable, params) as T
        }
    }
}

internal class RealWasmBuilder(target: MST) : WasmBuilder<Double>(f64, RealField, target) {
    override fun createModule(): BinaryenModule = readBinary(f64StandardFunctions)

    override fun visitNumeric(mst: Numeric): ExpressionRef = ctx.f64.const(mst.value)

    override fun visitUnary(mst: Unary): ExpressionRef = when (mst.operation) {
        SpaceOperations.MINUS_OPERATION -> ctx.f64.neg(visit(mst.value))
        SpaceOperations.PLUS_OPERATION -> visit(mst.value)
        PowerOperations.SQRT_OPERATION -> ctx.f64.sqrt(visit(mst.value))
        TrigonometricOperations.SIN_OPERATION -> ctx.call("sin", arrayOf(visit(mst.value)), f64)
        TrigonometricOperations.COS_OPERATION -> ctx.call("cos", arrayOf(visit(mst.value)), f64)
        TrigonometricOperations.TAN_OPERATION -> ctx.call("tan", arrayOf(visit(mst.value)), f64)
        TrigonometricOperations.ASIN_OPERATION -> ctx.call("asin", arrayOf(visit(mst.value)), f64)
        TrigonometricOperations.ACOS_OPERATION -> ctx.call("acos", arrayOf(visit(mst.value)), f64)
        TrigonometricOperations.ATAN_OPERATION -> ctx.call("atan", arrayOf(visit(mst.value)), f64)
        HyperbolicOperations.SINH_OPERATION -> ctx.call("sinh", arrayOf(visit(mst.value)), f64)
        HyperbolicOperations.COSH_OPERATION -> ctx.call("cosh", arrayOf(visit(mst.value)), f64)
        HyperbolicOperations.TANH_OPERATION -> ctx.call("tanh", arrayOf(visit(mst.value)), f64)
        HyperbolicOperations.ASINH_OPERATION -> ctx.call("asinh", arrayOf(visit(mst.value)), f64)
        HyperbolicOperations.ACOSH_OPERATION -> ctx.call("acosh", arrayOf(visit(mst.value)), f64)
        HyperbolicOperations.ATANH_OPERATION -> ctx.call("atanh", arrayOf(visit(mst.value)), f64)
        ExponentialOperations.EXP_OPERATION -> ctx.call("exp", arrayOf(visit(mst.value)), f64)
        ExponentialOperations.LN_OPERATION -> ctx.call("log", arrayOf(visit(mst.value)), f64)
        else -> super.visitUnary(mst)
    }

    override fun visitBinary(mst: Binary): ExpressionRef = when (mst.operation) {
        SpaceOperations.PLUS_OPERATION -> ctx.f64.add(visit(mst.left), visit(mst.right))
        SpaceOperations.MINUS_OPERATION -> ctx.f64.sub(visit(mst.left), visit(mst.right))
        RingOperations.TIMES_OPERATION -> ctx.f64.mul(visit(mst.left), visit(mst.right))
        FieldOperations.DIV_OPERATION -> ctx.f64.div(visit(mst.left), visit(mst.right))
        PowerOperations.POW_OPERATION -> ctx.call("pow", arrayOf(visit(mst.left), visit(mst.right)), f64)
        else -> super.visitBinary(mst)
    }
}

internal class IntWasmBuilder(target: MST) : WasmBuilder<Int>(i32, IntRing, target) {
    override fun visitNumeric(mst: Numeric): ExpressionRef = ctx.i32.const(mst.value)

    override fun visitUnary(mst: Unary): ExpressionRef = when (mst.operation) {
        SpaceOperations.MINUS_OPERATION -> ctx.i32.sub(ctx.i32.const(0), visit(mst.value))
        SpaceOperations.PLUS_OPERATION -> visit(mst.value)
        else -> super.visitUnary(mst)
    }

    override fun visitBinary(mst: Binary): ExpressionRef = when (mst.operation) {
        SpaceOperations.PLUS_OPERATION -> ctx.i32.add(visit(mst.left), visit(mst.right))
        SpaceOperations.MINUS_OPERATION -> ctx.i32.sub(visit(mst.left), visit(mst.right))
        RingOperations.TIMES_OPERATION -> ctx.i32.mul(visit(mst.left), visit(mst.right))
        else -> super.visitBinary(mst)
    }
}
