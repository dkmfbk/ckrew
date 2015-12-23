package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.io.StringReader;
import java.util.List;

import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * @author Loris
 * @version 1.3
 * 
 * Encoding of <code>Prl</code>, <code>Pd</code> and <code>Ploc</code> 
 * deduction rules of materialization calculus.
 * (Version for DLV).
 */
public class DeductionRuleset {

	//TODO: export strings to properties files
//	private final static String strPrl = 
//			"inst(X, bot, C) :- negtriple(X, V, Y, C), triple(X, V, Y, C).\n" + //
//			// # Equality rules #
//			"eq(X, X, C) :- nom(X, C).\n" + //
//			"eq(Y, X, C) :- eq(X, Y, C).\n" + //
//			"inst(Y, Z, C) :- eq(X, Y, C), inst(X, Z, C).\n" + //
//			"triple(Y, U, Z, C) :- eq(X, Y, C), triple(X, U, Z, C).\n" + //
//			"triple(Z, U, Y, C) :- eq(X, Y, C), triple(Z, U, X, C).\n" + //
//			"eq(X, Z, C) :- eq(X, Y, C), eq(Y, Z, C).\n" + //
//			"inst(X, bot, C) :- eq(X, Y, C), neq(X, Y, C).\n" + //
//			"inst(X, top, C) :- inst(X, Z, C).\n" + //
//			// # Class axioms rules #
//			"inst(X, Z, C) :- subClass(Y, Z, C), inst(X, Y, C).\n" + //
//			"inst(X, bot, C) :- supNot(Y, Z, C), inst(X, Y, C), inst(X, Z, C).\n" + //
//			"inst(X, Z, C) :- subConj(Y1, Y2, Z, C), inst(X, Y1, C), inst(X, Y2, C).\n" + //
//			"inst(X, Z, C) :- subEx(V, Y, Z, C), triple(X, V, XP, C), inst(XP, Y, C).\n" + //
//			"triple(X, R, X1, C) :- supEx(Y, R, X1, C), inst(X, Y, C).\n" + //
//			"inst(Y, Z1, C) :- supForall(Z, R, Z1, C), inst(X, Z, C), triple(X, R, Y, C).\n" + //
//			"eq(X1, X2, C) :- supLeqOne(Z, R, Z1, C), inst(X, Z, C), triple(X, R, X1, C), inst(X1, Z1, C), triple(X, R, X2, C), inst(X2, Z1, C).\n" + //
//			// # Roles axioms rules #
//			"triple(X, W, X1, C) :- subRole(V, W, C), triple(X, V, X1, C).\n" + //
//			"triple(X, W, Z, C) :- subRChain(U, V, W, C), triple(X, U, Y, C), triple(Y, V, Z, C).\n" + //
//			// # Roles properties rules #
//			"inst(X, bot, C) :- dis(U, V, C), triple(X, U, Y, C), triple(X, V, Y, C).\n" + //
//			"triple(Y, V, X, C) :- inv(U, V, C), triple(X, U, Y, C).\n" + //
//			"triple(Y, U, X, C) :- inv(U, V, C), triple(X, V, Y, C).\n" + //
//			"inst(X, bot, C) :- irr(U, C), triple(X, U, X, C).\n" + //
//			// ### TODO: ADD RULES FOR RANGE AND DOMAIN? ###
//			//"isa(Y, R) :- range(V, R), triple(X, V, Y).\n " + //
//			//"isa(X, R) :- domain(V, R), triple(X, V, Y).\n " + //
//			// "isa(X, R) :- transitive(R), triple(X, R, Y).\n " + //
//			"";

