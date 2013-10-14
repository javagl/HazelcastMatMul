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

import java.nio.FloatBuffer;

/**
 * Interface describing a 2D matrix with float values.
 * 
 * Note that this interface does not specify any validity
 * checks. The results of passing invalid row- or column
 * indices to any method are unspecified.  
 */
public interface FloatMatrix2D
{
	/**
	 * Returns the number of rows in this matrix
	 * 
	 * @return The number of rows in this matrix
	 */
    int getNumRows();
    
    /**
     * Returns the number of columns in this matrix
     * 
     * @return The number of columns in this matrix
     */
    int getNumColumns();
    
    /**
     * Returns the value at the specified position in this matrix
     * 
     * @param r The row
     * @param c The column
     * @return The value at the specified position.
     */
    float get(int r, int c);

    /**
     * Returns a sub-matrix that is a <i>view</i> on the
     * specified range of this matrix. That is, changes
     * in this matrix will be visible in the returned 
     * matrix.
     *  
     * @param r0 The start row of the sub-matrix, inclusive
     * @param c0 The start column of the sub-matrix, inclusive
     * @param r1 The end row of the sub-matrix, exclusive
     * @param c1 The end column of the sub-matrix, exclusive
     * @return The sub-matrix
     */
    FloatMatrix2D subMatrix(int r0, int c0, int r1, int c1);
    
    /**
     * Writes the data of this matrix into the given FloatBuffer.
     * The data will be written in column-major order, and the
     * position of the FloatBuffer will be updated accordingly.
     * <br />
     * <br />
     * This method is mainly intended for serialization purposes,
     * and for maximum efficiency, should only be called on
     * a matrix that uses some sort of column-major storage. 
     * 
     * @param values The buffer that will receive the values
     */
    void getDataColumnMajor(FloatBuffer values);
}
