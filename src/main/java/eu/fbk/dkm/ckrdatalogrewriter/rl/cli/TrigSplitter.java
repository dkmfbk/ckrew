package eu.fbk.dkm.ckrdatalogrewriter.rl.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;

/** 
 * @author Loris 
 * @version 1.3
 * 
 * Splits input TRIG files into single files representing modules. 
 */
public class TrigSplitter {

	//--- FIELDS ---------------------------------------------
	
	private String trigFileName;
	
	//Prefixes for all graphs
	private String prefix = "";
	
	//Map of names to graphs
    private HashMap<String, String> namedGraphs = new HashMap<String, String>();
	
    //List of output file names
    private LinkedList<String> outputFileNames = new LinkedList<String>();
    
	//--- CONSTRUCTOR ----------------------------------------
	
	public TrigSplitter(String tfn) {	
		this.trigFileName = tfn; 
	}

	//--- GET METHODS ----------------------------------------
	
	public String getTrigFolder(){
		return new File(trigFileName).getParent() + File.separator;
	}
	
	public LinkedList<String> getOutputFileNames(){
		return outputFileNames;
	}
	
	//--- TRIG SPLITTING -------------------------------------
	
	/**
	 * Reads the input TRIG file and splits it in its graphs.
	 * Graphs are stored in a internal hash map and prefixes 
	 * are recognized and stored.
	 * 
	 * @throws IOException 
	 */
	public void split() throws IOException{
		
	  BufferedReader reader = new BufferedReader(
                new FileReader(trigFileName));
      
      String graphName = "";
      String graphContent = "";
      String line;
      while ((line = reader.readLine()) != null) {
      	//System.out.println(line);
      	
      	//Recognize prefix lines and append to prefixes string.
      	if(line.toLowerCase().startsWith("@")){ 
      		prefix = prefix + line + "\n";
      		
      	//if line contains '{' open a new graph file with provided name and read until '}'
        } else if(line.contains("{")){
      	  	graphName = line.substring(0, line.indexOf('{'));
      	  	graphContent = "";
      	  	
      	//if line contains '}' close current graph and save to graphs map
      	} else if(line.contains("}")){
      		namedGraphs.put(graphName, graphContent);
      		
      	//otherwise, add current line to current graph
      	} else {
      		graphContent += line + "\n";
      	}
      }     	
      reader.close();
      
      //System.out.println("> Prefixes read:");
      //System.out.println(prefix);
      //
      //System.out.println("> NGraphs read:");
      //for (String key : namedGraphs.keySet()) {
    	//  System.out.println(key + "{");
    	//  System.out.println(namedGraphs.get(key) + "}");
	  //}      
	}
	
	/**
	 * Saves the splitted graphs to Turtle files (in the same folder 
	 * of the Trig file and with filenames equal to graphs names).
	 * 
	 * @throws IOException 
	 */
	public void saveToTurtleFiles() throws IOException{
		
	   for (String key : namedGraphs.keySet()) {
		   
		   String output_filename = key.substring(key.indexOf(':') + 1).trim() + ".n3";
		   String output_filepath = getTrigFolder() + output_filename;
		   //System.out.println(output_filepath);
		   
		   outputFileNames.add(output_filename);
		   		   
		   BufferedWriter writer = new BufferedWriter(
                  new FileWriter(output_filepath));
		   PrintWriter pwriter   = new PrintWriter(writer);
		   
		   pwriter.print(prefix);
		   pwriter.println();
		   pwriter.println("#--- " + key.trim() + " ---#");
		   pwriter.println();
		   pwriter.print(namedGraphs.get(key));
		   
		   writer.flush();
		   writer.close();
	   }
	}
	
	//--- MAIN -----------------------------------------------
	/**
	 * Tests the splitting on a test TRIG file.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String testFileName = "./testcase/trig-test/trig-test-file.trig";
		
		TrigSplitter testsplitter = new TrigSplitter(testFileName);
		testsplitter.split();
		testsplitter.saveToTurtleFiles();
		
		System.out.println("Split complete: " + testFileName);
		for (String name : testsplitter.getOutputFileNames()) {
			System.out.println(name);			
		}
		
	}
}
