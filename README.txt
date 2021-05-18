================================================================================
  CKRew: CKR datalog rewriter - README.txt
================================================================================

Prototype for datalog rewriter for CKR in SROIQ-RL fragment with global 
defeasible axioms.

*The prototype with included example data sources is a proof of concept of our
research idea and is intended to be used only to demonstrate and evaluate our
work. Please contact the authors for any other need.*

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
= USAGE =

Usage: ckrew <global-context-file> [<local-module-file> | <options>]

<global-context-file>
  Ontology file containing the global context for the input CKR.

<local-module-file>
  Ontology files (zero or more) containing a knowledge module for the input CKR.

Example: ckrew global.n3 m1.n3 m2.n3

Options:
 -mrckr: interprets input as multi-relational sCKR
 -dllite: interprets global ontology as (single context) DLliteR defeasible KB
 -v: verbose (prints more information about loading and rewriting process)
 -out <output-file>: specifies the path to the output program file 
                     (default: output.dlv)
 -trig: specifies that the input is provided as a single TRIG file
 -dlv <dlv-path>   : specifies the path to the DLV executable 
                     (default: localdlv/dlv)

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
= REQUIREMENTS =

 - DLV 2012-12-17 (or newer) [http://www.dlvsystem.com/dlv/]
   For ease of use, it is preferrable to install a copy of the DLV executable in 
   "/localdlv/dlv", the directory used by default as DLV path by the prototype

 - Java runtime version 1.7 (or greater)
 - Windows, Linux or Mac OS X operating system 

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
= DEMO =

Please refer to "/demo/DEMO_README.txt" for usage examples.

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
= KNOWN LIMITATIONS =

- [!] Input ontologies have to be in the SROIQ-RL Normal Form: currently, the
      normal form is only recognized and no tranformation is applied.

- [!] For input DLliteR defeasible knowledge bases: currently the input ontology
      need to be in Normal Form and no check on language is applied.
      
- [!] As part of the normal form and for the definition of "ckr:hasEvalMeta" and 
      "ckr:hasEvalObject", eval expression can only be expressed on named 
      classes and properties (i.e. no support for complex class expressions
      both in the object and meta parts).
      
- [!] Input ontology for global context has to import (owl:import) the 
      schema for CKR primitives (provided in /schemas/meta.n3).
      
- [!] Input ontologies have to be valid OWL ontologies: as an effect, all of the
      used symbols (classes and properties) have to be specified.

- [ ] Currently, no separation of global meta and object knowledge.
     
================================================================================