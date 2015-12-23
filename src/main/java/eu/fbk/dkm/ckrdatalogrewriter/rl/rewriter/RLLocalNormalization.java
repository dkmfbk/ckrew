package eu.fbk.dkm.ckrdatalogrewriter.rl.rewriter;

import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

/**
 * @author Loris
 * #version 1.3
 * 
 * Normalization in OWL RL for local ontologies.
 */
class RLLocalNormalization extends RLNormalization {

	//--- FIELDS ----------------------------------------------------------------
	
	//--- CONSTRUCTORS ----------------------------------------------------------
	
	public RLLocalNormalization() {
		super();
	}
	
	//--- NORMALIZATION ---------------------------------------------------------

	//public OWLOntology normalize(OWLOntology ontology) {

	//--- eval CHECK -----------------------------------------------------
	
	//checks if eval assertions are in the correct form (on IRIs and unique)
	void checkEvalAssertions(Set<OWLAnnotationAssertionAxiom> annotationSet){
		IRI metaIRI = null;
		IRI objectIRI = null;
		
		for (OWLAnnotationAssertionAxiom a : annotationSet) {
			//System.out.println(a);
			if (a.getProperty().equals(hasEvalMeta)){
				//System.out.println(a.getValue());					
				
				if (!(a.getValue() instanceof IRI)) {
					System.err.println(a + " not allowed in normal form.");
					throw new IllegalStateException();
				} else if (metaIRI != null) {
					System.err.println(a + ": more than one hasEvalMeta assertion.");
					throw new IllegalStateException();						
				} else metaIRI = (IRI) a.getValue();					
			}
			if (a.getProperty().equals(hasEvalObject)){
				//System.out.println(a.getValue());		
				
				if (!(a.getValue() instanceof IRI)) {
					System.err.println(a + " not allowed in normal form.");
					throw new IllegalStateException();
				} else if (objectIRI != null) {
					System.err.println(a + ": more than one hasEvalObject assertion.");
					throw new IllegalStateException();						
				} else objectIRI = (IRI) a.getValue();					
			}				
		}
		return;
	}
	
	//--- VISIT METHODS --------------------------------------------------
	
	/* The following axioms are rewritten (checked) for normal form */
	
