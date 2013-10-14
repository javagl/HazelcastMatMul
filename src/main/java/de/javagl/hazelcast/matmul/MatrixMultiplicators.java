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
package de.javagl.hazelcast.matmul;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

/**
 * Methods to create {@link MatrixMultiplicator} instances.
 */
public class MatrixMultiplicators
{
    /**
     * A factory for simple {@link MatrixMultiplicator} instances
     */
    private static class SimpleMatrixMultiplicatorFactory 
        implements Factory<MatrixMultiplicator>, Serializable
    {
        /**
         * Serial UID
         */
        private static final long serialVersionUID = -8379002256269568661L;

        @Override
        public MatrixMultiplicator create()
        {
            return createSimple();
        }
        
        @Override
        public String toString()
        {
            return getClass().getSimpleName();
        }
    }
    
    /**
     * A factory for parallel {@link MatrixMultiplicator} instances
     */
    private static class ParallelMatrixMultiplicatorFactory 
        implements Factory<MatrixMultiplicator>, Serializable
    {
        /**
         * Serial UID
         */
        
        private static final long serialVersionUID = 8944933183222229132L;
        /**
         * The block size
         */
        private final int blockSize;
        
        /**
         * Creates the factory for parallel {@link MatrixMultiplicator} 
         * instances with the given block size
         *  
         * @param blockSize The block size
         */
        ParallelMatrixMultiplicatorFactory(int blockSize)
        {
            this.blockSize = blockSize;
        }

        @Override
        public MatrixMultiplicator create()
        {
            return createParallelDefault(blockSize);
        }
        
        @Override
        public String toString()
        {
            return getClass().getSimpleName();
        }
    }
    
    
    /**
     * Returns a {@link Factory} that creates {@link MatrixMultiplicator} 
     * instances using {@link #createSimple()}
     * 
     * @return The factory
     */
    public static Factory<MatrixMultiplicator> createSimpleFactory()
    {
        return new SimpleMatrixMultiplicatorFactory();
    }
    
    /**
     * Returns a {@link Factory} that creates {@link MatrixMultiplicator} 
     * instances using {@link #createParallelDefault(int)} 
     * 
     * @param blockSize The block size
     * @return The factory
     */
    public static Factory<MatrixMultiplicator> createParallelFactory(
        int blockSize)
    {
        return new ParallelMatrixMultiplicatorFactory(blockSize);
    }
    
    
    /**
     * Create a simple {@link MatrixMultiplicator}
     * 
     * @return The {@link MatrixMultiplicator}
     */
    public static MatrixMultiplicator createSimple()
    {
        return new SimpleMatrixMultiplicator();
    }

    /**
     * Create a parallel {@link MatrixMultiplicator} that multiplies
     * the matrices by splitting them into blocks of the given size,
     * and multiplying the blocks in a thread pool whose size is 
     * fixed to the number of available processors.
     * 
     * @param blockSize The block size
     * @return The {@link MatrixMultiplicator}
     */
    public static MatrixMultiplicator createParallelDefault(int blockSize)
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
        return new ParallelMatrixMultiplicator(
            executorServiceFactory, blockSize,
            createSimpleFactory());
    }
    
    /**
     * Create a parallel {@link MatrixMultiplicator}
     * 
     * @param executorServiceFactory The {@link Factory} for the 
     * ExecutorService
     * @param blockSize The block size
     * @param subMatrixMultiplicatorFactory The {@link Factory} that will
     * be used to create the {@link MatrixMultiplicator} instances for the
     * blocks of the given size
     * @return The {@link MatrixMultiplicator}
     */
    public static MatrixMultiplicator createParallel(
        Factory<ExecutorService> executorServiceFactory, int blockSize,
        Factory<MatrixMultiplicator> subMatrixMultiplicatorFactory)
    {
        return new ParallelMatrixMultiplicator(
            executorServiceFactory, blockSize,
            subMatrixMultiplicatorFactory);
    }
    
    
    /**
     * Private constructor to prevent instantiation
     */
    private MatrixMultiplicators()
    {
        // Private constructor to prevent instantiation
    }
}
