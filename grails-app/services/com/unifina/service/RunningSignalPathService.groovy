package com.unifina.service

import com.unifina.domain.signalpath.RunningSignalPath

class RunningSignalPathService {

    def save(RunningSignalPath rsp) {
		rsp.save()
    }
}
