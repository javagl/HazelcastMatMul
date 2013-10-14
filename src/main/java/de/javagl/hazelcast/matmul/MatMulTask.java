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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A task describing the multiplication of two {@link FloatMatrix2D} objects.
 * Particularly, the multiplication of two block matrices that contribute to
 * the result of a larger matrix multiplication.
 */
public final class MatMulTask implements Callable<MatMulResult> 
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(MatMulTask.class.getName());
    
    /**
     * The row block index of the result matrix
     */
    private final int rowBlockIndex;
    
    /**
     * The column block index of the result matrix
     */
    private final int columnBlockIndex;
    
    /**
     * The first factor
     */
    private final FloatMatrix2D m0;

    /**
     * The second factor
     */
    private final FloatMatrix2D m1;
    
    /**
     * The {@link Factory} that will be used to create the 
     * {@link MatrixMultiplicator} for the multiplication
     * of the matrices. 
     */
    private final Factory<MatrixMultiplicator> matrixMultiplicatorFactory;

    /**
     * Creates a new matrix multiplication task
     * 
     * @param rowBlockIndex The row block index for the result matrix
     * @param columnBlockIndex The column block index for the result matrix
     * @param m0 The first factor
     * @param m1 The second factor
     * @param matrixMultiplicatorFactory The {@link Factory} that will be 
     * used to create the {@link MatrixMultiplicator} for the multiplication
     * of the matrices. If this argument is <code>null</code>, a default,
     * single-threaded multiplicator will be used.
     */
    public MatMulTask(int rowBlockIndex, int columnBlockIndex,
        FloatMatrix2D m0, FloatMatrix2D m1,
        Factory<MatrixMultiplicator> matrixMultiplicatorFactory) 
    {
        this.rowBlockIndex = rowBlockIndex;
        this.columnBlockIndex = columnBlockIndex;
        this.m0 = m0;
        this.m1 = m1;
        if (matrixMultiplicatorFactory == null)
        {
            this.matrixMultiplicatorFactory = 
                MatrixMultiplicators.createSimpleFactory();
        }
        else
        {
            this.matrixMultiplicatorFactory = matrixMultiplicatorFactory;
        }
    }
    
    /**
     * Returns the row block index of the result
     * 
     * @return The row block index of the result
     */
    public int getRowBlockIndex()
    {
        return rowBlockIndex;
    }
    
    /**
     * Returns the column block index of the result
     * 
     * @return The column block index of the result
     */
    public int getColumnBlockIndex()
    {
        return columnBlockIndex;
    }
    
    /**
     * Returns the first factor
     * 
     * @return The first factor
     */
    public FloatMatrix2D getM0()
    {
        return m0;
    }

    /**
     * Returns the second factor
     * 
     * @return The second factor
     */
    public FloatMatrix2D getM1()
    {
        return m1;
    }
    
    /**
     * Returns the {@link Factory} that will be used to create the 
     * {@link MatrixMultiplicator} for the multiplication of the 
     * matrices.
     *  
     * @return The factory
     */
    public Factory<MatrixMultiplicator> getMatrixMultiplicatorFactory()
    {
        return matrixMultiplicatorFactory;
    }

    @Override
    public MatMulResult call() 
    {
        MutableFloatMatrix2D result = 
            Matrices.createFloatMatrix2D(m0.getNumRows(), m1.getNumColumns());
        MatrixMultiplicator matrixMultiplicator =
            matrixMultiplicatorFactory.create();

        final Level level = Level.FINE;
        if (logger.isLoggable(level))
        {
            logger.log(level, "Multiplying blocks of size " +
                m0.getNumRows()+"x"+m0.getNumColumns()+" and "+
                m1.getNumRows()+"x"+m1.getNumColumns()+" and "+
                "for result with size "+
                result.getNumRows()+"x"+result.getNumColumns()+
                " at "+ rowBlockIndex+","+columnBlockIndex+
                " using "+matrixMultiplicator);
        }
        matrixMultiplicator.multiply(result, m0, m1);
        Point point = new Point(rowBlockIndex, columnBlockIndex);
        return new MatMulResult(point, result);
    }

}