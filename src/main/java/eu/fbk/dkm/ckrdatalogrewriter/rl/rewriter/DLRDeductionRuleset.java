package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.io.StringReader;
import java.util.List;

import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * @author Loris
 * @version 1.5
 * 
 * Encoding of <code>Pdlr</code>, <code>Pd-dlr</code> 
 * deduction rules for DLR datalog translation.
 * (Version for DLV).
 */
public class DLRDeductionRuleset {
	
	//### NEW DLliteR deduction RULES ###
	private final static String strPdlr = 
			// # Assertions rules #
			"instd(X, Z) :- insta(X, Z).\n" + //
			"tripled(X, R, Y) :- triplea(X, R, Y).\n" + //
			// # Equality rules #
			// # \top and \bot rules #
			//"instd(X, top, C, main) :- nom(X, C).\n" + //
			//"unsat(T) :- instd(X, bot, C, T).\n" + //			
			// # Class axioms rules #
			"instd(X, Z) :- subClass(Y, Z), instd(X, Y).\n" + //
			"-instd(X, Z) :- supNot(Y, Z), instd(X, Y).\n" + //
			"instd(X, Z) :- subEx(V, Z), tripled(X, V, XP).\n" + // *!*
			"tripled(X, R, X1) :- supEx(Y, R, X1), instd(X, Y).\n" + //			

			// # Roles axioms rules #
			"tripled(X, W, X1) :- subRole(V, W), tripled(X, V, X1).\n" + //			
			// # Roles properties rules #
			"-tripled(X, V, Y) :- dis(U, V), tripled(X, U, Y).\n" + //
			"-tripled(X, U, Y) :- dis(U, V), tripled(X, V, Y).\n" + //
			"tripled(Y, V, X) :- inv(U, V), tripled(X, U, Y).\n" + //
			"tripled(Y, U, X) :- inv(U, V), tripled(X, V, Y).\n" + //
			"-tripled(X, U, X) :- irr(U), const(X).\n" + //
			// # Non-disjunctive contrapositive rules #
			"-tripled(X, R, Y) :- ntriplea(X, R, Y).\n" + //			
			"-instd(X, Y) :- subClass(Y, Z), -instd(X, Z).\n" + //
			"instd(X, Y) :- supNot(Y, Z), -instd(X, Z).\n" + //
			"-tripled(X, V, XP) :- subEx(V, Z), const(XP), -instd(X, Z).\n" + //
			"-instd(X, Y) :- supEx(Y, R, X1), const(X), allNRel(X, R).\n" + //			
			"-tripled(X, V, X1) :- subRole(V, W), -tripled(X, W, X1).\n" + //
			"-tripled(Y, V, X) :- inv(U, V), -tripled(X, U, Y).\n" + //
			"-tripled(Y, U, X) :- inv(U, V), -tripled(X, V, Y).\n" + //
            //# allNRel support predicates #
            "allNRelStep(X, R, Y) :- first(Y), -tripled(X, R, Y).\n" + //
            "allNRelStep(X, R, Y) :- allNRelStep(X, R, YP), next(YP, Y), -tripled(X, R, Y).\n" + //
            "allNRel(X, R) :- last(Y), allNRelStep(X, R, Y).\n" + //
			// # (test predicates) #
			//"pos(X,Y,C) :- instd(X,Y,C,main).\n" + //
			//"posr(X,R,Y,C) :- tripled(X,R,Y,C,main).\n" + //
			//"pose(X1,X2,C) :- eq(X1,X2,C,main).\n" + //
			"";	
	
	//### NEW DLliteR defeasible RULES ###
	private final static String strPd =			
			// # Assertions propagation #			
			// # Class axioms propagation #			
			"instd(X, Z) :- def_subclass(Y, Z), instd(X, Y), not ovrSubClass(X, Y, Z).\n" +
			"-instd(X, Y) :- def_subclass(Y, Z), -instd(X, Z), not ovrSubClass(X, Y, Z).\n" +
			// # Roles axioms propagation #
			"tripled(X, W, X1) :- def_subr(V, W), tripled(X, V, X1), not ovrSubRole(X, X1, V, W).\n" +			
			"-tripled(X, V, X1) :- def_subr(V, W), -tripled(X, W, X1), not ovrSubRole(X, X1, V, W).\n" +			
			// # Roles properties propagation #
			"tripled(Y, V, X) :- def_inv(U, V), tripled(X, U, Y), not ovrInv(X, Y, U, V).\n" +
			"tripled(X, U, Y) :- def_inv(U, V), tripled(Y, V, X), not ovrInv(X, Y, U, V).\n" +
			"-tripled(X, U, X) :- def_irr(U), const(X), not ovrIrr(X, U).\n" +
			"-tripled(Y, V, X) :- def_inv(U, V), -tripled(X, U, Y), not ovrInv(X, Y, U, V).\n" +
			"-tripled(X, U, Y) :- def_inv(U, V), -tripled(Y, V, X), not ovrInv(X, Y, U, V).\n" +
			
			// # OVR rules #
			"ovrSubClass(X, Y, Z) :- def_subclass(Y,Z), instd(X, Y), -instd(X, Z).\n" +
			"ovrSubRole(X, Y, R, S) :- def_subr(R,S), tripled(X, R, Y), -tripled(X, S, Y).\n" +
			"ovrInv(X, Y, R, S) :- def_inv(R,S), tripled(X, R, Y), -tripled(Y, S, X).\n" +
			"ovrInv(X, Y, R, S) :- def_inv(R,S), tripled(Y, S, X), -tripled(X, R, Y).\n" +
			"ovrIrr(X, R) :- def_irr(R), tripled(X, R, X).\n" +
			// # TEST rules #
			"";	
	
	private static List<ProgramStatement> pdlr, pd;

	//--- INITIALIZATION ----------------------------------------------------------
	
	static {
		
        
		StringReader reader = new StringReader(strPdlr);
		DLProgramParser dlProgramParser = new DLProgramParser(reader);
		try {
			pdlr = dlProgramParser.program().getStatements();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		reader = new StringReader(strPd);
		dlProgramParser = new DLProgramParser(reader);
		try {
			pd = dlProgramParser.program().getStatements();
		} catch (ParseException e) {
			e.printStackTrace();
		}		
			
	}

	//--- GET METHODS ----------------------------------------------------------
	
	public static List<ProgramStatement> getPdlr() {
		return pdlr;
	}

	public static List<ProgramStatement> getPd() {
		return pd;
	}
	

}
//=======================================================================