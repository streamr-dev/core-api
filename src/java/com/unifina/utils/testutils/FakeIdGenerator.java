package com.unifina.utils.testutils;

import com.unifina.utils.IdGenerator;

public class FakeIdGenerator extends IdGenerator {
	private int index = 0;

	@Override
	public String generate() {
		return "id-" + (index++);
	}
}
