package com.unifina.signalpath.text;

import com.unifina.signalpath.*;
import org.stringtemplate.v4.*;
import org.stringtemplate.v4.compiler.STException;
import org.stringtemplate.v4.misc.ErrorManager;
import org.stringtemplate.v4.misc.STMessage;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *  Format incoming values into a string template
 *  For syntax, see https://github.com/antlr/stringtemplate4/blob/master/doc/cheatsheet.md
 */
public class StringTemplate extends AbstractSignalPathModule implements STErrorListener {
	private StringParameter template = new StringParameter(this, "template", "");
	private MapInput args = new MapInput(this, "args");
	private ListOutput errors = new ListOutput(this, "errors");
	private StringOutput result = new StringOutput(this, "result");

	private transient ST protoST;
	private transient List<String> errorList;

	@Override
	public void init() {
		addInput(template);
		addInput(args);
		addOutput(errors);
		addOutput(result);

		template.setTextArea(true);
	}

	@Override
	public void sendOutput() {
		errorList = new ArrayList<>();
		try {
			String t = template.getValue();
			if (protoST == null || !t.equals(protoST.impl.template)) {
				protoST = new ST(t);
			}

			// bugfix, see https://github.com/antlr/stringtemplate4/pull/145
			protoST.impl.formalArguments = null;

			// clone the prototype String Template and fill in the args
			ST st = new ST(protoST);
			for (Map.Entry<String, Object> kvp : args.getValue().entrySet()) {
				st.add(kvp.getKey(), kvp.getValue());
			}

			// copied from st.render(), only st.write has "this" as STErrorListener
			StringWriter out = new StringWriter();
			STWriter wr = new NoIndentWriter(out);
			wr.setLineWidth(-1);
			st.write(wr, this);

			result.send(out.toString());
		} catch (STException e) {
			// errorList has been accumulated by STErrorListener interface
		}
		errors.send(errorList);
		errorList = null;
	}

	@Override
	public void clearState() {
		protoST = null;
	}

	// STErrorListener
	public void compileTimeError(STMessage stMessage) { addToErrorList(stMessage); }
	public void runTimeError(STMessage stMessage) { addToErrorList(stMessage); }
	public void IOError(STMessage stMessage) { addToErrorList(stMessage); }
	public void internalError(STMessage stMessage) { addToErrorList(stMessage); }
	private void addToErrorList(STMessage msg) {
		if (errorList == null || msg == null) { return; }
		errorList.add(msg.toString());
	}
}
