import xml.etree.ElementTree as ET
import sys

'''
File used to implement the different functions that will be used to retrieve data from the files 
and to build the necessary data structures that are used to specify the constraints to the solver
'''

'''
Function used to display the usage message if there is a problem with the arguments
'''
def usage():
    print("Usage : {} <input file>".format(sys.argv[0]))
    sys.exit(0)

'''
Function used to print the different devices inside the file
'''
def get_devices(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    for device in root.findall('device'):
        print("Name: {}, type: {}".format(device.attrib['name'], device.attrib['type']))

'''
Function used to build the different data structures related to the streams.
Returns the streams, redundant, redundancies and names arrays
'''
def get_streams(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    streams = []
    redundant_links = []
    names = []
    redundancies = []
    rd_classes = []
    idx_c = 0
    for stream_idx, stream in enumerate(root.findall('stream')):
        stream_id = stream.attrib['id']
        src = stream.attrib['src']
        dst = stream.attrib['dest']
        speed = int(stream.attrib['size']) / int(stream.attrib['period'])
        redundancy = int(stream.attrib['rl'])

        entry = [src, dst, speed]

        if redundancy == 1:
            names.append(stream_id)
            streams.append(entry)
            redundant_links.append([])
            redundancies.append(1)
            rd_classes.append([idx_c])
            idx_c+=1
        else :
            result_rd = []
            for index in range(redundancy):
                names.append("{}_{}".format(stream_id, index))
                redundant_links.append([stream_idx + x for x in range(redundancy) if x != index])
                streams.append(entry)
                redundancies.append(1/redundancy)
                result_rd.append(idx_c)
                idx_c +=1
            rd_classes.append(result_rd)
    
    return streams, redundant_links, redundancies, names, rd_classes

'''
Function used to retrieve the information about the links inside the xml file
Returns the links array
'''
def get_links(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    links = []

    for link in root.findall('link') :
        src = link.attrib['src']
        dst = link.attrib['dest']
        speed = float(link.attrib['speed'])
        links.append([src, dst, speed])
    
    return links

'''
Function  used to retrieve the first links that can be used by the streams
'''
def get_first_links(links, streams):
    first_links = [[] for _ in range(len(streams))]
    non_first_links = [[] for _ in range(len(streams))]
    for idx_link, link in enumerate(links):
        for idx_stream, stream in enumerate(streams):
            if stream[0] == link[0]:
                first_links[idx_stream].append(idx_link)
            else:
                non_first_links[idx_stream].append(idx_link)
    
    return first_links, non_first_links

def get_forbidden(links, streams):
    forbidden = [[] for _ in range(len(streams))]

    for idx_link, link in enumerate(links):
        for idx_stream, stream in enumerate(streams):
            if stream[0] == link[1]:
                forbidden[idx_stream].append(idx_link)
            if stream[1] == link[0]:
                forbidden[idx_stream].append(idx_link)
    
    return forbidden

'''
Function used to retrieve the last links that can be used by the streams
'''
def get_last_links(links, streams):
    last_links = [[] for _ in range(len(streams))]
    non_last_links = [[] for _ in range(len(streams))]
    for idx_link, link in enumerate(links):
        for idx_stream, stream in enumerate(streams):
            if stream[1] == link[1]:
                last_links[idx_stream].append(idx_link)
            else:
                non_last_links[idx_stream].append(idx_link)
    return last_links, non_last_links

'''
Function used to retrieve the list of the links which are going to a specific link specified by its index
'''
def incomingLinks(idx_link, links):
  indexes = []
  for p in range(len(links)):
     if p!=idx_link:
       if links[idx_link][0]==links[p][1]:
         indexes.append(p)
  return indexes

'''
Function used to retrieve the list of the links which are coming from a specific link specified by its index
'''
def outgoingLinks(idx_link, links):
  indexes = []
  for p in range(len(links)):
     if p!= idx_link:
       if links[idx_link][1]==links[p][0]:
        indexes.append(p)
  return indexes

'''
Function used to get the common elements between two lists
'''
def intersection(lst1, lst2): 
    return list(set(lst1) & set(lst2))