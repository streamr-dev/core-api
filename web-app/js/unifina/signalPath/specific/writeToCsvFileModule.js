SignalPath.WriteToCsvFileModule = function(data, canvas, prot) {
    prot = prot || {};
    var pub = SignalPath.UIChannelModule(data,canvas,prot)

    var downloadLinkContainer
    var rowCountContainer
    var rowCount

    var super_createDiv = prot.createDiv;
    prot.createDiv = function() {
        super_createDiv();
        downloadLinkContainer = $("<div class='moduleDownloadLink' style='margin-top: 5px;'></div>");
        rowCountContainer = $("<div class='csvRowCount' style='margin-top: 5px; display: none; text-align: center; font-family: monospace;'>Total rows </div>")
        rowCount = $("<span>")
        rowCount.appendTo(rowCountContainer)
        prot.body.append(rowCountContainer);
        prot.body.append(downloadLinkContainer);
    }

    prot.receiveResponse = function(payload) {
        if (payload.type === "csvFileReady") {
            // Add download link to module
            var downloadUrl = Streamr.createLink("canvas", "downloadCsv") + "?filename=" + payload.file
            var $link = $("<a>", { href: downloadUrl })
            $link.html("<i class='fa fa-download'></i>&nbsp;&nbsp;" + payload.file + " (" + payload.kilobytes + " kB)")
            downloadLinkContainer.html($link)
            downloadLinkContainer.effect("highlight", {}, 2000)

            // Remove download link container when clicked
            $link.click(function(event) {
                downloadLinkContainer.html("")
                rowCount.text("")
                rowCountContainer.hide()
            })
        } else if (payload.type === "csvRowCount") {
            rowCount.text(payload.value)
            rowCountContainer.show()
        }
    }

    return pub
}