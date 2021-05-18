package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.drew.dlprogram.model.DLProgram;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
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
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

/**
 * @author Loris
 * @version 1.6
 * 
 * Encodes the <code>Irl</code> and <code>ID</code>
 * rules for translation to Datalog of the local MR-CKR ontologies.
 */
public class MRLocal2DatalogRewriter extends RLContext2DatalogRewriter {
    
	private OWLAnnotationProperty hasEvalMeta;
	private OWLAnnotationProperty hasEvalObject;
	
	private OWLAnnotationProperty hasAxiomType;
	private OWLAnnotationValue defeasibleTime;
	private OWLAnnotationValue defeasibleCovers;
	//private OWLAnnotationValue defeasible;

	private IRI time;
	private IRI covers;
	
    //--- CONSTRUCTORS ----------------------------------------------------------
   
    public MRLocal2DatalogRewriter() {
    	super();
    	//this.localID = null; 
	}
        
    //--- REWRITE METHOD --------------------------------------------------------
    
    @Override
	/**
	 * Returns the rewritten (local) ontology as a Datalog program.
	 * 
	 * @param ontology the ontology (local) to be rewritten
	 * @return the output Datalog program
	 */
	public DLProgram rewrite(OWLOntology ontology) {
		datalog = new DLProgram();
			
		//Instantiate local properties.
		this.ontology = ontology;
		this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		this.hasEvalMeta = factory.getOWLAnnotationProperty(
				IRI.create(CKR_SCHEMA_URI, "hasEvalMeta"));
		this.hasEvalObject = factory.getOWLAnnotationProperty(
				IRI.create(CKR_SCHEMA_URI, "hasEvalObject"));
		
    	this.hasAxiomType = factory.getOWLAnnotationProperty(
				IRI.create(CKR_SCHEMA_URI, "hasAxiomType"));
    	this.defeasibleTime = IRI.create(CKR_SCHEMA_URI, "defeasibleTime");
    	this.defeasibleCovers = IRI.create(CKR_SCHEMA_URI, "defeasibleCovers");
    	//this.defeasible = IRI.create(CKR_SCHEMA_URI, "defeasible");
    	
		this.time   = IRI.create(CKR_SCHEMA_URI, "time");
		this.covers = IRI.create(CKR_SCHEMA_URI, "covers");
    	
		//Normalization for local ontologies.
		//*!* Needed for correct reading of exists!
		ontology = new RLLocalNormalization().normalize(ontology);
		
		//Rewriting of local ontologies.
    	for (OWLLogicalAxiom ax : ontology.getLogicalAxioms()) {    		
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
        	
		//datalog.addAll(MRDeductionRuleset.getPrl());
			
	    //addFact(CKRRewritingVocabulary.PREC, //
		//		contextID,
		//		IRI.create("g"));
    	
		return datalog;
	}
	
    //XXX: #####################
    
	//--- IS DEFEASIBLE -----------------------------------------------------------------

	public boolean isDefeasible(OWLAxiom a){
		for (OWLAnnotation ann : a.getAnnotations(hasAxiomType)) {
			
			if (ann.getValue().equals(defeasibleTime) || ann.getValue().equals(defeasibleCovers))
				return true;
		}
		return false;
	}

	public IRI getDefeasibleTypeIRI(OWLAxiom a){
		for (OWLAnnotation ann : a.getAnnotations(hasAxiomType)) {
			
			if (ann.getValue().equals(defeasibleTime))
				return time;
			else if (ann.getValue().equals(defeasibleCovers))
				return covers;
		}
		return null;
	}	
    
	public boolean isDefeasibleTime(OWLAxiom a){
		for (OWLAnnotation ann : a.getAnnotations(hasAxiomType)) {
			//System.out.println(ann.getValue());
			
			if (ann.getValue().equals(defeasibleTime))
				return true;
		}
		return false;
	}

	public boolean isDefeasibleCovers(OWLAxiom a){
		for (OWLAnnotation ann : a.getAnnotations(hasAxiomType)) {
			//System.out.println(ann.getValue());
			
			if (ann.getValue().equals(defeasibleCovers))
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

	  IRI individualIRI = axiom.getIndividual().asOWLNamedIndividual().getIRI(); 
	  IRI classIRI = rewConceptName(axiom.getClassExpression().asOWLClass());
		
	  if(!individualIRI.getStart().equals(CKR_SCHEMA_URI)){//leave out CKR metaknowledge...
				  
		//add atom for def.axioms
	    //if(isDefeasible(axiom)){
	    //	IRI rel = getDefeasibleTypeIRI(axiom);
	    //	//System.out.println("D-" + rel.getFragment() + "( C(a) ): " + axiom.getAxiomWithoutAnnotations());
	    //	
		//	addFact(MRRewritingVocabulary.DEF_INSTA,
		//			individualIRI, 
		//			classIRI,
		//			contextID,
		//			rel);
	    //} else {
	    	
			addFact(MRRewritingVocabulary.INSTA,
					individualIRI, 
					classIRI,
					contextID);
	    //}
	  }
	}
	
	@Override
	//Rewrite R(a,b) + DEFEASIBLE
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
				
		//add fact for def.axiom
	    //if(isDefeasible(axiom)){
	    //	IRI rel = getDefeasibleTypeIRI(axiom);
	    //	//System.out.println("D-" + rel.getFragment() + "( R(a,b) ): " + axiom.getAxiomWithoutAnnotations());
	    //		    	
		//	addFact(MRRewritingVocabulary.DEF_TRIPLEA, //
		//			axiom.getSubject().asOWLNamedIndividual().getIRI(),
	 	//			axiom.getProperty().asOWLObjectProperty().getIRI(),
		//			axiom.getObject().asOWLNamedIndividual().getIRI(),
		//			contextID,
		//			rel);
        //
	    //} else {
	    	
			addFact(MRRewritingVocabulary.TRIPLEA, //
					axiom.getSubject().asOWLNamedIndividual().getIRI(),
	 				axiom.getProperty().asOWLObjectProperty().getIRI(),
					axiom.getObject().asOWLNamedIndividual().getIRI(),
					contextID);
	    //}		
	}
	
	//@Override
	////Rewrite \non R(a,b) + DEFEASIBLE
	//public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
	//			
	//	//add fact for def.axiom
	//    if(isDefeasible(axiom)){
	//    	IRI rel = getDefeasibleTypeIRI(axiom);
	//    	//System.out.println("D-" + rel.getFragment() + "( -R(a,b) ): " + axiom.getAxiomWithoutAnnotations());
	//    	
	//		addFact(MRRewritingVocabulary.DEF_NTRIPLEA, //
	//				axiom.getSubject().asOWLNamedIndividual().getIRI(), //
	//				axiom.getProperty().asOWLObjectProperty().getIRI(), //
	//				axiom.getObject().asOWLNamedIndividual().getIRI(),
	//				contextID,
	//				rel);
	//		
	//    } else {
	//    	
	//		addFact(MRRewritingVocabulary.NTRIPLEA, //
	//				axiom.getSubject().asOWLNamedIndividual().getIRI(), //
	//				axiom.getProperty().asOWLObjectProperty().getIRI(), //
	//				axiom.getObject().asOWLNamedIndividual().getIRI(),
	//				contextID);	
	//    }
	//}
	
	//- - TBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    
	@Override
	//Rewrite A \subs B + DEFEASIBLE
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression subClass = axiom.getSubClass();
		OWLClassExpression superClass = axiom.getSuperClass();

		// atomic case: A \subs C
		if ((subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
				&& (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {

			//add fact for def.axiom
		    if(isDefeasible(axiom)){
		    	IRI rel = getDefeasibleTypeIRI(axiom);
		    	//System.out.println("D-" + rel.getFragment() + "( A subs B ): " + axiom.getAxiomWithoutAnnotations());

			    addFact(MRRewritingVocabulary.DEF_SUBCLASS, //
						rewConceptName(subClass.asOWLClass()), 
						rewConceptName(superClass.asOWLClass()),
						contextID,
						rel
				);
			    
		    } else {

		    	//CASE FOR EVAL: eval(A,c) \subs B
		    	IRI metaContext = null;
		    	IRI objectClass = null; 
		    	for(OWLAnnotationAssertionAxiom a : subClass.asOWLClass().getAnnotationAssertionAxioms(ontology)) {
		    		if (a.getProperty().equals(hasEvalMeta)){
		    			metaContext = (IRI) a.getValue();
		    		} else if (a.getProperty().equals(hasEvalObject)) {
		    			objectClass = (IRI) a.getValue();
		    		}
		    	}
		    	if ((metaContext != null) && (objectClass != null)){
		    		addFact(MRRewritingVocabulary.SUB_EVAL_AT,
		    				objectClass, metaContext,//
		    				rewConceptName(superClass.asOWLClass()),
		    				contextID);				
		    	}
			
		    	addFact(MRRewritingVocabulary.SUB_CLASS, //
		    			rewConceptName(subClass.asOWLClass()), 
		    			rewConceptName(superClass.asOWLClass()),
		    			contextID);
			}
		}
				
		// A' subclass C
		else if (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
						
			// and(A, B) subclass C + DEFEASIBLE
			if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				// CASE WITH N>2 IN NORMALIZATION
				OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) subClass;
				Set<OWLClassExpression> operands = inter.getOperands();
				IRI[] params = new IRI[4];

				int i = 0;
				for (OWLClassExpression op : operands) {
					params[i++] = rewConceptName(op.asOWLClass());
				}
				params[2] = rewConceptName(superClass.asOWLClass());
				params[3] = contextID;
				
			    if(isDefeasible(axiom)){
			    	IRI rel = getDefeasibleTypeIRI(axiom);
			    	//System.out.println("D-" + rel.getFragment() + "( and(A, B) subs C ): " + axiom.getAxiomWithoutAnnotations());

				    addFact(MRRewritingVocabulary.DEF_SUBCONJ, //
				    		params[0],
				    		params[1],
							params[2],
							params[3],
							rel
					);
				    
			    } else {
					addFact(MRRewritingVocabulary.SUB_CONJ, params);
			    }
			}

			// exist(R,A) subclass B
			else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) subClass;

			    if(isDefeasible(axiom)){
			    	IRI rel = getDefeasibleTypeIRI(axiom);
			    	//System.out.println("D-" + rel.getFragment() + "( \\exists R.A \\subs B ): " + axiom.getAxiomWithoutAnnotations());

			    	addFact(MRRewritingVocabulary.DEF_SUBEX,//
			    			some.getProperty().asOWLObjectProperty().getIRI(),//
			    			rewConceptName(some.getFiller().asOWLClass()),//
			    			rewConceptName(superClass.asOWLClass()), 
			    			contextID,
			    			rel
			    	);
				    
			    } else {
			    	
			    	addFact(MRRewritingVocabulary.SUB_EX,//
			    			some.getProperty().asOWLObjectProperty().getIRI(),//
			    			rewConceptName(some.getFiller().asOWLClass()),//
			    			rewConceptName(superClass.asOWLClass()), 
			    			contextID
			    	);
			    }
			    
			} else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
				// {c} \subs A -> inst(c, A)
				OWLObjectOneOf oneOf = (OWLObjectOneOf) subClass;
								
				addFact(MRRewritingVocabulary.INSTA,
						oneOf.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),//
						rewConceptName(superClass.asOWLClass()),
						contextID);

			} else {
				throw new IllegalStateException();
			}
		}
		
		// A subclass C'
		else if (subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			
			if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				
					OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) superClass;
					OWLObjectOneOf object = (OWLObjectOneOf) some.getFiller();

				    if(isDefeasible(axiom)){
				    	IRI rel = getDefeasibleTypeIRI(axiom);
				    	//System.out.println("D-" + rel.getFragment() + "( A \\subs \\exists R.{a} ): " + axiom.getAxiomWithoutAnnotations());

				    	addFact(MRRewritingVocabulary.DEF_SUPEX,//
								rewConceptName(subClass.asOWLClass()),//
								some.getProperty().asOWLObjectProperty().getIRI(),
								object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),
								contextID,
				    			rel
				    	);
					    
				    } else {
				    	
						addFact(MRRewritingVocabulary.SUP_EX, //
								rewConceptName(subClass.asOWLClass()),//
								some.getProperty().asOWLObjectProperty().getIRI(),
								object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),
								contextID
						);
				    }
				    
			// A subclass all(R, C')		
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {

				OWLObjectAllValuesFrom all = (OWLObjectAllValuesFrom) superClass;
				OWLClass object = (OWLClass) all.getFiller();
				
			    if(isDefeasible(axiom)){
			    	IRI rel = getDefeasibleTypeIRI(axiom);
			    	//System.out.println("D-" + rel.getFragment() + "( A \\\\subs \\\\forall R.B ): " + axiom.getAxiomWithoutAnnotations());

			    	addFact(MRRewritingVocabulary.DEF_SUPALL,//
			    			rewConceptName(subClass.asOWLClass()),//
			    			all.getProperty().asOWLObjectProperty().getIRI(),
			    			rewConceptName(object.asOWLClass()),
			    			contextID,
			    			rel
			    	);
				    
			    } else {				
				
			    	addFact(MRRewritingVocabulary.SUP_ALL, //
			    			rewConceptName(subClass.asOWLClass()),//
			    			all.getProperty().asOWLObjectProperty().getIRI(),
			    			rewConceptName(object.asOWLClass()),
			    			contextID
			    	);
			    }
			    			    
			// A subclass max(R, 1, B')
		    // Assume n=1
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
				
				OWLObjectMaxCardinality max = (OWLObjectMaxCardinality) superClass;
				//OWLClass object = (OWLClass) max.getFiller();
				
			    if(isDefeasible(axiom)){
			    	IRI rel = getDefeasibleTypeIRI(axiom);
			    	//System.out.println("D-" + rel.getFragment() + "( A \\subs \\leq 1 R.T ): " + axiom.getAxiomWithoutAnnotations());

			    	addFact(MRRewritingVocabulary.DEF_SUPLEQONE,//
			    			rewConceptName(subClass.asOWLClass()),//
			    			max.getProperty().asOWLObjectProperty().getIRI(),
			    			contextID,
			    			rel
			    	);
				    
			    } else {
				
			    	addFact(MRRewritingVocabulary.SUP_LEQONE, //
			    			rewConceptName(subClass.asOWLClass()),//
			    			max.getProperty().asOWLObjectProperty().getIRI(),
			    			contextID);
			    }
						
			// A subclass not(B)
		    //Not in normal form
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {

			} else {
				throw new IllegalStateException();
			}
		}

		super.visit(axiom);
	}
	
	//- - RBOX AXIOMS - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		
	@Override
	//Rewrite R \subs S
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {

	    if(isDefeasible(axiom)){
	    	IRI rel = getDefeasibleTypeIRI(axiom);
	    	//System.out.println("D-" + rel.getFragment() + "( R \\subs S ): " + axiom.getAxiomWithoutAnnotations());

	    	addFact(MRRewritingVocabulary.DEF_SUBROLE,//
	    			axiom.getSubProperty().asOWLObjectProperty().getIRI(),//
	    			axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
	    			contextID,
	    			rel
	    	);
		    
	    } else {
		
	    	addFact(MRRewritingVocabulary.SUB_ROLE, //
	    			axiom.getSubProperty().asOWLObjectProperty().getIRI(),//
	    			axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
	    			contextID
	    	);
			
	    	//CASE FOR EVAL: eval(R,c) \subs S
	    	IRI metaContext = null;
	    	IRI objectProperty = null; 
	    	for (OWLAnnotationAssertionAxiom a : axiom.getSubProperty().asOWLObjectProperty()
	    			.getAnnotationAssertionAxioms(ontology)) {
	    		if (a.getProperty().equals(hasEvalMeta)){
	    			metaContext = (IRI) a.getValue();
	    		} else if (a.getProperty().equals(hasEvalObject)) {
	    			objectProperty = (IRI) a.getValue();
	    		}
	    	}
	    	if ((metaContext != null) && (objectProperty != null)){
	    		
	    		addFact(MRRewritingVocabulary.SUB_EVAL_R,
	    				objectProperty, metaContext,//
	    				axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
	    				contextID
	    		);				
	    	}
	    }
	}
	
	@Override
	//Rewrite R \circ S \subs T + DEFEASIBLE
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		List<OWLObjectPropertyExpression> chain = axiom.getPropertyChain();
		Iterator<OWLObjectPropertyExpression> iterator = chain.iterator();

		IRI r1 = iterator.next().asOWLObjectProperty().getIRI();
		IRI r2 = iterator.next().asOWLObjectProperty().getIRI();

	    if(isDefeasible(axiom)){
	    	IRI rel = getDefeasibleTypeIRI(axiom);
	    	//System.out.println("D-" + rel.getFragment() + "( R o S \\subs T ): " + axiom.getAxiomWithoutAnnotations());

			addFact(MRRewritingVocabulary.DEF_SUBRCHAIN, //
					r1,//
					r2,//
					axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
					contextID,
					rel
			);
			
	    } else {
	    	addFact(MRRewritingVocabulary.SUB_R_CHAIN, //
	    			r1,//
	    			r2,//
	    			axiom.getSuperProperty().asOWLObjectProperty().getIRI(),
	    			contextID//
	    	);
	    }
	}	
	
	@Override
	//Rewrite INV(R, S) + DEFEASIBLE
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		
	    if(isDefeasible(axiom)){
	    	IRI rel = getDefeasibleTypeIRI(axiom);
	    	//System.out.println("D-" + rel.getFragment() + "( INV(R,S) ): " + axiom.getAxiomWithoutAnnotations());
	    		    	
			addFact(MRRewritingVocabulary.DEF_INVROLE, //
					axiom.getFirstProperty().asOWLObjectProperty().getIRI(),
					axiom.getSecondProperty().asOWLObjectProperty().getIRI(),
					contextID, 
					rel
			);
	    } else {
	    	addFact(MRRewritingVocabulary.INV_ROLE, //
	    			axiom.getFirstProperty().asOWLObjectProperty().getIRI(),
	    			axiom.getSecondProperty().asOWLObjectProperty().getIRI(),
	    			contextID
	    	); 
	    }	
	}	
	
	@Override
	//Rewrite IRR(R) + DEFEASIBLE
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	IRI rel = getDefeasibleTypeIRI(axiom);
	    	//System.out.println("D-" + rel.getFragment() + "( IRR(R) ): " + axiom.getAxiomWithoutAnnotations());
	    	
			addFact(MRRewritingVocabulary.DEF_IRRROLE, //
					axiom.getProperty().asOWLObjectProperty().getIRI(),
					contextID,
					rel
			);
			
	    } else {
	    	addFact(MRRewritingVocabulary.IRR_ROLE, //
	    			axiom.getProperty().asOWLObjectProperty().getIRI(),
	    			contextID
	    	);
	    }		
	}
		
	@Override
	//Rewrite DIS(R,S)
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		
		ArrayList<IRI> r = new ArrayList<IRI>();		
		for(OWLObjectPropertyExpression pe : axiom.getProperties()){
			r.add(pe.asOWLObjectProperty().getIRI());
		}

		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	IRI rel = getDefeasibleTypeIRI(axiom);
	    	//System.out.println("D-" + rel.getFragment() + "( DIS(R,S) ): " + axiom.getAxiomWithoutAnnotations());
	    		    	
			addFact(MRRewritingVocabulary.DEF_DISROLE,
					r.get(0),
					r.get(1),
					contextID,
					rel
			);
	    } else {
	    	addFact(MRRewritingVocabulary.DIS_ROLE,
	    			r.get(0),
	    			r.get(1),
	    			contextID
	    	);
	    }
	}	
		
	@Override
	//Ignore Domain
	public void visit(OWLObjectPropertyDomainAxiom axiom) {

		return;
	}

	@Override
	//Ignore Range
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		
		return;
	}
	
	//- - SIGNATURE - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	//Individuals in signature
	public void visit(OWLNamedIndividual individual) {
		
		if(!individual.getIRI().getStart().equals(CKR_SCHEMA_URI)){
		
			addFact(MRRewritingVocabulary.NOM, 
					individual.getIRI(), 
					contextID
			);		
		}
	}
	
	@Override
	//Concepts in signature
	public void visit(OWLClass cls) {
		
		if(!cls.getIRI().getStart().equals(CKR_SCHEMA_URI)){
		
			addFact(MRRewritingVocabulary.CLS, 
					rewConceptName(cls), 
					contextID
			);
		}
	}
	
	@Override
	//Roles in signature
	public void visit(OWLObjectProperty property) {

		if(!property.getIRI().getStart().equals(CKR_SCHEMA_URI)){
			addFact(MRRewritingVocabulary.ROLE, 
					property.getIRI(), 
					contextID
			);
		}
	}
	
    //XXX: #####################
	
}
//=======================================================================