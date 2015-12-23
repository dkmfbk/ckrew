package eu.fbk.dkm.ckrdatalogrewriter.cli;

import it.unical.mat.wrapper.DLVInputProgram;
import it.unical.mat.wrapper.DLVInvocationException;

import java.io.IOException;

import org.semanticweb.drew.dlprogram.parser.ParseException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import eu.fbk.dkm.ckrdatalogrewriter.rl.cli.CKRDReWRLCLI;

/**
 * @author Loris 
 * @version 1.3 
 * 
 * Abstract class for command line interface.
 * (Extended from DreW CLI).
 */
public abstract class CommandLine {

	//--- ABSTRACT METHODS ------------------------------------
	/**
	 * Abstract method for parsing of command line parameters.
	 * 
	 * @param args command line parameters
	 * @return <code>true</code> if call conforms with usage, <code>false</code> otherwise. 
	 */
	public abstract boolean parseArgs(String[] args);

	/**
	 * Abstract method for the main execution thread of the application. 
	 */
	public abstract void go();

	/**
	 * Abstract method for handling and execution of input Conjunctive Query.<br/>
	 * (Not currently implemented).
	 *  
	 * @param ontology
	 * @param inputProgram 
	 */
	public abstract void handleCQ(OWLOntology ontology, DLVInputProgram inputProgram);
	
	//--- MAIN ------------------------------------------------
	/**
	 * @param args
	 * @throws DLVInvocationException
	 * @throws ParseException
	 * @throws IOException
	 * @throws OWLOntologyCreationException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException,
			IOException, ParseException, DLVInvocationException {
		
		if (args.length == 0) {
			String usage = "Usage: ckrew <global-context-file> [<local-module-file> | <options>]";
			System.err.println(usage);
			System.exit(0);
		} else {
			CKRDReWRLCLI.main(args);
		}		
	}

}
//=======================================================================