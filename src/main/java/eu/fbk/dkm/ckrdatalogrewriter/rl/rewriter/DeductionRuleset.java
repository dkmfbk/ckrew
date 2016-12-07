package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.io.StringReader;
import java.util.List;

import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * @author Loris
 * @version 1.4
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
	
//	private final static String strPrl = 
//			// # Assertions rules #
//			"instd(X, Z, C) :- insta(X, Z, C).\n" + //
//			"-instd(X, Z, C) :- -insta(X, Z, C).\n" + //
//			"tripled(X, R, Y, C) :- triplea(X, R, Y, C).\n" + //
//			"-tripled(X, R, Y, C) :- -triplea(X, R, Y, C).\n" + //
//			// # Equality rules #
//			"eq(X, X, C) :- nom(X, C).\n" + //
//			"eq(Y, X, C) :- eq(X, Y, C).\n" + //
//			"-eq(Y, X, C) :- -eq(X, Y, C).\n" + //
//			"instd(Y, Z, C) :- eq(X, Y, C), instd(X, Z, C).\n" + //
//			"-instd(Y, Z, C) :- eq(X, Y, C), -instd(X, Z, C).\n" + //
//			"tripled(Y, U, Z, C) :- eq(X, Y, C), tripled(X, U, Z, C).\n" + //
//			"-tripled(Y, U, Z, C) :- eq(X, Y, C), -tripled(X, U, Z, C).\n" + //
//			"tripled(Z, U, Y, C) :- eq(X, Y, C), tripled(Z, U, X, C).\n" + //
//			"-tripled(Z, U, Y, C) :- eq(X, Y, C), -tripled(Z, U, X, C).\n" + //
//			"eq(X, Z, C) :- eq(X, Y, C), eq(Y, Z, C).\n" + //
//			"-eq(X, Z, C) :- eq(X, Y, C), -eq(Y, Z, C).\n" + //			
//			//"instd(X, bot, C) :- eq(X, Y, C), neq(X, Y, C).\n" + //
//			"instd(X, top, C) :- instd(X, Z, C).\n" + //
//			// # Class axioms rules #
//			"instd(X, Z, C) :- subClass(Y, Z, C), instd(X, Y, C).\n" + //
//			"-instd(X, Y, C) :- subClass(Y, Z, C), -instd(X, Z, C).\n" + //			
//			"-instd(X, Z, C) :- supNot(Y, Z, C), instd(X, Y, C).\n" + //
//			"-instd(X, Y, C) :- supNot(Y, Z, C), instd(X, Z, C).\n" + //			
//			"instd(X, Z, C) :- subConj(Y1, Y2, Z, C), instd(X, Y1, C), instd(X, Y2, C).\n" + //
//			"instd(X, Z, C) :- subEx(V, Y, Z, C), tripled(X, V, XP, C), instd(XP, Y, C).\n" + //
//			"tripled(X, R, X1, C) :- supEx(Y, R, X1, C), instd(X, Y, C).\n" + //
//			"-instd(X, Y, C) :- supEx(Y, R, X1, C), -tripled(X, R, X1, C).\n" + //			
//			"instd(Y, Z1, C) :- supForall(Z, R, Z1, C), instd(X, Z, C), tripled(X, R, Y, C).\n" + //
//			"-instd(X, Z, C) :- supForall(Z, R, Z1, C), -instd(Y, Z1, C), tripled(X, R, Y, C).\n" + //			
//			"eq(X1, X2, C) :- supLeqOne(Z, R, Z1, C), instd(X, Z, C), tripled(X, R, X1, C), instd(X1, Z1, C), tripled(X, R, X2, C), instd(X2, Z1, C).\n" + //
//			// # Roles axioms rules #
//			"tripled(X, W, X1, C) :- subRole(V, W, C), tripled(X, V, X1, C).\n" + //
//			"-tripled(X, V, X1, C) :- subRole(V, W, C), -tripled(X, W, X1, C).\n" + //
//			"tripled(X, W, Z, C) :- subRChain(U, V, W, C), tripled(X, U, Y, C), tripled(Y, V, Z, C).\n" + //
//			// # Roles properties rules #
//			"-tripled(X, V, Y, C) :- dis(U, V, C), tripled(X, U, Y, C).\n" + //
//			"-tripled(X, U, Y, C) :- dis(U, V, C), tripled(X, V, Y, C).\n" + //
//			"tripled(Y, V, X, C) :- inv(U, V, C), tripled(X, U, Y, C).\n" + //
//			"tripled(Y, U, X, C) :- inv(U, V, C), tripled(X, V, Y, C).\n" + //
//			"-tripled(Y, V, X, C) :- inv(U, V, C), -tripled(X, U, Y, C).\n" + //
//			"-tripled(Y, U, X, C) :- inv(U, V, C), -tripled(X, V, Y, C).\n" + //
//			"-tripled(X, U, X, C) :- irr(U, C), nom(X, C).\n" + //
//			// ### TODO: ADD RULES FOR RANGE AND DOMAIN? ###
//			//"isa(Y, R) :- range(V, R), triple(X, V, Y).\n " + //
//			//"isa(X, R) :- domain(V, R), triple(X, V, Y).\n " + //
//			// "isa(X, R) :- transitive(R), triple(X, R, Y).\n " + //
//			
//			// # Non-disjunctive contrapositive rules #
//			"-instd(X, Y1, C) :- subConj(Y1, Y2, Z, C), -instd(X, Z, C), instd(X, Y2, C).\n" + //
//			"-instd(X, Y2, C) :- subConj(Y1, Y2, Z, C), -instd(X, Z, C), instd(X, Y1, C).\n" + //			
//			"-instd(X1, Y, C) :- subEx(V, Y, Z, C), -instd(X, Z, C), tripled(X, V, X1, C).\n" + //
//			"-tripled(X, V, X1, C) :- subEx(V, Y, Z, C), -instd(X, Z, C), instd(X1, Y, C).\n" + //
//			//TODO: revise rules for supLeqOne with \top case
//            "-tripled(X, R, X2, C) :- supLeqOne(Z, R, Z1, C), -eq(X1, X2, C), tripled(X,R,X1,C), instd(X, Z, C).\n" + //
//            "-instd(X, Z, C) :- supLeqOne(Z, R, Z1, C), -eq(X1, X2, C), tripled(X,R,X1,C), tripled(X,R,X2, C).\n" + //
//            "-tripled(X, U, Y, C) :- subRChain(U, V, W, C), -tripled(X,W,Z,C), tripled(Y,V,Z,C).\n" + //
//            "-tripled(Y, V, Z, C) :- subRChain(U, V, W, C), -tripled(X,W,Z,C), tripled(X,U,Y,C).\n" + //
//			"";

