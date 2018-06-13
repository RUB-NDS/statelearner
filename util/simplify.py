#!/usr/bin/env python3

import argparse
import re


class State:
    def __init__(self, name, parameters):
        self.name = name
        self.parameters = parameters

    def __str__(self):
        return '{0} {1}'.format(self.name, self.parameters)


class Edge:
    def __init__(self, start, end, parameters, label):
        self.start = start
        self.end = end
        self.parameters = parameters
        if label is None:
            self.label_in = None
            self.label_out = None
        else:
            split = label.split(' / ')
            if len(split) == 2:
                self.label_in = split[0]
                self.label_out = split[1]
            elif len(split) == 1:
                self.label_in = label
                self.label_out = None
            else:
                self.label_in = None
                self.label_out = None

    def __str__(self):
        parameters = ''
        closed = True
        if self.label_in is not None or self.label_out is not None:
            label = [self.label_in, self.label_out]
            if None in label:
                label.remove(None)
            label = ' / '.join(label)
            parameters += ' [label="{0}"'.format(label)
            closed = False
        if self.parameters is not None and self.parameters != '':
            if closed:
                parameters += ' ['
                closed = False
            else:
                parameters += ' '
            parameters += self.parameters
        if not closed:
            parameters += '];'

        return '{0} -> {1}{2}'.format(self.start, self.end, parameters)


def extract(lines):
    states = []
    edges = dict()
    for line in lines:
        if '{' in line or '}' in line:
            continue
        split = line.split()
        if len(split) < 1:
            continue
        if '->' in split:
            edge_parameters = ' '.join(split[3:])
            edge_parameters = edge_parameters.replace(';', '').replace('[', '').replace(']', '').replace('"', '')
            split_parameters = edge_parameters.split('=')
            label = None
            if 'label' in split_parameters:
                label = split_parameters[split_parameters.index('label') + 1]
                split_parameters.remove(label)
                split_parameters.remove('label')
            parameters = ' '.join(split_parameters)
            start = split[0]
            edge = Edge(start, split[2], parameters, label)
            if start not in edges:
                edges[start] = []
            edges[start].append(edge)
        else:
            state = State(split[0], ' '.join(split[1:]))
            states.append(state)

    return states, edges


def simplify(path):
    with open(path) as f:
        lines = f.readlines()
    states, states_to_edges = extract(lines)
    new_states = []
    new_edges = []
    for state in states:
        edges = states_to_edges[state.name]
        new_states.append(state)
        output_to_edges = dict()
        for edge in edges:
            if edge.label_out is None:
                new_edges.append(edge)
                continue
            output_and_end = '{0}/{1}'.format(edge.label_out, edge.end)
            if output_and_end not in output_to_edges:
                output_to_edges[output_and_end] = []
            output_to_edges[output_and_end].append(edge)
        for output_and_end, edges in output_to_edges.items():
            if len(edges) == 1:
                new_edges.append(edges[0])
            else:
                start = state.name
                end = edges[0].end
                label_out = edges[0].label_out
                parameters = edges[0].parameters
                state_label = re.sub(r'\W+', '', label_out)
                name = '{0}X{1}X{2}'.format(start, end, state_label)
                invisible_state = State(name, '[shape=point width=0.01 height=0.01];')
                new_states.append(invisible_state)
                for edge in edges:
                    new_edges.append(Edge(start, invisible_state.name,
                                          edge.parameters + 'dir=none', edge.label_in))
                new_edges.append(Edge(invisible_state.name, end,
                                      parameters, label_out))
    print('digraph g {')
    print('\tgraph [rankdir=LR,concentrate=true];')
    for state in new_states:
        print('\t{0}'.format(state))
    for edge in new_edges:
        print('\t{0}'.format(edge))
    print('}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Beautify state machine of TLS implementation')
    parser.add_argument('files', nargs=argparse.REMAINDER,
                        help='State machine file in DOT format')
    args = parser.parse_args()
    files = args.files

    for file_path in files:
        simplify(file_path)
