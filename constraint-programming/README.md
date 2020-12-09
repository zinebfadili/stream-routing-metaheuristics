François Goudineau (s201589)

Zineb Fadili (s201501)

### 02229 - Systems Optimization 
## Requirements
In order to run the constraint programming program you will need to have python3 installed on your machine, as well as the OR-Tools library.
To install OR-Tools, just type the following command in a terminal:
> python3 -m pip install --upgrade --user ortools

## Running the program
To run the program itself, go the the "constraint-programming" folder and run the following command:
> python3 main.py \<Path to your file\>

You need to specify the name of the file containing the network description in argument. The output file with the result will be written as the input file name with the _solution.xml extension.

#### For Example:
> python3 main.py TC0

will produce the following file:

> TC0_solution.xml