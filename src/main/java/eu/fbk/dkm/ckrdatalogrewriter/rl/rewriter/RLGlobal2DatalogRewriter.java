package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
//import org.semanticweb.drew.el.SymbolEncoder;
//import org.semanticweb.drew.el.reasoner.DatalogFormat;
//import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Loris
 * @version 1.4
 * 
 * Encodes the <code>Irl</code> and <code>Iglob</code>
 * rules for translation to Datalog of the global ontology.
 * 
 */
public class RLGlobal2DatalogRewriter extends RLContext2DatalogRewriter {
	
	private OWLAnnotationProperty hasAxiomType;
	private OWLAnnotationValue defeasible;
	//private Term contextTerm, globalTerm, indiv1Term, indiv2Term, indiv3Term;

    //--- CONSTRUCTORS ----------------------------------------------------------
    
    public RLGlobal2DatalogRewriter() {

        //SymbolEncoder<IRI> iriEncoder = CKRDReWELManager.getInstance().getIRIEncoder();
        //SymbolEncoder<OWLSubClassOfAxiom> superSomeAxiomEncoder = CKRDReWELManager.getInstance()
        //        .getSuperSomeAxiomEncoder();
    	
    	this.contextID = IRI.create("g");
    	
//		this.contextTerm = getVariable("C");
//		this.globalTerm = getConstant(contextID);
//		this.indiv1Term = getVariable("X");
//		this.indiv2Term = getVariable("Y");
//		this.indiv3Term = getVariable("Z");
	}

    //--- REWRITE METHOD --------------------------------------------------------
    
    @Override
	/**
	 * Returns the rewritten (global) ontology as a Datalog program.
	 * 
	 * @param ontology the ontology (global) to be rewritten
	 * @return the output Datalog program
	 */
	public DLProgram rewrite(OWLOntology ontology) {
		datalog = new DLProgram();
		
		//Instantiate global properties.
		this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
    	this.hasAxiomType = factory.getOWLAnnotationProperty(
				IRI.create(CKR_SCHEMA_URI, "hasAxiomType"));
    	this.defeasible = IRI.create(CKR_SCHEMA_URI, "defeasible");
		
		//Normalize input ontology.
		ontology = new RLGlobalNormalization().normalize(ontology);
		    	    	
		//Rewriting for ontology components.
		for (OWLLogicalAxiom ax : ontology.getLogicalAxioms()) {

			//For every logical axiom, check if it is defeasible
			//if (isDefeasible(ax))
			//	System.out.println("Defeasible axiom: " + ax.getAxiomWithoutAnnotations());
			ax.accept(this);
		}
		
		for (OWLNamedIndividual i : ontology.getIndividualsInSignature()) {
			i.accept(this);
		}

		for (OWLObjectProperty p : ontology.getObjectPropertiesInSignature()) {
			p.accept(this);
		}

		for (OWLClass cls : ontology.getClassesInSignature()) {
			cls.accept(this);
		}

 		//Add Prl deduction rules.
		datalog.addAll(DeductionRuleset.getPrl());
		
		return datalog;
	}
    
	//--- IS DEFEASIBLE -----------------------------------------------------------------

	public boolean isDefeasible(OWLAxiom a){
		for (OWLAnnotation ann : a.getAnnotations(hasAxiomType)) {
			if (ann.getValue().equals(defeasible))
				return true;
		}
		return false;
	}
    
	//--- ONTOLOGY VISIT METHODS --------------------------------------------------------
	//NOTE: in visiting the axioms, we suppose that the ontology has already been normalized.
	
