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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import de.javagl.hazelcast.matmul.Factory;
import de.javagl.hazelcast.matmul.MatMulResult;
import de.javagl.hazelcast.matmul.MatMulTask;
import de.javagl.hazelcast.matmul.Matrices;
import de.javagl.hazelcast.matmul.MatrixMultiplicator;
import de.javagl.hazelcast.matmul.MatrixMultiplicators;
import de.javagl.hazelcast.matmul.MutableFloatMatrix2D;
import de.javagl.hazelcast.matmul.util.LoggerUtil;

/**
 * A Hazelcast matrix multiplication client. Will perform a matrix
 * multiplication A * B = C once locally and once in a Hazelcast
 * cluster, and compare the results. 
 */
public class MatMulClient
{
    /**
     * The logger used in this class
     */
    private static final Logger logger = 
        Logger.getLogger(MatMulClient.class.getName());
    
    /**
     * The number of rows in matrix A
     */
    private static int rowsA = 1000;

    /**
     * The number of columns in matrix A 
     * (also the number of rows in matrix B)
     */
    private static int columnsA = 1000;
    
    /**
     * The number of columns in matrix B
     */
    private static int columnsB = 1000;
    
    /**
     * The size of the sub-matrices that will be dispatched
     * to the cluster nodes.
     */
    private static int clusterNodeBlockSize = 500;
    
    /**
     * The size of the sub-matrices that will be dispatched
     * to a thread pool executor on each cluster node 
     * (and in the local execution) 
     */
    private static int nodeProcessorBlockSize = 50;
    
    /**
     * The comma-separated list of server addresses
     */
    private static String serverURLs = "127.0.0.1:5701";
    
    /**
     * The number of benchmark steps
     */
    private static int benchmarkSteps = 0;
    
    /**
     * The step size for benchmark runs. The matrix sizes will be 
     * increased by this amount, {@link #benchmarkSteps} times
     */
    private static int benchmarkStepSize = 1000;
    
    /**
     * The number of benchmark runs for each matrix size
     */
    private static int benchmarkRuns = 3;
    
    /**
     * The HazelcastInstance for this client
     */
    private static HazelcastInstance hazelcastInstance; 
    
    /**
     * Start the client
     * 
     * @param args When no arguments are given, then default settings
     * will be used. Otherwise, it is assumed that the first argument
     * is the name of a properties file with the settings for the
     * test run.
     */
    public static void main(String[] args)
    {
        Logger logger = Logger.getLogger("");
        LoggerUtil.configureDefault(logger);
        
        String propertiesFileName = "MatMulClient.properties";
        if (args.length != 0)
        {
            propertiesFileName = args[0];
        }
        readProperties(propertiesFileName);
        createHazeclastInstance();
        
        //runBasicTest(hazelcastInstance.getExecutorService(
        //    "matMulExecutorService"));
        
        MatrixMultiplicator multiplicator0 = 
            MatrixMultiplicators.createParallelDefault(
                nodeProcessorBlockSize);
        MatrixMultiplicator multiplicator1 =
            createHazelcastMatrixMultiplicator();
        
        int maxRowsA = rowsA + benchmarkSteps * benchmarkStepSize;
        int maxColumnsA = columnsA + benchmarkSteps * benchmarkStepSize;
        int maxColumnsB = columnsB + benchmarkSteps * benchmarkStepSize;
        MatMulTests.runBenchmark(
            rowsA, maxRowsA,  benchmarkStepSize, 
            columnsA, maxColumnsA, benchmarkStepSize, 
            columnsB, maxColumnsB, benchmarkStepSize,
            benchmarkRuns,
            multiplicator0, multiplicator1);

        hazelcastInstance.getLifecycleService().shutdown();
    }
    
    
    /**
     * Create a {@link MatrixMultiplicator} using Hazelcast
     * 
     * @return The {@link MatrixMultiplicator}
     */
    private static MatrixMultiplicator createHazelcastMatrixMultiplicator()
    {
        Factory<ExecutorService> executorServiceFactory = 
            new Factory<ExecutorService>()
        {
            @Override
            public ExecutorService create()
            {
                IExecutorService executorService = 
                    hazelcastInstance.getExecutorService(
                        "matMulExecutorService");
                return executorService;
            }
            
            @Override
            public String toString()
            {
                return "hazelcastExecutorServiceFactory";
            }
        };
        
        // Creates a MatrixMultiplicator that will dispatch MatMulTask objects 
        // to the executor service. 
        Factory<MatrixMultiplicator> subMatrixMultiplicatorFactory =
            MatrixMultiplicators.createParallelFactory(nodeProcessorBlockSize);
        MatrixMultiplicator multiplicator = 
            MatrixMultiplicators.createParallel(
                executorServiceFactory, clusterNodeBlockSize,
                subMatrixMultiplicatorFactory);
        return multiplicator;
    }
    
    
    /**
     * Creates the Hazelcast Client instance
     */
    public static void createHazeclastInstance()
    {
        ClientConfig clientConfig = new ClientConfig();
        MatMulUtils.initSerializers(clientConfig.getSerializationConfig());
        String urls[] = serverURLs.split(",");
        
        logger.info("Server URLs: "+Arrays.toString(urls));
        
        clientConfig.addAddress(urls);
        hazelcastInstance = 
            HazelcastClient.newHazelcastClient(clientConfig);
    }
    
