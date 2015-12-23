#CKRew download and installation

##Download
An archive containing the latest version of CKRew (1.3) can be downloaded at:  
http://dkm.fbk.eu/resources/ckr/ckr-datalog-rewriter-d-1.3.zip

##Requirements

- DLV 2012-12-17 (or newer) (http://www.dlvsystem.com/dlv/)  
  For ease of use, it is preferrable to install a copy of the DLV executable in 
  `/localdlv/dlv`, the directory used by default as DLV path by the prototype
- Java runtime version 1.7 (or greater)

##Installation

- Extract the contents of the .zip archive
- Execute the `ckrew.bat` (Windows) or `ckrew.sh` (Unix) script in the main folder  
   
- Examples can be found in `/demo` folder
- Refer to the [examples](demos.html) page or `/demo/DEMO_README.txt` for examples usage.

##Usage

> `ckrew <global-context-file> [<local-module-file> | <options>]`

`<global-context-file>`  
  Ontology file containing the global context for the input CKR.

`<local-module-file>`  
  Ontology files (zero or more) containing a knowledge module for the input CKR.

**Example:** `ckrew global.n3 m1.n3 m2.n3`  

**Options:**  
 **`-v`:** verbose (prints more information about loading and rewriting process)  
 **`-out <output-file>`:** specifies the path to the output program file (default: `output.dlv`)  
 **`-trig`:** specifies that the input is provided as a single TRIG file   
 **`-dlv <dlv-path>`:** specifies the path to the DLV executable (default: `localdlv/dlv`)  

