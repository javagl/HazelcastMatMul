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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Implementation of a {@link MatrixMultiplicator} that uses
 * an ExecutorService for a parallel matrix multiplication.
 * <br />
 * The implementation is conceptually similar to the SUMMA matrix 
 * multiplication algorithm: 
 * http://www.cs.utexas.edu/ftp/techreports/tr95-13.pdf <br />
 * Since broadcasting the input data among the grid nodes is not 
 * applicable in our case, the remaining core element is that 
 * the matrix multiplication is described as a sum of outer products. 
 * <br />
 * For a multiplication of two matrices 'A' and 'B':
 * <code><pre>
 *                      B00 B01
 * A00 A01 A02 A03      B10 B11      C00 C01
 * A10 A11 A12 A13  *   B20 B21  =   C10 C11 
 * A20 A21 A22 A23      B30 B31      C20 C21
 * </pre></code>
 * The result matrix 'C' can be considered as the sum of several 
 * outer products between rows of 'A' and columns of 'B'. Each 
 * block 'Crc' of the result matrix 'C' will be of the form
 * <code><pre>
 * Crc = 0;
 * for (p = 0 to numOuterProducts)
 * {
 *     Crc += Arp * Bpc;
 * }
 * </pre></code>
 * where 'numOuterProducts' is the number of columns in 'A' (which is 
 * equal to the number of rows in 'B'). <br />
 * <br />
 * These outer products are computed in parallel, for all elements 
 * 'Crc' of the result matrix, and then summed up.
 */
final class ParallelMatrixMultiplicator implements MatrixMultiplicator
{
    /**
     * The factory for the ExecutorService
     */
    private final Factory<ExecutorService> executorServiceFactory;
    
    /**
     * The block size for the matrices
     */
    private final int blockSize;
    
    /**
     * The {@link Factory} that will be used to create 
     * {@link MatrixMultiplicator} instances for the 
     * sub-matrices.
     */
    private final Factory<MatrixMultiplicator> subMatrixMultiplicatorFactory;
    
    /**
     * Creates a new matrix multiplicator that can multiply two matrices
     * by submitting tasks to compute blocks of the specified size to 
     * the ExecutorService that is created by the given factory.
     * 
     * @param executorServiceFactory The {@link Factory} for the
     * ExecutorService
     * @param blockSize The block size
     * @param subMatrixMultiplicatorFactory The {@link Factory} that will be 
     * used to create {@link MatrixMultiplicator} instances for the 
     * sub-matrices. 
     */
    ParallelMatrixMultiplicator(
        Factory<ExecutorService> executorServiceFactory, int blockSize,
        Factory<MatrixMultiplicator> subMatrixMultiplicatorFactory)
    {
        this.executorServiceFactory = executorServiceFactory;
        this.blockSize = blockSize;
        this.subMatrixMultiplicatorFactory = subMatrixMultiplicatorFactory;
    }
    
    @Override
    public void multiply(
        MutableFloatMatrix2D C, FloatMatrix2D A, FloatMatrix2D B)
    {
        //System.out.println("Multiply A "+debugString(A));
        //System.out.println("and      B "+debugString(B));
        
        ExecutorService executorService = 
            executorServiceFactory.create();
        try
        {
            multiplyImpl(executorService, C, A, B);
        }
        finally
        {
            executorService.shutdown();
        }
    }
    
    /**
     * Implementation of the multiplication method
     * 
     * @param executorService The executor service
     * @param C The result matrix
     * @param A The first factor
     * @param B The second factor
     */
    private void multiplyImpl(ExecutorService executorService, 
        MutableFloatMatrix2D C, FloatMatrix2D A, FloatMatrix2D B)
    {
        int numOuterProducts = divCeil(A.getNumColumns(), blockSize);
        Matrices.fill(C, 0.0f);
        for (int p=0; p<numOuterProducts; p++)
        {
            List<MatMulTask> tasks = createTasks(A, B, p);
            List<Future<MatMulResult>> futures = null;
            try
            {
                futures = executorService.invokeAll(tasks);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return;
            }
            
            for (Future<MatMulResult> future : futures)
            {
                MatMulResult result = getSafe(future);
                Point point = result.getPoint();
                FloatMatrix2D block = result.getMatrix();
                int rb = point.r;
                int cb = point.c;
                MutableFloatMatrix2D Crc =
                    createBlockSubMatrix(C, rb, cb, rb+1, cb+1);
                Matrices.add(Crc, Crc, block);
            }
        }
    }
    
    

    /**
     * Creates a list of tasks for computing the products of the 
     * sub-block-matrices of the given matrices. 
     * 
     * @param A The first matrix
     * @param B The second matrix
     * @param p The current number of the outer product
     * @return The list of tasks
     */
    private List<MatMulTask> createTasks(
        final FloatMatrix2D A, final FloatMatrix2D B, int p)
    {
        final int numRowBlocks = divCeil(A.getNumRows(), blockSize);
        final int numColumnBlocks = divCeil(B.getNumColumns(), blockSize);
        final int numOuterProducts = divCeil(A.getNumColumns(), blockSize);
        
        List<MatMulTask> tasks = new ArrayList<MatMulTask>();
        for (int rb=0; rb<numRowBlocks; rb++)
        {
            for (int cb=0; cb<numColumnBlocks; cb++)
            {
                MatMulTask task =
                    createTask(A, B, p, rb, cb, numOuterProducts);
                tasks.add(task);
            }
        }
        return tasks;
    }
    
