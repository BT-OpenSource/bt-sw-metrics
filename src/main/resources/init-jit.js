
var Log = {
    elem: false,
    write: function(text) {
        if (!this.elem) {
            this.elem = document.getElementById('log');
        }
        this.elem.innerHTML = text;
    }
};

var treeMap;

function init() {
    $jit.id('infovis').innerHTML = '';

    //init TreeMap
    treeMap = new $jit.TM.Squarified({
        contrained: true,
        levelsToShow: 1,
        //where to inject the visualization
        injectInto: 'infovis',
        //parent box title heights
        titleHeight: 20,
        //don't enable animations
        animate: false,
        //box offsets
        offset: 1,
        //Attach left and right click events
        Events: {
            enable: true,
            onClick: function(node) {
                if (node) treeMap.enter(node);
            },
            onRightClick: function() {
                treeMap.out();
            }
        },
        //Enable tips
        Tips: {
            enable: true,
            //add positioning offsets
            offsetX: 20,
            offsetY: 0,
            //implement the onShow method to
            //add content to the tooltip when a node
            //is hovered
            onShow: function(tip, node, isLeaf, domElement) {
                var html = "<div class=\"tip-title\">" + node.name + "</div>"
                    + "<div class=\"tip-text\">";
                var data = node.data;
                var fullPath = '';
                if (data.p) {
                    fullPath = PARAMS.pathPrefix;
                    if (data.p.indexOf("/") != 0) {
                        fullPath += "/";
                    }
                    fullPath += data.p;
                    Log.write("<a target='_blank' href='" + fullPath + "'>" + fullPath + "</a>");
                }
                if (data.c) {
                    html += PARAMS.colourMetric + ": " + data.c + "<br/>";
                }
                if (data.mc) {
                    html += PARAMS.colourMetric + " (max): " + data.mc + " <span style='background-color: " + data.ms + "'>____</span><br/>";
                }
                if (data.$area) {
                    html += PARAMS.sizeMetric + ": " + data.$area + "<br/>";
                }
                if (data.x) {
                    for (var i = 0; i < data.x.length; i++) {
                        html += PARAMS.extraColumns[i] + ": " + data.x[i] + "<br/>";
                    }
                }
                html += "</div>";
                tip.innerHTML =  html;
            }
        },
        //Add the name of the node in the correponding label
        //This method is called once, on label creation.
        onCreateLabel: function(domElement, node) {
            domElement.innerHTML = node.name;
            var style = domElement.style;
            style.display = '';
            style.border = '1px solid transparent';
            domElement.onmouseover = function() {
                style.border = '1px solid #9FD4FF';
            };
            domElement.onmouseout = function() {
                style.border = '1px solid transparent';
            };
        }
    });
    treeMap.loadJSON(PARAMS.treeMapJson);
    treeMap.refresh();
    //end

    //add event to the back button
    var back = $jit.id('back');
    $jit.util.addEvent(back, 'click', function() {
        treeMap.out();
    });

    //add event to the top button
    var top = $jit.id('top');
    $jit.util.addEvent(top, 'click', function() {
        var root = treeMap.graph.getNode(PARAMS.treeMapJson.id);
        treeMap.enter(root);
    });
}

window.onload = init;
