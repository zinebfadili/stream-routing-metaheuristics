# Genetic Algorithm
This is the code for the Genetic Algorithm (GA) used for course 02229-System Optimisation
Ragnar Sandberg Mikkelsen s111975
Laura-Andreea Petre s192671

## Test Cases
The GA has been run on the following test cases:
Name: TC0_example
Purpose: Use this as a simple example testcase

TC1_check_red - 
Purpose: Use this to test that your methods favors splitting the stream along disjunct paths instead of having all of them overlap on the shorter path (See solution)

TC1_check_red2 - 
Purpose: Similar to the original TC1, but contains more link and is modeled to be similar to an example given in class.

TC2_check_bw - 
Purpose: Use this to test that your method avoids overloading the bandwidth of a link, even at the cost of a longer path (See solution)

TC3_medium - 
Purpose: A simple medium sized testcase

TC4_split_and_merge -
Purpose: Use this to test that your method computes the bandwidth correctly, considering that redundant copies of a stream can split and merge along the route. (Stream0 merges on link SW2,ES2 and thus only consumes 0,8 Mbit/s)

TC5_large1 -
Purpose: A large testcase

TC6_large2 -
Purpose: A large testcase

TC7_huge -
Purpose: A huge testcase to test the scalability of your solution

Each test case is located in the input folder

The solutions to the test cases are located in the output folder

## Requirements

To run the program you will need to have java version 8 or higher installed

## How to run

The GA program takes 5 arguments: maxGenerations, populationSize, maxUnchanged, seed, testcase_name

Example:
```
java -jar ga.jar 1000 100 100 1234 TC3_medium.
```
_The dot (.) after the testcase name is important_