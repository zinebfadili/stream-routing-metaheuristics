from xml.dom import minidom
import xml.etree.ElementTree as xml

'''
Function used to retrieve the order in which a stream uses the links
to go from source to destination
'''
def put_in_order(solution, links, departures, destinations):
    in_order = []
    for index_stream, stream in enumerate(solution):
        departure = links[departures[index_stream][0]][0]
        destination = links[destinations[index_stream][0]][1]        
        order = dfs(departure, destination, stream, [], [], links)
        in_order.append(order)
    return in_order

'''
Function used to print the routes that all substreams take
'''
def routes_by_stream(in_order, rd_classes):
    for idx_class, rd_class in enumerate(rd_classes):
        print("Stream{}:".format(idx_class))
        for stream in rd_class:
            print("\t", end='')
            print(in_order[stream])

'''
Function used to print the description of the links rather than their index
'''
def print_links(indexes, links):
    for index in indexes:
        print("{} ".format(links[index]), end="")
    print()

'''
Function used to generate the xml output 
'''
def generate_output(filename, in_order, rd_classes, input_name):
    root = xml.Element("solution")
    tree = xml.ElementTree(root)
    root.set("tc_name", input_name)
    for idx_class, rd_class in enumerate(rd_classes):
        stream_elt = xml.Element("stream")
        stream_elt.set("id", "Stream{}".format(idx_class))
        root.append(stream_elt)
        for stream in rd_class:
            route_elt = xml.Element("route")
            stream_elt.append(route_elt)
            for link in in_order[stream]:
                link_elt = xml.Element("link")
                link_elt.set("src", link[0])
                link_elt.set("dest", link[1])
                route_elt.append(link_elt)
    xmlstr = minidom.parseString(xml.tostring(root)).toprettyxml()
    with open(filename, 'w') as f:
        f.write(xmlstr)

'''
Depth First Search used to put the solution in order:
 - current is the name of the current device
 - end is the final destination
 - solution is the list of links that the stream takes
 - path is the path we built so far to go to current
 - visited is an array containing all the links we already visited
 - streams is the description of the links
'''
def dfs(current, end, solution, path, visited, links):
    if current == end:
        return path.copy()
    else:
        for index in solution:
            stream = links[index].copy()
            if stream[1] not in visited and stream[0] == current:
                new_visited = visited.copy()
                new_visited.append(stream[1])
                new_path = path.copy()
                new_path.append(stream)
                new_current = stream[1]
                res = dfs(new_current, end, solution, new_path, new_visited, links)
                if res is not None:
                    return res



def main():
    pass

if __name__ == '__main__':
    main()