## MITM Search on SKINNYe

This folder contains the implementation of MITM search on SKINNYe. It uses the gurobi solver to solve the MILP model to get the MITM trails.

## Compiling and running

Before running, you may want to create a folder `skinnye/` and `skinnye_v2/` as the default output files will be saved in this folder and if it does not exist it will raise an error. 

## Explanation for files

- `MITM_Skinnye_64_256_rankequivalence_class_0123.py` is the code based on the rank-equivalence class [{0,1,2,3}] of attacking 31-round SKINNYe-64-256.
- `MITM_Skinnye_64_256_v2.py` is the code of attacking 27-round SKINNYe-64-256 v2.