	//- - ABOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	//Rewrite C(a) + DEFEASIBLE
	public void visit(OWLClassAssertionAxiom axiom) {
		// int c =
		// iriEncoder.encode(axiom.getClassExpression().asOWLClass().getIRI());
		// int a =
		// iriEncoder.encode(axiom.getIndividual().asOWLNamedIndividual().getIRI());
		// addFact(RewritingVocabulary.SUB_CLASS, a, c);

		IRI individualIRI = axiom.getIndividual().asOWLNamedIndividual().getIRI(); 
		IRI classIRI = rewConceptName(axiom.getClassExpression().asOWLClass());
		
		addFact(CKRRewritingVocabulary.INSTA,
				individualIRI, 
				classIRI,
				contextID,
				IRI.create(MAIN));
		
		//add atom for def.axiom
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( C(a) ): " + axiom.getAxiomWithoutAnnotations());
	    	
//			Term indivTerm = getConstant(individualIRI);
//			Term classTerm = getConstant(classIRI);
//
//			Literal ovrinsta = getLiteral(true, CKRRewritingVocabulary.OVRINSTA, 
//					                              indivTerm, classTerm, contextTerm);
//			Literal ninstd = getLiteral(false, CKRRewritingVocabulary.INSTD, 
//					                            indivTerm, classTerm, contextTerm);
//			Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//					                          contextTerm, globalTerm);			
//			addRule(ovrinsta, ninstd, prec);

			addFact(CKRRewritingVocabulary.DEF_INSTA,
					individualIRI, 
					classIRI);
	    }
	}
	
	@Override
	//Rewrite R(a,b) + DEFEASIBLE
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		// int p =
		// iriEncoder.encode(axiom.getProperty().asOWLObjectProperty().getIRI());
		// int s =
		// iriEncoder.encode(axiom.getSubject().asOWLNamedIndividual().getIRI());
		// int o =
		// iriEncoder.encode(axiom.getObject().asOWLNamedIndividual().getIRI());
		//
		// addFact(RewritingVocabulary.SUP_EX, s, p, o, o);
		
		addFact(CKRRewritingVocabulary.TRIPLEA, //
				axiom.getSubject().asOWLNamedIndividual().getIRI(),
 				axiom.getProperty().asOWLObjectProperty().getIRI(),
				axiom.getObject().asOWLNamedIndividual().getIRI(),
				contextID,
				IRI.create(MAIN)//
		);
		
		//add fact for def.axiom
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( R(a,b) ): " + axiom.getAxiomWithoutAnnotations());
	    	
//			Term subjectTerm = getConstant(axiom.getSubject().asOWLNamedIndividual().getIRI());
//			Term propertyTerm = getConstant(axiom.getProperty().asOWLObjectProperty().getIRI());
//			Term objectTerm = getConstant(axiom.getObject().asOWLNamedIndividual().getIRI());
//
//			Literal ovrtriplea = getLiteral(true, CKRRewritingVocabulary.OVRTRIPLEA, 
//					                              subjectTerm, propertyTerm, objectTerm, contextTerm);
//			Literal ntripled = getLiteral(false, CKRRewritingVocabulary.TRIPLED, 
//					                              subjectTerm, propertyTerm, objectTerm, contextTerm);
//			Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//					                          contextTerm, globalTerm);
//			addRule(ovrtriplea, ntripled, prec);
	    	
			addFact(CKRRewritingVocabulary.DEF_TRIPLEA, //
					axiom.getSubject().asOWLNamedIndividual().getIRI(),
	 				axiom.getProperty().asOWLObjectProperty().getIRI(),
					axiom.getObject().asOWLNamedIndividual().getIRI()//
			);
	    }		
	}
	
	@Override
	//Rewrite \non R(a,b)
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		
//		addNegativeFact(CKRRewritingVocabulary.TRIPLEA, 
//				axiom.getSubject().asOWLNamedIndividual().getIRI(), 
//				axiom.getProperty().asOWLObjectProperty().getIRI(), 
//				axiom.getObject().asOWLNamedIndividual().getIRI(),
//				contextID
//		);
	
//		Constant ind1 = getConstant(axiom.getSubject().asOWLNamedIndividual().getIRI());
//		Constant roleTerm = getConstant(axiom.getProperty().asOWLObjectProperty().getIRI());
//		Constant ind2 = getConstant(axiom.getObject().asOWLNamedIndividual().getIRI());
//		
//		Literal unsat = getLiteral(true, CKRRewritingVocabulary.UNSAT, 
//                getConstant(contextID), getVariable("T"));
//		
//		Literal tripled = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                ind1, roleTerm, ind2, getConstant(contextID), getVariable("T"));
//
//		addRule(unsat, tripled);		
		
		addFact(CKRRewritingVocabulary.NTRIPLEA, //
				axiom.getSubject().asOWLNamedIndividual().getIRI(), //
				axiom.getProperty().asOWLObjectProperty().getIRI(), //
				axiom.getObject().asOWLNamedIndividual().getIRI(),
				contextID
		);
		
		//add fact for def.axiom
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( -R(a,b) ): " + axiom.getAxiomWithoutAnnotations());
	    	
