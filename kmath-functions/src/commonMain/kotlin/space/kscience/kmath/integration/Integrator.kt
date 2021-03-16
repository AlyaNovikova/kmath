package space.kscience.kmath.integration

/**
 * A general interface for all integrators
 */
public interface Integrator<I: Integrand> {
    /**
     * Run one integration pass and return a new [Integrand] with a new set of features
     */
    public fun evaluate(integrand: I): I
}