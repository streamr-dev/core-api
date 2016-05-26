SignalPath.FilterModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.GenericModule(data,canvas,prot)

    /**
     * Fill in an output slot to match `pass` input so that variadic pairs' growing doesn't look odd.
     */
    var superAddInput = prot.addInput;
    function addInput(data, clazz) {
        var endpoint = superAddInput(data, clazz);
        if (endpoint.getName() === 'pass') {
            var output = prot.addOutput({
                name: "notused"
            })
            output.div.css("display", "none")
        }
        return endpoint
    }
    prot.addInput = addInput;

    return pub;
}