//			Term subjectTerm = getConstant(axiom.getSubject().asOWLNamedIndividual().getIRI());
//			Term propertyTerm = getConstant(axiom.getProperty().asOWLObjectProperty().getIRI());
//			Term objectTerm = getConstant(axiom.getObject().asOWLNamedIndividual().getIRI());
//
//			Literal ovrntriplea = getLiteral(true, CKRRewritingVocabulary.OVRNTRIPLEA, 
//					                              subjectTerm, propertyTerm, objectTerm, contextTerm);
//			Literal tripled = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//					                              subjectTerm, propertyTerm, objectTerm, contextTerm);
//			Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//					                          contextTerm, globalTerm);			
//			addRule(ovrntriplea, tripled, prec);
	    	
			addFact(CKRRewritingVocabulary.DEF_NTRIPLEA, //
					axiom.getSubject().asOWLNamedIndividual().getIRI(), //
					axiom.getProperty().asOWLObjectProperty().getIRI(), //
					axiom.getObject().asOWLNamedIndividual().getIRI()
			);	    	
	    }		
	}
	
	//- - TBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    
	@Override
	//Rewrite A \subs B
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression subClass = axiom.getSubClass();
		OWLClassExpression superClass = axiom.getSuperClass();
		
		// atomic case: A \subs C
		if ((subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
				&& (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
			// int a =
			// iriEncoder.encode(axiom.getSubClass().asOWLClass().getIRI());
			// int c =
			// iriEncoder.encode(axiom.getSuperClass().asOWLClass().getIRI());

			//if (subClass.isOWLThing()) {
			//	// addFact(RewritingVocabulary.TOP, c);
			//	addFact(CKRRewritingVocabulary.TOP, axiom.getSuperClass()
			//			.asOWLClass().getIRI());
			//} else if (superClass.isOWLNothing()) {
			//	// addFact(RewritingVocabulary.BOT, a);
			//	addFact(CKRRewritingVocabulary.BOT, axiom.getSubClass()
			//			.asOWLClass().getIRI());
			//} else /* (!subClass.isOWLNothing() && !superClass.isOWLThing()) */{
				// addFact(RewritingVocabulary.SUB_CLASS, a, c);

			       addFact(CKRRewritingVocabulary.SUB_CLASS, //
						rewConceptName(subClass.asOWLClass()), 
						rewConceptName(superClass.asOWLClass()),
						contextID);
			//}
				//add fact for def.axiom
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( A subs B ): " + axiom.getAxiomWithoutAnnotations());
			    	
//					Term subclassTerm = getConstant(subClass.asOWLClass().getIRI());
//					Term superclassTerm = getConstant(superClass.asOWLClass().getIRI());
//
//					Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUBCLASS, 
//							                            indiv1Term, subclassTerm, superclassTerm, contextTerm);
//					Literal ninstd = getLiteral(false, CKRRewritingVocabulary.INSTD, 
//							                            indiv1Term, superclassTerm, contextTerm);
//					Literal instd = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv1Term, subclassTerm, contextTerm);
//					Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//							                          contextTerm, globalTerm);
//					addRule(ovrsubs, ninstd, instd, prec);

				    addFact(CKRRewritingVocabulary.DEF_SUBCLASS, //
							rewConceptName(subClass.asOWLClass()), 
							rewConceptName(superClass.asOWLClass())
					);
			    }								
		}
		
		// A' subclass C
		else if (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			// and(A, B) subclass C
			if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				// TODO: CASE WITH N>2 IN NORMALIZATION
				// OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) subClass;
				// Set<OWLClassExpression> operands = inter.getOperands();
				// int[] params = new int[3];
				// int i = 0;
				// for (OWLClassExpression op : operands) {
				//   params[i++] = iriEncoder.encode(op.asOWLClass().getIRI());
				// }
				// params[2] = iriEncoder.encode(superClass.asOWLClass().getIRI());
				// addFact(RewritingVocabulary.SUB_CONJ, params);
				OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) subClass;
				Set<OWLClassExpression> operands = inter.getOperands();
				IRI[] params = new IRI[4];
				int i = 0;
				for (OWLClassExpression op : operands) {
					params[i++] = rewConceptName(op.asOWLClass());
				}
				params[2] = rewConceptName(superClass.asOWLClass());
				params[3] = contextID;

				addFact(CKRRewritingVocabulary.SUB_CONJ, params);
				
				//add rule for overriding
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( A1 \\and A2 \\subs B ): " + axiom.getAxiomWithoutAnnotations());
			    	
//					Term subclassATerm = getConstant(params[0]);
//					Term subclassBTerm = getConstant(params[1]);
//					Term superclassTerm = getConstant(superClass.asOWLClass().getIRI());
//					
//					Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUBCONJ, 
//							                            indiv1Term, subclassATerm, subclassBTerm, superclassTerm, contextTerm);
//					Literal ninstd = getLiteral(false, CKRRewritingVocabulary.INSTD, 
//							                            indiv1Term, superclassTerm, contextTerm);
//					Literal instdA = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv1Term, subclassATerm, contextTerm);
//					Literal instdB = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv1Term, subclassBTerm, contextTerm);					
//					Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//							                          contextTerm, globalTerm);
//					addRule(ovrsubs, ninstd, instdA, instdB, prec);
			    	
			    	addFact(CKRRewritingVocabulary.DEF_SUBCONJ, params[0], params[1], params[2]);
			    }
			}
			
			// exist(R,A) subclass B
			else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) subClass;
				// int r =
				// iriEncoder.encode(some.getProperty().asOWLObjectProperty().getIRI());
				// int a =
				// iriEncoder.encode(some.getFiller().asOWLClass().getIRI());
				// int b = iriEncoder.encode(superClass.asOWLClass().getIRI());
				//
				// addFact(RewritingVocabulary.SUB_EX, r, a, b);
				addFact(CKRRewritingVocabulary.SUB_EX,//
						some.getProperty().asOWLObjectProperty().getIRI(),//
						rewConceptName(some.getFiller().asOWLClass()),//
						rewConceptName(superClass.asOWLClass()), 
						contextID);
				
				//add rule for overriding
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( \\exists R.A \\subs B ): " + axiom.getAxiomWithoutAnnotations());
			    	
