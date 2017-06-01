package com.unifina.signalpath.blockchain;

import com.unifina.api.NotPermittedException;
import com.unifina.domain.security.IntegrationKey;
import com.unifina.domain.security.SecUser;
import com.unifina.service.EthereumIntegrationKeyService;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Parameter;
import com.unifina.signalpath.PossibleValue;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.List;

class EthereumAccountParameter extends Parameter<IntegrationKey> {
	EthereumAccountParameter(AbstractSignalPathModule owner, String name) {
		super(owner, name, null, "String");
		setCanConnect(false);
	}

	@Override
	public IntegrationKey parseValue(String id) {
		return (IntegrationKey) InvokerHelper.invokeMethod(IntegrationKey.class, "get", id);
	}

	String getAddress() {
		if (hasValue()) {
			checkPermission();
			return (String) getValue().toMap().get("address");
		}
		return null;
	}

	String getPrivateKey() {
		if (hasValue()) {
			checkPermission();
			return (String) getValue().toMap().get("privateKey");
		}
		return null;
	}

	private void checkPermission() {
		SecUser loggedInUser = getOwner().getGlobals().getUser();
		SecUser integrationKeyUser = getValue().getUser();
		if (!integrationKeyUser.equals(loggedInUser)) {
			throw new NotPermittedException("Not permitted to use integration key " + getValue().getId());
		}
	}

	@Override
	public Object formatValue(IntegrationKey value) {
		return value == null ? null : value.getId();
	}

	@Override
	protected List<PossibleValue> getPossibleValues() {
		EthereumIntegrationKeyService service = getOwner().getGlobals().getBean(EthereumIntegrationKeyService.class);
		List<IntegrationKey> integrationKeys = service.getAllKeysForUser(getOwner().getGlobals().getUser());

		List<PossibleValue> possibleValues = new ArrayList<>();
		possibleValues.add(new PossibleValue("(none)", null));
		for (IntegrationKey key : integrationKeys) {
			possibleValues.add(new PossibleValue(key.getName(), key.getId()));
		}
		return possibleValues;
	}
}
