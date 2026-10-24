"""Joins MCP joined.srg (notch -> srg) with Calamus gen2 (notch -> intermediary).

usage: python3 genmappings.py <joined.srg> <calamus mappings.tiny> <out.gz>

Output lines (tab separated):
  C  notch  intermediary  srg/mcp class
  M  notchOwner  notchName  notchDesc  intermediary  srg
  F  notchOwner  notchName  notchDesc  intermediary  srg
"""
import gzip
import sys

srg_path, tiny_path, out_path = sys.argv[1:]

classes, methods, fields = {}, {}, {}
owner = None
with open(tiny_path) as f:
    next(f)
    for line in f:
        p = line.rstrip("\n").split("\t")
        if p[0] == "c":
            owner = p[1]
            classes[owner] = p[2]
        elif len(p) == 5 and p[1] == "m":
            methods[(owner, p[3], p[2])] = p[4]
        elif len(p) == 5 and p[1] == "f":
            fields[(owner, p[3])] = (p[2], p[4])

srg_classes, srg_methods, srg_fields = {}, {}, {}
with open(srg_path) as f:
    for line in f:
        p = line.split()
        if p[0] == "CL:":
            srg_classes[p[1]] = p[2]
        elif p[0] == "FD:":
            o, n = p[1].rsplit("/", 1)
            srg_fields[(o, n)] = p[2].rsplit("/", 1)[1]
        elif p[0] == "MD:":
            o, n = p[1].rsplit("/", 1)
            srg_methods[(o, n, p[2])] = p[3].rsplit("/", 1)[1]

out = []
for notch, inter in classes.items():
    out.append(f"C\t{notch}\t{inter}\t{srg_classes.get(notch, notch)}")
for (o, n, d), inter in methods.items():
    out.append(f"M\t{o}\t{n}\t{d}\t{inter}\t{srg_methods.get((o, n, d), '-')}")
for (o, n), (d, inter) in fields.items():
    out.append(f"F\t{o}\t{n}\t{d}\t{inter}\t{srg_fields.get((o, n), '-')}")

def check(kind, pairs):
    seen = {}
    for s, i in pairs:
        if s != "-" and seen.setdefault(s, i) != i:
            print(f"warning: {kind} {s} maps to {seen[s]} and {i}", file=sys.stderr)

check("method", ((srg_methods.get(k, "-"), v) for k, v in methods.items()))
check("field", ((srg_fields.get(k, "-"), v[1]) for k, v in fields.items()))
missing = sum(1 for k in srg_methods if k not in methods) + sum(1 for k in srg_fields if k not in fields)
print(f"{len(classes)} classes, {len(methods)} methods, {len(fields)} fields, {missing} srg members without intermediary", file=sys.stderr)

with gzip.open(out_path, "wt") as f:
    f.write("\n".join(out) + "\n")
