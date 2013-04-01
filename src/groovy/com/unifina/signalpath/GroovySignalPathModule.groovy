package com.unifina.signalpath

abstract class GroovySignalPathModule extends AbstractSignalPathModule {

	@Override
	public void init() {
		autoInitIO(this)
	}
	
	/**
	 * Provides automatic IO initialization. All declared Parameter,
	 * Input and Output fields are snooped via reflection and reported to 
	 * the superclass.
	 */
	public static void autoInitIO(AbstractSignalPathModule mod) {
		List inputs = []
		List outputs = []
		
		def cls = mod.class
//		while (AbstractSignalPathModule.class.isAssignableFrom(cls)) {
			def meta = new ExpandoMetaClass(cls)
			meta.initialize()
			meta.properties.each { p ->
				if (p.getter != null) {
					
//					if (com.unifina.signalpath.Input.class.isAssignableFrom(p.getter.returnType) || 
//						com.unifina.signalpath.Output.class.isAssignableFrom(p.getter.returnType)) {
						
						def io = p.getter.invoke(mod, null)
	
						if (io instanceof Input)
						inputs.add([name:p.name, input:io])
						else if (io instanceof Output)
						outputs.add([name:p.name, output:io])
	
//					}
						
				}
			}
			
			def fields = cls.getFields()
			fields.each {
				if (Input.class.isAssignableFrom(it.type)) {
					inputs.add([name:it.name,input:it.get(mod)])
				}
				else if (Output.class.isAssignableFrom(it.type)) {
					outputs.add([name:it.name,output:it.get(mod)])
				}
			}
//			cls = cls.getSuperclass()
//		}
		
		inputs = inputs.sort(new Comparator() {
			public int compare(Object o1, Object o2) {
				return o1.name.compareTo(o2.name)
			}
		})
		outputs = outputs.sort(new Comparator() {
			public int compare(Object o1, Object o2) {
				return o1.name.compareTo(o2.name)
			}
		})
		
		inputs.each { mod.addInput(it.input) }
		outputs.each { mod.addOutput(it.output) }
	}

}
