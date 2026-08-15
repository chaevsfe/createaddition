package com.mrh0.createaddition.client;

import com.mrh0.createaddition.CreateAddition;
import com.zurrtum.create.client.catnip.lang.LangBuilder;

public class CALang {

	public static LangBuilder builder() {
		return new LangBuilder(CreateAddition.MODID);
	}

	public static LangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}
}
