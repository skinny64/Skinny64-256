# Libs

This folder contains all the necessary java jars.

- Jackson v2.11.0: This is the JSON parsing library. The jars needed are jackson-core, jackson-databind and jackson-annotations. They can be found in [the github page](https://github.com/FasterXML/jackson). Precisely, you can download the different jars at:
  - [jackson-core](https://github.com/FasterXML/jackson-core/releases/tag/jackson-core-2.11.0)
  - [jackson-databind](https://github.com/FasterXML/jackson-databind/releases/tag/jackson-databind-2.11.0)
  - [jackson-annotations](https://github.com/FasterXML/jackson-annotations/releases/tag/jackson-annotations-2.11.0)
- Javatuples v1.2: Useful library for dealing with tuples. The jar can be found in [the github page](https://github.com/javatuples/javatuples/downloads)
- Picocli v4.3.2: The library to have a nice CLI. The jar can be found in [the github page](https://github.com/remkop/picocli/releases/tag/v4.3.2)
- Gurobi v9.0.2: The MIP solver for step 1. This solver is commercial but there are free licences for academic purposes. The jar in this folder is the one of the version v9.0.2, but you may want to change it depending on your specific installation of Gurobi. For all the details on how to download, install and get a licence, check their [website](https://www.gurobi.com/)
- Choco v4.10.2: The CP solver for step 2. This solver is open-source and can be downloaded from [the github page](https://github.com/chocoteam/choco-solver/releases/tag/4.10.2)
- Sandwichproba v1.0.0: This needs to be compiled from the source in this repository. To do so you can go to [the sandwichproba folder](../sandwichproba), execute `make jar`, and copy the created jar `sandwichproba.jar` into your libs folder.