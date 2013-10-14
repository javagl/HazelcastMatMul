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

/**
 * A point. Usually referring to an entry in a matrix.
 */
public final class Point implements Serializable
{
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 2871433544817171172L;

	/**
	 * The row of this point
	 */
	public final int r;

	/**
	 * The column of this point
	 */
	public final int c;
	
	/**
	 * Creates a new point
	 * 
	 * @param r The row of this point
	 * @param c The column of this point
	 */
	public Point(int r, int c)
	{
		this.r = r;
		this.c = c;
	}
	
	@Override
	public String toString() 
	{
		return "("+r+","+c+")";
	}

	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + c;
		result = prime * result + r;
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (c != other.c)
			return false;
		if (r != other.r)
			return false;
		return true;
	}
	
	
}
