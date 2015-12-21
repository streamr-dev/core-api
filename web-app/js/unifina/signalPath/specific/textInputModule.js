SignalPath.TextInputModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.GenericModule(data, canvas, prot)

    var area = null;
    var headers = [];

    var superCreateDiv = prot.createDiv;
    function createDiv() {
        superCreateDiv();
        var textField = new StreamrTextInput(prot.body)
    }
    prot.createDiv = createDiv;

    return pub
}
