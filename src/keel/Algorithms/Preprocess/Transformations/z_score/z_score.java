/***********************************************************************

	This file is part of KEEL-software, the Data Mining tool for regression, 
	classification, clustering, pattern mining and so on.

	Copyright (C) 2004-2010
	
	F. Herrera (herrera@decsai.ugr.es)
    L. Sánchez (luciano@uniovi.es)
    J. Alcalá-Fdez (jalcala@decsai.ugr.es)
    S. García (sglopez@ujaen.es)
    A. Fernández (alberto.fernandez@ujaen.es)
    J. Luengo (julianlm@decsai.ugr.es)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see http://www.gnu.org/licenses/
  
**********************************************************************/

/**
 * <p>
 * @author Written by Julián Luengo Martín 10/11/2005
 * @version 0.2
 * @since JDK 1.5
 * </p>
 */
package keel.Algorithms.Preprocess.Transformations.z_score;
import java.io.*;
import java.util.*;
import keel.Dataset.*;
import keel.Algorithms.Preprocess.Basic.*;

/**
 * <p>
 * This class performs the z-score transformation. The data is transformed so their
 * distribution now follows a normal distribution.
 * </p>
 */
public class z_score {
    
    double [] mean = null;
    double [] std_dev = null;
    double tempData = 0;
    double new_min,new_max;
    String[][] X = null; //matrix of transformed data
    
    int ndatos = 0;
    int nentradas = 0;
    int tipo = 0;
    int direccion = 0;
    int nvariables = 0;
    int nsalidas = 0;
    
    InstanceSet IS;
    String input_train_name = new String();
    String input_test_name = new String();
    String output_train_name = new String();
    String output_test_name = new String();
    String temp = new String();
    String data_out = new String("");
    
    /** Creates a new instance of z_score 
     * @param fileParam The path to the configuration file with all the parameters in KEEL format
     */
    public z_score(String fileParam) {
        config_read(fileParam);
        IS = new InstanceSet();
    }
    