	private final static String strPrl = 
			// # Assertions rules #
			"instd(X, Z, C) :- insta(X, Z, C).\n" + //
			"-instd(X, Z, C) :- -insta(X, Z, C).\n" + //
			"tripled(X, R, Y, C) :- triplea(X, R, Y, C).\n" + //
			"-tripled(X, R, Y, C) :- -triplea(X, R, Y, C).\n" + //
			// # Equality rules #
			"eq(X, X, C) :- nom(X, C).\n" + //
			"eq(Y, X, C) :- eq(X, Y, C).\n" + //
			"instd(Y, Z, C) :- eq(X, Y, C), instd(X, Z, C).\n" + //
			"-instd(Y, Z, C) :- eq(X, Y, C), -instd(X, Z, C).\n" + //
			"tripled(Y, U, Z, C) :- eq(X, Y, C), tripled(X, U, Z, C).\n" + //
			"-tripled(Y, U, Z, C) :- eq(X, Y, C), -tripled(X, U, Z, C).\n" + //
			"tripled(Z, U, Y, C) :- eq(X, Y, C), tripled(Z, U, X, C).\n" + //
			"-tripled(Z, U, Y, C) :- eq(X, Y, C), -tripled(Z, U, X, C).\n" + //
			"eq(X, Z, C) :- eq(X, Y, C), eq(Y, Z, C).\n" + //
			//"instd(X, bot, C) :- eq(X, Y, C), neq(X, Y, C).\n" + //
			"instd(X, top, C) :- instd(X, Z, C).\n" + //
			// # Class axioms rules #
			"instd(X, Z, C) :- subClass(Y, Z, C), instd(X, Y, C).\n" + //
			"-instd(X, Z, C) :- supNot(Y, Z, C), instd(X, Y, C).\n" + //
			"instd(X, Z, C) :- subConj(Y1, Y2, Z, C), instd(X, Y1, C), instd(X, Y2, C).\n" + //
			"instd(X, Z, C) :- subEx(V, Y, Z, C), tripled(X, V, XP, C), instd(XP, Y, C).\n" + //
			"tripled(X, R, X1, C) :- supEx(Y, R, X1, C), instd(X, Y, C).\n" + //
			"instd(Y, Z1, C) :- supForall(Z, R, Z1, C), instd(X, Z, C), tripled(X, R, Y, C).\n" + //
			"eq(X1, X2, C) :- supLeqOne(Z, R, Z1, C), instd(X, Z, C), tripled(X, R, X1, C), instd(X1, Z1, C), tripled(X, R, X2, C), instd(X2, Z1, C).\n" + //
			// # Roles axioms rules #
			"tripled(X, W, X1, C) :- subRole(V, W, C), tripled(X, V, X1, C).\n" + //
			"tripled(X, W, Z, C) :- subRChain(U, V, W, C), tripled(X, U, Y, C), tripled(Y, V, Z, C).\n" + //
			// # Roles properties rules #
			"-tripled(X, V, Y, C) :- dis(U, V, C), tripled(X, U, Y, C).\n" + //
			"-tripled(X, U, Y, C) :- dis(U, V, C), tripled(X, V, Y, C).\n" + //
			"tripled(Y, V, X, C) :- inv(U, V, C), tripled(X, U, Y, C).\n" + //
			"tripled(Y, U, X, C) :- inv(U, V, C), tripled(X, V, Y, C).\n" + //
			"-tripled(X, U, X, C) :- irr(U, C), nom(X, C).\n" + //
			// ### TODO: ADD RULES FOR RANGE AND DOMAIN? ###
			//"isa(Y, R) :- range(V, R), triple(X, V, Y).\n " + //
			//"isa(X, R) :- domain(V, R), triple(X, V, Y).\n " + //
			// "isa(X, R) :- transitive(R), triple(X, R, Y).\n " + //
			"";

