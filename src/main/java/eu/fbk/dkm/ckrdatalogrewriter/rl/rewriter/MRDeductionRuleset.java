package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.io.StringReader;
import java.util.List;

import org.semanticweb.drew.dlprogram.model.ProgramStatement;
import org.semanticweb.drew.dlprogram.parser.DLProgramParser;
import org.semanticweb.drew.dlprogram.parser.ParseException;

/**
 * @author Loris
 * @version 1.6
 * 
 * Encoding of <code>Pdlr</code>, <code>Pd-dlr</code> 
 * deduction rules for MR-CKR datalog translation.
 * (Version for DLV).
 */
public class MRDeductionRuleset {

	//### MR-CKR Global deduction rules ###
	private final static String strPglob = 
			// # Assertions rules # 
			"relation(covers).\n" + //
			"relation(time).\n" + //

			"relation_weight(covers, 1)." + //
			"relation_weight(time, 2)." + //

			"prec(C1, C2, REL) :- prec(C1, C3, REL), prec(C3, C2, REL).\n" + //
			"preceq(C1, C2, REL) :- prec(C1, C2, REL).\n" + //
			"preceq(C1, C1, REL) :- context(C1), relation(REL).\n" + //
			
			"";		
	
	//### SROIQ-RL local deduction rukes ###
	private final static String strPrl = 
			// # Assertions rules #
			"instd(X, Z, C, main) :- insta(X, Z, C).\n" + //
			"tripled(X, R, Y, C, main) :- triplea(X, R, Y, C).\n" + //
			
			//"unsat(T) :- ninsta(X, Z, C), instd(X, Z, C, T).\n" + //
			//"unsat(T) :- ntriplea(X, R, Y, C), tripled(X, R, Y, C, T).\n" + //
			"unsat(T) :- eq(X, Y, C, T).\n" + //
			// # \top and \bot rules #
			"instd(X, top, C, main) :- nom(X, C).\n" + //
			"unsat(T) :- instd(X, bot, C, T).\n" + //			
			// # Class axioms rules #
			"instd(X, Z, C, T) :- subClass(Y, Z, C), instd(X, Y, C, T).\n" + //			
			"instd(X, Z, C, T) :- subConj(Y1, Y2, Z, C), instd(X, Y1, C, T), instd(X, Y2, C, T).\n" + //
			"instd(X, Z, C, T) :- subEx(V, Y, Z, C), tripled(X, V, XP, C, T), instd(XP, Y, C, T).\n" + //
			"tripled(X, R, X1, C, T) :- supEx(Y, R, X1, C), instd(X, Y, C, T).\n" + //			
			"instd(Y, Z1, C, T) :- supForall(Z, R, Z1, C), instd(X, Z, C, T), tripled(X, R, Y, C, T).\n" + //
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
	