    /**
     * Create a tasks for computing the product of the specified 
     * sub-block-matrices of the given matrices. That is, this method 
     * returns a task that is the computation of 
     * <code>Arp * Bpc</code> for a given <code>r<code>, <code>p<code>
     * and <code>c<code>, with <code>r<code> being the
     * {@link MatMulTask#getRowBlockIndex() row block index} of 
     * the task, and <code>c<code> being the
     * {@link MatMulTask#getColumnBlockIndex() row block index} of the task.
     * The result of this task will be one element of the sum that is 
     * computed for the blocks of the result matrix:  
     * <code><pre>
     * Crc = 0;
     * for (p = 0 to numOuterProducts)
     * {
     *     Crc += Arp * Bpc;
     * }
     * </pre></code>
     * 
     * @param A The first matrix
     * @param B The second matrix
     * @param p The current number of the outer product
     * @param rb The current row block index
     * @param cb The current column block index
     * @param numOuterProducts The number of outer products
     * @return The task for computing the product
     */
    private MatMulTask createTask(
        final FloatMatrix2D A, final FloatMatrix2D B,
        final int p, final int rb, final int cb,
        final int numOuterProducts)
    {
        // These are the block-row of A and the block-column
        // of B that are transferred to the processors in the
        // grid in the SUMMA matrix multiplication algorithm
        FloatMatrix2D Ar_ = 
            createBlockSubMatrix(A, rb, 0, rb+1, numOuterProducts);
        FloatMatrix2D B_c = 
            createBlockSubMatrix(B, 0, cb, numOuterProducts, cb+1);

        // These are the blocks that are actually multiplied.
        // These could be created directly from the input 
        // matrices, but the Ar_ and B_c matrices are created
        // as a tribute to SUMMA.
        FloatMatrix2D Arp = 
            createBlockSubMatrix(Ar_, 0, p, 1, p+1);
        FloatMatrix2D Bpc = 
            createBlockSubMatrix(B_c, p, 0, p+1, 1);
        
        final boolean debugPrint = false;
        //debugPrint = true;
        if (debugPrint)
        {
            System.out.println("For outer product "+p);
            System.out.println("     at row block "+rb);
            System.out.println("    and col block "+cb);
    
            System.out.println("Block row Ar_ is "+debugString(Ar_));
            System.out.println("Block col B_c is "+debugString(B_c));
            
            System.out.println("For outer product "+p);
            System.out.println("Multiply Arp "+debugString(Arp));
            System.out.println("and      Bpc "+debugString(Bpc));
        }
        
        return new MatMulTask(rb, cb, Arp, Bpc, subMatrixMultiplicatorFactory);
    }
    
    /**
     * Creates a sub-matrix of the given matrix from the given block
     * coordinates, using the current {@link #blockSize} 
     *  
     * @param m The input matrix
     * @param rb0 The start row block of the sub-matrix, inclusive
     * @param cb0 The start column block of the sub-matrix, inclusive
     * @param rb1 The end row block of the sub-matrix, exclusive
     * @param cb1 The end column block of the sub-matrix, exclusive
     * @return The sub-matrix
     */
    private FloatMatrix2D createBlockSubMatrix(FloatMatrix2D m, 
        int rb0, int cb0, int rb1, int cb1)
    {
        return Matrices.createSubMatrixClamping(
            m, rb0*blockSize, cb0*blockSize, rb1*blockSize, cb1*blockSize);
    }

    /**
     * Creates a sub-matrix of the given matrix from the given block
     * coordinates, using the current {@link #blockSize} 
     *  
     * @param m The input matrix
     * @param rb0 The start row block of the sub-matrix, inclusive
     * @param cb0 The start column block of the sub-matrix, inclusive
     * @param rb1 The end row block of the sub-matrix, exclusive
     * @param cb1 The end column block of the sub-matrix, exclusive
     * @return The sub-matrix
     */
    private MutableFloatMatrix2D createBlockSubMatrix(MutableFloatMatrix2D m, 
        int rb0, int cb0, int rb1, int cb1)
    {
        return Matrices.createSubMatrixClamping(
            m, rb0*blockSize, cb0*blockSize, rb1*blockSize, cb1*blockSize);
    }

    
    /**
     * Returns the contents of the given future, handling the possible
     * exceptions by either interrupting the calling thread or
     * re-throwing them as RuntimeExceptions
     * 
     * @param <T> The type of the future
     * @param future The future
     * @return The contents of the future
     */
    private static <T> T getSafe(Future<T> future)
    {
        try
        {
            return future.get();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return null;
        }
        catch (ExecutionException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                RuntimeException runtimeException = (RuntimeException) cause;
                throw runtimeException;
            }
            throw new RuntimeException(
                "Unexpected checked exception", e);
        }
    }
    
    /**
     * Returns ceil(a/b)
     * 
     * @param a The dividend
     * @param b The divisor
     * @return The result
     */
    private static int divCeil(int a, int b)
    {
        return (a + b - 1) / b;
    }
    
    
    @Override
    public String toString()
    {
        return getClass().getSimpleName()+"["+subMatrixMultiplicatorFactory+"]";
    }
    
    /**
     * For debugging only
     * @param m Do not use
     * @return Do not use
     */
    private static String debugString(FloatMatrix2D m)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Size "+m.getNumRows()+"x"+m.getNumColumns()+"\n");
        sb.append(Matrices.toString(m));
        return sb.toString();
    }
    
}