	private final static String strPd =
			// # Assertions propagation #
			"instd(X, Z, C) :- insta(X, Z, G), prec(C, G), not ovrInsta(X, Z, C).\n" +
//		    "-instd(X, Z, C) :- -insta(X, Z, \"g\"), prec(C, \"g\"), not ovrNInsta(X, Z, C)" +
			"tripled(X, R, Y, C) :- triplea(X, R, Y, G), prec(C, G), not ovrTriplea(X, R, Y, C).\n" +
			"-tripled(X, R, Y, C) :- -triplea(X, R, Y, G), prec(C, G), not ovrNTriplea(X, R, Y, C).\n" +
			// # Class axioms propagation #			
			"instd(X, Z, C) :- subClass(Y, Z, G), instd(X, Y, C), prec(C, G), not ovrSubClass(X, Y, Z, C).\n" +
			"-instd(X, Z, C) :- supNot(Y, Z, G), instd(X, Y, C), prec(C, G), not ovrSupNot(X, Y, Z, C).\n" +
			"instd(X, Z, C) :- subConj(Y1, Y2, Z, G), instd(X, Y1, C), instd(X, Y2, C), prec(C, G), not ovrSubConj(X, Y1, Y2, Z, C).\n" +
			"instd(X, Z, C) :- subEx(V, Y, Z, G), tripled(X, V, X1, C), instd(X1, Y, C), prec(C, G), not ovrSubEx(X, V, Y, Z, C).\n" +
			"tripled(X, R, X1, C) :- supEx(Y, R, X1, G), instd(X, Y, C), prec(C, G), not ovrSupEx(X, Y, R, X1, C).\n" +
			"instd(Y, Z1, C) :- supForall(Z, R, Z1, G), instd(X, Z, C), tripled(X, R, Y, C), prec(C, G), not ovrSupAll(X, Y, Z, R, Z1, C).\n" +
			"eq(X1, X2, C) :- supLeqOne(Z, R, Z1, G), instd(X, Z, C), tripled(X, R, X1, C), instd(X1, Z1, C), tripled(X, R, X2, C), instd(X2, Z1, C), prec(C, G), not ovrSupLeqOne(X, X1, X2, Z, R, Z1, C).\n" +
			// # Roles axioms propagation #
			"tripled(X, W, X1, C) :- subRole(V, W, G), tripled(X, V, X1, C), prec(C, G), not ovrSubRole(X, X1, V, W, C).\n" +
			"tripled(X, W, Z, C) :- subRChain(U, V, W, G), tripled(X, U, Y, C), tripled(Y, V, Z, C), prec(C, G), not ovrSubRChain(X, Y, Z, U, V, W, C).\n" +
			// # Roles properties propagation #
			"-tripled(X, V, Y, C) :- dis(U, V, G), tripled(X, U, Y, C), prec(C, G), not ovrDis(X, Y, U, V, C).\n" +
			"-tripled(X, U, Y, C) :- dis(U, V, G), tripled(X, V, Y, C), prec(C, G), not ovrDis(X, Y, U, V, C).\n" +
			"tripled(Y, V, X, C) :- inv(U, V, G), tripled(X, U, Y, C), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
			"tripled(Y, U, X, C) :- inv(U, V, G), tripled(X, V, Y, C), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
			"-tripled(X, U, X, C) :- irr(U, G), nom(X, C), prec(C, G), not ovrIrr(X, U, C).\n" +
			"";
	
	private final static String strPloc = 
			"instd(X,B,C) :- subEval(A, C1, B, C), instd(CP, C1, G), instd(X, A, CP).\n" +
	        "tripled(X,T,Y,C) :- subEvalR(R, C1, T, C), instd(CP, C1, G), tripled(X, R, Y, CP).\n" + 
	        "eq(X,Y,C) :- nom(X, C), eq(X,Y,C1).\n"	;
	
	private static List<ProgramStatement> prl, pd, ploc;

	//private static List<ProgramStatement> xsbDeclaration;

	//--- INITIALIZATION ----------------------------------------------------------
	
	static {
		
//		StringReader reader = new StringReader(strPel);
//		DLProgramParser dlProgramParser = new DLProgramParser(reader);
//		
//		try {
//			pel = dlProgramParser.program().getStatements();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
        
		//TODO: define the following as a method
		StringReader reader = new StringReader(strPrl);
		DLProgramParser dlProgramParser = new DLProgramParser(reader);
		try {
			prl = dlProgramParser.program().getStatements();
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
		
		reader = new StringReader(strPloc);
		dlProgramParser = new DLProgramParser(reader);
		try {
			ploc = dlProgramParser.program().getStatements();
		} catch (ParseException e) {
			e.printStackTrace();
		}		
	
	}

	//--- GET METHODS ----------------------------------------------------------
	
	public static List<ProgramStatement> getPrl() {
		return prl;
	}

	public static List<ProgramStatement> getPd() {
		return pd;
	}
	
	public static List<ProgramStatement> getPloc() {
		return ploc;
	}
	
//	public static List<ProgramStatement> getPel() {
//	    return pel;
//  }

}
//=======================================================================