//					Term propertyTerm = getConstant(some.getProperty().asOWLObjectProperty().getIRI());
//					Term subclassATerm = getConstant(some.getFiller().asOWLClass().getIRI());
//					Term superclassTerm = getConstant(superClass.asOWLClass().getIRI());
//
//					Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUBEX, 
//							                            indiv1Term, propertyTerm, subclassATerm, superclassTerm, contextTerm);
//					Literal ninstd = getLiteral(false, CKRRewritingVocabulary.INSTD, 
//							                            indiv1Term, superclassTerm, contextTerm);
//					Literal tripled = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                        indiv1Term, propertyTerm, indiv2Term, contextTerm);
//					Literal instd = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv2Term, subclassATerm, contextTerm);					
//					Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//							                          contextTerm, globalTerm);
//					addRule(ovrsubs, ninstd, tripled, instd, prec);	
			    	
					addFact(CKRRewritingVocabulary.DEF_SUBEX,//
							some.getProperty().asOWLObjectProperty().getIRI(),//
							rewConceptName(some.getFiller().asOWLClass()),//
							rewConceptName(superClass.asOWLClass())
					);
			    }

			} else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
				// {c} \subs A -> inst(c, A)
				OWLObjectOneOf oneOf = (OWLObjectOneOf) subClass;
				// int c =
				// iriEncoder.encode(oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI());
				// int a = iriEncoder.encode(superClass.asOWLClass().getIRI());
				// addFact(RewritingVocabulary.SUB_CLASS, c, a);
				addFact(CKRRewritingVocabulary.INSTA, //
						oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),//
						rewConceptName(superClass.asOWLClass()),
						contextID,
						IRI.create(MAIN));
				
				//add rule for overriding
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( {a} \\subs B ): " + axiom.getAxiomWithoutAnnotations());
			    	
