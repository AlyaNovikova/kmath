package space.kscience.kmath.ejml

import org.ejml.dense.row.factory.DecompositionFactory_DDRM
import org.ejml.simple.SimpleMatrix
import space.kscience.kmath.linear.DeterminantFeature
import space.kscience.kmath.linear.LupDecompositionFeature
import space.kscience.kmath.linear.MatrixFeature
import space.kscience.kmath.linear.plus
import space.kscience.kmath.misc.UnstableKMathAPI
import space.kscience.kmath.nd.getFeature
import kotlin.random.Random
import kotlin.random.asJavaRandom
import kotlin.test.*

internal class EjmlMatrixTest {
    private val random = Random(0)

    private val randomMatrix: SimpleMatrix
        get() {
            val s = random.nextInt(2, 100)
            return SimpleMatrix.random_DDRM(s, s, 0.0, 10.0, random.asJavaRandom())
        }

    @Test
    fun rowNum() {
        val m = randomMatrix
        assertEquals(m.numRows(), EjmlMatrix(m).rowNum)
    }

    @Test
    fun colNum() {
        val m = randomMatrix
        assertEquals(m.numCols(), EjmlMatrix(m).rowNum)
    }

    @Test
    fun shape() {
        val m = randomMatrix
        val w = EjmlMatrix(m)
        assertEquals(listOf(m.numRows(), m.numCols()), w.shape.toList())
    }

    @OptIn(UnstableKMathAPI::class)
    @Test
    fun features() {
        val m = randomMatrix
        val w = EjmlMatrix(m)
        val det = w.getFeature<DeterminantFeature<Double>>() ?: fail()
        assertEquals(m.determinant(), det.determinant)
        val lup = w.getFeature<LupDecompositionFeature<Double>>() ?: fail()

        val ludecompositionF64 = DecompositionFactory_DDRM.lu(m.numRows(), m.numCols())
            .also { it.decompose(m.ddrm.copy()) }

        assertEquals(EjmlMatrix(SimpleMatrix(ludecompositionF64.getLower(null))), lup.l)
        assertEquals(EjmlMatrix(SimpleMatrix(ludecompositionF64.getUpper(null))), lup.u)
        assertEquals(EjmlMatrix(SimpleMatrix(ludecompositionF64.getRowPivot(null))), lup.p)
    }

    private object SomeFeature : MatrixFeature {}

    @OptIn(UnstableKMathAPI::class)
    @Test
    fun suggestFeature() {
        assertNotNull((EjmlMatrix(randomMatrix) + SomeFeature).getFeature<SomeFeature>())
    }

    @Test
    fun get() {
        val m = randomMatrix
        assertEquals(m[0, 0], EjmlMatrix(m)[0, 0])
    }

    @Test
    fun origin() {
        val m = randomMatrix
        assertSame(m, EjmlMatrix(m).origin)
    }
}
