
/**
 * ChartInput is an Input with the following modifications:
 * - Input name is not shown
 * - Rename option is not shown in context menu
 * - Y-axis indicator button is shown for connected inputs
 * - Y-axis assignment can be cycled by clicking on the Y-axis indicator button
 *
 * Events:
 * - yAxisChanged(inputName, yAxis)
 */
SignalPath.ChartInput = function(json, parentDiv, module, type, pub) {
    pub = pub || {};
    pub = SignalPath.Input(json, parentDiv, module, type, pub);
    
    var btnDefaultClass = "btn-default"
    var btnPopoverClass = "btn-warning"
    var popoverClass = "popover-warning"
    
    var tooltipAxisId = generateId()
    
    // Use 1-based index for display to the user
    var displayedAxis = json.yAxis + 1
    // Cycle Y-axis button
    var $yAxisSelectorButton = $("<div class='y-axis-number btn "+btnDefaultClass+" btn-xs "+popoverClass+" popover-colorful'></div>")
    
    pub.seriesIndex = null
    pub.disableContextMenu = true
    
    var super_createDiv = pub.createDiv
    pub.createDiv = function() {
        var div = super_createDiv()
        div.bind("spConnect", function(event, output) {
            div.find(".y-axis-number").show()
            jsPlumb.repaint($(module.div).find("div.input"))
        })
        div.bind("spDisconnect", function(event, output) {
            div.find(".y-axis-number").hide()
            jsPlumb.repaint($(module.div).find("div.input"));
        })
        
        $yAxisSelectorButton.click(cycleYAxis)
        pub.div.tooltip({
            container: SignalPath.getParentElement(),
            selector: ".y-axis-number",
            html: true,
            title: function() {
                return "This input is drawn on y-axis <strong><span id='"+tooltipAxisId+"'>"+displayedAxis+"</span></strong>."
            }
        })
        updateButton()
        
        return div
    }
    
    function generateId() {
        var result = "tooltip_content_"+new Date().getTime()
        while ($("#"+result).size()>0)
            result = "tooltip_content_"+new Date().getTime()
        return result
    }
    
    function getNextYAxis(current) {
        // Find unique yaxis numbers and count how many inputs we have at each
        var inputs = module.getInputs()
        var connectedInputs = inputs.filter(function(input) {
            return input.isConnected()
        })
        var yAxisCounts = {}
        var yAxisUniqueIds = []
        connectedInputs.forEach(function(input) {
            if (yAxisCounts[input.json.yAxis.toString()]===undefined) {
                yAxisCounts[input.json.yAxis.toString()] = 1
                yAxisUniqueIds.push(input.json.yAxis)
            }
            else yAxisCounts[input.json.yAxis.toString()] = yAxisCounts[input.json.yAxis.toString()] + 1
        })
        
        // Sort yaxis ids numerically in ascending order
        yAxisUniqueIds.sort(function(a, b){return a-b})
        
        // Find the smallest free axis index
        var smallestFreeAxis = 0
        while (yAxisUniqueIds.indexOf(smallestFreeAxis)>=0)
            smallestFreeAxis++
        
        // If this input is the only one with in the current number, we're at the "free" position
        var atFree = (yAxisCounts[json.yAxis.toString()] === 1)
        
        // If we are at the "free" number, get the next valid number
        if (atFree) {
            return yAxisUniqueIds[(yAxisUniqueIds.indexOf(json.yAxis) + 1) % yAxisUniqueIds.length]
        }
        // Else if the next number if the smallest free number, return that
        else if (json.yAxis+1 === smallestFreeAxis) {
            return json.yAxis+1
        }
        // Else if we're at the end of the array, wrap to the lesser of (smallestFreeAxis, yAxisUniqueIds[0])
        else if (yAxisUniqueIds.indexOf(json.yAxis) === yAxisUniqueIds.length-1) {
            return Math.min(smallestFreeAxis, yAxisUniqueIds[0])
        }
        // Else just return the next number, regardless of free or not
        else {
            return json.yAxis + 1
        }
    }
    
    function updateButton() {
        displayedAxis = json.yAxis + 1
        $yAxisSelectorButton.html(displayedAxis)
        $("#"+tooltipAxisId).html(displayedAxis)
    }
    
    function cycleYAxis() {
        var oldYAxis = json.yAxis
        json.yAxis = getNextYAxis()
        updateButton()
        if (oldYAxis !== json.yAxis) {
            $(pub.div).trigger('yAxisChanged', [pub.getName(), json.yAxis])
        }
    }
    
    pub.getDisplayName = function(connected) {
        return $yAxisSelectorButton
    }
    
    var super_getContextMenu = pub.getContextMenu
    pub.getContextMenu = function(div) {
        var menu = []
        
        // Add y-axis cycle option (does the same thing as left-clicking the button)
        menu.push({title: "Cycle Y-axis", cmd: "yaxis"});
        
        // Chart inputs need not be renamed
        $(super_getContextMenu(div)).each(function(i,o) {
            if (o.title!=="Rename")
                menu.push(o)
        })
        
        return menu;
    }
    
    var super_handleContextMenuSelection = pub.handleContextMenuSelection
    pub.handleContextMenuSelection = function(target, selection) {
        if (selection=="yaxis")
            cycleYAxis()
        else super_handleContextMenuSelection(target, selection);
    }
    
    pub.showYAxisWarning = function(seriesName, popover) {
        
        if (popover) {
            $yAxisSelectorButton.popover({
                content: "Some series may not be showing properly. Use these buttons to cycle Y-axis assignments.",
                placement: "right",
                trigger: "manual"
            })
            $yAxisSelectorButton.popover('show')
        }
        
        $yAxisSelectorButton.removeClass(btnDefaultClass).addClass(btnPopoverClass)
        
        var destroyFunc = (function(b) {
            return function() {
                b.popover('destroy')
                b.removeClass(btnPopoverClass).addClass(btnDefaultClass)
            }
        })($yAxisSelectorButton)
        
        $yAxisSelectorButton.siblings(".popover").click(destroyFunc)
        setTimeout(destroyFunc, 8000)
    }
    
    return pub;
}