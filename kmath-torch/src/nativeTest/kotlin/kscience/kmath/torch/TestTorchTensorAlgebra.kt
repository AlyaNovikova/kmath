package kscience.kmath.torch

import kscience.kmath.linear.RealMatrixContext
import kscience.kmath.operations.invoke
import kscience.kmath.structures.Matrix
import kotlin.math.*
import kotlin.test.*

internal fun testingScalarProduct(device: Device = Device.CPU): Unit {
    TorchTensorRealAlgebra {
        val lhs = randUniform(shape = intArrayOf(3), device = device)
        val rhs = randUniform(shape = intArrayOf(3), device = device)
        val product = lhs dot rhs
        var expected = 0.0
        lhs.elements().forEach {
            expected += it.second * rhs[it.first]
        }
        assertTrue(abs(expected - product.value()) < TOLERANCE)
    }
}

internal fun testingMatrixMultiplication(device: Device = Device.CPU): Unit {
    TorchTensorRealAlgebra {
        setSeed(SEED)

        val lhsTensor = randNormal(shape = intArrayOf(3, 3), device = device)
        val rhsTensor = randNormal(shape = intArrayOf(3, 3), device = device)
        val product = lhsTensor dot rhsTensor

        val expected: Matrix<Double> = RealMatrixContext {
            val lhs = produce(3, 3) { i, j -> lhsTensor[intArrayOf(i, j)] }
            val rhs = produce(3, 3) { i, j -> rhsTensor[intArrayOf(i, j)] }
            lhs dot rhs
        }

        val lhsTensorCopy = lhsTensor.copy()
        val rhsTensorCopy = rhsTensor.copy()

        lhsTensorCopy dotAssign rhsTensor
        lhsTensor dotRightAssign rhsTensorCopy

        var error = 0.0
        product.elements().forEach {
            error += abs(expected[it.first] - it.second) +
                    abs(expected[it.first] - lhsTensorCopy[it.first]) +
                    abs(expected[it.first] - rhsTensorCopy[it.first])
        }
        assertTrue(error < TOLERANCE)
    }
}

internal fun testingLinearStructure(device: Device = Device.CPU): Unit {
    TorchTensorRealAlgebra {
        withChecks {
        val shape = intArrayOf(3)
        val tensorA = full(value = -4.5, shape = shape, device = device)
        val tensorB = full(value = 10.9, shape = shape, device = device)
        val tensorC = full(value = 789.3, shape = shape, device = device)
        val tensorD = full(value = -72.9, shape = shape, device = device)
        val tensorE = full(value = 553.1, shape = shape, device = device)
        val result = 15.8 * tensorA - 1.5 * tensorB * (-tensorD) + 0.02 * tensorC / tensorE - 39.4
        val expected = copyFromArray(
            array = (1..3).map {
                15.8 * (-4.5) - 1.5 * 10.9 * 72.9 + 0.02 * 789.3 / 553.1 - 39.4
            }
                .toDoubleArray(),
            shape = shape,
            device = device
        )

        val assignResult = full(value = 0.0, shape = shape, device = device)
        tensorA *= 15.8
        tensorB *= 1.5
        tensorB *= -tensorD
        tensorC *= 0.02
        tensorC /= tensorE
        assignResult += tensorA
        assignResult -= tensorB
        assignResult += tensorC
        assignResult += -39.4

        val error = (expected - result).abs().sum().value() +
                (expected - assignResult).abs().sum().value()
        assertTrue(error < TOLERANCE)
        println(expected)
    }}
}

internal fun testingTensorTransformations(device: Device = Device.CPU): Unit {
    TorchTensorRealAlgebra {
        setSeed(SEED)
        val tensor = randNormal(shape = intArrayOf(3, 3), device = device)
        val result = tensor.exp().log()
        val assignResult = tensor.copy()
        assignResult.transposeAssign(0, 1)
        assignResult.expAssign()
        assignResult.logAssign()
        assignResult.transposeAssign(0, 1)
        val error = tensor - result
        error.absAssign()
        error.sumAssign()
        error += (tensor - assignResult).abs().sum()
        assertTrue(error.value() < TOLERANCE)
    }
}

internal fun testingBatchedSVD(device: Device = Device.CPU): Unit {
    TorchTensorRealAlgebra {
        val tensor = randNormal(shape = intArrayOf(7, 5, 3), device = device)
        val (tensorU, tensorS, tensorV) = tensor.svd()
        val error = tensor - (tensorU dot (diagonalEmbedding(tensorS) dot tensorV.transpose(-2,-1)))
        assertTrue(error.abs().sum().value() < TOLERANCE)
    }
}

internal fun testingBatchedSymEig(device: Device = Device.CPU): Unit {
    TorchTensorRealAlgebra {
        val tensor = randNormal(shape = intArrayOf(5,5), device = device)
        val tensorSigma = tensor + tensor.transpose(-2,-1)
        val (tensorS, tensorV) = tensorSigma.symEig()
        val error = tensorSigma - (tensorV dot (diagonalEmbedding(tensorS) dot tensorV.transpose(-2,-1)))
        assertTrue(error.abs().sum().value() < TOLERANCE)
    }
}

internal class TestTorchTensorAlgebra {

    @Test
    fun testScalarProduct() = testingScalarProduct()

    @Test
    fun testMatrixMultiplication() = testingMatrixMultiplication()

    @Test
    fun testLinearStructure() = testingLinearStructure()

    @Test
    fun testTensorTransformations() = testingTensorTransformations()

    @Test
    fun testBatchedSVD() = testingBatchedSVD()

    @Test
    fun testBatchedSymEig() = testingBatchedSymEig()

}