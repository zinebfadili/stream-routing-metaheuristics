from modelisation import *
from streamrouting import *
from output import *
import os
import sys

'''
Main function used to launch the program on the different configuration files.
The user is supposed to execute the program by passing the name of the configuration file in argument.

Example: 
    ./main TC1_check_red.app_network_description

Tested with python3
'''
def main():
    # checking if the arguments are correct and retrieving the network description file
    if len(sys.argv) != 2:
        usage()
    input_file = sys.argv[1]
    
    # building the different data structures needed for the solver
    streams, redundant, redundancies, names, rd_classes = get_streams(input_file)
    links = get_links(input_file)
    arrivals, non_arrivals = get_last_links(links, streams)
    departs, non_departs = get_first_links(links, streams)
    forbidden = get_forbidden(links, streams)
    
    # retrieving the solution
    solution = StreamRouting(streams, links, redundancies, names, departs, arrivals, non_departs, non_arrivals, forbidden, redundant, rd_classes)
    
    # outputing the solution in the xml file
    if solution is not None:
        order = put_in_order(solution, links, departs, arrivals)
        _, name = os.path.split(input_file)
        output_name = name + "_solution.xml"
        generate_output(output_name, order, rd_classes, name)


if __name__ == '__main__':
    main()