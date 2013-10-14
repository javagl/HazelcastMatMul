/*
 * www.javagl.de - Hazelcast Matrix Multiplication
 *
 * Copyright (c) 2013 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.hazelcast.matmul.hazelcast;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import de.javagl.hazelcast.matmul.ExecutorExtensions;
import de.javagl.hazelcast.matmul.Factory;
import de.javagl.hazelcast.matmul.FloatMatrix2D;
import de.javagl.hazelcast.matmul.Matrices;
import de.javagl.hazelcast.matmul.MatrixMultiplicator;
import de.javagl.hazelcast.matmul.MatrixMultiplicators;
import de.javagl.hazelcast.matmul.MutableFloatMatrix2D;
import de.javagl.hazelcast.matmul.util.LoggerUtil;

/**
 * Methods for running tests and benchmarks for {@link MatrixMultiplicator}s
 */
public class MatMulTests
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(MatMulTests.class.getName());
    
    /**
     * Runs a basic test
     * @param args Not used
     */
    public static void main(String[] args)
    {
        LoggerUtil.configureDefault(Logger.getLogger("de"));

        MatrixMultiplicator multiplicator0 = 
            MatrixMultiplicators.createParallelDefault(100);
        MatrixMultiplicator multiplicator1 = 
            createNestedParallelMatrixMultiplicator();
        
        final int rA = 1000;
        final int cArB = 1500;
        final int cB = 1000;
        runBasicTest(rA, cArB, cB, multiplicator0, multiplicator1);
        //runBenchmark(multiplicator0, multiplicator1);
    }
    
    /**
     * Create a nested parallel {@link MatrixMultiplicator}
     * 
     * @return The {@link MatrixMultiplicator}
     */
    private static MatrixMultiplicator createNestedParallelMatrixMultiplicator()
    {
        final int numberOfThreads = Runtime.getRuntime().availableProcessors();
        Factory<ExecutorService> executorServiceFactory = 
            new Factory<ExecutorService>()
        {
            @Override
            public ExecutorService create()
            {
                return ExecutorExtensions.newExceptionAwareFixedThreadPool(
                    numberOfThreads);
            }
        };
        final int clusterNodeBlockSize = 100;
        final int nodeProcessorBlockSize = 10;
        Factory<MatrixMultiplicator> subMatrixMultiplicatorFactory =
            MatrixMultiplicators.createParallelFactory(nodeProcessorBlockSize);
        MatrixMultiplicator multiplicator = 
            MatrixMultiplicators.createParallel(
                executorServiceFactory, clusterNodeBlockSize,
                subMatrixMultiplicatorFactory);
        return multiplicator;
        
    }
    
    
    /**
     * Runs a basic test by multiplying random matrices A and B with the 
     * specified sizes using the given {@link MatrixMultiplicator}s, and 
     * comparing the results.
     * 
     * @param rA The number of rows in A
     * @param cArB The number of columns in A (= number of rows in B) 
     * @param cB The number of columns in B
     * @param multiplicator0 The first {@link MatrixMultiplicator}
     * @param multiplicator1 The second {@link MatrixMultiplicator}
     */
    public static void runBasicTest(int rA, int cArB, int cB,
        MatrixMultiplicator multiplicator0,
        MatrixMultiplicator multiplicator1)
    {
        MutableFloatMatrix2D A = Matrices.createFloatMatrix2D(rA, cArB);
        MutableFloatMatrix2D B = Matrices.createFloatMatrix2D(cArB, cB);
        Matrices.fillRandom(A);
        Matrices.fillRandom(B);
        final int runs = 1;
        final boolean verify = true;
        runTest(A, B, multiplicator0, multiplicator1, runs, verify);
    }
    
    /**
     * Runs a simple benchmark for comparing the given 
     * {@link MatrixMultiplicator}s by feeding them with matrices
     * of different sizes and printing timing information
     * 
     * @param multiplicator0 The first {@link MatrixMultiplicator}
     * @param multiplicator1 The second {@link MatrixMultiplicator}
     */
    public static void runBenchmark(
        MatrixMultiplicator multiplicator0,
        MatrixMultiplicator multiplicator1)
    {
        final int minRA = 100;
        final int maxRA = 200;
        final int stepRA = 50;

        final int minCA = 100;
        final int maxCA = 200;
        final int stepCA = 50;

        final int minCB = 100;
        final int maxCB = 200;
        final int stepCB = 50;
        
        final int runs = 5;
        final boolean verify = true;
        
        for (int rA = minRA; rA <= maxRA; rA += stepRA)
        {
            for (int cA = minCA; cA <= maxCA; cA += stepCA)
            {
                for (int cB = minCB; cB <= maxCB; cB += stepCB)
                {
                    MutableFloatMatrix2D A = 
                        Matrices.createFloatMatrix2D(rA, cA);
                    MutableFloatMatrix2D B = 
                        Matrices.createFloatMatrix2D(cA, cB);
                    Matrices.fillRandom(A);
                    Matrices.fillRandom(B);
                    
                    runTest(A, B, multiplicator0, multiplicator1, runs, verify);
                }
            }
        }
    }
    
    /**
     * Multiplies the given matrices with the given 
     * {@link MatrixMultiplicator}s
     * 
     * @param A The first matrix
     * @param B The second matrix
     * @param multiplicator0 The first {@link MatrixMultiplicator}
     * @param multiplicator1 The second {@link MatrixMultiplicator}
     * @param runs The number of runs (how often to repeat the multiplication)
     * @param verify Whether the results should be compared
     */
    private static void runTest(
        FloatMatrix2D A, FloatMatrix2D B,
        MatrixMultiplicator multiplicator0,
        MatrixMultiplicator multiplicator1, 
        int runs, boolean verify)
    {
        int numRows = A.getNumRows();
        int numColumns = B.getNumColumns();
        final MutableFloatMatrix2D C0 = 
            Matrices.createFloatMatrix2D(numRows, numColumns);
        final MutableFloatMatrix2D C1 = 
            Matrices.createFloatMatrix2D(numRows, numColumns);

        logger.info("Running test with "+multiplicator0);
        logger.info("              and "+multiplicator1);
        logger.info("A: "+A.getNumRows()+"x"+A.getNumColumns());
        logger.info("B: "+B.getNumRows()+"x"+B.getNumColumns());
        logger.info("C: "+C0.getNumRows()+"x"+C0.getNumColumns());
        
        for (int i=0; i<runs; i++)
        {
            logger.info("Run "+i+" of "+runs+" with "+multiplicator0);
            runTest(C0, A, B, multiplicator0);

            logger.info("Run "+i+" of "+runs+" with "+multiplicator1);
            runTest(C1, A, B, multiplicator1);

            if (verify)
            {
                verify(C0, C1);
            }
        }
    }
    
    /**
     * Runs a single matrix multiplication and prints timing results
     * 
     * @param C The matrix that will store the product
     * @param A The first factor
     * @param B The second factor
     * @param multiplicator The {@link MatrixMultiplicator} to use
     */
    private static void runTest(
        MutableFloatMatrix2D C, FloatMatrix2D A, FloatMatrix2D B,
        MatrixMultiplicator multiplicator)
    {
        long before = System.nanoTime();
        multiplicator.multiply(C, A, B);
        long after = System.nanoTime();
        double durationMS = (after-before)/1e6;
        logger.info("duration "+
            String.format(Locale.ENGLISH, "%8.2f", durationMS)+
            " ms for "+multiplicator);
        
    }
    
    /**
     * Compare the given matrices, and print some error message when they
     * are not equal
     * 
     * @param C0 The first matrix
     * @param C1 The second matrix
     */
    private static void verify(FloatMatrix2D C0, FloatMatrix2D C1)
    {
        final float EPSILON = 1e-3f; 
        boolean passed = Matrices.equal(C0, C1, EPSILON);
        logger.info("Passed? "+passed);
        if (!passed)
        {
            int printedSize = 6;
            FloatMatrix2D sub0 = C0.subMatrix(0, 0, printedSize, printedSize);
            FloatMatrix2D sub1 = C1.subMatrix(0, 0, printedSize, printedSize);
            logger.warning("ERROR!");
            logger.warning("Upper "+printedSize+"x"+printedSize+" elements:");
            logger.warning("Result 0:\n"+Matrices.toString(sub0));
            logger.warning("Result 1:\n"+Matrices.toString(sub1));
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private MatMulTests()
    {
        // Private constructor to prevent instantiation
    }
}
