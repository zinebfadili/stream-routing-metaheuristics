from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from ortools.sat.python import cp_model
from modelisation import *
from itertools import product


def StreamRouting(streams, links, redundancy, names, departs, arrivals, non_departs, non_arrivees, forbidden, redundant, rd_classes):
    """Demonstrates how to link integer constraints together."""
    num_streams = len(streams)
    num_links = len(links)

    redon_lengths = []
    for i in range(len(rd_classes)):
      redon_lengths.append(1/(len(rd_classes[i])))
    
    model = cp_model.CpModel()

    # we create the variables used by the solver to represent the links taken by the substreams
    x = {}
    for i in range(num_streams):
        for j in range(num_links):
            x[(i, j)] = model.NewIntVar(0, 1, 'x_%i_%i' % (i, j))

    b = {}
    # for each link we create a new value 
    for l in range(num_links):
      for s in range(len(rd_classes)):
        b[(l, s)] = model.NewBoolVar('b_%i_%i' % (l, s))

    # setting the b variable to true if substreams go through the link
    for l in range(num_links):
      for s in range(len(rd_classes)):
        model.Add(sum([x[k, l] for k in rd_classes[s]]) != 0).OnlyEnforceIf(b[(l, s)])
        model.Add(sum([x[k, l] for k in rd_classes[s]]) == 0).OnlyEnforceIf(b[(l, s)].Not())
    

    # coefficient that will be used to compute the cunsomption (either 0 or 1)
    c = {}
    # for each link we create a new value 
    for l in range(num_links):
      for s in range(len(rd_classes)):
        c[(l, s)] = model.NewIntVar(0, 1, 'c_%i_%i' % (l, s))

    # c is equal to one if b is true, else 0
    for l in range(num_links):
      for s in range(len(rd_classes)):
        model.Add(c[(l, s)]==1).OnlyEnforceIf(b[(l, s)])
        model.Add(c[(l, s)]!= 1).OnlyEnforceIf(b[(l, s)].Not())

    # bandwidth consumption
    for l in range(num_links):
      model.Add(sum([int(streams[rd_classes[s][0]][2] * 10000)*c[l,s] for s in range(len(rd_classes))])<= int(links[l][2] * 10000))
    
    #every link going out of the destination link of a stream should not be used, every link going into the start link of a link should not be used, avoid cycles
    for i in range(num_streams):
      model.Add(sum([x[i,j] for j in forbidden[i]])== 0)

    #one of the start links should be used
    for i in range(num_streams):
      model.Add(sum([x[i,j] for j in departs[i]])== 1) 

    #one of the arrival links should be used
    for i in range(num_streams):
      model.Add(sum([x[i,j] for j in arrivals[i]])== 1) 

    # can only take one link if one link which arrives to the link is taken
    for i in range(num_streams):
      for j in non_departs[i]:
        model.Add(sum([x[i,n] for n in incomingLinks(j, links)])==1).OnlyEnforceIf(x[i,j]) 

    # can only take one link if one link which go from you that is taken
    for i in range(num_streams):
      for j in non_arrivees[i]:
        model.Add(sum([x[i,n] for n in outgoingLinks(j, links)])==1).OnlyEnforceIf(x[i,j]) 

    # can only take one link if a link going to you is taken and if one going from you is taken
    for i in range(num_streams):
      for j in intersection(non_departs[i],non_arrivees[i]):
        model.Add(sum([x[i,n] for n in incomingLinks(j, links)])==1).OnlyEnforceIf(x[i,j])
        model.Add(sum([x[i,n] for n in outgoingLinks(j, links)])==1).OnlyEnforceIf(x[i,j])  

################## minimize length and redundancy ##################################
    
    val_length = num_links*num_streams*num_streams
    #length
    length = model.NewIntVar(0,val_length, 'length')
    for j in range(num_links):
      for i in range(num_streams):
        length +=x[i, j]

    l_o = {}
    for s in range(len(rd_classes)):
      for l in range(num_links):
        for i in rd_classes[s]:
          l_o[(i, l)] = model.NewIntVar(0, len(rd_classes[s])*num_streams*num_links*1000,'l_o_%i_%i' % (i, l))
  
    h = {}
    for i in range(num_streams):
      for j in range(num_links):
        h[(i, j)] = model.NewBoolVar('h_%i_%i' % (i, j))

    for i in range(num_streams):
      for j in range(num_links):
        model.Add(x[i,j]==1).OnlyEnforceIf(h[i,j])
        model.Add(x[i,j]==0).OnlyEnforceIf(h[i,j].Not())


    for s in range(len(rd_classes)):
      for l in range(num_links):
        for i in rd_classes[s]:
          model.Add(l_o[i, l]==(sum([x[k, l] for k in rd_classes[s]])-1)).OnlyEnforceIf(h[i,l])
          model.Add(l_o[i, l]==0).OnlyEnforceIf(h[i,l].Not())


    redon = model.NewIntVar(0, num_streams*num_links, 'redon')
    for l in range(num_links):
      for s in range(len(rd_classes)):
        for i in rd_classes[s]:
          redon +=l_o[i, l]
    
    # telling the solver to minimize the length and redundancy
    model.Minimize(100*redon + length)

    # solving the model
    solver = cp_model.CpSolver()
    status = solver.Solve(model)
    print('Solve status: %s' % solver.StatusName(status))

    # printing the solution if optimal
    if status == cp_model.OPTIMAL:
        solution = []
        print('Optimal objective value: %i' % solver.ObjectiveValue())
        for i in range(num_streams):
          sub = []
          print('  ')
          for j in range(num_links):
            if solver.Value(x[(i, j)])==1:
                print('Stream %s going through link %d  %s.' %
                      (names[i], j, links[j]))
                sub.append(j)
          solution.append(sub)
        print('----------------')
        print('Statistics')
        print('  - conflicts : %i' % solver.NumConflicts())
        print('  - branches  : %i' % solver.NumBranches())
        print('  - wall time : %f s' % solver.WallTime())
        return solution