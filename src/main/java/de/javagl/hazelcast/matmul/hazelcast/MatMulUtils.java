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

import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.nio.serialization.StreamSerializer;

import de.javagl.hazelcast.matmul.FloatMatrix2D;
import de.javagl.hazelcast.matmul.MatMulResult;
import de.javagl.hazelcast.matmul.MatMulTask;
import de.javagl.hazelcast.matmul.MutableFloatMatrix2D;

/**
 * Utility methods related to the Hazelcast matrix multiplication
 */
class MatMulUtils
{
    /**
     * Initialize the StreamSerializers for the matrix multiplication
     * 
     * @param serializationConfig The SerializationConfig
     */
    static void initSerializers(SerializationConfig serializationConfig)
    {
        MatMulUtils.addSerializer(serializationConfig, 
            MatMulTask.class, new MatMulTaskSerializer());
        MatMulUtils.addSerializer(serializationConfig, 
            FloatMatrix2D.class, new FloatMatrix2DSerializer());
        MatMulUtils.addSerializer(serializationConfig, 
            MutableFloatMatrix2D.class, new FloatMatrix2DSerializer());
        MatMulUtils.addSerializer(serializationConfig, 
            MatMulResult.class, new MatMulResultSerializer());
    }
    
    /**
     * Add the specified SerializerConfig to the given SerializationConfig
     * 
     * @param <T> The type of the serialized objects
     * @param serializationConfig The SerializationConfig
     * @param typeClass The type class
     * @param streamSerializer The StreamSerializer
     */
    static <T> void addSerializer(
        SerializationConfig serializationConfig, 
        Class<? extends T> typeClass, StreamSerializer<T> streamSerializer)
    {
        SerializerConfig serializerConfig = new SerializerConfig();
        serializerConfig.setTypeClass(typeClass);
        serializerConfig.setImplementation(streamSerializer);
        serializationConfig.addSerializerConfig(serializerConfig);    
    }

    /**
     * Private constructor to prevent instantiation
     */
    private MatMulUtils()
    {
        // Private constructor to prevent instantiation
    }

}
