package com.unifina.signalpath.list;

import com.unifina.signalpath.*;

import java.util.ArrayList;
import java.util.List;

public class ConstantList extends AbstractConstantModule<List> {

	@Override
	protected Parameter<List> createConstantParameter() {
		return new ListParameter(this, "list", new ArrayList());
	}

	@Override
	protected Output<List> createOutput() {
		return new ListOutput(this,"out");
	}
}