//					Term indivTerm = getConstant(oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI());
//					Term classTerm = getConstant(superClass.asOWLClass().getIRI());
//
//					Literal ovrinsta = getLiteral(true, CKRRewritingVocabulary.OVRINSTA, 
//							                              indivTerm, classTerm, contextTerm);
//					Literal ninstd = getLiteral(false, CKRRewritingVocabulary.INSTD, 
//							                            indivTerm, classTerm, contextTerm);
//					Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//							                          contextTerm, globalTerm);					
//					addRule(ovrinsta, ninstd, prec);	
			    	
					addFact(CKRRewritingVocabulary.DEF_INSTA,
							oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),//
							rewConceptName(superClass.asOWLClass())
					);			    	
			    }

			} else {
				throw new IllegalStateException();
			}
		}
		
		// A subclass C'
		else if (subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			
			if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) superClass;
				OWLObjectOneOf object = (OWLObjectOneOf) some.getFiller();
				
				addFact(CKRRewritingVocabulary.SUP_EX, //
						rewConceptName(subClass.asOWLClass()),//
						some.getProperty().asOWLObjectProperty().getIRI(),
						object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),
						contextID);

				//add rule for overriding
				if(isDefeasible(axiom)){
					//System.out.println("D( A \\subs \\exists R.{a} ): " + axiom.getAxiomWithoutAnnotations());
					
//					Term propertyTerm = getConstant(some.getProperty().asOWLObjectProperty().getIRI());
//					Term subclassTerm = getConstant(subClass.asOWLClass().getIRI());
//					Term nominalTerm = getConstant(object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI());
//
//					Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUPEX, 
//							                            indiv1Term, subclassTerm, propertyTerm, nominalTerm, contextTerm);
//					Literal ntripled = getLiteral(false, CKRRewritingVocabulary.TRIPLED, 
//	                                                    indiv1Term, propertyTerm, nominalTerm, contextTerm);
//					Literal instd = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//	                                                    indiv1Term, subclassTerm, contextTerm);					
//					Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//							                          contextTerm, globalTerm);
//					addRule(ovrsubs, ntripled, instd, prec);
					
					addFact(CKRRewritingVocabulary.DEF_SUPEX, //
							rewConceptName(subClass.asOWLClass()),//
							some.getProperty().asOWLObjectProperty().getIRI(),
							object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI());
				}
				
			// A subclass all(R, C')		
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {

				OWLObjectAllValuesFrom all = (OWLObjectAllValuesFrom) superClass;
				OWLClass object = (OWLClass) all.getFiller();
				
				addFact(CKRRewritingVocabulary.SUP_ALL, //
						rewConceptName(subClass.asOWLClass()),//
						all.getProperty().asOWLObjectProperty().getIRI(),
						rewConceptName(object.asOWLClass()),
						contextID);
				
				//add rule for overriding
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( A \\subs \\forall R.B ): " + axiom.getAxiomWithoutAnnotations());
			    	
//					Term propertyTerm = getConstant(all.getProperty().asOWLObjectProperty().getIRI());
//					Term subclassTerm = getConstant(subClass.asOWLClass().getIRI());
//					Term objectTerm = getConstant(object.asOWLClass().getIRI());
//
//					Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUPALL, 
//							                            indiv1Term, indiv2Term, subclassTerm, propertyTerm, objectTerm, contextTerm);
//					Literal ninstd = getLiteral(false, CKRRewritingVocabulary.INSTD, 
//                                                        indiv2Term, objectTerm, contextTerm);					
//					Literal instd = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv1Term, subclassTerm, contextTerm);
//					Literal tripled = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                        indiv1Term, propertyTerm, indiv2Term, contextTerm);
//					Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//							                          contextTerm, globalTerm);					
//					addRule(ovrsubs, ninstd, instd, tripled, prec);
			    	
					addFact(CKRRewritingVocabulary.DEF_SUPALL, //
							rewConceptName(subClass.asOWLClass()),//
							all.getProperty().asOWLObjectProperty().getIRI(),
							rewConceptName(object.asOWLClass())
					);			    	
			    }
			    
			// A subclass max(R, 1, B')
		    // Assuming n=1
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
				
				OWLObjectMaxCardinality max = (OWLObjectMaxCardinality) superClass;
				//OWLClass object = (OWLClass) max.getFiller();
				
				addFact(CKRRewritingVocabulary.SUP_LEQONE, //
						rewConceptName(subClass.asOWLClass()),//
						max.getProperty().asOWLObjectProperty().getIRI(),
						contextID);
				
				//add rule for overriding
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( A \\subs \\leq 1 R.B ): " + axiom.getAxiomWithoutAnnotations());
			    	