//	//### OLD TEST(c, lit(x,y)) RULES ###
//	private final static String strPrl = 
//			// # Assertions rules #
//			"instd(X, Z, C, T) :- insta(X, Z, C, T).\n" + //
//			"tripled(X, R, Y, C, T) :- triplea(X, R, Y, C, T).\n" + //
//			"unsat(C,T) :- ninsta(X, Z, C), instd(X, Z, C, T).\n" + //
//			"unsat(C,T) :- ntriplea(X, R, Y, C), tripled(X, R, Y, C, T).\n" + //
//			//"unsat(C,T) :- neq(X, Y, C), eq(X, Y, C, T).\n" + //
//			"unsat(C,T) :- eq(X, Y, C, T).\n" + //
//			// # Equality rules #
//			//"eq(X, X, C, main) :- nom(X, C).\n" + //
//			//"eq(Y, X, C, T) :- eq(X, Y, C, T).\n" + //
//			//"instd(Y, Z, C, T) :- eq(X, Y, C, T), instd(X, Z, C, T).\n" + //
//			//"tripled(Y, U, Z, C, T) :- eq(X, Y, C, T), tripled(X, U, Z, C, T).\n" + //
//			//"tripled(Z, U, Y, C, T) :- eq(X, Y, C, T), tripled(Z, U, X, C, T).\n" + //
//			//"eq(X, Z, C, T) :- eq(X, Y, C, T), eq(Y, Z, C, T).\n" + //
//			// # \top and \bot rules #
//			"instd(X, top, C, main) :- nom(X, C).\n" + //
//			"unsat(C, T) :- instd(X, bot, C, T).\n" + //			
//			// # Class axioms rules #
//			"instd(X, Z, C, T) :- subClass(Y, Z, C), instd(X, Y, C, T).\n" + //			
//			"instd(X, Z, C, T) :- subConj(Y1, Y2, Z, C), instd(X, Y1, C, T), instd(X, Y2, C, T).\n" + //
//			"instd(X, Z, C, T) :- subEx(V, Y, Z, C), tripled(X, V, XP, C, T), instd(XP, Y, C, T).\n" + //
//			"tripled(X, R, X1, C, T) :- supEx(Y, R, X1, C), instd(X, Y, C, T).\n" + //			
//			"instd(Y, Z1, C, T) :- supForall(Z, R, Z1, C), instd(X, Z, C, T), tripled(X, R, Y, C, T).\n" + //
//			//"eq(X1, X2, C, T) :- supLeqOne(Z, R, C), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T).\n" + //
//			"unsat(C, T) :- supLeqOne(Z, R, C), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T), X1 != X2.\n" + //
//			// # Roles axioms rules #
//			"tripled(X, W, X1, C, T) :- subRole(V, W, C), tripled(X, V, X1, C, T).\n" + //
//			"tripled(X, W, Z, C, T) :- subRChain(U, V, W, C), tripled(X, U, Y, C, T), tripled(Y, V, Z, C, T).\n" + //
//			// # Roles properties rules #
//			"unsat(C, T) :- dis(U, V, C), tripled(X, U, Y, C, T), tripled(X, V, Y, C, T).\n" + //
//			"tripled(Y, V, X, C, T) :- inv(U, V, C), tripled(X, U, Y, C, T).\n" + //
//			"tripled(Y, U, X, C, T) :- inv(U, V, C), tripled(X, V, Y, C, T).\n" + //
//			"unsat(C, T) :- irr(U, C), tripled(X, U, X, C, T).\n" + //
//			":- unsat(C, main).\n" + //
//			// # (test predicates) #
//			//"pos(X,Y,C) :- instd(X,Y,C,main).\n" + //
//			//"posr(X,R,Y,C) :- tripled(X,R,Y,C,main).\n" + //
//			//"pose(X1,X2,C) :- eq(X1,X2,C,main).\n" + //
//			"";

	//### NEW TEST(lit(x,y,c)) RULES ###
	private final static String strPrl = 
			// # Assertions rules #
			"instd(X, Z, C, T) :- insta(X, Z, C, T).\n" + //
			"tripled(X, R, Y, C, T) :- triplea(X, R, Y, C, T).\n" + //
			"unsat(T) :- ninsta(X, Z, C), instd(X, Z, C, T).\n" + //
			"unsat(T) :- ntriplea(X, R, Y, C), tripled(X, R, Y, C, T).\n" + //
			//"unsat(C,T) :- neq(X, Y, C), eq(X, Y, C, T).\n" + //
			"unsat(T) :- eq(X, Y, C, T).\n" + //
			// # Equality rules #
			//"eq(X, X, C, main) :- nom(X, C).\n" + //
			//"eq(Y, X, C, T) :- eq(X, Y, C, T).\n" + //
			//"instd(Y, Z, C, T) :- eq(X, Y, C, T), instd(X, Z, C, T).\n" + //
			//"tripled(Y, U, Z, C, T) :- eq(X, Y, C, T), tripled(X, U, Z, C, T).\n" + //
			//"tripled(Z, U, Y, C, T) :- eq(X, Y, C, T), tripled(Z, U, X, C, T).\n" + //
			//"eq(X, Z, C, T) :- eq(X, Y, C, T), eq(Y, Z, C, T).\n" + //
			// # \top and \bot rules #
			"instd(X, top, C, main) :- nom(X, C).\n" + //
			"unsat(T) :- instd(X, bot, C, T).\n" + //			
			// # Class axioms rules #
			"instd(X, Z, C, T) :- subClass(Y, Z, C), instd(X, Y, C, T).\n" + //			
			"instd(X, Z, C, T) :- subConj(Y1, Y2, Z, C), instd(X, Y1, C, T), instd(X, Y2, C, T).\n" + //
			"instd(X, Z, C, T) :- subEx(V, Y, Z, C), tripled(X, V, XP, C, T), instd(XP, Y, C, T).\n" + //
			"tripled(X, R, X1, C, T) :- supEx(Y, R, X1, C), instd(X, Y, C, T).\n" + //			
			"instd(Y, Z1, C, T) :- supForall(Z, R, Z1, C), instd(X, Z, C, T), tripled(X, R, Y, C, T).\n" + //
			//"eq(X1, X2, C, T) :- supLeqOne(Z, R, C), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T).\n" + //
			"unsat(T) :- supLeqOne(Z, R, C), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T), X1 != X2.\n" + //
			// # Roles axioms rules #
			"tripled(X, W, X1, C, T) :- subRole(V, W, C), tripled(X, V, X1, C, T).\n" + //
			"tripled(X, W, Z, C, T) :- subRChain(U, V, W, C), tripled(X, U, Y, C, T), tripled(Y, V, Z, C, T).\n" + //
			// # Roles properties rules #
			"unsat(T) :- dis(U, V, C), tripled(X, U, Y, C, T), tripled(X, V, Y, C, T).\n" + //
			"tripled(Y, V, X, C, T) :- inv(U, V, C), tripled(X, U, Y, C, T).\n" + //
			"tripled(Y, U, X, C, T) :- inv(U, V, C), tripled(X, V, Y, C, T).\n" + //
			"unsat(T) :- irr(U, C), tripled(X, U, X, C, T).\n" + //
			":- unsat(main).\n" + //
			// # (test predicates) #
			//"pos(X,Y,C) :- instd(X,Y,C,main).\n" + //
			//"posr(X,R,Y,C) :- tripled(X,R,Y,C,main).\n" + //
			//"pose(X1,X2,C) :- eq(X1,X2,C,main).\n" + //
			"";	
	
