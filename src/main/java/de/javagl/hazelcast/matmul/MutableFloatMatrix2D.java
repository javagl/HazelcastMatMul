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
 * Interface for a {@link FloatMatrix2D} that may be modified.
 */
public interface MutableFloatMatrix2D extends FloatMatrix2D
{
	/**
	 * Set the given value at the given position in this matrix
	 * 
	 * @param r The row
	 * @param c The column
	 * @param value The value to set
	 */
    void set(int r, int c, float value);
    
    /**
     * Set the values from the given FloatBuffer in this matrix,
     * in column-major order. The position of the buffer will be
     * updated accordingly.
     * <br />
     * <br />
     * This method is mainly intended for serialization purposes,
     * and for maximum efficiency, should only be called on
     * a matrix that uses some sort of column-major storage. 
     * 
     * @param values The buffer containing the values to set.
     */
    void setDataColumnMajor(FloatBuffer values);

    /**
     * {@inheritDoc}
     * 
     * This method returns a mutable view on the specified
     * sub-matrix. Changes in the returned matrix will also
     * be visible in this matrix.
     */
    @Override
    MutableFloatMatrix2D subMatrix(int r0, int c0, int r1, int c1);
}
