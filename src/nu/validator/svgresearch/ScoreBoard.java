/*
 * Copyright (c) 2008 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.svgresearch;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class ScoreBoard {
    int total = 0;
    
    int nonSvgRoot = 0;

    int nonNamespaceSvgRoot = 0;

    int otherNamespaceSvgRoot = 0;

    int hasFlowRoot = 0;

    int hasDoctype = 0;

    int hasInternalSubset = 0;

    int hasMetadata = 0;

    int hasStyleAttribute = 0;

    int hasPresentationAttributes = 0;
    
    int hasStyleElement = 0;

    int hasDefinitionElementsOutsideDefs = 0;
    
    final Map<NameTriple,Integer> prefixedSvgElements = new HashMap<NameTriple,Integer>();

    final Map<NameTriple,Integer> foreignElementsInMetadata = new HashMap<NameTriple,Integer>();

    final Map<NameTriple,Integer> foreignElementsElsewhere = new HashMap<NameTriple,Integer>();

    final Map<NameTriple,Integer> prefixedAttributes = new HashMap<NameTriple,Integer>();

    final Map<NameTriple,Integer> fontAttributes = new HashMap<NameTriple,Integer>();

    final Map<String,Integer> unconventionalXLinkPrefixes = new HashMap<String,Integer>();
    
    final Map<String,Integer> fontParent = new HashMap<String,Integer>();
    
    final Map<String,Integer> piTargets = new HashMap<String,Integer>();

    final Map<String,Integer> requiredExtensions = new HashMap<String,Integer>();
    
    final Map<String,Integer> internalEntities = new HashMap<String,Integer>();
    
    final Map<String,Integer> creator = new HashMap<String,Integer>();
    
    private void printRatio(String label, int count) {
        System.out.printf("%s: %f\n", label, (((double)count))/((double)total));
    }
    
    private void printMap(String label, Map<? extends Object, Integer> map) {
        System.out.println();
        System.out.println(label);
        SortedSet<SortStruct> sort = new TreeSet<SortStruct>();
        for (Map.Entry<? extends Object, Integer> entry : map.entrySet()) {
            sort.add(new SortStruct(entry.getValue().intValue(), entry.getKey().toString()));
        }
        for (SortStruct sortStruct : sort) {
            printRatio(sortStruct.getLabel(), sortStruct.getCount());
        }
    }
    
    public void printScoreBoard() {
        System.out.println("----------------");
        System.out.print("Total: ");
        System.out.println(total);
        
        printRatio("nonSvgRoot", nonSvgRoot);

        printRatio("nonNamespaceSvgRoot", nonNamespaceSvgRoot);

        printRatio("otherNamespaceSvgRoot", otherNamespaceSvgRoot);

        printRatio("hasFlowRoot", hasFlowRoot);

        printRatio("hasDoctype", hasDoctype);

        printRatio("hasInternalSubset", hasInternalSubset);

        printRatio("hasMetadata", hasMetadata);

        printRatio("hasStyleAttribute", hasStyleAttribute);

        printRatio("hasPresentationAttributes", hasPresentationAttributes);
        
        printRatio("hasStyleElement", hasStyleElement);

        printRatio("hasDefinitionElementsOutsideDefs", hasDefinitionElementsOutsideDefs);

        System.out.println();
        
        printMap("prefixedSvgElements", prefixedSvgElements);

        printMap("foreignElementsInMetadata", foreignElementsInMetadata);

        printMap("foreignElementsElsewhere", foreignElementsElsewhere);

        printMap("prefixedAttributes", prefixedAttributes);

        printMap("fontAttributes", fontAttributes);

        printMap("unconventionalXLinkPrefixes", unconventionalXLinkPrefixes);
        
        printMap("fontParent", fontParent);
        
        printMap("piTargets", piTargets);

        printMap("requiredExtensions", requiredExtensions);
        
        printMap("internalEntities", internalEntities);
        
        printMap("creator", creator);

        
        System.out.println("----------------");
    }
}
