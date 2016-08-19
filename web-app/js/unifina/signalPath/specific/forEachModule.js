SignalPath.ForEachModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.GenericModule(data,canvas,prot)

    var canvasSelector = $(
        '<div class="subcanvas-selector">'+
            '<label for="subcanvas-selector">Subcanvases</label>'+
            '<div class="form-inline">'+
                '<select class="form-control input-sm"/>'+
                '<button class="btn btn-default btn-sm" type="button"><i class="fa fa-search"></i></button>'+
            '</div>'+
        '</div>'
    )

    function refreshSubcanvases() {
        if (SignalPath.isRunning()) {
            $.getJSON(Streamr.createLink({uri: 'api/v1/canvases/' + SignalPath.getId() + '/modules/' + prot.getHash()}), {runtime: true}, function(runtimeJson) {
                canvasSelector.remove()
                canvasSelector.find('select').empty()

                var keys = Object.keys(runtimeJson.canvasesByKey || {})
                if (keys.length) {
                    var select = canvasSelector.find('select')
                    keys.forEach(function(key) {
                        var option = $("<option/>", {
                            text: key,
                            value: JSON.stringify(runtimeJson.canvasesByKey[key])
                        })
                        select.append(option)
                    })

                    prot.body.append(canvasSelector)
                    canvasSelector.find('button').click(function() {
                        var json = JSON.parse(canvasSelector.find('select').val())
                        json.id = SignalPath.getId()
                        json.state = 'running'
                        SignalPath.load(json)
                        /*SignalPath.loadJSON(json)*/

/*                        $.ajax({
                            type: 'POST',
                            url: Streamr.createLink('canvas', 'editor'),
                            data: canvasSelector.find('select').val(),
                            contentType: 'application/json',
                            success: function (data) {
                                var w = window.open('about:blank', 'subcanvas');
                                if (!w || !w.document) {
                                    Streamr.showError("Please allow the popup to open the sub-canvas in another window.")
                                }
                                else {
                                    w.document.write(data);
                                    w.document.close();
                                }
                            }
                        })*/

                    })
                }
            })
        }
    }

    var super_createDiv = prot.createDiv
    prot.createDiv = function() {
        super_createDiv()
        setTimeout(refreshSubcanvases)
    }

    return pub;
}
