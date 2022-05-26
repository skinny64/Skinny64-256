# Boomerang Search on SKINNY

This folder contains the implementation of a boomerang search. It uses the gurobi solver to compute truncated boomerang, and the choco solver to compute the actual boomerang, and a better approximation of the probability of this boomerang.

## Compiling and running

A simple make will compile the code.

Before running, you may want to create a folder `output/` as the default output files will be saved in this folder and if it does not exist it will raise an error. To run, the command is `java -jar boomerangsearch.jar --options`. An example can be seen with `make runboth`. There are different options that can be seen with the `-help` option.

## Generation of tikz

There is a script to generate a tikz representation of the boomerang differential. To compile, the only difference with the previous compilation is the command `make tikz`. Then as previously a jar has been generated as `solutiontotikz.jar` that has a CLI. An example of command can be seen when doing `make runtikz1`.

The output is directly done in stdout, so is has to be copy/pasted in your latex project by hand. Only the tikzpicture is generated, without the headers and imports for the compilation. When generating a step 1 tikz representation, the jar will create the representation with the grid and the colors. When generating a step 2 representation, the jar will only write the values of the cells, so the output has to be added to an output of a step 1 to actually understand what is the cell.

## Explanation for files and directories

- `step1/`, `step2/` and `solutiontotikz/` contain all the classes for models and generation of tikz representation.
- Compilation and running files (does not include output), can be cleaned by `make clean`:
  - `boomerangsearch/` is a folder created by the compilation.
  - `boomerangsearch.jar` is the runnable jar for the models.
  - `solutiontotikz.jar` is the runnable jar for the generation of a tikz representation.
  - `model.mps` is an export of the model. Can be used to do auto-tuning of parameters.
- `META-INF/` is the folder declaring the manifest files, defining the dependencies for the construction of the jars
- `tune1.prm` is a set of parameters that will be used by Gurobi during the first step of the search.
