package scientifik.kmath.asm.internal

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import scientifik.kmath.operations.Algebra

private val methodNameAdapters: Map<Pair<String, Int>, String> by lazy {
    hashMapOf(
        "+" to 2 to "add",
        "*" to 2 to "multiply",
        "/" to 2 to "divide",
        "+" to 1 to "unaryPlus",
        "-" to 1 to "unaryMinus",
        "-" to 2 to "minus"
    )
}

/**
 * Checks if the target [context] for code generation contains a method with needed [name] and [arity], also builds
 * type expectation stack for needed arity.
 *
 * @return `true` if contains, else `false`.
 */
internal fun <T> AsmBuilder<T>.buildExpectationStack(context: Algebra<T>, name: String, arity: Int): Boolean {
    val theName = methodNameAdapters[name to arity] ?: name
    val hasSpecific = context.javaClass.methods.find { it.name == theName && it.parameters.size == arity } != null
    val t = if (primitiveMode && hasSpecific) primitiveMask else tType
    repeat(arity) { expectationStack.push(t) }
    return hasSpecific
}

/**
 * Checks if the target [context] for code generation contains a method with needed [name] and [arity] and inserts
 * [AsmBuilder.invokeAlgebraOperation] of this method.
 *
 * @return `true` if contains, else `false`.
 */
internal fun <T> AsmBuilder<T>.tryInvokeSpecific(context: Algebra<T>, name: String, arity: Int): Boolean {
    val theName = methodNameAdapters[name to arity] ?: name

    context.javaClass.methods.find {
        var suitableSignature = it.name == theName && it.parameters.size == arity

        if (primitiveMode && it.isBridge)
            suitableSignature = false

        suitableSignature
    } ?: return false

    val owner = context::class.asm

    invokeAlgebraOperation(
        owner = owner.internalName,
        method = theName,
        descriptor = Type.getMethodDescriptor(primitiveMaskBoxed, *Array(arity) { primitiveMask }),
        expectedArity = arity,
        opcode = Opcodes.INVOKEVIRTUAL
    )

    return true
}