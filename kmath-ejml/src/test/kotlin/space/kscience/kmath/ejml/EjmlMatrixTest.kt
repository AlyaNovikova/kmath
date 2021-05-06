/*
 * Copyright 2018-2021 KMath contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.ejml

import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.CommonOps_DDRM
import org.ejml.dense.row.RandomMatrices_DDRM
import org.ejml.dense.row.factory.DecompositionFactory_DDRM
import space.kscience.kmath.linear.DeterminantFeature
import space.kscience.kmath.linear.LupDecompositionFeature
import space.kscience.kmath.linear.getFeature
import space.kscience.kmath.misc.UnstableKMathAPI
import space.kscience.kmath.nd.StructureND
import kotlin.random.Random
import kotlin.random.asJavaRandom
import kotlin.test.*

fun <T : Any> assertMatrixEquals(expected: StructureND<T>, actual: StructureND<T>) {
    assertTrue { StructureND.contentEquals(expected, actual) }
}

internal class EjmlMatrixTest {
    private val random = Random(0)

    private val randomMatrix: DMatrixRMaj
        get() {
            val s = random.nextInt(2, 100)
            val d = DMatrixRMaj(s, s)
            RandomMatrices_DDRM.fillUniform(d, random.asJavaRandom())
            return d
        }

    @Test
    fun rowNum() {
        val m = randomMatrix
        assertEquals(m.numRows, EjmlDoubleMatrix(m).rowNum)
    }

    @Test
    fun colNum() {
        val m = randomMatrix
        assertEquals(m.numCols, EjmlDoubleMatrix(m).rowNum)
    }

    @Test
    fun shape() {
        val m = randomMatrix
        val w = EjmlDoubleMatrix(m)
        assertContentEquals(intArrayOf(m.numRows, m.numCols), w.shape)
    }

    @OptIn(UnstableKMathAPI::class)
    @Test
    fun features() {
        val m = randomMatrix
        val w = EjmlDoubleMatrix(m)
        val det: DeterminantFeature<Double> = EjmlLinearSpaceDDRM.getFeature(w) ?: fail()
        assertEquals(CommonOps_DDRM.det(m), det.determinant)
        val lup: LupDecompositionFeature<Double> = EjmlLinearSpaceDDRM.getFeature(w) ?: fail()

        val ludecompositionF64 = DecompositionFactory_DDRM.lu(m.numRows, m.numCols)
            .also { it.decompose(m.copy()) }

        assertMatrixEquals(EjmlDoubleMatrix(ludecompositionF64.getLower(null)), lup.l)
        assertMatrixEquals(EjmlDoubleMatrix(ludecompositionF64.getUpper(null)), lup.u)
        assertMatrixEquals(EjmlDoubleMatrix(ludecompositionF64.getRowPivot(null)), lup.p)
    }

    @Test
    fun get() {
        val m = randomMatrix
        assertEquals(m[0, 0], EjmlDoubleMatrix(m)[0, 0])
    }

    @Test
    fun origin() {
        val m = randomMatrix
        assertSame(m, EjmlDoubleMatrix(m).origin)
    }
}
