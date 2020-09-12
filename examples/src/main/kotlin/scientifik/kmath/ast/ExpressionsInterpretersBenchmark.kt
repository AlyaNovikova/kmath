package scientifik.kmath.ast

import scientifik.kmath.asm.compile
import scientifik.kmath.expressions.Expression
import scientifik.kmath.expressions.expressionInField
import scientifik.kmath.expressions.invoke
import scientifik.kmath.operations.Field
import scientifik.kmath.operations.RealField
import kotlin.random.Random
import kotlin.system.measureTimeMillis

private class ExpressionsInterpretersBenchmark {
    private val algebra: Field<Double> = RealField

    fun functionalExpression() {
        val expr = algebra.expressionInField {
            variable("x") * const(2.0) + const(2.0) / variable("x") - const(16.0)
        }

        invokeAndSum(expr)
    }

    fun mstExpression() {
        val expr = algebra.mstInField {
            symbol("x") * number(2.0) + number(2.0) / symbol("x") - number(16.0)
        }

        invokeAndSum(expr)
    }

    fun asmExpression() {
        val expr = algebra.mstInField {
            symbol("x") * number(2.0) + number(2.0) / symbol("x") - number(16.0)
        }.compile()

        invokeAndSum(expr)
    }

    private fun invokeAndSum(expr: Expression<Double>) {
        val random = Random(0)
        var sum = 0.0

        repeat(1000000) {
            sum += expr("x" to random.nextDouble())
        }

        println(sum)
    }
}

fun main() {
    val benchmark = ExpressionsInterpretersBenchmark()

    val fe = measureTimeMillis {
        benchmark.functionalExpression()
    }

    println("fe=$fe")

    val mst = measureTimeMillis {
        benchmark.mstExpression()
    }

    println("mst=$mst")

    val asm = measureTimeMillis {
        benchmark.asmExpression()
    }

    println("asm=$asm")
}
