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

import java.util.Locale;
import java.util.Random;

/**
 * Utility methods related to matrices. Hence the name...
 * <br />
 * <br />
 * None of these methods perform any sanity checks. It is 
 * up to the caller to make sure that the matrices are non-<code>null</code>
 * and have appropriate sizes for the respective operations.
 */
public final class Matrices
{
	/**
	 * Random number generator for creating or filling matrices
	 * with random values
	 */
    private static final Random random = new Random(0);
    
    /**
     * Creates a {@link MutableFloatMatrix2D} with the given number
     * of rows and columns. Although it might be considered as an
     * implementation detail: The returned matrix will store its
     * data in column-major order. Thus, the 
     * {@link FloatMatrix2D#getDataColumnMajor(java.nio.FloatBuffer)}
     * will work very efficiently.
     * 
     * @param numRows The number of rows
     * @param numColumns The number of columns
     * @return The new matrix
     */
    public static MutableFloatMatrix2D createFloatMatrix2D(
    	int numRows, int numColumns)
    {
        return new ColumnMajor1DArrayFloatMatrix2D(numRows, numColumns);
    }

    /**
     * Fill the given matrix with the given value. That is, set each
     * element of the matrix to have the given value.
     * 
     * @param matrix The matrix
     * @param value The value
     */
    public static void fill(MutableFloatMatrix2D matrix, float value)
    {
        for (int c=0; c<matrix.getNumColumns(); c++)
        {
            for (int r=0; r<matrix.getNumRows(); r++)
            {
                matrix.set(r, c, value);
            }
        }
    }

    /**
     * Fill the given matrix with random values between 0.0f and 1.0f.
     * 
     * @param matrix The matrix
     */
    public static void fillRandom(MutableFloatMatrix2D matrix)
    {
        fillRandom(matrix, random, 0.0f, 1.0f);
    }

    /**
     * Fill the given matrix with random values in the given range,
     * using the given random number generator.
     * 
     * @param matrix The matrix
     * @param random The random number generator
     * @param min The minimum value
     * @param max The maximum value
     */
    public static void fillRandom(MutableFloatMatrix2D matrix, 
        Random random, float min, float max)
    {
        for (int c=0; c<matrix.getNumColumns(); c++)
        {
            for (int r=0; r<matrix.getNumRows(); r++)
            {
                float value = min + random.nextFloat() * (max-min);
                matrix.set(r, c, value);
            }
        }
    }

    /**
     * Fill the given matrix with contiguous values from 0.0 to 
     * (matrix.getNumRows()*matrix.getNumColumns()), exclusive
     * 
     * @param matrix The matrix to fill
     */
    public static void fillContiguous(MutableFloatMatrix2D matrix)
    {
        int n = 0;
        for (int c=0; c<matrix.getNumColumns(); c++)
        {
            for (int r=0; r<matrix.getNumRows(); r++)
            {
                matrix.set(r, c, n);
                n++;
            }
        }
    }
    