	//### MR-CKR rules for defeasible axioms ###
	private final static String strPd =			
			// ### STRICT PROPAGATION ###
			"instd(X, Z, C, main) :- insta(X, Z, C1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"tripled(X, R, Y, C, main) :- triplea(X, R, Y, C1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			//"unsat(T) :- ninsta(X, Z, C1), instd(X, Z, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			//"unsat(T) :- ntriplea(X, R, Y, C1), tripled(X, R, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +

			"instd(X, Z, C, T) :- subClass(Y, Z, C1), instd(X, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"instd(X, Z, C, T) :- subConj(Y1, Y2, Z, C1), instd(X, Y1, C, T), instd(X, Y2, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"instd(X, Z, C, T) :- subEx(V, Y, Z, C1), tripled(X, V, X1, C, T), instd(X1, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"tripled(X, R, X1, C, T) :- supEx(Y, R, X1, C1), instd(X, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +			
			"instd(Y, Z1, C, T) :- supForall(Z, R, Z1, C1), instd(X, Z, C, T), tripled(X, R, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"unsat(T) :- supLeqOne(Z, R, C1), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T), X1 != X2, prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +

			"tripled(X, W, X1, C, T) :- subRole(V, W, C1), tripled(X, V, X1, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +			
			"tripled(X, W, Z, C, T) :- subRChain(U, V, W, C1), tripled(X, U, Y, C, T), tripled(Y, V, Z, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +

			"unsat(T) :- dis(U, V, C1), tripled(X, U, Y, C, T), tripled(X, V, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"tripled(Y, V, X, C, T) :- inv(U, V, C1), tripled(X, U, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"tripled(X, U, Y, C, T) :- inv(U, V, C1), tripled(Y, V, X, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"unsat(T) :- irr(U, C1), tripled(X, U, X, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +			
			
			
			// ### DEFEASIBLE PROPAGATION ###
			//"instd(X, Z, C, main) :- def_insta(X,Z,C1,REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrInsta(X, Z, C1, C, REL1).\n" +
			//"tripled(X, R, Y, C, main) :- def_triplea(X, R, Y, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrTriplea(X, R, Y, C1, C, REL1).\n" +
			//"unsat(T) :- def_ninsta(X, Z, C1, REL1), instd(X, Z, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrNinsta(X, Z, C1, C, REL1).\n" +
			//"unsat(T) :- def_ntriplea(X, R, Y, C1, REL1), tripled(X, R, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrNtriplea(X, R, Y, C1, C, REL1).\n" +
			
			"instd(X, Z, C, T) :- def_subclass(Y, Z, C1, REL1), instd(X, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrSubClass(X, Y, Z, C1, C, REL1).\n" +
			"instd(X, Z, C, T) :- def_subcnj(Y1, Y2, Z, C1, REL1), instd(X, Y1, C, T), instd(X, Y2, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrSubConj(X, Y1, Y2, Z, C1, C, REL1).\n" +
			"instd(X, Z, C, T) :- def_subex(V, Y, Z, C1, REL1), tripled(X, V, X1, C, T), instd(X1, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrSubEx(X, V, Y, Z, C1, C, REL1).\n" +
			"tripled(X, R, X1, C, T) :- def_supex(Y, R, X1, C1, REL1), instd(X, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrSupEx(X, Y, R, X1, C1, C, REL1).\n" +
			"instd(Y, Z1, C, T) :- def_supforall(Z, R, Z1, C1, REL1), instd(X, Z, C, T), tripled(X, R, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrSupAll(X, Y, Z, R, Z1, C1, C, REL1).\n" +
			"unsat(T) :- def_supleqone(Z, R, C1, REL1), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T), X1 != X2, prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrSupLeqOne(X, X1, X2, Z, R, C1, C, REL1).\n" +
			
			"tripled(X, W, X1, C, T) :- def_subr(V, W, C1, REL1), tripled(X, V, X1, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrSubRole(X, X1, V, W, C1, C, REL1).\n" +			
			"tripled(X, W, Z, C, T) :- def_subrc(U, V, W, C1, REL1), tripled(X, U, Y, C, T), tripled(Y, V, Z, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrSubRChain(X, Y, Z, U, V, W, C1, C, REL1).\n" +

			"unsat(T) :- def_dis(U, V, C1, REL1), tripled(X, U, Y, C, T), tripled(X, V, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrDis(X, Y, U, V, C1, C, REL1).\n" +
			"tripled(Y, V, X, C, T) :- def_inv(U, V, C1, REL1), tripled(X, U, Y, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrInv(X, Y, U, V, C1, C, REL1).\n" +
			"tripled(X, U, Y, C, T) :- def_inv(U, V, C1, REL1), tripled(Y, V, X, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrInv(X, Y, U, V, C1, C, REL1).\n" +
			"unsat(T) :- def_irr(U, C1, REL1), tripled(X, U, X, C, T), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not ovrIrr(X, U, C1, C, REL1).\n" +

			
			// ### PARALLEL PROPAGATION ###
			//"instd(X, Z, C, main) :- def_insta(X,Z,C1,REL1), preceq(C,C1,REL2), REL1 != REL2.\n" +
			//"tripled(X, R, Y, C, main) :- def_triplea(X, R, Y, C1, REL1), preceq(C,C1,REL2), REL1 != REL2.\n" +
			//"unsat(T) :- def_ninsta(X, Z, C1, REL1), instd(X, Z, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			//"unsat(T) :- def_ntriplea(X, R, Y, C1, REL1), tripled(X, R, Y, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +

			"instd(X, Z, C, T) :- def_subclass(Y, Z, C1, REL1), instd(X, Y, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			"instd(X, Z, C, T) :- def_subcnj(Y1, Y2, Z, C1, REL1), instd(X, Y1, C, T), instd(X, Y2, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			"instd(X, Z, C, T) :- def_subex(V, Y, Z, C1, REL1), tripled(X, V, X1, C, T), instd(X1, Y, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			"tripled(X, R, X1, C, T) :- def_supex(Y, R, X1, C1, REL1), instd(X, Y, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			"instd(Y, Z1, C, T) :- def_supforall(Z, R, Z1, C1, REL1), instd(X, Z, C, T), tripled(X, R, Y, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			"unsat(T) :- def_supleqone(Z, R, C1, REL1), instd(X, Z, C, T), tripled(X, R, X1, C, T), tripled(X, R, X2, C, T), X1 != X2, preceq(C,C1,REL2), REL1 != REL2.\n" +

			"tripled(X, W, X1, C, T) :- def_subr(V, W, C1, REL1), tripled(X, V, X1, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +			
			"tripled(X, W, Z, C, T) :- def_subrc(U, V, W, C1, REL1), tripled(X, U, Y, C, T), tripled(Y, V, Z, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +

			"unsat(T) :- def_dis(U, V, C1, REL1), tripled(X, U, Y, C, T), tripled(X, V, Y, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			"tripled(Y, V, X, C, T) :- def_inv(U, V, C1, REL1), tripled(X, U, Y, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			"tripled(X, U, Y, C, T) :- def_inv(U, V, C1, REL1), tripled(Y, V, X, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +
			"unsat(T) :- def_irr(U, C1, REL1), tripled(X, U, X, C, T), preceq(C,C1,REL2), REL1 != REL2.\n" +

		
			// ### OVR rules ###
			//"ovrInsta(X, Y, C1, C, REL1) :- def_insta(X,Y,C1,REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not test_fails(nlit(X,Y,C)).\n" +
			//"ovrTriplea(X, R, Y, C1, C, REL1) :- def_triplea(X,R,Y, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, not test_fails(nrel(X,R,Y,C)).\n" +
			//"ovrNinsta(X, Z, C1, C, REL1) :- def_ninsta(X,Z, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, instd(X, Z, C, main).\n" +
			//"ovrNtriplea(X, R, Y, C1, C, REL1) :- def_ntriplea(X,R,Y, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, tripled(X, R, Y, C, main).\n" +
			
			"ovrSubClass(X, Y, Z, C1, C, REL1) :- def_subclass(Y,Z, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, instd(X, Y, C, main), not test_fails(nlit(X,Z,C)).\n" +
			"ovrSubConj(X, Y1, Y2, Z, C1, C, REL1) :- def_subcnj(Y1,Y2,Z, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, instd(X, Y1, C, main), instd(X, Y2, C, main), not test_fails(nlit(X,Z,C)).\n" +
			"ovrSubEx(X, R, Y, Z, C1, C, REL1) :- def_subex(R,Y,Z, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, tripled(X, R, W, C, main), instd(W, Y, C, main), not test_fails(nlit(X,Z,C)).\n" +
			"ovrSupEx(X, Y, R, W, C1, C, REL1) :- def_supex(Y,R,W, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, instd(X, Y, C, main), not test_fails(nrel(X,R,W,C)).\n" +
			"ovrSupAll(X, Y, Z, R, W, C1, C, REL1) :- def_supforall(Z,R,W, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, instd(X, Z, C, main), tripled(X, R, Y, C, main), not test_fails(nlit(Y,W,C)).\n" +
			"ovrSupLeqOne(X, X1, X2, Z, R, C1, C, REL1) :- def_supleqone(Z,R, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, instd(X, Z, C, main), tripled(X, R, X1, C, main), tripled(X, R, X2, C, main), X1 != X2.\n" +			

			"ovrSubRole(X, Y, R, S, C1, C, REL1) :- def_subr(R,S, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, tripled(X, R, Y, C, main), not test_fails(nrel(X, S, Y, C)).\n" +
			"ovrSubRChain(X, Y, Z, R, S, T, C1, C, REL1) :- def_subrc(R,S,T, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, tripled(X, R, Y, C, main), tripled(Y, S, Z, C, main), not test_fails(nrel(X, T, Z, C)).\n" +			
			
			"ovrDis(X, Y, R, S, C1, C, REL1) :- def_dis(R,S, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, tripled(X, R, Y, C, main), tripled(X, S, Y, C, main).\n" +
			"ovrInv(X, Y, R, S, C1, C, REL1) :- def_inv(R,S, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, tripled(X, R, Y, C, main), not test_fails(nrel(Y, S, X, C)).\n" +
			"ovrInv(X, Y, R, S, C1, C, REL1) :- def_inv(R,S, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, tripled(Y, S, X, C, main), not test_fails(nrel(X, R, Y, C)).\n" +
			"ovrIrr(X, R, C1, C, REL1) :- def_irr(R, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2, tripled(X, R, X, C, main).\n" +

			
			// ### TEST rules ###
			//"test(nlit(X,Y,C)) :- def_insta(X,Y,C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			//"                  :- test_fails(nlit(X,Y,C)), ovrInsta(X, Y, C1, C, REL).\n" +
			//"test(nrel(X,R,Y,C)) :- def_triplea(X,R,Y, C1, REL1), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			//"                     :- test_fails(nrel(X,R,Y,C)), ovrTriplea(X, R, Y, C1, C, REL).\n" +
			
			"test(nlit(X,Z,C)) :- def_subclass(Y,Z, C1, REL1), instd(X, Y, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"                   :- test_fails(nlit(X,Z,C)), ovrSubClass(X, Y, Z, C1, C, REL).\n" +			
			"test(nlit(X,Z,C)) :- def_subcnj(Y1, Y2, Z, C1, REL1), instd(X, Y1, C, main), instd(X, Y2, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"                   :- test_fails(nlit(X,Z,C)), ovrSubConj(X, Y1, Y2, Z, C1, C, REL).\n" +			
			"test(nlit(X,Z,C)) :- def_subex(R, Y, Z, C1, REL1), tripled(X, R, W, C, main), instd(W, Y, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"                   :- test_fails(nlit(X,Z,C)), ovrSubEx(X, V, Y, Z, C1, C, REL).\n" +			
			"test(nrel(X,R,W,C)) :- def_supex(Y, R, W, C1, REL1), instd(X, Y, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"                   :- test_fails(nrel(X,R,W,C)), ovrSupEx(X, Y, R, W, C1, C, REL).\n" +			
			"test(nlit(Y,W,C)) :- def_supforall(Z,R,W, C1, REL1), instd(X, Z, C, main), tripled(X, R, Y, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"                   :- test_fails(nlit(Y,W,C)), ovrSupAll(X, Y, Z, R, W, C1, C, REL).\n" +						
			
			"test(nrel(X,S,Y,C)) :- def_subr(R,S, C1, REL1), tripled(X, R, Y, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"                   :- test_fails(nrel(X,S,Y,C)), ovrSubRole(X, Y, R, S, C1, C, REL).\n" +			
			"test(nrel(X, T, Z, C)) :- def_subrc(R,S,T, C1, REL1), tripled(X, R, Y, C, main), tripled(Y, S, Z, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"                   :- test_fails(nrel(X, T, Z, C)), ovrSubRChain(X, Y, Z, R, S, T, C1, C, REL).\n" +			
			
			//"test(C, nrel(X, S, Y)) :- def_dis(R,S), tripled(X, R, Y, C, main), prec(C,G).\n" +
			//"                   :- test_fails(C, nrel(X, S, Y)), ovrDis(X, Y, R, S, C).\n" +						
			"test(nrel(Y, S, X, C)) :- def_inv(R,S, C1, REL1), tripled(X, R, Y, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"test(nrel(Y, R, X, C)) :- def_inv(R,S, C1, REL1), tripled(X, S, Y, C, main), prec(C,C2,REL1), preceq(C2,C1,REL2), REL1 != REL2.\n" +
			"                   :- test_fails(nrel(X, S, Y, C)), ovrInv(X, Y, R, S, C1, C, REL1).\n" +
			"                   :- test_fails(nrel(X, R, Y, C)), ovrInv(X, Y, R, S, C1, C, REL1).\n" +
			//"test(C, nrel(X, R, X)) :- def_irr(R), tripled(X, R, Y, C, main), prec(C,G).\n" +
			//"                   :- test_fails(C, nrel(X, R, X)), ovrIrr(X, R, C).\n" +		
			
			"test_fails(nlit(X, Z, C)) :- instd(X, Z, C, nlit(X, Z, C)), not unsat(nlit(X, Z, C)).\n" +
			"test_fails(nrel(X, R, Y, C)) :- tripled(X, R, Y, C, nrel(X, R, Y, C)), not unsat(nrel(X, R, Y, C)).\n" +

			"instd(X, Z, C, nlit(X, Z, C)) :- test(nlit(X, Z, C)).\n" +
            "tripled(X, R, Y, C, nrel(X, R, Y, C)) :- test(nrel(X, R, Y, C)).\n" +
            
            "instd(X1, Y1, C, Z) :- instd(X1, Y1, C, main), test(Z).\n" +
            "tripled(X1, R, Y1, C, Z) :- tripled(X1, R, Y1, C, main), test(Z).\n" +

            "";	
		
	//### EVAL local rules ###
	private final static String strPeval = 
			"instd(X,B,C,T) :- subEval(A, C1, B, C), instd(X, A, C1, T).\n" +
	        "tripled(X,S,Y,C,T) :- subEvalR(R, C1, S, C), tripled(X, R, Y, C1, T).\n" + 
					
			"instd(X,B,C,T) :- subEval(A, C1, B, C2), instd(X, A, C1, T), prec(C, C3, REL1), preceq(C3, C2, REL2), REL1 != REL2.\n" +
	        "tripled(X,S,Y,C,T) :- subEvalR(R, C1, S, C2), tripled(X, R, Y, C1, T), prec(C, C3, REL1), preceq(C3, C2, REL2), REL1 != REL2.\n" + 
	        "" ;
	
	private static List<ProgramStatement> pglob, prl, pd, peval;

	//--- INITIALIZATION ----------------------------------------------------------
	
	static {
		
		StringReader reader = new StringReader(strPglob);
		DLProgramParser dlProgramParser = new DLProgramParser(reader);
		try {
			pglob = dlProgramParser.program().getStatements();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		reader = new StringReader(strPrl);
		dlProgramParser = new DLProgramParser(reader);
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
		
		reader = new StringReader(strPeval);
		dlProgramParser = new DLProgramParser(reader);
		try {
			peval = dlProgramParser.program().getStatements();
		} catch (ParseException e) {
			e.printStackTrace();
		}
			
	}

	//--- GET METHODS ----------------------------------------------------------
	
	public static List<ProgramStatement> getPglob() {
		return pglob;
	}

	public static List<ProgramStatement> getPrl() {
		return prl;
	}

	public static List<ProgramStatement> getPd() {
		return pd;
	}
	
	public static List<ProgramStatement> getPeval() {
		return peval;
	}

}
//=======================================================================