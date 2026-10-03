"""Focused structural checks, NOT a PDF/UA certification or assistive-reader test.

Usage: bundled-python Test-PdfStructure.py <generated qa-reports directory>
Requires pypdf from the bundled workspace runtime; does not modify the PDFs.
"""
import re
import sys
from pathlib import Path
from pypdf import PdfReader
from pypdf.generic import ContentStream


def check(path):
    reader = PdfReader(path)
    catalog = reader.trailer["/Root"]
    assert catalog["/MarkInfo"]["/Marked"], "Missing marked-document flag"
    assert catalog["/Lang"] in ("en", "es"), "Missing expected language"
    assert reader.metadata.title, "Missing title"
    root = catalog["/StructTreeRoot"]
    nums = root["/ParentTree"]["/Nums"]
    parents = {int(nums[i]): nums[i + 1].get_object() for i in range(0, len(nums), 2)}
    references = set()
    figures = 0
    headings = []

    def walk(node):
        nonlocal figures
        element = node.get_object()
        role = element.get("/S")
        if role == "/Figure":
            assert element.get("/Alt"), "Figure lacks alternate description"
            figures += 1
        if role and re.fullmatch(r"/H[1-6]", role):
            headings.append(int(role[-1]))
        kids = element.get("/K", [])
        if not isinstance(kids, list):
            kids = [kids]
        for kid in kids:
            child = kid.get_object()
            if child.get("/Type") == "/MCR":
                page = child["/Pg"]
                key, mcid = int(page["/StructParents"]), int(child["/MCID"])
                assert (key, mcid) not in references, "Duplicate marked-content reference"
                references.add((key, mcid))
                assert parents[key][mcid].get_object() == element, "Broken parent-tree link"
            else:
                walk(kid)

    root_kids = root["/K"]
    if not isinstance(root_kids, list):
        root_kids = [root_kids]
    for kid in root_kids:
        walk(kid)
    assert headings[0] == 1
    for previous, current in zip(headings, headings[1:]):
        assert current <= previous + 1, "Skipped heading level"
    content_references = set()
    for page in reader.pages:
        key = int(page["/StructParents"])
        properties = page["/Resources"].get("/Properties", {})
        stack = []
        for operands, operator in ContentStream(page.get_contents(), reader).operations:
            if operator in (b"BMC", b"BDC"):
                stack.append(operands[0])
                if operator == b"BDC" and operands[0] != "/Artifact":
                    props = operands[1]
                    if isinstance(props, str):
                        props = properties[props].get_object()
                    content_references.add((key, int(props["/MCID"])))
            elif operator == b"EMC":
                assert stack, "Unbalanced marked content"
                stack.pop()
            elif operator in (b"Tj", b"TJ", b"Do", b"S", b"f"):
                assert stack, "Unmarked visible content"
        assert not stack, "Unclosed marked content"
        for font_ref in page["/Resources"].get("/Font", {}).values():
            font = font_ref.get_object()
            descriptor = font["/DescendantFonts"][0].get_object()["/FontDescriptor"]
            assert any(k in descriptor for k in ("/FontFile", "/FontFile2", "/FontFile3")), "Unembedded font"
            assert "/ToUnicode" in font, "Missing Unicode map"
    assert content_references == references, "Content and structure references do not match"
    text = "\n".join(page.extract_text() for page in reader.pages)
    if path.name.startswith("medications-"):
        assert set(re.findall(r"QA-(\d+)", text)) == {str(i) for i in range(1, 121)}, "Dropped reminders"
    if path.name.startswith("health-"):
        assert figures == 4, "Expected one accessible figure per measurement type"
    print(f"PASS {path.name}: {len(reader.pages)} pages, {len(references)} content links, {figures} figures")


files = sorted(Path(sys.argv[1]).glob("*.pdf"))
assert files, "No sample PDFs found"
for file in files:
    check(file)