    /**
     * Returns whether the given matrices are epsilon-equal.
     * That is, whether they have the same size, and all values
     * are equal up to the given epsilon
     * 
     * @param m0 The first matrix
     * @param m1 The second matrix
     * @param epsilon The epsilon
     * @return Whether the matrices are epsilon-equal
     */
    public static boolean equal(
        FloatMatrix2D m0, FloatMatrix2D m1, float epsilon)
    {
        if (m0.getNumRows() != m1.getNumRows())
        {
            return false;
        }
        if (m0.getNumColumns() != m1.getNumColumns())
        {
            return false;
        }
        for (int c=0; c<m0.getNumColumns(); c++)
        {
            for (int r=0; r<m1.getNumRows(); r++)
            {
                float v0 = m0.get(r, c);
                float v1 = m1.get(r, c);
                if (!equal(v0, v1, epsilon))
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Returns whether the given values are equal up to the 
     * given epsilon
     * 
     * @param x The first value
     * @param y The second value
     * @param epsilon The epsilon
     * @return Returns whether the given values are epsilon-equal
     */
    private static boolean equal(float x, float y, float epsilon)
    {
      return Math.abs(x - y) <= epsilon * Math.abs(x);
    }
    
    /**
     * Creates a sub-matrix of the given matrix, clamping the specified
     * rectangle if necessary to be in a valid range.
     *  
     * @param m The input matrix
     * @param r0 The start row of the sub-matrix, inclusive
     * @param c0 The start column of the sub-matrix, inclusive
     * @param r1 The end row of the sub-matrix, exclusive
     * @param c1 The end column of the sub-matrix, exclusive
     * @return The sub-matrix
     */
    static FloatMatrix2D createSubMatrixClamping(
        FloatMatrix2D m, int r0, int c0, int r1, int c1)
    {
        return m.subMatrix(
            Math.max(0, r0), 
            Math.max(0, c0), 
            Math.min(m.getNumRows(), r1), 
            Math.min(m.getNumColumns(), c1));
    }
    
    /**
     * Creates a sub-matrix of the given matrix, clamping the specified
     * rectangle if necessary to be in a valid range.
     *  
     * @param m The input matrix
     * @param r0 The start row of the sub-matrix, inclusive
     * @param c0 The start column of the sub-matrix, inclusive
     * @param r1 The end row of the sub-matrix, exclusive
     * @param c1 The end column of the sub-matrix, exclusive
     * @return The sub-matrix
     */
    static MutableFloatMatrix2D createSubMatrixClamping(
        MutableFloatMatrix2D m, int r0, int c0, int r1, int c1)
    {
        return m.subMatrix(
            Math.max(0, r0), 
            Math.max(0, c0), 
            Math.min(m.getNumRows(), r1), 
            Math.min(m.getNumColumns(), c1));
    }

    

    /**
     * Add the given matrices, and return the result as a new matrix
     * 
     * @param m0 The first matrix
     * @param m1 The second matrix
     * @return The result
     */
    public static MutableFloatMatrix2D add(
        FloatMatrix2D m0, FloatMatrix2D m1)
    {
        MutableFloatMatrix2D result = 
        	createFloatMatrix2D(m0.getNumRows(), m0.getNumColumns());
        add(result, m0, m1);
        return result;
    }
    
    /**
     * Add the given matrices, and store the result in the given matrix
     * 
     * @param result The result
     * @param m0 The first matrix
     * @param m1 The second matrix
     */
    public static void add(
        MutableFloatMatrix2D result, FloatMatrix2D m0, FloatMatrix2D m1)
    {
        for (int c=0; c<result.getNumColumns(); c++)
        {
            for (int r=0; r<result.getNumRows(); r++)
            {
                result.set(r, c, m0.get(r, c)+m1.get(r, c));
            }
        }
    }
    
    /**
     * Subtract the given matrices, and return the result as a new matrix
     * 
     * @param m0 The first matrix
     * @param m1 The second matrix
     * @return The result
     */
    public static MutableFloatMatrix2D subtract(
        FloatMatrix2D m0, FloatMatrix2D m1)
    {
        MutableFloatMatrix2D result = 
        	createFloatMatrix2D(m0.getNumRows(), m0.getNumColumns());
        subtract(result, m0, m1);
        return result;
    }
 
    /**
     * Subtract the given matrices, and store the result in the given matrix
     * 
     * @param result The result
     * @param m0 The first matrix
     * @param m1 The second matrix
     */
    public static void subtract(
        MutableFloatMatrix2D result, FloatMatrix2D m0, FloatMatrix2D m1)
    {
        for (int c=0; c<result.getNumColumns(); c++)
        {
            for (int r=0; r<result.getNumRows(); r++)
            {
                result.set(r, c, m0.get(r, c)-m1.get(r, c));
            }
        }
    }
 
    /**
     * Multiply the given matrices, and store the result in the given matrix
     * 
     * @param m0 The first matrix
     * @param m1 The second matrix
     * @return The result
     */
    public static MutableFloatMatrix2D multiply(
        FloatMatrix2D m0, FloatMatrix2D m1)
    {
    	MutableFloatMatrix2D result = 
    		Matrices.createFloatMatrix2D(m0.getNumRows(), m1.getNumColumns());
    	multiply(result, m0, m1);
    	return result;
    }
    
    /**
     * Multiply the given matrices, and store the result in the given matrix
     * 
     * @param result The result
     * @param m0 The first matrix
     * @param m1 The second matrix
     */
    public static void multiply(
        MutableFloatMatrix2D result, FloatMatrix2D m0, FloatMatrix2D m1)
    {
        multiplySimple(result, m0, m1);
        //multiplyOuterProduct(result, m0, m1);
    }
    
    /**
     * Multiply the given matrices, and store the result in the given matrix.
     * 
     * Straightforward implementation.
     * 
     * @param result The result
     * @param m0 The first matrix
     * @param m1 The second matrix
     */
    private static void multiplySimple(
        MutableFloatMatrix2D result, FloatMatrix2D m0, FloatMatrix2D m1)
    {
        int r0 = m0.getNumRows();
        int c0 = m0.getNumColumns();
        int c1 = m1.getNumColumns();
        for (int r=0; r<r0; r++)
        {
            for (int c=0; c<c1; c++)
            {
                float sum = 0;
                for (int n=0; n<c0; n++)
                {
                    sum += m0.get(r,n) * m1.get(n, c);
                }
                result.set(r, c, sum);
            }
        }
    }

    /**
     * Multiply the given matrices, and store the result in the given matrix.
     * 
     * "Outer-product-like" implementation.
     * 
     * @param result The result
     * @param m0 The first matrix
     * @param m1 The second matrix
     */
    /*
    private static void multiplyOuterProduct(
        MutableFloatMatrix2D result, FloatMatrix2D m0, FloatMatrix2D m1)
    {
        int r0 = m0.getNumRows();
        int c0 = m0.getNumColumns();
        int c1 = m1.getNumColumns();
        for (int r=0; r<r0; r++)
        {
            for (int c=0; c<c1; c++)
            {
                result.set(r, c, 0);
            }
        }
        
        for (int n=0; n<c0; n++)
        {
            for (int r=0; r<r0; r++)
            {
                for (int c=0; c<c1; c++)
                {
                    result.set(r, c, result.get(r, c) + m0.get(r,n) * m1.get(n, c));
                }
            }
        }
    }
    */

    /**
     * Returns a formatted, multi-line String representation
     * of the given matrix, using a default locale and number
     * format.
     * 
     * @param matrix The matrix
     * @return The String representation of the given matrix
     */
    public static String toString(FloatMatrix2D matrix)
    {
        return toString(matrix, Locale.ENGLISH, "%6.2f");
    }
    
    /**
     * Returns a formatted, multi-line String representation
     * of the given matrix using the given locale and number
     * format
     * 
     * @param matrix The matrix
     * @param locale The locale
     * @param format The number format string
     * @return The String representation of the given matrix
     */
    public static String toString(
        FloatMatrix2D matrix, Locale locale, String format)
    {
        StringBuilder sb = new StringBuilder();
        for (int r=0; r<matrix.getNumRows(); r++)
        {
            for (int c=0; c<matrix.getNumColumns(); c++)
            {
                float value = matrix.get(r,c);
                sb.append(String.format(locale, format, value)+" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private Matrices()
    {
    	// Private constructor to prevent instantiation
    }
    
    
}
