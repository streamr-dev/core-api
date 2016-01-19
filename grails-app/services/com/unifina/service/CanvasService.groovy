package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas

class CanvasService {
	public List<Canvas> findAllBy(SecUser currentUser,
								  String nameFilter,
								  Boolean adhocFilter,
								  Canvas.Type typeFilter,
								  Canvas.State stateFilter) {

		def query = Canvas.where { user == currentUser }

		if (nameFilter) {
			query = query.where {
				name == nameFilter
			}
		}
		if (adhocFilter != null) {
			query = query.where {
				adhoc == adhocFilter
			}
		}
		if (typeFilter) {
			query = query.where {
				type == typeFilter
			}
		}
		if (stateFilter) {
			query = query.where {
				state == stateFilter
			}
		}

		return query.findAll()
	}
}