//	private final static String strPd =
//			// # Assertions propagation #
//			"instd(X, Z, C) :- insta(X, Z, G), prec(C, G), not ovrInsta(X, Z, C).\n" +
////		    "-instd(X, Z, C) :- -insta(X, Z, \"g\"), prec(C, \"g\"), not ovrNInsta(X, Z, C)" +
//			"tripled(X, R, Y, C) :- triplea(X, R, Y, G), prec(C, G), not ovrTriplea(X, R, Y, C).\n" +
//			"-tripled(X, R, Y, C) :- -triplea(X, R, Y, G), prec(C, G), not ovrNTriplea(X, R, Y, C).\n" +
//			// # Class axioms propagation #			
//			"instd(X, Z, C) :- subClass(Y, Z, G), instd(X, Y, C), prec(C, G), not ovrSubClass(X, Y, Z, C).\n" +
//			"-instd(X, Y, C) :- subClass(Y, Z, G), -instd(X, Z, C), prec(C, G), not ovrSubClass(X, Y, Z, C).\n" +
//			"-instd(X, Z, C) :- supNot(Y, Z, G), instd(X, Y, C), prec(C, G), not ovrSupNot(X, Y, Z, C).\n" +
//			"-instd(X, Y, C) :- supNot(Y, Z, G), instd(X, Z, C), prec(C, G), not ovrSupNot(X, Y, Z, C).\n" +			
//			"instd(X, Z, C) :- subConj(Y1, Y2, Z, G), instd(X, Y1, C), instd(X, Y2, C), prec(C, G), not ovrSubConj(X, Y1, Y2, Z, C).\n" +
//			"instd(X, Z, C) :- subEx(V, Y, Z, G), tripled(X, V, X1, C), instd(X1, Y, C), prec(C, G), not ovrSubEx(X, V, Y, Z, C).\n" +
//			"tripled(X, R, X1, C) :- supEx(Y, R, X1, G), instd(X, Y, C), prec(C, G), not ovrSupEx(X, Y, R, X1, C).\n" +
//			"-instd(X, Y, C) :- supEx(Y, R, X1, G), -tripled(X, R, X1, C), prec(C, G), not ovrSupEx(X, Y, R, X1, C).\n" +			
//			"instd(Y, Z1, C) :- supForall(Z, R, Z1, G), instd(X, Z, C), tripled(X, R, Y, C), prec(C, G), not ovrSupAll(X, Y, Z, R, Z1, C).\n" +
//			"-instd(X, Z, C) :- supForall(Z, R, Z1, G), -instd(Y, Z1, C), tripled(X, R, Y, C), prec(C, G), not ovrSupAll(X, Y, Z, R, Z1, C).\n" +
//			"eq(X1, X2, C) :- supLeqOne(Z, R, Z1, G), instd(X, Z, C), tripled(X, R, X1, C), instd(X1, Z1, C), tripled(X, R, X2, C), instd(X2, Z1, C), prec(C, G), not ovrSupLeqOne(X, X1, X2, Z, R, Z1, C).\n" +
//			// # Roles axioms propagation #
//			"tripled(X, W, X1, C) :- subRole(V, W, G), tripled(X, V, X1, C), prec(C, G), not ovrSubRole(X, X1, V, W, C).\n" +
//			"-tripled(X, V, X1, C) :- subRole(V, W, G), -tripled(X, W, X1, C), prec(C, G), not ovrSubRole(X, X1, V, W, C).\n" +			
//			"tripled(X, W, Z, C) :- subRChain(U, V, W, G), tripled(X, U, Y, C), tripled(Y, V, Z, C), prec(C, G), not ovrSubRChain(X, Y, Z, U, V, W, C).\n" +
//			// # Roles properties propagation #
//			"-tripled(X, V, Y, C) :- dis(U, V, G), tripled(X, U, Y, C), prec(C, G), not ovrDis(X, Y, U, V, C).\n" +
//			"-tripled(X, U, Y, C) :- dis(U, V, G), tripled(X, V, Y, C), prec(C, G), not ovrDis(X, Y, U, V, C).\n" +
//			"tripled(Y, V, X, C) :- inv(U, V, G), tripled(X, U, Y, C), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
//			"tripled(Y, U, X, C) :- inv(U, V, G), tripled(X, V, Y, C), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
//			"-tripled(Y, V, X, C) :- inv(U, V, G), -tripled(X, U, Y, C), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
//			"-tripled(Y, U, X, C) :- inv(U, V, G), -tripled(X, V, Y, C), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +			
//			"-tripled(X, U, X, C) :- irr(U, G), nom(X, C), prec(C, G), not ovrIrr(X, U, C).\n" +			
//			// # Non-disjunctive contrapositive rules #
//			"-instd(X, Y1, C) :- subConj(Y1, Y2, Z, G), -instd(X, Z, C), instd(X, Y2, C), prec(C, G), not ovrSubConj(X, Y1, Y2, Z, C).\n" + //
//			"-instd(X, Y2, C) :- subConj(Y1, Y2, Z, G), -instd(X, Z, C), instd(X, Y1, C), prec(C, G), not ovrSubConj(X, Y1, Y2, Z, C).\n" + //			
//			"-instd(X1, Y, C) :- subEx(V, Y, Z, G), -instd(X, Z, C), tripled(X, V, X1, C), prec(C, G), not ovrSubEx(X, V, Y, Z, C).\n" + //
//			"-tripled(X, V, X1, C) :- subEx(V, Y, Z, G), -instd(X, Z, C), instd(X1, Y, C), prec(C, G), not ovrSubEx(X, V, Y, Z, C).\n" + //
//			//TODO: revise rules for supLeqOne with \top case
//            "-tripled(X, R, X2, C) :- supLeqOne(Z, R, Z1, G), -eq(X1, X2, C), tripled(X,R,X1,C), instd(X, Z, C), prec(C, G), not ovrSupLeqOne(X, X1, X2, Z, R, Z1, C).\n" + //
//            "-instd(X, Z, C) :- supLeqOne(Z, R, Z1, G), -eq(X1, X2, C), tripled(X,R,X1,C), tripled(X,R,X2, C), prec(C, G), not ovrSupLeqOne(X, X1, X2, Z, R, Z1, C).\n" + //
//            "-tripled(X, U, Y, C) :- subRChain(U, V, W, G), -tripled(X,W,Z,C), tripled(Y,V,Z,C), prec(C, G), not ovrSubRChain(X, Y, Z, U, V, W, C).\n" + //
//            "-tripled(Y, V, Z, C) :- subRChain(U, V, W, G), -tripled(X,W,Z,C), tripled(X,U,Y,C), prec(C, G), not ovrSubRChain(X, Y, Z, U, V, W, C).\n" + //			
//			"";

