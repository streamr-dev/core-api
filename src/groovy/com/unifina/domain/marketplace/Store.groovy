package com.unifina.domain.marketplace

class Store<T> {
	void save(T o, Map<String, Boolean> params) {
		o.save(params)
	}
	void saveAndValidateAndFailOnError(T o) {
		o.save([validate: true, failOnError: true])
	}
	void saveAndValidate(T o) {
		o.save([validate: true, failOnError: false])
	}
	void saveAndFailOnError(T o) {
		o.save([validate: false, failOnError: true])
	}
}
