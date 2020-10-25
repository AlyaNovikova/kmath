package kscience.kmath.expressions

/**
 * And object that could be differentiated
 */
public interface Differentiable<T> {
    public fun derivativeOrNull(orders: Map<Symbol, Int>): T?
}

public fun <T> Differentiable<T>.derivative(orders: Map<Symbol, Int>): T =
    derivativeOrNull(orders) ?: error("Derivative with orders $orders not provided")

/**
 * An expression that provid
 */
public interface DifferentiableExpression<T> : Differentiable<Expression<T>>, Expression<T>

public fun <T> DifferentiableExpression<T>.derivative(vararg orders: Pair<Symbol, Int>): Expression<T> =
    derivative(mapOf(*orders))

public fun <T> DifferentiableExpression<T>.derivative(symbol: Symbol): Expression<T> = derivative(symbol to 1)

public fun <T> DifferentiableExpression<T>.derivative(name: String): Expression<T> =
    derivative(StringSymbol(name) to 1)

//public interface DifferentiableExpressionBuilder<T, E, A : ExpressionAlgebra<T, E>>: ExpressionBuilder<T,E,A> {
//    public override fun expression(block: A.() -> E): DifferentiableExpression<T>
//}

public abstract class FirstDerivativeExpression<T> : DifferentiableExpression<T> {

    public abstract fun derivativeOrNull(symbol: Symbol): Expression<T>?

    public override fun derivativeOrNull(orders: Map<Symbol, Int>): Expression<T>? {
        val dSymbol = orders.entries.singleOrNull { it.value == 1 }?.key ?: return null
        return derivativeOrNull(dSymbol)
    }
}