//					Term propertyTerm = getConstant(max.getProperty().asOWLObjectProperty().getIRI());
//					Term subclassTerm = getConstant(subClass.asOWLClass().getIRI());
//					Term objectTerm = getConstant(object.asOWLClass().getIRI());					
//
//					Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUPLEQONE, 
//							                            indiv1Term, indiv2Term, indiv3Term, subclassTerm, propertyTerm, objectTerm, contextTerm);
//					Literal ovrsubs2 = getLiteral(true, CKRRewritingVocabulary.OVRSUPLEQONE, 
//                                                        indiv1Term, indiv3Term, indiv2Term, subclassTerm, propertyTerm, objectTerm, contextTerm);					
//					Literal neq = getLiteral(false, CKRRewritingVocabulary.EQ, 
//                                                        indiv2Term, indiv3Term, contextTerm);
//					Literal instdA = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv1Term, subclassTerm, contextTerm);
//					Literal tripledy = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                        indiv1Term, propertyTerm, indiv2Term, contextTerm);					
//					Literal tripledz = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                        indiv1Term, propertyTerm, indiv3Term, contextTerm);
//					Literal instdBy = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv2Term, objectTerm, contextTerm);
//					Literal instdBz = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv3Term, objectTerm, contextTerm);
//					Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//							                          contextTerm, globalTerm);
//					addRule(ovrsubs, neq, instdA, tripledy, tripledz, instdBy, instdBz, prec);
//					addRule(ovrsubs2, neq, instdA, tripledy, tripledz, instdBy, instdBz, prec);

					addFact(CKRRewritingVocabulary.DEF_SUPLEQONE, //
							rewConceptName(subClass.asOWLClass()),//
							max.getProperty().asOWLObjectProperty().getIRI());			    	
			    }
			    
			// A subclass not(B)
			//No more in normal form! 
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {

//				OWLObjectComplementOf not = (OWLObjectComplementOf) superClass;
//				IRI negatedClass = null;
//				
//				for (OWLClassExpression ce : not.getNestedClassExpressions()) {
//					if (!ce.isAnonymous()) negatedClass = ce.asOWLClass().getIRI();
//				}
//				
//				addFact(CKRRewritingVocabulary.SUP_NOT, //
//						subClass.asOWLClass().getIRI(),//
//						negatedClass,
//						contextID);
//
//				//add rule for overriding
//			    if(isDefeasible(axiom)){
//			    	//System.out.println("D( A \\subs \\not B ): " + axiom.getAxiomWithoutAnnotations());
//			    	
//					Term subclassTerm = getConstant(subClass.asOWLClass().getIRI());
//					Term negatedTerm = getConstant(negatedClass);
//
//					Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUPNOT, 
//							                            indiv1Term, subclassTerm, negatedTerm, contextTerm);
//					Literal instdA = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv1Term, subclassTerm, contextTerm);
//					Literal instdB = getLiteral(true, CKRRewritingVocabulary.INSTD, 
//                                                        indiv1Term, negatedTerm, contextTerm);
//					Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//							                          contextTerm, globalTerm);
//					addRule(ovrsubs, instdA, instdB, prec);
//			    }

			//if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
			//	throw new IllegalStateException();
			//}
			//else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
			//	throw new IllegalStateException();
			//}
			//else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
			//} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
			//} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY) {  
			//} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_HAS_SELF) {

			} else {
				throw new IllegalStateException();
			}
		}
		
		super.visit(axiom);
	}
	
	//- - RBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	
	@Override
	//Rewrite R \subs S + DEFEASIBLE
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		// int r =
		// iriEncoder.encode(axiom.getSubProperty().asOWLObjectProperty().getIRI());
		// int s =
		// iriEncoder.encode(axiom.getSuperProperty().asOWLObjectProperty().getIRI());
		//
		// addFact(RewritingVocabulary.SUB_ROLE, r, s);
		addFact(CKRRewritingVocabulary.SUB_ROLE, //
				axiom.getSubProperty().asOWLObjectProperty().getIRI(),//
				axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
				contextID);
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( R \\subs S ): " + axiom.getAxiomWithoutAnnotations());
	    	
