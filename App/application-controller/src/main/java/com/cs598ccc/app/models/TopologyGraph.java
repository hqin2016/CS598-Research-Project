package com.cs598ccc.app.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopologyGraph {
    public Map<Vertex, List<Edge>> adj = new HashMap<>();

    public static class Vertex {
        public String name;

        public Vertex(String name) {
            this.name = name;
        }
    }

    public static class Edge {
        public String from;
        public String to;
        public Double weight;

        public Edge(String from, String to) {
            this.from = from;
            this.to = to;
            this.weight = 1.0;
        }

        public Edge(String from, String to, Double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    public void addVertex(String name) {
        Vertex v = new Vertex(name);
        List<Edge> edges = new ArrayList<>();
        this.adj.put(v, edges);
    }

    public void addVertex(String name, String[] dependencies) {
        Vertex v = new Vertex(name);
        List<Edge> edges = new ArrayList<>();
        Double weight = (double) (1.0 / dependencies.length);
        for (String dep : dependencies) {
            edges.add(new Edge(name, dep, weight));
        }
        this.adj.put(v, edges);
    }
}
