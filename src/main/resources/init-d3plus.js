var visualisation;

function sizeWeightedMean(arr, field) {
    if (arr.length == 1) {
        if (typeof arr[0] === "undefined") {
            return undefined;
        } else {
            return arr[0][field];
        }
    }

    var sizeMetric = getCurrentSizeMetric();
    var total = arr.reduce(function(acc, val) {
        if (typeof(val[field]) === "number") {
            acc.values += val[field] * val[sizeMetric];
        } else if (val[field] != "") {
            acc.mixed = true;
        }
        acc.sizes += val[sizeMetric];
        return acc;
    }, {values: 0, sizes: 0, mixed: false});
    if (!total.mixed && total.sizes > 0) {
        return total.values / total.sizes;
    } else {
        return undefined;
    }
}

function generateAggregation(type) {
    var aggregation = {};
    var currentSizeMetric = getCurrentSizeMetric();
    PARAMS.extraColumns
        .concat([PARAMS.sizeMetric, PARAMS.colourMetric])
        .filter(function(item) {
            return item != currentSizeMetric;
        })
        .forEach(function(item, index) {
            if (type == "size-weighted") {
                aggregation[item] = function(array) { return sizeWeightedMean(array, item) };
            } else {
                aggregation[item] = type;
            }
        });
    //console.log(aggregation);
    return aggregation;
}

function getCurrentColourMetric() {
    return document.getElementById("colour-select").value;
}

function changeColour() {
    var metric = getCurrentColourMetric();
    if (metric != "") {
        //console.log("Changing colour to " + metric);
        visualisation
            .color(metric)
            .draw();
        forceRedrawIfNecessary();
    }
}

function getTooltipMetrics() {
    // See https://stackoverflow.com/a/5867262
    var result = [];
    var select = document.getElementById("tooltip-select");
    var options = select && select.options;
    var opt;

    for (var i = 0; i < options.length; i++) {
        opt = options[i];

        if (opt.selected) {
            result.push(opt.value || opt.text);
        }
    }
    return result;
}

function changeTooltip() {
    var metrics = getTooltipMetrics();
    if (metrics != []) {
        //console.log("Changing colour to " + metric);
        visualisation
            .tooltip([PARAMS.colourMetric, PARAMS.sizeMetric].concat(metrics))
            .draw();
        forceRedrawIfNecessary();
    }
}

function forceRedrawIfNecessary() {
    // Bug in IE means that sometime the redraw does not show the text
    // correctly (at all) so we schedule a re-draw.
    if (!hasVisualisationTextDisplayed()) {
        setTimeout(function() { visualisation.draw(); }, 500);
    }
}


function hasVisualisationTextDisplayed() {
    return tspanInClassContainsText("d3plus_label") || tspanInClassContainsText("d3plus_share");
}

// This looks for the content of <tspan> elements within elements
// of the named class and determines if any contain some content.

function tspanInClassContainsText(className) {
    var elements = document.getElementsByClassName(className);
    var i;
    for (i = 0; i < elements.length; i++) {
        var tspans = elements.item(i).getElementsByTagName("tspan");
        if (tspans.length > 0 && typeof tspans[0].innerHTML != "undefined" && tspans[0] != "") {
            return true;
        }
    }
    return false;
}

function changeSize() {
    var metric = getCurrentSizeMetric();
    if (metric != "") {
        changeAggregation();
    }
}

function getCurrentSizeMetric() {
    return document.getElementById("size-select").value;
}

function changeAggregation() {
    var select = document.getElementById("aggregation-select");
    //console.log("Changing aggregation to " + select.value);
    createVisualisation(getCurrentSizeMetric(), getCurrentColourMetric(), select.value);
}

function formatTooltipNumeric(value, obj) {
    if (obj.key == "share") {
        return value.toFixed(1) + '%';
    // This works out if it looks like an integer
    } else if (Math.abs(value) - ((Math.abs(value) / 1) | 0) < 0.0001) {
        return value;
    } else {
        return value.toFixed(3);
    }
}

function createVisualisation(sizeMetric, colourMetric, aggregationType) {
    document.getElementById("infovis").innerHTML = "";
    var aggregation = generateAggregation(aggregationType);
    visualisation = d3plus.viz()
        //.dev(true)    // Enable debugging, if necessary
        .container("#infovis")
        .data(PARAMS.treeMapJson)
        .aggs(aggregation)
        .size({value: sizeMetric})
        .type("tree_map")
        .id(PARAMS.pathComponentIdList)
        .depth(0)
        .color(colourMetric)
        //.color(function(data) { return data.colour; })
        .format({
            number: formatTooltipNumeric,
            text: function(value, key) {
                if (value.lastIndexOf("@", 0) === 0) {
                    return PARAMS.idToNameMap[value];
                } else {
                    return value;
                }
            }
        })
        .tooltip([PARAMS.colourMetric, PARAMS.sizeMetric])
        .legend(true)
        .labels({"align": "left", "valign": "top"})
        .draw();
}

function init() {
    createVisualisation(PARAMS.sizeMetric, PARAMS.colourMetric, "size-weighted");
}

window.onload = init;