    /**
     * <p>
     * Process the training and test files provided in the parameters file to the constructor.
     * </p>
     */
    public void normalize(){
        try {
            
            // Load in memory a dataset that contains a classification problem
            IS.readSet(input_train_name,true);
            int in = 0;
            int out = 0;
            
            ndatos = IS.getNumInstances();
            nvariables = Attributes.getNumAttributes();
            nentradas = Attributes.getInputNumAttributes();
            nsalidas = Attributes.getOutputNumAttributes();
            
            X = new String[ndatos][nvariables];//matrix with transformed data
            double [] sum = new double[nvariables];//vector with sums of each attribute
            double [] square_sum = new double[nvariables];
            mean = new double[nvariables];
            std_dev = new double[nvariables];
            int [] numAtt = new int[nvariables];
            //initialization of vectors
            for(int k=0;k<nvariables;k++){
                sum[k] = 0.0;
                square_sum[k] = 0.0;
                numAtt[k] = 0;
            }
            
            for(int i = 0;i < ndatos;i++){
                Instance inst = IS.getInstance(i);
                
                in = 0;
                out = 0;
                
                for(int j = 0; j < nvariables;j++){
                    Attribute a = Attributes.getAttribute(j);
                    
                    direccion = a.getDirectionAttribute();
                    tipo = a.getType();
                    
                    if(direccion == Attribute.INPUT){
                        if(tipo != Attribute.NOMINAL && !inst.getInputMissingValues(in)){//we want to normalize everything but nominal attributes
                            sum[j] += inst.getInputRealValues(in);
                            square_sum[j] += inst.getInputRealValues(in)*inst.getInputRealValues(in);
                            numAtt[j]++;
                            
                        } else{
                            if(!inst.getInputMissingValues(in))
                                X[i][j] = inst.getInputNominalValues(in); //points out its a nominal value, look at vector fila
                            else
                                X[i][j] = new String("?");
                        }
                        in++;
                    } else{
                        if(direccion == Attribute.OUTPUT){
                            if(tipo != Attribute.NOMINAL && !inst.getOutputMissingValues(out)){//we want to normalize everything but nominal attributes
                                sum[j] += inst.getOutputRealValues(out);
                                square_sum[j] += inst.getOutputRealValues(out);
                                numAtt[j]++;
                            } else{
                                if(!inst.getOutputMissingValues(out))
                                    X[i][j] = inst.getOutputNominalValues(out); //points out its a nominal value, look at vector fila
                                else
                                    X[i][j] = new String("?");
                            }
                            out++;
                        }
                    }
                }
            }
            
            for(int j=0;j<nvariables;j++){
                if(numAtt[j]!=0){ //it has numerical values
                    mean[j] = sum[j]/numAtt[j];
                    std_dev[j] = (double) square_sum[j]/numAtt[j] - (mean[j]*mean[j]);
                    std_dev[j] = Math.sqrt(std_dev[j]);
                }
            }
            
            for(int i = 0;i < ndatos;i++){
                Instance inst = IS.getInstance(i);
                
                in = 0;
                out = 0;
                
                for(int j = 0; j < nvariables;j++){
                    Attribute a = Attributes.getAttribute(j);
                    
                    direccion = a.getDirectionAttribute();
                    tipo = a.getType();
                    
                    if(direccion == Attribute.INPUT){
                        if(tipo != Attribute.NOMINAL && !inst.getInputMissingValues(in)){//we want to normalize everything but nominal attributes
                            if(std_dev[j]!=0)
                                tempData = (inst.getInputRealValues(in) - mean[j])/std_dev[j];
                            else
                                tempData = mean[j];
                            X[i][j] = new String(String.valueOf(tempData));
                        }
                        in++;
                    } else{
                        if(direccion == Attribute.OUTPUT){
                            if(tipo != Attribute.NOMINAL && !inst.getOutputMissingValues(out)){//we want to normalize everything but nominal attributes
                                if(std_dev[j]!=0)
                                    tempData = (inst.getOutputRealValues(out) - mean[j])/std_dev[j];
                                else
                                    tempData = mean[j];
                                X[i][j] = new String(String.valueOf(tempData));
                            }
                            out++;
                        }
                    }
                }
            }
            for(int j = 0; j < nvariables;j++){
                Attribute a = Attributes.getAttribute(j);
                
                if(std_dev[j]!=0){
                    new_min = (a.getMinAttribute() - mean[j])/std_dev[j];
                    new_max = (a.getMaxAttribute() - mean[j])/std_dev[j];
                } else
                    new_min = new_max = mean[j];
                tipo = a.getType();
                if(tipo != Attribute.NOMINAL){
                    a.setBounds(new_min,new_max);
                }
            }
        }catch (Exception e){
            System.out.println("Dataset exception = " + e );
            System.exit(-1);
        }
        write_results(output_train_name);
        /***************************************************************************************/
        //does a test file associated exist?
        if(input_train_name.compareTo(input_test_name)!=0){
            try {
                
            	//delete the modified attributes! the test set has the older
            	//bounds
            	Attributes.clearAll();
                // Load in memory a dataset that contains a classification problem
                IS.readSet(input_test_name,true);
                int in = 0;
                int out = 0;
                
                ndatos = IS.getNumInstances();
                nvariables = Attributes.getNumAttributes();
                nentradas = Attributes.getInputNumAttributes();
                nsalidas = Attributes.getOutputNumAttributes();
                
                X = new String[ndatos][nvariables];//matrix with transformed data
                double [] sum = new double[nvariables];//vector with sums of each attribute
                double [] square_sum = new double[nvariables];
                mean = new double[nvariables];
                std_dev = new double[nvariables];
                int [] numAtt = new int[nvariables];
                //initialization of vectors
                for(int k=0;k<nvariables;k++){
                    sum[k] = 0.0;
                    square_sum[k] = 0.0;
                    numAtt[k] = 0;
                }
                
                for(int i = 0;i < ndatos;i++){
                    Instance inst = IS.getInstance(i);
                    
                    in = 0;
                    out = 0;
                    
                    for(int j = 0; j < nvariables;j++){
                        Attribute a = Attributes.getAttribute(j);
                        
                        direccion = a.getDirectionAttribute();
                        tipo = a.getType();
                        
                        if(direccion == Attribute.INPUT){
                            if(tipo != Attribute.NOMINAL && !inst.getInputMissingValues(in)){//we want to normalize everything but nominal attributes
                                sum[j] += inst.getInputRealValues(in);
                                square_sum[j] += inst.getInputRealValues(in)*inst.getInputRealValues(in);
                                numAtt[j]++;
                                
                            } else{
                                if(!inst.getInputMissingValues(in))
                                    X[i][j] = inst.getInputNominalValues(in); //points out its a nominal value, look at vector fila
                                else
                                    X[i][j] = new String("?");
                            }
                            in++;
                        } else{
                            if(direccion == Attribute.OUTPUT){
                                if(tipo != Attribute.NOMINAL && !inst.getOutputMissingValues(out)){//we want to normalize everything but nominal attributes
                                    sum[j] += inst.getOutputRealValues(out);
                                    square_sum[j] += inst.getOutputRealValues(out);
                                    numAtt[j]++;
                                } else{
                                    if(!inst.getOutputMissingValues(out))
                                        X[i][j] = inst.getOutputNominalValues(out); //points out its a nominal value, look at vector fila
                                    else
                                        X[i][j] = new String("?");
                                }
                                out++;
                            }
                        }
                    }
                }
                
                for(int j=0;j<nvariables;j++){
                    if(numAtt[j]!=0){ //it has numerical values
                        mean[j] = sum[j]/numAtt[j];
                        std_dev[j] = (double) square_sum[j]/numAtt[j] - (mean[j]*mean[j]);
                        std_dev[j] = Math.sqrt(std_dev[j]);
                    }
                }
                
                for(int i = 0;i < ndatos;i++){
                    Instance inst = IS.getInstance(i);
                    
                    in = 0;
                    out = 0;
                    
                    for(int j = 0; j < nvariables;j++){
                        Attribute a = Attributes.getAttribute(j);
                        
                        direccion = a.getDirectionAttribute();
                        tipo = a.getType();
                        
                        if(direccion == Attribute.INPUT){
                            if(tipo != Attribute.NOMINAL && !inst.getInputMissingValues(in)){//we want to normalize everything but nominal attributes
                                if(std_dev[j]!=0)
                                    tempData = (inst.getInputRealValues(in) - mean[j])/std_dev[j];
                                else
                                    tempData = mean[j];
                                X[i][j] = new String(String.valueOf(tempData));
                            }
                            in++;
                        } else{
                            if(direccion == Attribute.OUTPUT){
                                if(tipo != Attribute.NOMINAL && !inst.getOutputMissingValues(out)){//we want to normalize everything but nominal attributes
                                    if(std_dev[j]!=0)
                                        tempData = (inst.getOutputRealValues(out) - mean[j])/std_dev[j];
                                    else
                                        tempData = mean[j];
                                    X[i][j] = new String(String.valueOf(tempData));
                                }
                                out++;
                            }
                        }
                    }
                }
                for(int j = 0; j < nvariables;j++){
                    Attribute a = Attributes.getAttribute(j);
                    
                    if(std_dev[j]!=0){
                        new_min = (a.getMinAttribute() - mean[j])/std_dev[j];
                        new_max = (a.getMaxAttribute() - mean[j])/std_dev[j];
                    } else
                        new_min = new_max = mean[j];
                    tipo = a.getType();
                    if(tipo != Attribute.NOMINAL){
                        a.setBounds(new_min,new_max);
                    }
                }
            }catch (Exception e){
                System.out.println("Dataset exception = " + e );
                System.exit(-1);
            }
            write_results(output_test_name);
        }
    }
    
    
    private void config_read(String fileParam){
        File inputFile = new File(fileParam);
        
        if (inputFile == null || !inputFile.exists()) {
            System.out.println("parameter "+fileParam+" file doesn't exists!");
            System.exit(-1);
        }
        //begin the configuration read from file
        try {
            FileReader file_reader = new FileReader(inputFile);
            BufferedReader buf_reader = new BufferedReader(file_reader);
            //FileWriter file_write = new FileWriter(outputFile);
            
            String line;
            
            do{
                line = buf_reader.readLine();
            }while(line.length()==0); //avoid empty lines for processing -> produce exec failure
            String out[]= line.split("algorithm = ");
            //alg_name = new String(out[1]); //catch the algorithm name
            //input & output filenames
            do{
                line = buf_reader.readLine();
            }while(line.length()==0);
            out= line.split("inputData = ");
            out = out[1].split("\\s\"");
            input_train_name = new String(out[0].substring(1, out[0].length()-1));
            input_test_name = new String(out[1].substring(0, out[1].length()-1));
            if(input_test_name.charAt(input_test_name.length()-1)=='"')
                input_test_name = input_test_name.substring(0,input_test_name.length()-1);
            
            do{
                line = buf_reader.readLine();
            }while(line.length()==0);
            out = line.split("outputData = ");
            out = out[1].split("\\s\"");
            output_train_name = new String(out[0].substring(1, out[0].length()-1));
            output_test_name = new String(out[1].substring(0, out[1].length()-1));
            if(output_test_name.charAt(output_test_name.length()-1)=='"')
                output_test_name = output_test_name.substring(0,output_test_name.length()-1);
            
            file_reader.close();
            
        } catch (IOException e) {
            System.out.println("IO exception = " + e );
            System.exit(-1);
        }
    }
    