//			Term role1Term = getConstant(axiom.getSubProperty().asOWLObjectProperty().getIRI());
//			Term role2Term = getConstant(axiom.getSuperProperty().asOWLObjectProperty().getIRI());
//			
//			Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUBROLE, 
//					                              indiv1Term, indiv2Term, role1Term, role2Term, contextTerm);
//			Literal ntripled = getLiteral(false, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv1Term, role2Term, indiv2Term, contextTerm);
//			Literal tripled = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv1Term, role1Term, indiv2Term, contextTerm);
//			Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//					                          contextTerm, globalTerm);			
//			addRule(ovrsubs, ntripled, tripled, prec);	
	    	
			addFact(CKRRewritingVocabulary.DEF_SUBROLE, //
					axiom.getSubProperty().asOWLObjectProperty().getIRI(),//
					axiom.getSuperProperty().asOWLObjectProperty().getIRI());
	    }
	}
	
	@Override
	//Rewrite R \circ S \subs T + DEFEASIBLE
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		List<OWLObjectPropertyExpression> chain = axiom.getPropertyChain();
		Iterator<OWLObjectPropertyExpression> iterator = chain.iterator();
		// int r1 =
		// iriEncoder.encode(iterator.next().asOWLObjectProperty().getIRI());
		// int r2 =
		// iriEncoder.encode(iterator.next().asOWLObjectProperty().getIRI());
		// int r =
		// iriEncoder.encode(axiom.getSuperProperty().asOWLObjectProperty().getIRI());
		//
		// addFact(RewritingVocabulary.SUB_R_CHAIN, r1, r2, r);
		IRI r1 = iterator.next().asOWLObjectProperty().getIRI();
		IRI r2 = iterator.next().asOWLObjectProperty().getIRI();
		
		addFact(CKRRewritingVocabulary.SUB_R_CHAIN, //
				r1,//
				r2,//
				axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
				contextID//
		);
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( R o S \\subs T ): " + axiom.getAxiomWithoutAnnotations());
	    	
//			Term role1Term = getConstant(r1);
//			Term role2Term = getConstant(r2);
//			Term role3Term = getConstant(axiom.getSuperProperty().asOWLObjectProperty().getIRI());
//
//			Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRSUBRCHAIN, 
//					                              indiv1Term, indiv2Term, indiv3Term, role1Term, role2Term, role3Term, contextTerm);
//			Literal ntripled = getLiteral(false, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv1Term, role3Term, indiv3Term, contextTerm);
//			Literal tripled1 = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv1Term, role1Term, indiv2Term, contextTerm);
//			Literal tripled2 = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv2Term, role2Term, indiv3Term, contextTerm);
//			Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//					                          contextTerm, globalTerm);			
//			addRule(ovrsubs, ntripled, tripled1, tripled2, prec);	
	    	
			addFact(CKRRewritingVocabulary.DEF_SUBRCHAIN, //
					r1,//
					r2,//
					axiom.getSuperProperty().asOWLObjectProperty().getIRI());
	    }
	}
	
	@Override
	//Rewrite INV(R, S) + DEFEASIBLE
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		
		addFact(CKRRewritingVocabulary.INV_ROLE, //
				axiom.getFirstProperty().asOWLObjectProperty().getIRI(),
				axiom.getSecondProperty().asOWLObjectProperty().getIRI(),
				contextID);
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( INV(R,S) ): " + axiom.getAxiomWithoutAnnotations());
	    	
