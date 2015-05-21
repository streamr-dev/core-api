package com.unifina.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Used to compile and load user-defined Groovy scripts at runtime.
 * All code is assigned to the codeBase "/java/untrusted" so that
 * proper Permissions can be granted in a Policy.
 * @author Henri
 *
 */
public class UserJavaClassLoader extends URLClassLoader {
	
	protected ClassLoader delegate;
	
	Map<String,Class> cache = new HashMap<>();
	
	DiagnosticCollector<JavaFileObject> diagnostics = null;
	
	private CodeSource cs; 
	
	private static final Logger log = Logger.getLogger(UserJavaClassLoader.class);
	
	public UserJavaClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
		this.delegate = parent;
		try {
			cs = new CodeSource(new URL("file:/java/untrusted"), new Certificate[0]);
		} catch (MalformedURLException e) {}
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		PackageAccessHelper.checkAccess(name,cs.getLocation().getPath());
		if (cache.containsKey(name))
			return cache.get(name);
		else return delegate.loadClass(name);
	}

	public boolean parseClass(String name, String source) {
        // We get an instance of JavaCompiler. Then
        // we create a file manager
        // (our custom implementation of it)
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ClassFileManager fileManager = new
            ClassFileManager(compiler
                .getStandardFileManager(null, null, null));
		
        // Dynamic compiling requires specifying
        // a list of "files" to compile. In our case
        // this is a list containing one "file" which is in our case
        // our own implementation (see details below)
        List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
        jfiles.add(new CharSequenceJavaFileObject(name, source));
	    
        // Create a classpath using current classloader hierarchy
		 ClassLoader cl = getClass().getClassLoader();
		 Set<String> urls = new HashSet<>();
		 
		 while (cl!=null && cl instanceof URLClassLoader) {
			 URL[] clUrls = ((URLClassLoader)cl).getURLs();
			 for (URL url : clUrls) {
				 try {
					 // decode special characters such as # from url
					 urls.add(URLDecoder.decode(url.getPath(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					log.error(e);
				}
			 }
			 cl = cl.getParent();
		 }
	    
		 String cp = StringUtils.join(urls.toArray(),File.pathSeparator);
		 
		 List<String> optionList = new ArrayList<String>();
		 
		 // set compiler's classpath to be same as the runtime's
		 optionList.addAll(Arrays.asList("-classpath",cp));
		 
		 diagnostics = new DiagnosticCollector<JavaFileObject>();
		 
		 JavaCompiler.CompilationTask task = compiler.getTask(null,fileManager,diagnostics,optionList,null,jfiles);
	    
	    boolean success = task.call();

	    if (success) {
	    	for (JavaClassObject ob : fileManager.getClassObjects()) {
		    	byte[] bytes = ob.getBytes();
		        Class<?> clazz = super.defineClass(ob.getName(), bytes, 0, bytes.length, cs);
				cache.put(ob.getName(),clazz);
	    	}
	    }
	    
	    return success;
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
		if (diagnostics==null)
			return null;
		else return diagnostics.getDiagnostics();
	}
	
	public class CharSequenceJavaFileObject extends SimpleJavaFileObject {

	    /**
	    * CharSequence representing the source code to be compiled
	    */
	    private CharSequence content;

	    /**
	    * This constructor will store the source code in the
	    * internal "content" variable and register it as a
	    * source code, using a URI containing the class full name
	    *
	    * @param className
	    *            name of the public class in the source code
	    * @param content
	    *            source code to compile
	    */
	    public CharSequenceJavaFileObject(String className,
	        CharSequence content) {
	        super(URI.create("string:///" + className.replace('.', '/')
	            + Kind.SOURCE.extension), Kind.SOURCE);
	        this.content = content;
	    }

	    /**
	    * Answers the CharSequence to be compiled. It will give
	    * the source code stored in variable "content"
	    */
	    @Override
	    public CharSequence getCharContent(
	        boolean ignoreEncodingErrors) {
	        return content;
	    }
	}
	
	public class JavaClassObject extends SimpleJavaFileObject {

		private String name;
		
	    /**
	    * Byte code created by the compiler will be stored in this
	    * ByteArrayOutputStream so that we can later get the
	    * byte array out of it
	    * and put it in the memory as an instance of our class.
	    */
	    protected final ByteArrayOutputStream bos =
	        new ByteArrayOutputStream();

	    /**
	    * Registers the compiled class object under URI
	    * containing the class full name
	    *
	    * @param name
	    *            Full name of the compiled class
	    * @param kind
	    *            Kind of the data. It will be CLASS in our case
	    */
	    public JavaClassObject(String name, Kind kind) {
	        super(URI.create("string:///" + name.replace('.', '/')
	            + kind.extension), kind);
	        this.name = name;
	    }

	    /**
	    * Will be used by our file manager to get the byte code that
	    * can be put into memory to instantiate our class
	    *
	    * @return compiled byte code
	    */
	    public byte[] getBytes() {
	        return bos.toByteArray();
	    }

	    public String getName() {
	    	return name;
	    }
	    
	    /**
	    * Will provide the compiler with an output stream that leads
	    * to our byte array. This way the compiler will write everything
	    * into the byte array that we will instantiate later
	    */
	    @Override
	    public OutputStream openOutputStream() throws IOException {
	        return bos;
	    }
	}
	
	public class ClassFileManager extends ForwardingJavaFileManager {
		/**
		* JavaClassObject that will store the
		* compiled bytecode of our class. They are stored in 
		*/
		private HashMap<String, JavaClassObject> classObjectByName = new HashMap<>();
		
		/**
		* Will initialize the manager with the specified
		* standard java file manager
		*
		* @param standardManger
		*/
		public ClassFileManager(StandardJavaFileManager
		    standardManager) {
		    super(standardManager);
		}
		
		public Collection<JavaClassObject> getClassObjects() {
			return classObjectByName.values();
		}
		
		/**
		* Gives the compiler an instance of the JavaClassObject
		* so that the compiler can write the byte code into it.
		*/
		@Override
		public JavaFileObject getJavaFileForOutput(Location location,
		    String className, Kind kind, FileObject sibling)
		        throws IOException {
			JavaClassObject jclassObject = new JavaClassObject(className, kind);
			classObjectByName.put(className, jclassObject);
			return jclassObject;
		}
	}
	
}
