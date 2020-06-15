package scientifik.kmath.structures

import scientifik.kmath.operations.*

/**
 * Base class for an element with context, containing strides
 */
abstract class BufferedNDElement<T, C> : NDBuffer<T>(), NDElement<T, C, NDBuffer<T>> {
    abstract override val context: BufferedNDAlgebra<T, C>

    override val strides get() = context.strides

    override val shape: IntArray get() = context.shape
}

class BufferedNDSpaceElement<T, S : Space<T>>(
    override val context: BufferedNDSpace<T, S>,
    override val buffer: Buffer<T>
) : BufferedNDElement<T, S>(), SpaceElement<NDBuffer<T>, BufferedNDSpaceElement<T, S>, BufferedNDSpace<T, S>> {

    override fun unwrap(): NDBuffer<T> = this

    override fun NDBuffer<T>.wrap(): BufferedNDSpaceElement<T, S> {
        context.check(this)
        return BufferedNDSpaceElement(context, buffer)
    }
}

class BufferedNDRingElement<T, R : Ring<T>>(
    override val context: BufferedNDRing<T, R>,
    override val buffer: Buffer<T>
) : BufferedNDElement<T, R>(), RingElement<NDBuffer<T>, BufferedNDRingElement<T, R>, BufferedNDRing<T, R>> {

    override fun unwrap(): NDBuffer<T> = this

    override fun NDBuffer<T>.wrap(): BufferedNDRingElement<T, R> {
        context.check(this)
        return BufferedNDRingElement(context, buffer)
    }
}

class BufferedNDFieldElement<T, F : Field<T>>(
    override val context: BufferedNDField<T, F>,
    override val buffer: Buffer<T>
) : BufferedNDElement<T, F>(), FieldElement<NDBuffer<T>, BufferedNDFieldElement<T, F>, BufferedNDField<T, F>> {

    override fun unwrap(): NDBuffer<T> = this

    override fun NDBuffer<T>.wrap(): BufferedNDFieldElement<T, F> {
        context.check(this)
        return BufferedNDFieldElement(context, buffer)
    }
}


/**
 * Element by element application of any operation on elements to the whole array. Just like in numpy
 */
operator fun <T : Any, F : Field<T>> Function1<T, T>.invoke(ndElement: BufferedNDElement<T, F>) =
    ndElement.context.run { map(ndElement) { invoke(it) }.toElement() }

/* plus and minus */

/**
 * Summation operation for [BufferedNDElement] and single element
 */
operator fun <T : Any, F : Space<T>> BufferedNDElement<T, F>.plus(arg: T) =
    context.map(this) { it + arg }.wrap()

/**
 * Subtraction operation between [BufferedNDElement] and single element
 */
operator fun <T : Any, F : Space<T>> BufferedNDElement<T, F>.minus(arg: T) =
    context.map(this) { it - arg }.wrap()

/* prod and div */

/**
 * Product operation for [BufferedNDElement] and single element
 */
operator fun <T : Any, F : Ring<T>> BufferedNDElement<T, F>.times(arg: T) =
    context.map(this) { it * arg }.wrap()

/**
 * Division operation between [BufferedNDElement] and single element
 */
operator fun <T : Any, F : Field<T>> BufferedNDElement<T, F>.div(arg: T) =
    context.map(this) { it / arg }.wrap()