//	//### OLD TEST(c, lit(x,y)) RULES ###
//	private final static String strPd =			
//			// # Assertions propagation #
//			"instd(X, Z, C, T) :- insta(X, Z, G, T), prec(C, G), not ovrInsta(X, Z, C).\n" +
//			"tripled(X, R, Y, C, T) :- triplea(X, R, Y, G, T), prec(C, G), not ovrTriplea(X, R, Y, C).\n" +
//			"unsat(C, T) :- ninsta(X, Z, G), instd(X, Z, C, T), prec(C, G), not ovrNinsta(X, Z, C).\n" +
//			"unsat(C, T) :- ntriplea(X, R, Y, G), tripled(X, R, Y, C, T), prec(C, G), not ovrNtriplea(X, R, Y, C).\n" +
//			// # Class axioms propagation #			
//			"instd(X, Z, C, T) :- subClass(Y, Z, G), instd(X, Y, C, T), prec(C, G), not ovrSubClass(X, Y, Z, C).\n" +
//			"instd(X, Z, C, T) :- subConj(Y1, Y2, Z, G), instd(X, Y1, C, T), instd(X, Y2, C, T), prec(C, G), not ovrSubConj(X, Y1, Y2, Z, C).\n" +
//			"instd(X, Z, C, T) :- subEx(V, Y, Z, G), tripled(X, V, X1, C, T), instd(X1, Y, C, T), prec(C, G), not ovrSubEx(X, V, Y, Z, C).\n" +
//			"tripled(X, R, X1, C, T) :- supEx(Y, R, X1, G), instd(X, Y, C, T), prec(C, G), not ovrSupEx(X, Y, R, X1, C).\n" +			
//			"instd(Y, Z1, C, T) :- supForall(Z, R, Z1, G), instd(X, Z, C, T), tripled(X, R, Y, C, T), prec(C, G), not ovrSupAll(X, Y, Z, R, Z1, C).\n" +
//			"unsat(C, T) :- supLeqOne(Z, R, G), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T), X1 != X2, prec(C, G), not ovrSupLeqOne(X, X1, X2, Z, R, C).\n" +
//			// # Roles axioms propagation #
//			"tripled(X, W, X1, C, T) :- subRole(V, W, G), tripled(X, V, X1, C, T), prec(C, G), not ovrSubRole(X, X1, V, W, C).\n" +			
//			"tripled(X, W, Z, C, T) :- subRChain(U, V, W, G), tripled(X, U, Y, C, T), tripled(Y, V, Z, C, T), prec(C, G), not ovrSubRChain(X, Y, Z, U, V, W, C).\n" +
//			// # Roles properties propagation #
//			"unsat(C,T) :- dis(U, V, G), tripled(X, U, Y, C, T), tripled(X, V, Y, C, T), prec(C, G), not ovrDis(X, Y, U, V, C).\n" +
//			"tripled(Y, V, X, C, T) :- inv(U, V, G), tripled(X, U, Y, C, T), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
//			"tripled(X, U, Y, C, T) :- inv(U, V, G), tripled(Y, V, X, C, T), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
//			"unsat(C,T) :- irr(U, G), tripled(X, U, X, C, T), prec(C, G), not ovrIrr(X, U, C).\n" +
//			// # OVR rules #
//			"ovrInsta(X, Y, C) :- def_insta(X,Y), prec(C,g), not test_fails(C, nlit(X,Y)).\n" +
//			"ovrTriplea(X, R, Y, C) :- def_triplea(X,R,Y), prec(C,g), not test_fails(C, nrel(X,R,Y)).\n" +
//			"ovrNinsta(X, Z, C) :- def_ninsta(X,Z), prec(C,g), instd(X, Z, C, main).\n" +
//			"ovrNtriplea(X, R, Y, C) :- def_ntriplea(X,R,Y), prec(C,g), tripled(X, R, Y, C, main).\n" +
//			"ovrSubClass(X, Y, Z, C) :- def_subclass(Y,Z), prec(C,g), instd(X, Y, C, main), not test_fails(C, nlit(X,Z)).\n" +
//			"ovrSubConj(X, Y1, Y2, Z, C) :- def_subcnj(Y1,Y2,Z), prec(C,g), instd(X, Y1, C, main), instd(X, Y2, C, main), not test_fails(C, nlit(X,Z)).\n" +
//			"ovrSubEx(X, R, Y, Z, C) :- def_subex(R,Y,Z), prec(C,g), tripled(X, R, W, C, main), instd(W, Y, C, main), not test_fails(C, nlit(X,Z)).\n" +
//			"ovrSupEx(X, Y, R, W, C) :- def_supex(Y,R,W), prec(C,g), instd(X, Y, C, main), not test_fails(C, nrel(X,R,W)).\n" +
//			"ovrSupAll(X, Y, Z, R, W, C) :- def_supforall(Z,R,W), prec(C,g), instd(X, Z, C, main), tripled(X, R, Y, C, main), not test_fails(C, nlit(Y,W)).\n" +
//			"ovrSupLeqOne(X, X1, X2, Z, R, C) :- def_supleqone(Z,R), prec(C,g), instd(X, Z, C, main), tripled(X, R, X1, C, main), tripled(X, R, X2, C, main), X1 != X2.\n" +			
//			"ovrSubRole(X, Y, R, S, C) :- def_subr(R,S), prec(C,g), tripled(X, R, Y, C, main), not test_fails(C, nrel(X, S, Y)).\n" +
//			"ovrSubRChain(X, Y, Z, R, S, T, C) :- def_subrc(R,S,T), prec(C,g), tripled(X, R, Y, C, main), tripled(Y, S, Z, C, main), not test_fails(C, nrel(X, T, Z)).\n" +			
//			"ovrDis(X, Y, R, S, C) :- def_dis(R,S), prec(C,g), tripled(X, R, Y, C, main), tripled(X, S, Y, C, main).\n" +
//			"ovrInv(X, Y, R, S, C) :- def_inv(R,S), prec(C,g), tripled(X, R, Y, C, main), not test_fails(C, nrel(Y, S, X)).\n" +
//			"ovrInv(X, Y, R, S, C) :- def_inv(R,S), prec(C,g), tripled(Y, S, X, C, main), not test_fails(C, nrel(X, R, Y)).\n" +
//			"ovrIrr(X, R, C) :- def_irr(R), prec(C,g), tripled(X, R, X, C, main).\n" +
//			// # TEST rules #
//			"test(C, nlit(X,Y)) :- def_insta(X,Y), prec(C,g).\n" +
//			"                   :- test_fails(C, nlit(X,Y)), ovrInsta(X, Y, C).\n" +
//			"test(C, nrel(X,R,Y)) :- def_triplea(X,R,Y), prec(C,g).\n" +
//			"                     :- test_fails(C, nrel(X,R,Y)), ovrTriplea(X, R, Y, C).\n" +			
//			"test(C, nlit(X,Z)) :- def_subclass(Y,Z), instd(X, Y, C, main), prec(C,g).\n" +
//			"                   :- test_fails(C, nlit(X,Z)), ovrSubClass(X, Y, Z, C).\n" +			
//			"test(C, nlit(X,Z)) :- def_subcnj(Y1, Y2, Z), instd(X, Y1, C, main), instd(X, Y2, C, main), prec(C,g).\n" +
//			"                   :- test_fails(C, nlit(X,Z)), ovrSubConj(X, Y1, Y2, Z, C).\n" +			
//			"test(C, nlit(X,Z)) :- def_subex(R, Y, Z), tripled(X, R, W, C, main), instd(W, Y, C, main), prec(C,g).\n" +
//			"                   :- test_fails(C, nlit(X,Z)), ovrSubEx(X, V, Y, Z, C).\n" +			
//			"test(C, nrel(X,R,W)) :- def_supex(Y, R, W), instd(X, Y, C, main), prec(C,g).\n" +
//			"                   :- test_fails(C, nrel(X,R,W)), ovrSupEx(X, Y, R, W, C).\n" +			
//			"test(C, nlit(Y,W)) :- def_supforall(Z,R,W), instd(X, Z, C, main), tripled(X, R, Y, C, main), prec(C,g).\n" +
//			"                   :- test_fails(C, nlit(Y,W)), ovrSupAll(X, Y, Z, R, W, C).\n" +						
//			//"test(C, neq(X1,X2)) :- def_supleqone(Z,R), instd(X, Z, C, main), tripled(X, R, X1, C, main), tripled(X, R, X2, C, main), prec(C,g).\n" +
//			//"                   :- test_fails(C, neq(X1,X2)), ovrSupLeqOne(X, X1, X2, Z, R, C).\n" +						
//			"test(C, nrel(X,S,Y)) :- def_subr(R,S), tripled(X, R, Y, C, main), prec(C,g).\n" +
//			"                   :- test_fails(C, nrel(X,S,Y)), ovrSubRole(X, Y, R, S, C).\n" +			
//			"test(C, nrel(X, T, Z)) :- def_subrc(R,S,T), tripled(X, R, Y, C, main), tripled(Y, S, Z, C, main), prec(C,g).\n" +
//			"                   :- test_fails(C, nrel(X, T, Z)), ovrSubRChain(X, Y, Z, R, S, T, C).\n" +			
//			//"test(C, nrel(X, S, Y)) :- def_dis(R,S), tripled(X, R, Y, C, main), prec(C,G).\n" +
//			//"                   :- test_fails(C, nrel(X, S, Y)), ovrDis(X, Y, R, S, C).\n" +						
//			"test(C, nrel(Y, S, X)) :- def_inv(R,S), tripled(X, R, Y, C, main), prec(C,g).\n" +
//			"test(C, nrel(Y, R, X)) :- def_inv(R,S), tripled(X, S, Y, C, main), prec(C,g).\n" +
//			"                   :- test_fails(C, nrel(X, S, Y)), ovrInv(X, Y, R, S, C).\n" +
//			"                   :- test_fails(C, nrel(X, R, Y)), ovrInv(X, Y, R, S, C).\n" +
//			//"test(C, nrel(X, R, X)) :- def_irr(R), tripled(X, R, Y, C, main), prec(C,G).\n" +
//			//"                   :- test_fails(C, nrel(X, R, X)), ovrIrr(X, R, C).\n" +		
//			"test_fails(C, nlit(X, Z)) :- instd(X, Z, C, nlit(X, Z)), not unsat(C, nlit(X, Z)).\n" +
//			"test_fails(C, nrel(X, R, Y)) :- tripled(X, R, Y, C, nrel(X, R, Y)), not unsat(C, nrel(X, R, Y)).\n" +
//			//"test_fails(C, neq(X1, X2)) :- eq(X1, X2, C, neq(X1, X2)), not unsat(C, neq(X1, X2)).\n" +
//            "instd(X, Z, C, nlit(X, Z)) :- test(C, nlit(X, Z)).\n" +
//            "tripled(X, R, Y, C, nrel(X, R, Y)) :- test(C, nrel(X, R, Y)).\n" +
//            //"eq(X1, X2, C, neq(X1, X2)) :- test(C, neq(X1, X2)).\n" +
//            "instd(X1, Y1, C, Z) :- instd(X1, Y1, C, main), test(C, Z).\n" +
//            "tripled(X1, R, Y1, C, Z) :- tripled(X1, R, Y1, C, main), test(C, Z).\n" +
//            //"eq(X1, X2, C, Z) :- eq(X1, X2, C, main), test(C, Z).\n" +
//			"";

	//### NEW TEST(lit(x,y,c)) RULES ###
	private final static String strPd =			
			// # Assertions propagation #
			"instd(X, Z, C, T) :- insta(X, Z, G, T), prec(C, G), not ovrInsta(X, Z, C).\n" +
			"tripled(X, R, Y, C, T) :- triplea(X, R, Y, G, T), prec(C, G), not ovrTriplea(X, R, Y, C).\n" +
			"unsat(T) :- ninsta(X, Z, G), instd(X, Z, C, T), prec(C, G), not ovrNinsta(X, Z, C).\n" +
			"unsat(T) :- ntriplea(X, R, Y, G), tripled(X, R, Y, C, T), prec(C, G), not ovrNtriplea(X, R, Y, C).\n" +
			// # Class axioms propagation #			
			"instd(X, Z, C, T) :- subClass(Y, Z, G), instd(X, Y, C, T), prec(C, G), not ovrSubClass(X, Y, Z, C).\n" +
			"instd(X, Z, C, T) :- subConj(Y1, Y2, Z, G), instd(X, Y1, C, T), instd(X, Y2, C, T), prec(C, G), not ovrSubConj(X, Y1, Y2, Z, C).\n" +
			"instd(X, Z, C, T) :- subEx(V, Y, Z, G), tripled(X, V, X1, C, T), instd(X1, Y, C, T), prec(C, G), not ovrSubEx(X, V, Y, Z, C).\n" +
			"tripled(X, R, X1, C, T) :- supEx(Y, R, X1, G), instd(X, Y, C, T), prec(C, G), not ovrSupEx(X, Y, R, X1, C).\n" +			
			"instd(Y, Z1, C, T) :- supForall(Z, R, Z1, G), instd(X, Z, C, T), tripled(X, R, Y, C, T), prec(C, G), not ovrSupAll(X, Y, Z, R, Z1, C).\n" +
			"unsat(T) :- supLeqOne(Z, R, G), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T), X1 != X2, prec(C, G), not ovrSupLeqOne(X, X1, X2, Z, R, C).\n" +
			// # Roles axioms propagation #
			"tripled(X, W, X1, C, T) :- subRole(V, W, G), tripled(X, V, X1, C, T), prec(C, G), not ovrSubRole(X, X1, V, W, C).\n" +			
			"tripled(X, W, Z, C, T) :- subRChain(U, V, W, G), tripled(X, U, Y, C, T), tripled(Y, V, Z, C, T), prec(C, G), not ovrSubRChain(X, Y, Z, U, V, W, C).\n" +
			// # Roles properties propagation #
			"unsat(T) :- dis(U, V, G), tripled(X, U, Y, C, T), tripled(X, V, Y, C, T), prec(C, G), not ovrDis(X, Y, U, V, C).\n" +
			"tripled(Y, V, X, C, T) :- inv(U, V, G), tripled(X, U, Y, C, T), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
			"tripled(X, U, Y, C, T) :- inv(U, V, G), tripled(Y, V, X, C, T), prec(C, G), not ovrInv(X, Y, U, V, C).\n" +
			"unsat(T) :- irr(U, G), tripled(X, U, X, C, T), prec(C, G), not ovrIrr(X, U, C).\n" +
			// # OVR rules #
			"ovrInsta(X, Y, C) :- def_insta(X,Y), prec(C,g), not test_fails(nlit(X,Y,C)).\n" +
			"ovrTriplea(X, R, Y, C) :- def_triplea(X,R,Y), prec(C,g), not test_fails(nrel(X,R,Y,C)).\n" +
			"ovrNinsta(X, Z, C) :- def_ninsta(X,Z), prec(C,g), instd(X, Z, C, main).\n" +
			"ovrNtriplea(X, R, Y, C) :- def_ntriplea(X,R,Y), prec(C,g), tripled(X, R, Y, C, main).\n" +
			"ovrSubClass(X, Y, Z, C) :- def_subclass(Y,Z), prec(C,g), instd(X, Y, C, main), not test_fails(nlit(X,Z,C)).\n" +
			"ovrSubConj(X, Y1, Y2, Z, C) :- def_subcnj(Y1,Y2,Z), prec(C,g), instd(X, Y1, C, main), instd(X, Y2, C, main), not test_fails(nlit(X,Z,C)).\n" +
			"ovrSubEx(X, R, Y, Z, C) :- def_subex(R,Y,Z), prec(C,g), tripled(X, R, W, C, main), instd(W, Y, C, main), not test_fails(nlit(X,Z,C)).\n" +
			"ovrSupEx(X, Y, R, W, C) :- def_supex(Y,R,W), prec(C,g), instd(X, Y, C, main), not test_fails(nrel(X,R,W,C)).\n" +
			"ovrSupAll(X, Y, Z, R, W, C) :- def_supforall(Z,R,W), prec(C,g), instd(X, Z, C, main), tripled(X, R, Y, C, main), not test_fails(nlit(Y,W,C)).\n" +
			"ovrSupLeqOne(X, X1, X2, Z, R, C) :- def_supleqone(Z,R), prec(C,g), instd(X, Z, C, main), tripled(X, R, X1, C, main), tripled(X, R, X2, C, main), X1 != X2.\n" +			
			"ovrSubRole(X, Y, R, S, C) :- def_subr(R,S), prec(C,g), tripled(X, R, Y, C, main), not test_fails(nrel(X, S, Y, C)).\n" +
			"ovrSubRChain(X, Y, Z, R, S, T, C) :- def_subrc(R,S,T), prec(C,g), tripled(X, R, Y, C, main), tripled(Y, S, Z, C, main), not test_fails(nrel(X, T, Z, C)).\n" +			
			"ovrDis(X, Y, R, S, C) :- def_dis(R,S), prec(C,g), tripled(X, R, Y, C, main), tripled(X, S, Y, C, main).\n" +
			"ovrInv(X, Y, R, S, C) :- def_inv(R,S), prec(C,g), tripled(X, R, Y, C, main), not test_fails(nrel(Y, S, X, C)).\n" +
			"ovrInv(X, Y, R, S, C) :- def_inv(R,S), prec(C,g), tripled(Y, S, X, C, main), not test_fails(nrel(X, R, Y, C)).\n" +
			"ovrIrr(X, R, C) :- def_irr(R), prec(C,g), tripled(X, R, X, C, main).\n" +
			// # TEST rules #
			"test(nlit(X,Y,C)) :- def_insta(X,Y), prec(C,g).\n" +
			"                  :- test_fails(nlit(X,Y,C)), ovrInsta(X, Y, C).\n" +
			"test(nrel(X,R,Y,C)) :- def_triplea(X,R,Y), prec(C,g).\n" +
			"                     :- test_fails(nrel(X,R,Y,C)), ovrTriplea(X, R, Y, C).\n" +			
			"test(nlit(X,Z,C)) :- def_subclass(Y,Z), instd(X, Y, C, main), prec(C,g).\n" +
			"                   :- test_fails(nlit(X,Z,C)), ovrSubClass(X, Y, Z, C).\n" +			
			"test(nlit(X,Z,C)) :- def_subcnj(Y1, Y2, Z), instd(X, Y1, C, main), instd(X, Y2, C, main), prec(C,g).\n" +
			"                   :- test_fails(nlit(X,Z,C)), ovrSubConj(X, Y1, Y2, Z, C).\n" +			
			"test(nlit(X,Z,C)) :- def_subex(R, Y, Z), tripled(X, R, W, C, main), instd(W, Y, C, main), prec(C,g).\n" +
			"                   :- test_fails(nlit(X,Z,C)), ovrSubEx(X, V, Y, Z, C).\n" +			
			"test(nrel(X,R,W,C)) :- def_supex(Y, R, W), instd(X, Y, C, main), prec(C,g).\n" +
			"                   :- test_fails(nrel(X,R,W,C)), ovrSupEx(X, Y, R, W, C).\n" +			
			"test(nlit(Y,W,C)) :- def_supforall(Z,R,W), instd(X, Z, C, main), tripled(X, R, Y, C, main), prec(C,g).\n" +
			"                   :- test_fails(nlit(Y,W,C)), ovrSupAll(X, Y, Z, R, W, C).\n" +						
			//"test(C, neq(X1,X2)) :- def_supleqone(Z,R), instd(X, Z, C, main), tripled(X, R, X1, C, main), tripled(X, R, X2, C, main), prec(C,g).\n" +
			//"                   :- test_fails(C, neq(X1,X2)), ovrSupLeqOne(X, X1, X2, Z, R, C).\n" +						
			"test(nrel(X,S,Y,C)) :- def_subr(R,S), tripled(X, R, Y, C, main), prec(C,g).\n" +
			"                   :- test_fails(nrel(X,S,Y,C)), ovrSubRole(X, Y, R, S, C).\n" +			
			"test(nrel(X, T, Z, C)) :- def_subrc(R,S,T), tripled(X, R, Y, C, main), tripled(Y, S, Z, C, main), prec(C,g).\n" +
			"                   :- test_fails(nrel(X, T, Z, C)), ovrSubRChain(X, Y, Z, R, S, T, C).\n" +			
			//"test(C, nrel(X, S, Y)) :- def_dis(R,S), tripled(X, R, Y, C, main), prec(C,G).\n" +
			//"                   :- test_fails(C, nrel(X, S, Y)), ovrDis(X, Y, R, S, C).\n" +						
			"test(nrel(Y, S, X, C)) :- def_inv(R,S), tripled(X, R, Y, C, main), prec(C,g).\n" +
			"test(nrel(Y, R, X, C)) :- def_inv(R,S), tripled(X, S, Y, C, main), prec(C,g).\n" +
			"                   :- test_fails(nrel(X, S, Y, C)), ovrInv(X, Y, R, S, C).\n" +
			"                   :- test_fails(nrel(X, R, Y, C)), ovrInv(X, Y, R, S, C).\n" +
			//"test(C, nrel(X, R, X)) :- def_irr(R), tripled(X, R, Y, C, main), prec(C,G).\n" +
			//"                   :- test_fails(C, nrel(X, R, X)), ovrIrr(X, R, C).\n" +		
			"test_fails(nlit(X, Z, C)) :- instd(X, Z, C, nlit(X, Z, C)), not unsat(nlit(X, Z, C)).\n" +
			"test_fails(nrel(X, R, Y, C)) :- tripled(X, R, Y, C, nrel(X, R, Y, C)), not unsat(nrel(X, R, Y, C)).\n" +
			//"test_fails(C, neq(X1, X2)) :- eq(X1, X2, C, neq(X1, X2)), not unsat(C, neq(X1, X2)).\n" +
            "instd(X, Z, C, nlit(X, Z, C)) :- test(nlit(X, Z, C)).\n" +
            "tripled(X, R, Y, C, nrel(X, R, Y, C)) :- test(nrel(X, R, Y, C)).\n" +
            //"eq(X1, X2, C, neq(X1, X2)) :- test(C, neq(X1, X2)).\n" +
            "instd(X1, Y1, C, Z) :- instd(X1, Y1, C, main), test(Z).\n" +
            "tripled(X1, R, Y1, C, Z) :- tripled(X1, R, Y1, C, main), test(Z).\n" +
            //"eq(X1, X2, C, Z) :- eq(X1, X2, C, main), test(C, Z).\n" +
			"";	
	
	private final static String strPloc = 
			"instd(X,B,C,T) :- subEval(A, C1, B, C), instd(CP, C1, G, T), instd(X, A, CP, T).\n" +
	        "tripled(X,S,Y,C,T) :- subEvalR(R, C1, S, C), instd(CP, C1, G, T), tripled(X, R, Y, CP, T).\n" + 
			//"-instd(X, A, CP) :- subEval(A, C1, B, C), instd(CP, C1, G), -instd(X,B,C).\n" +
	        //"-tripled(X, R, Y, CP) :- subEvalR(R, C1, T, C), instd(CP, C1, G), -tripled(X,T,Y,C).\n" +
	        //"-eq(X,Y,C) :- nom(X, C), -eq(X,Y,C1).\n" +
	        //"eq(X,Y,C,T) :- nom(X, C), eq(X,Y,C1,T).\n" +
	        "" ;
	
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