    private void write_results(String output){
    	Attribute a;
    	String header = "";
		int i, j, k;
		int aux;
        //File OutputFile = new File(output_train_name.substring(1, output_train_name.length()-1));
        try {
        	
        	FileWriter file_write = new FileWriter(output);

    		/* Printing input attributes */
    		header += "@relation " + Attributes.getRelationName()+ "\n";
    		for (i = 0; i < Attributes.getNumAttributes(); i++) {
    			a = Attributes.getAttribute(i);
    			if(a.getDirectionAttribute()==Attribute.INPUT){
    				header += "@attribute " + a.getName() + " ";
    				if (a.getType() == Attribute.NOMINAL) {
    					header += "{";
    					for (j = 0; j < a.getNominalValuesList().size(); j++) {
    						header += (String) a.getNominalValuesList()
    						.elementAt(j);
    						if (j < a.getNominalValuesList().size() - 1) {
    							header += ", ";
    						}
    					}
    					header += "}\n";
    				} else {
    					if (a.getType() == Attribute.INTEGER) {
    						header += "integer";
    						header += " ["
    							+ String.valueOf((int) a.getMinAttribute()) + ", "+ String.valueOf((int) a.getMaxAttribute()) + "]\n";
    					} else {
    						header += "real";
    						header += " ["
    							+ String.valueOf(a.getMinAttribute())
    							+ ", "
    							+ String.valueOf(a.getMaxAttribute())
    							+ "]\n";
    					}
    				}
    			}else{
    				/* Printing output attribute */
    				header += "@attribute " + a.getName() + " ";
    				if (a.getType() == Attribute.NOMINAL) {
    					header += "{";
    					for (j = 0; j < a.getNominalValuesList().size(); j++) {
    						header += (String) a.getNominalValuesList().elementAt(j);
    						if (j < a.getNominalValuesList().size() - 1) {
    							header += ", ";
    						}
    					}
    					header += "}\n";
    				} else {
    					header += "integer ["
    						+ String.valueOf((int) a.getMinAttribute()) + ", "
    						+ String.valueOf((int) a.getMaxAttribute()) + "]\n";
    				}
    			}
    		}
    		file_write.write(header);
    		
    		file_write.write(Attributes.getInputHeader()+"\n");
    		file_write.write(Attributes.getOutputHeader()+"\n");
    		
            //now, print the normalized data
            file_write.write("@data\n");
            for(i=0;i<ndatos;i++){
                file_write.write(X[i][0]);
                for(j=1;j<nvariables;j++){
                    file_write.write(","+X[i][j]);
                }
                file_write.write("\n");
            }
            file_write.close();
        } catch (IOException e) {
            System.out.println("IO exception = " + e );
            System.exit(-1);
        }
    }
    
}

