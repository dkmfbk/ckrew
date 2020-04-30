package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.util.ArrayList;
import java.util.LinkedList;

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
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
//import org.semanticweb.drew.el.SymbolEncoder;
//import org.semanticweb.drew.el.reasoner.DatalogFormat;
//import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Loris
 * @version 1.5
 * 
 * Encodes the <code>Idlr</code> rules for 
 * translation to Datalog of the input DLliteR DKB.
 * 
 */
public class DLRGlobal2DatalogRewriter extends RLContext2DatalogRewriter {
	
	private OWLAnnotationProperty hasAxiomType;
	private OWLAnnotationValue defeasible;
	
	//Ordered list of constants in the program
	private LinkedList<IRI> constList;

    //--- CONSTRUCTORS ----------------------------------------------------------
    
    public DLRGlobal2DatalogRewriter() {
    	
    	this.contextID = IRI.create("g");
    	constList = new LinkedList<IRI>();
	}

    //--- REWRITE METHOD --------------------------------------------------------
    
    @Override
	/**
	 * Returns the rewritten (DKB) ontology as a Datalog program.
	 * 
	 * @param ontology the ontology (DKB) to be rewritten
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
    	//TODO: add normalization (check)
		//ontology = new RLGlobalNormalization().normalize(ontology);
		    	    	
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

		//Add constant supporting facts.
		if(!constList.isEmpty()){
			
			//Add const facts.
			for (IRI c : constList){				
				//System.out.println("const: " + c.toString());
				addFact(DLRRewritingVocabulary.CONST, c);
			}
			
			//Add allNRel supporting facts.
			//first literal
			//System.out.println("first: " + constList.getFirst().toString());
			addFact(DLRRewritingVocabulary.FIRST, constList.getFirst());

			//next literal
			IRI prec = null; 
			
			for (IRI curr : constList){
				
				if(prec != null){
					//System.out.println("next: " + prec.toString() + "," + curr.toString());
					addFact(DLRRewritingVocabulary.NEXT, prec, curr);	
				}
				prec = curr;
			}
			
			//last literal
			//System.out.println("last: " + constList.getLast().toString());
			addFact(DLRRewritingVocabulary.LAST, constList.getLast());
		}
		
		//XXX: ######################
		
 		//Add Pdlr deduction rules.
		datalog.addAll(DLRDeductionRuleset.getPdlr());
		
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
	//Rewrite C(a)
	public void visit(OWLClassAssertionAxiom axiom) {

	  IRI individualIRI = axiom.getIndividual().asOWLNamedIndividual().getIRI(); 
	  IRI classIRI = rewConceptName(axiom.getClassExpression().asOWLClass());
		
	  if(!individualIRI.getStart().equals(CKR_SCHEMA_URI)){//leave out CKR metaknowledge...
		
		addFact(DLRRewritingVocabulary.INSTA,
				individualIRI, 
				classIRI);
				//contextID,
				//IRI.create(MAIN));
		
		//add atom for def.axiom
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( C(a) ): " + axiom.getAxiomWithoutAnnotations());
	    	
			//addFact(CKRRewritingVocabulary.DEF_INSTA,
			//		individualIRI, 
			//		classIRI);
	    }
	  }
	}
	
	@Override
	//Rewrite R(a,b)
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		
		addFact(DLRRewritingVocabulary.TRIPLEA, //
				axiom.getSubject().asOWLNamedIndividual().getIRI(),
 				axiom.getProperty().asOWLObjectProperty().getIRI(),
				axiom.getObject().asOWLNamedIndividual().getIRI());
				//contextID,
				//IRI.create(MAIN));
		
		//add fact for def.axiom
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( R(a,b) ): " + axiom.getAxiomWithoutAnnotations());
	    		    	
			//addFact(CKRRewritingVocabulary.DEF_TRIPLEA, //
			//		axiom.getSubject().asOWLNamedIndividual().getIRI(),
	 		//		axiom.getProperty().asOWLObjectProperty().getIRI(),
			//		axiom.getObject().asOWLNamedIndividual().getIRI()//
			//);
	    }		
	}
	
	@Override
	//Rewrite \non R(a,b)
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		
		addFact(DLRRewritingVocabulary.NTRIPLEA, //
				axiom.getSubject().asOWLNamedIndividual().getIRI(), //
				axiom.getProperty().asOWLObjectProperty().getIRI(), //
				axiom.getObject().asOWLNamedIndividual().getIRI()
				//contextID
		);
		
		//add fact for def.axiom
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( -R(a,b) ): " + axiom.getAxiomWithoutAnnotations());
	    	
			//addFact(CKRRewritingVocabulary.DEF_NTRIPLEA, //
			//		axiom.getSubject().asOWLNamedIndividual().getIRI(), //
			//		axiom.getProperty().asOWLObjectProperty().getIRI(), //
			//		axiom.getObject().asOWLNamedIndividual().getIRI()
			//);	    	
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
			//}

				//add fact for def.axiom
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( A subs B ): " + axiom.getAxiomWithoutAnnotations());

				    addFact(DLRRewritingVocabulary.DEF_SUBCLASS, //
							rewConceptName(subClass.asOWLClass()), 
							rewConceptName(superClass.asOWLClass())
					);
			    } else {
				    addFact(DLRRewritingVocabulary.SUB_CLASS, //
							rewConceptName(subClass.asOWLClass()), 
							rewConceptName(superClass.asOWLClass())
							//contextID
					);
			    }								
		}
		
		// A' subclass C
		else if (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			
			// exists(R) subclass B
			if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) subClass;

				addFact(DLRRewritingVocabulary.SUB_EX,
						some.getProperty().asOWLObjectProperty().getIRI(),//
						//rewConceptName(some.getFiller().asOWLClass()),//
						rewConceptName(superClass.asOWLClass()) 
						//contextID
				);
				
				//add rule for overriding
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( \\exists R.A \\subs B ): " + axiom.getAxiomWithoutAnnotations());
			    				    	
					//addFact(CKRRewritingVocabulary.DEF_SUBEX,//
					//		some.getProperty().asOWLObjectProperty().getIRI(),//
					//		rewConceptName(some.getFiller().asOWLClass()),//
					//		rewConceptName(superClass.asOWLClass())
					//);
			    } 

			} else {
				throw new IllegalStateException();
			}
		}
		
		// A subclass C'
		else if (subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			
			// A \subs \exists R.\top
			if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) superClass;
				//OWLObjectOneOf object = (OWLObjectOneOf) some.getFiller();
				
				IRI subClassIRI = rewConceptName(subClass.asOWLClass());
				IRI propertyIRI = some.getProperty().asOWLObjectProperty().getIRI();
				
				addFact(DLRRewritingVocabulary.SUP_EX, //* !!! *
						subClassIRI,
						propertyIRI,
						//rewConceptName(some.getFiller().asOWLClass()), //THING
						getAuxConstant(subClassIRI, propertyIRI) 		 //Auxiliary constant aux-A-R
						//object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI(),
						//contextID
				);
				
				//add rule for overriding
				if(isDefeasible(axiom)){
					//System.out.println("D( A \\subs \\exists R.\\top ): " + axiom.getAxiomWithoutAnnotations());
										
					//addFact(CKRRewritingVocabulary.DEF_SUPEX, //
					//		rewConceptName(subClass.asOWLClass()),//
					//		some.getProperty().asOWLObjectProperty().getIRI(),
					//		object.getIndividuals().iterator().next().asOWLNamedIndividual().getIRI());
				}
							    
			// A subclass not(B) 
			} else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {

				OWLObjectComplementOf not = (OWLObjectComplementOf) superClass;
				IRI negatedClass = null;
				
				for (OWLClassExpression ce : not.getNestedClassExpressions()) {
					if (!ce.isAnonymous()) negatedClass = ce.asOWLClass().getIRI();
				}
				
				addFact(DLRRewritingVocabulary.SUP_NOT, //
						subClass.asOWLClass().getIRI(),//
						negatedClass
						//contextID
				);
				
				//add rule for overriding
			    if(isDefeasible(axiom)){
			    	//System.out.println("D( A \\subs \\not B ): " + axiom.getAxiomWithoutAnnotations());
			    }
			    
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
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( R \\subs S ): " + axiom.getAxiomWithoutAnnotations());
	    	
			addFact(DLRRewritingVocabulary.DEF_SUBROLE, //
					axiom.getSubProperty().asOWLObjectProperty().getIRI(),//
					axiom.getSuperProperty().asOWLObjectProperty().getIRI()
			);
			
	    } else {
	    	
			addFact(DLRRewritingVocabulary.SUB_ROLE, //
					axiom.getSubProperty().asOWLObjectProperty().getIRI(),//
					axiom.getSuperProperty().asOWLObjectProperty().getIRI()
					//contextID
			);	    	
	    }
	}
		
	@Override
	//Rewrite INV(R, S)
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		
		addFact(DLRRewritingVocabulary.INV_ROLE, //
				axiom.getFirstProperty().asOWLObjectProperty().getIRI(),
				axiom.getSecondProperty().asOWLObjectProperty().getIRI()
				//contextID
		);
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( INV(R,S) ): " + axiom.getAxiomWithoutAnnotations());
	    		    	
			//addFact(CKRRewritingVocabulary.DEF_INVROLE, //
			//		axiom.getFirstProperty().asOWLObjectProperty().getIRI(),
			//		axiom.getSecondProperty().asOWLObjectProperty().getIRI());	    	
	    }
	}
	
	@Override
	//Rewrite IRR(R)
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		
		addFact(DLRRewritingVocabulary.IRR_ROLE, //
				axiom.getProperty().asOWLObjectProperty().getIRI()
				//contextID
		);
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( IRR(R) ): " + axiom.getAxiomWithoutAnnotations());
	    		    	
			//addFact(CKRRewritingVocabulary.DEF_IRRROLE, //
			//		axiom.getProperty().asOWLObjectProperty().getIRI());	    	
	    }
	}
	
	@Override
	//Rewrite DIS(R,S)
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		
		ArrayList<IRI> r = new ArrayList<IRI>();		
		for(OWLObjectPropertyExpression pe : axiom.getProperties()){
			r.add(pe.asOWLObjectProperty().getIRI());
		}
		
		addFact(DLRRewritingVocabulary.DIS_ROLE,
				r.get(0),
				r.get(1)
				//contextID
		);
		
		//add rule for overriding
	    if(isDefeasible(axiom)){
	    	//System.out.println("D( DIS(R,S) ): " + axiom.getAxiomWithoutAnnotations());
	    		    	
			//addFact(CKRRewritingVocabulary.DEF_DISROLE,
			//		r.get(0),
			//		r.get(1));
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
		
			addFact(DLRRewritingVocabulary.NOM, 
					individual.getIRI() 
					//contextID
			);
		
			//*!* add constant to the constant enumeration
			constList.add(individual.getIRI());
		}
	}
	
	@Override
	//Concepts in signature
	public void visit(OWLClass cls) {
		
		if(!cls.getIRI().getStart().equals(CKR_SCHEMA_URI)){
		
			addFact(DLRRewritingVocabulary.CLS, 
					rewConceptName(cls) 
					//contextID
			);
		}
	}
	
	@Override
	//Roles in signature
	public void visit(OWLObjectProperty property) {

		if(!property.getIRI().getStart().equals(CKR_SCHEMA_URI)){
			addFact(DLRRewritingVocabulary.ROLE, 
					property.getIRI() 
					//contextID
			);
		}
	}
	
	//XXX: ######################
	
	//- - Auxiliary Constants - - - - - - - - - - - - - - - - - - -	
	
	private IRI getAuxConstant(IRI subClass, IRI property){
		
		IRI aux = IRI.create("aux-" + 
		           subClass.getFragment() + 
		           "-" +
		           property.getFragment());
		
		//*!* add constant to the constant enumeration (if not added)
		if(!constList.contains(aux)){
			constList.add(aux);
		}
		
		return aux;
	}
	
}
//=======================================================================