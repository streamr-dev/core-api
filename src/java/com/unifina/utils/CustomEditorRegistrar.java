package com.unifina.utils;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;


public class CustomEditorRegistrar implements PropertyEditorRegistrar {

	@Override
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		DecimalFormat numberFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
		numberFormat.setGroupingUsed(false);
        registry.registerCustomEditor(double.class, new CustomNumberEditor(Double.class, numberFormat, false));
        registry.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, numberFormat, true));
        
        String dateFormat = "yyyy-MM-dd";
        registry.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(dateFormat), true));
	}

}