	@Override
	//Restrict A \subs C
	public Object visit(OWLSubClassOfAxiom axiom) {

		OWLClassExpression subClass = axiom.getSubClass();
		OWLClassExpression superClass = axiom.getSuperClass();

		// simple case: A subclass C
		if ((subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
				&& (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
			// leave out A subclass \top and \bot subclass A
			if (!subClass.isOWLNothing() && !superClass.isOWLThing()) {
				manager.addAxiom(normalizedOnt, axiom);
			} 
			
			//CASE FOR EVAL: if eval(A',C') \subs C, normalize
			//TODO: NORMALIZE: THIS REQUIRES TO MODIFY GLOBAL AND THE SET OF MODULES
			
			checkEvalAssertions(subClass.asOWLClass().getAnnotationAssertionAxioms(ontology));			
		}
		// A' subclass C'
		//TODO: NORMALIZE
		else if ((subClass.getClassExpressionType() != ClassExpressionType.OWL_CLASS)
				&& (superClass.getClassExpressionType() != ClassExpressionType.OWL_CLASS)) {
		//	OWLClass freshClass = getFreshClass();
		//	factory.getOWLSubClassOfAxiom(subClass, freshClass).accept(this);
		//	factory.getOWLSubClassOfAxiom(freshClass, superClass).accept(this);
				
			System.err.println(axiom + " not allowed in normal form.");
			throw new IllegalStateException();
		}
		// A' subclass C
		else if (superClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			// and(A', B) subclass C
			// and(B, A') subclass C
			// -> {A' subclass D, and(D,C) subclass B}
			//TODO: NORMALIZE, only case for atomic intersection of size 2
			if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				
				OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) subClass;
				Set<OWLClassExpression> operands = inter.getOperands();
				//Set<OWLClassExpression> newOps = new HashSet<>();
				if (operands.size() == 2) {
					boolean normalized = true;
					for (OWLClassExpression op : operands) {
						if (!(op.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
							normalized = false;
							break;
						}
					}
					if (normalized) {
						manager.addAxiom(normalizedOnt, axiom);
					} 
					//	else {
					//	for (OWLClassExpression op : operands) {
					//		if (!(op.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
					//			OWLClass freshClass = getFreshClass();
					//			newOps.add(freshClass);
					//			// manager.addAxiom(normalizedOnt, factory
					//			// .getOWLSubClassOfAxiom(op, freshClass));
					//			//
					//			factory.getOWLSubClassOfAxiom(op, freshClass)
					//					.accept(this);
					//		} else {
					//			newOps.add(op);
					//		}
					//	}
                    //
					//	manager.addAxiom(normalizedOnt, factory
					//			.getOWLSubClassOfAxiom(factory
					//					.getOWLObjectIntersectionOf(newOps),
					//					superClass));
					//}
				//} else if (operands.size() > 2) {
				//	Iterator<OWLClassExpression> iterator = operands.iterator();
				//	OWLClassExpression first = iterator.next();
				//	OWLClassExpression second = iterator.next();
                //
				//	OWLClass freshClass = getFreshClass();
				//	factory.getOWLSubClassOfAxiom(
				//			factory.getOWLObjectIntersectionOf(first, second),
				//			freshClass).accept(this);
                //
				//	Set<OWLClassExpression> rest = new HashSet<>();
				//	rest.add(freshClass);
				//	while (iterator.hasNext()) {
				//		rest.add(iterator.next());
				//	}
				//	factory.getOWLSubClassOfAxiom(
				//			factory.getOWLObjectIntersectionOf(rest),
				//			superClass)//
				//			.accept(this);
				} else {
					System.err.println(axiom + " not allowed in normal form.");
					throw new IllegalStateException();
				}
			}
			// or(C, D) subclass B
			// -> {C subclass B, D subclass B,}
			//TODO: NORMALIZE, only case for atomic union of size 2
			else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
				
				OWLObjectUnionOf union = (OWLObjectUnionOf) subClass;
				Set<OWLClassExpression> operands = union.getOperands();
				//Set<OWLClassExpression> newOps = new HashSet<>();
				if (operands.size() == 2) {
					boolean normalized = true;
					for (OWLClassExpression op : operands) {
						if (!(op.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
							normalized = false;
							break;
						}
					}
					if (normalized) {
						manager.addAxiom(normalizedOnt, axiom);
					} 
				} else {
					System.err.println(axiom + " not allowed in normal form.");
					throw new IllegalStateException();
				}
			}
			// exist(R,A') subclass B
			//TODO: NORMALIZE, only case for atomic A
			else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) subClass;
				OWLClassExpression filler = some.getFiller();
				
				if (filler.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
					manager.addAxiom(normalizedOnt, axiom);
				} else {
					//// OWLClass freshClass = getFreshClass("some_");
					//OWLClass freshClass = getFreshClass();
					//factory.getOWLSubClassOfAxiom(
					//		factory.getOWLObjectSomeValuesFrom(
					//				some.getProperty(), freshClass), superClass) //
					//		.accept(this);
					//factory.getOWLSubClassOfAxiom(filler, freshClass).accept(
					//		this);
					
					System.err.println(axiom + " not allowed in normal form.");
					throw new IllegalStateException();
				}
				
				//CASE FOR EVAL: if \exists eval(R,C') \subs C, normalize
				//TODO: NORMALIZE: THIS REQUIRES TO MODIFY GLOBAL AND THE SET OF MODULES

				//FIXME: not currently supported in NF
				for (OWLAnnotationAssertionAxiom a : some.getProperty().asOWLObjectProperty()
						.getAnnotationAssertionAxioms(ontology)) {
					//System.out.println(a);				
					if (a.getProperty().equals(hasEvalMeta) || a.getProperty().equals(hasEvalObject)){
						//System.out.println(a.getValue());
						//if (!(a.getValue() instanceof IRI)) {
							System.err.println(a + " not allowed in normal form.");
							throw new IllegalStateException();
						//}
					}
				}
			}

			// {a} subclass C 
			//(we assume that by profile that {a} is a singleton)
			else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
				manager.addAxiom(normalizedOnt, axiom);
			}
			// A subclass S Self
			else if (subClass.getClassExpressionType() == ClassExpressionType.OBJECT_HAS_SELF) {
				//manager.addAxiom(normalizedOnt, axiom);
					
				System.err.println(axiom + " not allowed in normal form.");
				throw new IllegalStateException();				
			}
			// not in normal form
			else {
				//System.err.println("Warning. Ignoring axiom " + axiom);
				
				System.err.println(axiom + " not allowed in normal form.");
				throw new IllegalStateException();
			}
		} 
		// A subclass C'
		else if (subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
			// (A -> or(B, C))
			if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
				//System.err.println("Warning. Ignoring axiom " + axiom);					
				System.err.println(axiom + " not allowed in normal form.");
				throw new IllegalStateException();
			}
			// A subclass not(B)
			//TODO: NORMALIZE
			else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
				OWLObjectComplementOf c = (OWLObjectComplementOf) superClass;
				OWLClassExpression operand = c.getOperand();
				
				if (!(operand.getClassExpressionType() == ClassExpressionType.OWL_CLASS)){
					//	OWLClass freshClass = getFreshClass();
					//	factory.getOWLSubClassOfAxiom(subClass, freshClass)
					//			.accept(this);
					//	factory.getOWLSubClassOfAxiom(
					//			factory.getOWLObjectIntersectionOf(freshClass, operand),
					//			factory.getOWLNothing()) //
					//			.accept(this);
					
					System.err.println(axiom + " not allowed in normal form.");
					throw new IllegalStateException();
				} else {
					manager.addAxiom(normalizedOnt, axiom);
				}
			} 
			// A subclass and(B, C)
			//TODO: NORMALIZE
			else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
				//OWLObjectIntersectionOf and = (OWLObjectIntersectionOf) superClass;
				//for (OWLClassExpression op : and.getOperands()) {
				//	factory.getOWLSubClassOfAxiom(subClass, op).accept(this);
				//}

				System.err.println(axiom + " not allowed in normal form.");
				throw new IllegalStateException();
			}
			// A subclass exists(R, C') -> {A subclass some(R,D), D subclass C'}
			//TODO: NORMALIZE!
			else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
				//System.out.println(axiom.toString());
								
				OWLObjectSomeValuesFrom some = (OWLObjectSomeValuesFrom) superClass;
				OWLClassExpression filler = some.getFiller();
				//OWLObjectPropertyExpression property = some.getProperty();
								
				if (filler.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
					//System.out.println("Property: " + property.toString());
				
					//OWLObjectProperty hasmodule = 
					//		factory.getOWLObjectProperty(
					//				IRI.create("http://dkm.fbk.eu/ckr/meta#", "hasModule"));
					//OWLObjectProperty hasattribute = 
					//		factory.getOWLObjectProperty(
					//				IRI.create("http://dkm.fbk.eu/ckr/meta#", "hasAttribute"));
					//
					//if(property.equals(hasmodule) || 
					//   property.getSuperProperties(ontology).contains(hasattribute)){
						//System.out.println("SupEx on property " + property.toString() + " ignored.");
						manager.addAxiom(normalizedOnt, axiom);
					//}
				} else {					
					System.err.println(axiom + " not allowed in normal form.");
					throw new IllegalStateException();
				}
				//if (filler.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
				//	manager.addAxiom(normalizedOnt, axiom);
				//} else {
				//	OWLClass freshClass = getFreshClass("SOME_");
				//	factory.getOWLSubClassOfAxiom(
				//			subClass,
				//			factory.getOWLObjectSomeValuesFrom(
				//					some.getProperty(), freshClass)) //
				//			.accept(this);
				//	factory.getOWLSubClassOfAxiom(freshClass, filler).accept(
				//			this);
				//}
			} 
			// A subclass all(R, C') -> {A subclass all(R, D), D subclass C'}
			//TODO: NORMALIZE!
			else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
				
				OWLObjectAllValuesFrom all = (OWLObjectAllValuesFrom) superClass;
				OWLClassExpression filler = all.getFiller();

				if (filler.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
					manager.addAxiom(normalizedOnt, axiom);
				} else {
					//System.err.println("Warning. ignore " + axiom);
					System.err.println(axiom + " not allowed in normal form.");
					throw new IllegalStateException();
				}
			}
			// A subclass max(R, 1, B') -> {A subclass max(R, 1, D), D subclass B'}
			//TODO: NORMALIZE! CASE n=0 and n=1!
			else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
				
				OWLObjectMaxCardinality max = (OWLObjectMaxCardinality) superClass;
				
				if (max.getCardinality() == 0) {
					
					//System.err.println("Warning. ignore " + axiom);		
					System.err.println(axiom + " not allowed in normal form.");
					throw new IllegalStateException();

				} else if (max.getCardinality() == 1) {
				
					OWLClassExpression filler = max.getFiller();
				    
					if (filler.getClassExpressionType() == ClassExpressionType.OWL_CLASS){
						manager.addAxiom(normalizedOnt, axiom);
						
					} else {
						//System.err.println("Warning. ignore " + axiom);		
						System.err.println(axiom + " not allowed in normal form.");
						throw new IllegalStateException();
					}
				}
			}
			//// A subclass min(R, n, B') -> {A subclass min(R, n, D), D subclass
			//// B'}
			//else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY) {
			//	//System.err.println("Warning. ignore " + axiom);
			//	System.err.println(axiom + " not allowed in normal form.");
			//	throw new IllegalStateException();
			//}
			//else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_ONE_OF) {
			//	manager.addAxiom(normalizedOnt, axiom);
			//} // A subclassof R Self
			//else if (superClass.getClassExpressionType() == ClassExpressionType.OBJECT_HAS_SELF) {
			//	manager.addAxiom(normalizedOnt, axiom);
			//}
			else {
				//System.err.println("Warning. ignore " + axiom);
				//return null;		
				System.err.println(axiom + " not allowed in normal form.");
				throw new IllegalStateException();
			}
		}
		return null;
	}
			
	@Override
	//Add R \subs S
	public Object visit(OWLSubObjectPropertyOfAxiom axiom) {		
		manager.addAxiom(normalizedOnt, axiom);
		
		//CASE FOR EVAL: if eval(R,C') \subs S, normalize
		//TODO: NORMALIZE: THIS REQUIRES TO MODIFY GLOBAL AND THE SET OF MODULES
		
		OWLObjectPropertyExpression subProperty = axiom.getSubProperty();
		
		checkEvalAssertions(subProperty.asOWLObjectProperty().getAnnotationAssertionAxioms(ontology));
				
		return null;
	}
			
	@Override
	//Restrict chains to size 2
	//TODO: normalize chains?
	public Object visit(OWLSubPropertyChainOfAxiom axiom) {
		if(axiom.getPropertyChain().size() > 2){
			System.err.println(axiom + " not allowed in normal form.");
			throw new IllegalStateException();
		} else {
			manager.addAxiom(normalizedOnt, axiom);
			
			//CASE FOR EVAL: if eval(R,C') \circ S \subs T, normalize
			//TODO: NORMALIZE: THIS REQUIRES TO MODIFY GLOBAL AND THE SET OF MODULES
			
			for (OWLObjectPropertyExpression property : axiom.getPropertyChain()) {
				//FIXME: not currently supported in NF
				for (OWLAnnotationAssertionAxiom a : property.asOWLObjectProperty()
						.getAnnotationAssertionAxioms(ontology)) {
					//System.out.println(a);				
					if (a.getProperty().equals(hasEvalMeta) || a.getProperty().equals(hasEvalObject)){
						//System.out.println(a.getValue());
						//if (!(a.getValue() instanceof IRI)) {
							System.err.println(a + " not allowed in normal form.");
							throw new IllegalStateException();
						//}
					}
				}				
			}
			
		}
		return null;
	}

	@Override
	//Add Dis(R,S) (object)
	public Object visit(OWLDisjointObjectPropertiesAxiom axiom) {
		manager.addAxiom(normalizedOnt, axiom);
		
		//CASE FOR EVAL: if Dis(eval(R,C'), S), normalize
		//TODO: TEST!!
		//TODO: NORMALIZE: THIS REQUIRES TO MODIFY GLOBAL AND THE SET OF MODULES
		
		for (OWLObjectPropertyExpression property : axiom.getProperties()) {
			//FIXME: not currently supported in NF
			for (OWLAnnotationAssertionAxiom a : property.asOWLObjectProperty()
					.getAnnotationAssertionAxioms(ontology)) {
				//System.out.println(a);				
				if (a.getProperty().equals(hasEvalMeta) || a.getProperty().equals(hasEvalObject)){
					//System.out.println(a.getValue());
					//if (!(a.getValue() instanceof IRI)) {
						System.err.println(a + " not allowed in normal form.");
						throw new IllegalStateException();
					//}
				}
			}				
		}
		
		return null;
	}

	//---------------------------------------------------------------------
	
	@Override
	//Restrict R(a,b)
	public Object visit(OWLObjectPropertyAssertionAxiom axiom) {
		//System.out.println(axiom.toString());
		if(!axiom.getProperty().isAnonymous()){
			manager.addAxiom(normalizedOnt, axiom);
			return null;
	    } else {
			System.err.println(axiom + " not allowed in normal form.");
			throw new IllegalStateException();
	    }
	}
	
	@Override
	//Restrict \non R(a,b) 
	public Object visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		//System.out.println(axiom.toString());
		if(!axiom.getProperty().isAnonymous()){
			manager.addAxiom(normalizedOnt, axiom);
			return null;
	    } else {
			System.err.println(axiom + " not allowed in normal form.");
			throw new IllegalStateException();
	    }		
	}

	@Override
	//Restrict C(a)
	public Object visit(OWLClassAssertionAxiom axiom) {
		if(axiom.getClassExpression().getClassExpressionType() == ClassExpressionType.OWL_CLASS){
		  manager.addAxiom(normalizedOnt, axiom);
		} else {
			System.err.println(axiom + " not allowed in normal form.");
			throw new IllegalStateException();
		}
		return null;
	}	
				
}
//=======================================================================