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
            prot.addPlaceholderOutput()
        }
        return endpoint
    }
    prot.addInput = addInput;

    return pub;
}
