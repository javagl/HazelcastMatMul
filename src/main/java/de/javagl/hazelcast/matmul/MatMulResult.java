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

/**
 * The result of a {@link MatMulTask}
 */
public final class MatMulResult  
{
    /**
     * The point describing the row and column block index of the result
     */
    private final Point point;
    
    /**
     * The result of the multiplication
     */
    private final MutableFloatMatrix2D matrix;
    
    /**
     * Creates a new matrix multiplication result
     * 
     * @param point The point describing the row and column block index
     * @param matrix The result of the multiplication
     */
    public MatMulResult(Point point, MutableFloatMatrix2D matrix)
    {
        this.point = point;
        this.matrix = matrix;
    }
    
    /**
     * Returns the point describing the row and column block index of the result
     * 
     * @return the point describing the row and column block index of the result
     */
    public Point getPoint()
    {
        return point;
    }
    
    /**
     * Returns the result of the multiplication
     * 
     * @return The result of the multiplication
     */
    public MutableFloatMatrix2D getMatrix()
    {
        return matrix;
    }
}
