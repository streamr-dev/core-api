SignalPath.ExportCSVModule = function(data, canvas, prot) {
    prot = prot || {};
    var pub = SignalPath.UIChannelModule(data,canvas,prot)

    var downloadLinkContainer
    var statusContainer
    var rowStatus
    var sizeStatus

    var super_createDiv = prot.createDiv;
    prot.createDiv = function() {
        super_createDiv();
        downloadLinkContainer = $("<div class='moduleDownloadLink' style='margin-top: 5px;'></div>");
        statusContainer = $("<div>", {
            "class": "csvRowCount",
            "style": "margin-top: 5px; display: none; text-align: center; font-family: monospace;"
        })
        rowStatus = $("<span>")
        sizeStatus = $("<span>")
        statusContainer.append(
            document.createTextNode("Rows: "),
            rowStatus,
            document.createTextNode(", Size: "),
            sizeStatus,
            document.createTextNode(" kB")
        )
        prot.body.append(statusContainer);
        prot.body.append(downloadLinkContainer);
    }

    prot.receiveResponse = function(payload) {
        if (payload.type === "csvUpdate") {

            // Update status details
            rowStatus.text(payload.rows)
            sizeStatus.text(payload.kilobytes)
            statusContainer.show()

            // Add download link to module if offered
            if (payload.file) {
                var downloadUrl = Streamr.createLink("canvas", "downloadCsv") + "?filename=" + payload.file
                var $link = $("<a>", {href: downloadUrl})
                $link.html("<i class='fa fa-download'></i>&nbsp;&nbsp;" + payload.file)
                downloadLinkContainer.html($link)
                downloadLinkContainer.effect("highlight", {}, 2000)

                // Remove download link container when clicked
                $link.click(pub.clean.bind(pub))
            }
        }
    }

    var superClean = pub.clean;
    pub.clean = function() {
        superClean()
        // If prot.createDiv has been called...
        if (downloadLinkContainer) {
            downloadLinkContainer.html("")
            rowStatus.text("")
            sizeStatus.text("")
            statusContainer.hide()
        }
    }

    return pub
}