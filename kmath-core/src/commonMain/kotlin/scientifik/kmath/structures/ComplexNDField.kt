package scientifik.kmath.structures

import scientifik.kmath.operations.Complex
import scientifik.kmath.operations.ComplexField
import scientifik.kmath.operations.FieldElement
import scientifik.kmath.operations.complex

typealias ComplexNDElement = BufferedNDFieldElement<Complex, ComplexField>

/**
 * An optimized nd-field for complex numbers
 */
class ComplexNDField(override val shape: IntArray) :
    BufferedNDField<Complex, ComplexField>,
    ExtendedNDField<Complex, ComplexField, NDBuffer<Complex>> {

    override val strides: Strides = DefaultStrides(shape)

    override val elementContext: ComplexField get() = ComplexField
    override val zero: ComplexNDElement by lazy { produce { zero } }
    override val one: ComplexNDElement by lazy { produce { one } }

    inline fun buildBuffer(size: Int, crossinline initializer: (Int) -> Complex): Buffer<Complex> =
        Buffer.complex(size) { initializer(it) }

    /**
     * Inline transform an NDStructure to another structure
     */
    override fun map(
        arg: NDBuffer<Complex>,
        transform: ComplexField.(Complex) -> Complex
    ): ComplexNDElement {
        check(arg)
        val array = buildBuffer(arg.strides.linearSize) { offset -> ComplexField.transform(arg.buffer[offset]) }
        return BufferedNDFieldElement(this, array)
    }

    override fun produce(initializer: ComplexField.(IntArray) -> Complex): ComplexNDElement {
        val array = buildBuffer(strides.linearSize) { offset -> elementContext.initializer(strides.index(offset)) }
        return BufferedNDFieldElement(this, array)
    }

    override fun mapIndexed(
        arg: NDBuffer<Complex>,
        transform: ComplexField.(index: IntArray, Complex) -> Complex
    ): ComplexNDElement {
        check(arg)
        return BufferedNDFieldElement(
            this,
            buildBuffer(arg.strides.linearSize) { offset ->
                elementContext.transform(
                    arg.strides.index(offset),
                    arg.buffer[offset]
                )
            })
    }

    override fun combine(
        a: NDBuffer<Complex>,
        b: NDBuffer<Complex>,
        transform: ComplexField.(Complex, Complex) -> Complex
    ): ComplexNDElement {
        check(a, b)
        return BufferedNDFieldElement(
            this,
            buildBuffer(strides.linearSize) { offset -> elementContext.transform(a.buffer[offset], b.buffer[offset]) })
    }

    override fun NDBuffer<Complex>.toElement(): FieldElement<NDBuffer<Complex>, *, out BufferedNDField<Complex, ComplexField>> =
        BufferedNDFieldElement(this@ComplexNDField, buffer)

    override fun power(arg: NDBuffer<Complex>, pow: Number): ComplexNDElement = map(arg) { power(it, pow) }

    override fun exp(arg: NDBuffer<Complex>): ComplexNDElement = map(arg) { exp(it) }

    override fun ln(arg: NDBuffer<Complex>): ComplexNDElement = map(arg) { ln(it) }

    override fun sin(arg: NDBuffer<Complex>): ComplexNDElement = map(arg) { sin(it) }

    override fun cos(arg: NDBuffer<Complex>): ComplexNDElement = map(arg) { cos(it) }

    override fun tan(arg: NDBuffer<Complex>): ComplexNDElement = map(arg) { tan(it) }

    override fun asin(arg: NDBuffer<Complex>): ComplexNDElement = map(arg) { asin(it) }

    override fun acos(arg: NDBuffer<Complex>): ComplexNDElement = map(arg) { acos(it) }

    override fun atan(arg: NDBuffer<Complex>): ComplexNDElement = map(arg) { atan(it) }
}


/**
 * Fast element production using function inlining
 */
inline fun BufferedNDField<Complex, ComplexField>.produceInline(crossinline initializer: ComplexField.(Int) -> Complex): ComplexNDElement {
    val buffer = Buffer.complex(strides.linearSize) { offset -> ComplexField.initializer(offset) }
    return BufferedNDFieldElement(this, buffer)
}

/**
 * Map one [ComplexNDElement] using function with indices.
 */
inline fun ComplexNDElement.mapIndexed(crossinline transform: ComplexField.(index: IntArray, Complex) -> Complex): ComplexNDElement =
    context.produceInline { offset -> transform(strides.index(offset), buffer[offset]) }

/**
 * Map one [ComplexNDElement] using function without indices.
 */
inline fun ComplexNDElement.map(crossinline transform: ComplexField.(Complex) -> Complex): ComplexNDElement {
    val buffer = Buffer.complex(strides.linearSize) { offset -> ComplexField.transform(buffer[offset]) }
    return BufferedNDFieldElement(context, buffer)
}

/**
 * Element by element application of any operation on elements to the whole array. Just like in numpy
 */
operator fun Function1<Complex, Complex>.invoke(ndElement: ComplexNDElement): ComplexNDElement =
    ndElement.map { this@invoke(it) }


/* plus and minus */

/**
 * Summation operation for [BufferedNDElement] and single element
 */
operator fun ComplexNDElement.plus(arg: Complex): ComplexNDElement = map { it + arg }

/**
 * Subtraction operation between [BufferedNDElement] and single element
 */
operator fun ComplexNDElement.minus(arg: Complex): ComplexNDElement =
    map { it - arg }

operator fun ComplexNDElement.plus(arg: Double): ComplexNDElement =
    map { it + arg }

operator fun ComplexNDElement.minus(arg: Double): ComplexNDElement =
    map { it - arg }

fun NDField.Companion.complex(vararg shape: Int): ComplexNDField = ComplexNDField(shape)

fun NDElement.Companion.complex(vararg shape: Int, initializer: ComplexField.(IntArray) -> Complex): ComplexNDElement =
    NDField.complex(*shape).produce(initializer)

/**
 * Produce a context for n-dimensional operations inside this real field
 */
inline fun <R> ComplexField.nd(vararg shape: Int, action: ComplexNDField.() -> R): R {
    return NDField.complex(*shape).run(action)
}