    /**
     * Read the configuration for this client, namely the fields, from
     * a properties file with the given name.
     * 
     * @param fileName The name of the properties file
     */
    private static void readProperties(String fileName)
    {
        logger.info("Reading properties file '"+fileName+"'");

        Properties properties = new Properties();
        InputStream inputStream = null;
        try 
        {
            inputStream = new FileInputStream(fileName);
            properties.load(inputStream);
        } 
        catch (IOException e) 
        {
            logger.severe(
                "Could not read properties file '"+fileName+"'. " +
                "Using defaults");
            logger.log(Level.SEVERE, e.getMessage(), e);
            return;
        }    
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    logger.warning("Could not close stream");
                    e.printStackTrace();
                }
            }
        }
        rowsA = parseInt(properties, "rowsA", rowsA);
        columnsA = parseInt(properties, "columnsA", columnsA);
        columnsB = parseInt(properties, "columnsB", columnsB);
        clusterNodeBlockSize = parseInt(
            properties, "clusterNodeBlockSize", clusterNodeBlockSize);
        nodeProcessorBlockSize = 
            parseInt(
                properties, "nodeProcessorBlockSize", nodeProcessorBlockSize);
        serverURLs = properties.getProperty("serverURLs", serverURLs);
        benchmarkSteps = parseInt(
            properties, "benchmarkSteps", benchmarkSteps);
        benchmarkStepSize = parseInt(
            properties, "benchmarkStepSize", benchmarkStepSize);
        benchmarkRuns = parseInt(
            properties, "benchmarkRuns", benchmarkRuns);

    }

    /**
     * Parse an integer value from the specified properties, returning 
     * the given default value if no value could be parsed.
     * 
     * @param properties The properties
     * @param name The property name
     * @param defaultValue The default value
     * @return The parsed integer value
     */
    private static int parseInt(
        Properties properties, String name, int defaultValue)
    {
        return parseInt(name, properties.getProperty(name), defaultValue);
    }
    
    /**
     * Parse an integer value from the given string, returning the given
     * default value if no value could be parsed.
     * 
     * @param name The name of the field
     * @param string The string containing the integer value
     * @param defaultValue The default value
     * @return The parsed integer value
     */
    private static int parseInt(String name, String string, int defaultValue)
    {
        try
        {
            int result = Integer.parseInt(string);
            logger.info(name+"="+result);
            return result;
        }
        catch (NumberFormatException e)
        {
            logger.warning(
                "Invalid value for "+name+": "+string+". " +
                "Using default ("+defaultValue+")");
            return defaultValue;
        }
    }
    


    /**
     * Performs a single, simple matrix multiplication with the given
     * ExecutorService
     *  
     * @param executorService The ExecutorService
     */
    private static void runBasicTest(ExecutorService executorService)
    {
        MutableFloatMatrix2D m0 = Matrices.createFloatMatrix2D(2, 3);
        MutableFloatMatrix2D m1 = Matrices.createFloatMatrix2D(3, 4);
        
        Matrices.fillRandom(m0);
        Matrices.fillRandom(m1);
        
        Future<MatMulResult> future = 
            executorService.submit(new MatMulTask(0, 0, m0, m1, null));
        MatMulResult result;
        try
        {
            result = future.get();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return;
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException("Error", e);
        }
        System.out.println("Result:\n"+Matrices.toString(result.getMatrix()));
    }
}
