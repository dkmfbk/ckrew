#Examples

The description and examples presented in this page can be found in the `/demo` folder
of the CKRew installation archive (see [download page](installation.html)).

##Introduction
The files presented in this demo represent an implementation for examples
introduced in recent CKR publications [[1](#ref1), [2](#ref2), [3](#ref3), [4](#ref4), [5](#ref5)].   
Their goal is to demonstrate
the functionality of the CKR rewriter prototype and provide an intuitive 
interpretation for its output.

The five examples included in this demo correspond to the `.bat` (and `.sh`) files
contained in folder `/demo`.  
By running one of these shell files, the prototype is executed using the 
corresponding RDF files in the `../testcase` folder and the output program is 
saved to `output.dlv`.  

For each example, a query demonstrating the result of the translation is 
contained in one of the `.dlv` files in the `/demo` folder.  
Example queries over the output program can be run (assuming that a local copy 
of DLV is installed in `../localdlv`) by executing:

> ``../localdlv/dlv output.dlv -cautious queryname.dlv``

In the following, for each of the examples, we give a brief description for 
the represented scenario and an intuitive explanation for the query results.
For an extended and formal definition of each example, please consider the 
mentioned references.

## Example 1: Football cup winner

**Reference:** [[1](#ref1)]  
**Shell file:** `eswc12-demo`  
**Query file:** `winnerQuery.dlv`  

This example shows a simple case of knowledge propagation across contexts.
The scenario is that of football competitions and their rules for qualification 
across them.
We consider the case of Club World Cup 2011 and Champions League 2011. The two 
competitions are represented as two distinct contexts.
In Club World Cup 2011, we state that `:barcelona` was the `:Winner` team.
In the Champions League context, we define that its "`:Winner` is a `:UEFATeam` 
that participated as `:Team` in this year Club World Cup". This can be easily 
realized using eval expressions that import teams from `:UEFAClubFootball` class 
of events and teams from Club World Cup 2011 and then considers their 
intersection.

The query contained in `winnerQuery.dlv` asks for the `:Winner` in the Champions 
League context. It can be checked that `:barcelona` is returned as the result, 
since (by local inference in the context of Club World Cup 2011) it is 
recognized as a Club World Cup participant and it is asserted that it is 
an `:UEFATeam` (in the knowledge module associated to the `:UEFAClubFootball` 
context class). Eval axioms permit to propagate such knowledge to the context of 
Champions League 2011, where such team is then classified as `:Winner`.

## Example 2: Tourists preferred teams

**Reference:** [[2](#ref2), [4](#ref4)]  
**Shell file:** `tourism-demo`  
**Query file:** `preferredTeamQuery.dlv`  

This example shows the mechanism of knowledge organization in modules and
propagation across contexts by eval axioms.  
Domain of interest of this example is recommendation of touristic events.
In particular, we concentrate on `:SportsEvents` (precisely `:VolleyMatches`) and 
the interests of `:SportiveTourists`: in this case, contexts are used to represent
the situation of a single volley match and the local set of preferences of 
sportive tourists.  
In the modules representing information about single matches we store facts such 
as `:Winner` and `:Loser` teams. In modules associated to tourists, we store their 
`:PreferredTeams`.  
Moreover, using eval expressions, in the module associated to the context class
of all `:SportiveTourists` we state that their "`:PreferredTeams` include the 
`:TopTeams` from all `:SportiveEvents`". In turn, `:TopTeams` are defined in the 
module associated to `:SportiveEvents` as "`:Winners` from `:TopMatches`" (defined in 
the global context as those volley matches that are played inside a 
`:VolleyA1Competition`).

The query contained in `preferredTeamQuery.dlv` asks for the `:PreferredTeams` in 
the context of a particular `:SportiveTourist`, `:volley_fan_01`.  
In the module associated to the tourist, only `:andreoli_latina_volley`
is explicitly asserted as `:PreferredTeam`: however, by the above relations 
defined for all `:SportiveTourists`, also the two `:TopTeams` 
`:itas_diatec_trentino_volley` and `:casa_modena_volley` are returned.

## Example 3: Interesting tourism events

**Reference:** [[3](#ref3), [4](#ref4)]  
**Shell file:** `tourism-example-d`  
**Query file:** `interestingQuery.dlv`  

This example presents a first demonstration for defeasible inheritance of global
axioms.  
The presented scenario is similar to that of previous example, as we reason 
about tourists preferences about events.
In this case, under the global context we only have a single context, 
representing one `:cultural_tourist`.  
In the global knowledge we state, using a default axiom that "by default
all of the `:Cheap` events are `:Interesting`". Moreover, we assert that a local 
market (`:market`) and a football match (`:fbmatch`) are both `:Cheap` events.
In the module of the local context for the `:cultural_tourist`, however, we 
explicitly assert that for him "the football match is not `:Interesting`".

The query in `interestingQuery.dlv` asks for all `:Interesting` events in the 
local context of the `:cultural_tourist`.  
By the interpretation of defeasible global axioms, we have that the inclusion of 
`:fbmatch` in `:Interesting` events is disregarded at this local context: the local 
negation basically proves the overriding of the defeasible axiom for the 
`:fbmatch` instance.
However, since we have no evidence of the contrary, the axiom can be 
instantiated for the `:market` instance, which is in fact returned as an 
`:Interesting` event for the tourist.

## Example 4: Current workers

**Reference:** [[3](#ref3), [4](#ref4)]  
**Shell file:** `workers-example-d`  
**Query file:** `workingNowQuery.dlv`  

Goal of this example is to present the interaction of defeasible axioms with
eval expression in order to represent a form of defeasible knowledge 
propagation.  
In this example we want to represent information about employees in an 
organization, where local contexts describe the situation in a single year.
At the global context, we express the defeasible axiom that in a given year 
"in general, all of the employees `:WorkingBefore` (i.e. the year before) are 
also `:WorkingNow`".  
In the context associated to year 2012, we assert as `:WorkingNow` three 
employees: `:alice`, `:bob` and `:charlie`.
In the context representing year 2013, we import (using an eval expression) all 
of the employees that were working in 2012 as `:WorkingBefore`. However, we also 
assert that `:charlie` is no more `:WorkingNow` for the organization.

The query in `workingNowQuery.dlv` asks for employees `:WorkingNow` in the 
context of year 2013.
Following from the eval expression, `:alice`, `:bob` and `:charlie` are imported as 
`:WorkingBefore`. However, only `:alice` and `:bob` are returned as `:WorkingNow`: 
the local negative assertion about `:charlie` overrides the instance of the 
defeasible inclusion for this individual.

## Example 5: Department members

**Reference:** [[5](#ref5)]  
**Shell file:** `department-example-d`  
**Query file:** `hasCourseQuery.dlv` 

In this example we demonstrate the option for rewriting DL-Lite_R defeasible
knowledge bases (DKBs) and the possibility of reasoning on defeasible 
existential axioms.   
We consider the scenario of the organization of an university research
department: using a defeasible axiom, we specify that "in general, department
members need to teach at least a course". This is represented as an axiom of
the kind D(`:DeptMember` \isa \exists `:hasCourse`).   
Moreover, each `:Professor` and each `:PhDStudent` is a `:DeptMember`.    
However, a `:PhDStudent` can not hold a course, i.e. we have a strict axiom
`:PhDStudent` \isa \not \exists `:hasCourse`.   
In the example DKB we have two members: `:alice` is a `:Professor` and `:bob` is a
`:PhDStudent`.

The query in `hasCourseQuery.dlv` asks for members for which it is known that
they teach at least a course.   
By the interpretation of the defeasible axiom, we have that the axiom is 
disregarded in the case of `:bob`. This is due to the fact that the strict
axiom for `:PhDStudent` allows to derive a justification for the overriding 
of the `:bob` instance. On the other hand, the axiom is applied to the instance
of `:alice`, since no overriding can be derived: thus `:alice` is the only
instance that is returned by the query.

***

## References

<a id="ref1">[1]</a>
Bozzato, L., Corcoglioniti, F., Homola, M., Joseph, M., Serafini, L.:   
*Managing Contextualized Knowledge with the CKR.*  
Poster in: ESWC2012 (2012)([paper](http://2012.eswc-conferences.org/sites/default/files/eswc2012_submission_338.pdf))

<a id="ref2">[2]</a>
Bozzato, L., Serafini, L.:  
*Materialization Calculus for Contexts in the Semantic Web.*   
In: DL2013. CEUR-WP, CEUR-WS.org (2013)([paper](http://ceur-ws.org/Vol-1014/paper_51.pdf))

<a id="ref3">[3]</a>
Bozzato, L., Eiter, T., Serafini, L.:   
*Defeasibility in contextual reasoning with CKR.*   
In: ARCOE-13 (2013)([notes](http://www.arcoe.org/2013/notes.html))

<a id="ref4">[4]</a>
Bozzato, L., Eiter, T., Serafini, L.:    
*Enhancing context knowledge repositories with justifiable exceptions.*   
In: Artificial Intelligence 257:72-126 (2018)([paper](https://doi.org/10.1016/j.artint.2017.12.005))

<a id="ref5">[5]</a>
Bozzato, L., Eiter, T., Serafini, L.:   
*Reasoning on DL-Lite_R with Defeasibility in ASP.*   
In: RuleML+RR 2019 (2019)([paper](https://doi.org/10.1007/978-3-030-31095-0_2))