//			Term role1Term = getConstant(axiom.getFirstProperty().asOWLObjectProperty().getIRI());
//			Term role2Term = getConstant(axiom.getSecondProperty().asOWLObjectProperty().getIRI());
//
//			Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRINVROLE, 
//					                              indiv1Term, indiv2Term, role1Term, role2Term, contextTerm);
//			Literal ntripled1 = getLiteral(false, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv2Term, role2Term, indiv1Term, contextTerm);
//			Literal tripled1 = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv1Term, role1Term, indiv2Term, contextTerm);
//			
//			Literal ntripled2 = getLiteral(false, CKRRewritingVocabulary.TRIPLED, 
//												  indiv1Term, role1Term, indiv2Term, contextTerm);
//			Literal tripled2 = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                    							  indiv2Term, role2Term, indiv1Term, contextTerm);
//			
//			Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//					                          contextTerm, globalTerm);
//			
//			addRule(ovrsubs, ntripled1, tripled1, prec);
//			addRule(ovrsubs, ntripled2, tripled2, prec);
	    	
			addFact(CKRRewritingVocabulary.DEF_INVROLE, //
					axiom.getFirstProperty().asOWLObjectProperty().getIRI(),
					axiom.getSecondProperty().asOWLObjectProperty().getIRI());	    	
	    }
	}
	
	@Override
	//Rewrite IRR(R) + DEFEASIBLE
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		
		addFact(CKRRewritingVocabulary.IRR_ROLE, //
				axiom.getProperty().asOWLObjectProperty().getIRI(),
				contextID);
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( IRR(R) ): " + axiom.getAxiomWithoutAnnotations());
	    	
//			Term roleTerm = getConstant(axiom.getProperty().asOWLObjectProperty().getIRI());
//		
//			Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRIRRROLE, 
//					                              indiv1Term, roleTerm, contextTerm);
//			Literal tripled = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv1Term, roleTerm, indiv1Term, contextTerm);
//			Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//					                          contextTerm, globalTerm);	
//			addRule(ovrsubs, tripled, prec);
	    	
			addFact(CKRRewritingVocabulary.DEF_IRRROLE, //
					axiom.getProperty().asOWLObjectProperty().getIRI());	    	
	    }
	}
	
	@Override
	//Rewrite DIS(R,S) + DEFEASIBLE
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		
		ArrayList<IRI> r = new ArrayList<IRI>();		
		for(OWLObjectPropertyExpression pe : axiom.getProperties()){
			r.add(pe.asOWLObjectProperty().getIRI());
		}
		
		addFact(CKRRewritingVocabulary.DIS_ROLE,
				r.get(0),
				r.get(1),
				contextID);
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( DIS(R,S) ): " + axiom.getAxiomWithoutAnnotations());
	    	
//			Term role1Term = getConstant(r.get(0));
//			Term role2Term = getConstant(r.get(1));
//
//			Literal ovrsubs = getLiteral(true, CKRRewritingVocabulary.OVRDISROLE, 
//					                              indiv1Term, indiv2Term, role1Term, role2Term, contextTerm);
//			//Literal ovrsubs2 = getLiteral(true, CKRRewritingVocabulary.OVRDISROLE, 
//            //                                      indiv1Term, indiv2Term, role2Term, role1Term, contextTerm);			
//			Literal tripled1 = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv1Term, role1Term, indiv2Term, contextTerm);
//			Literal tripled2 = getLiteral(true, CKRRewritingVocabulary.TRIPLED, 
//                                                  indiv1Term, role2Term, indiv2Term, contextTerm);
//			Literal prec = getLiteral(true, CKRRewritingVocabulary.PREC, 
//					                          contextTerm, globalTerm);
//			addRule(ovrsubs, tripled1, tripled2, prec);
//			//addRule(ovrsubs2, tripled1, tripled2, prec);
	    	
			addFact(CKRRewritingVocabulary.DEF_DISROLE,
					r.get(0),
					r.get(1));
	    }
	}
	
	//- - SIGNATURE - - - - - - - - - - - - - - - - - - - - - - - -	
	
}
//=======================================================================