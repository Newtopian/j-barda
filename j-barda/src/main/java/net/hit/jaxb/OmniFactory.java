package net.hit.jaxb;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;

/**
 * @author edaigneault
 *
 */
public class OmniFactory {
	Logger														log											= LoggerFactory.getLogger(getClass());
	private String										packagePrefix;
	private Map<Class<?>, Class<?>>		ifToTypeMap;
	private Map<String, Class<?>>			kNameToClassMap;
	private Map<Class<?>, XmlSchema>	classToSchemaMap;
	private JAXBContext								ctx;

	private Map<String, Object>				packageObjectFactoryMap	= new HashMap<>();
	private Map<Class<?>, Method>			instanceMethodMap				= new HashMap<>();

	private List<Class<?>>						cloneableList						= new ArrayList<>();

	/**
	 * @param packagePrefix
	 *          prefix for the packages to scan
	 *
	 */
	public OmniFactory(String packagePrefix) {
		this.packagePrefix = packagePrefix;
		this.ifToTypeMap = new HashMap<>();
		this.kNameToClassMap = new HashMap<String, Class<?>>();
		this.classToSchemaMap = new HashMap<>();
	}

	/**
	 * @throws IOException
	 *           could not scan claspath
	 * @throws JAXBException
	 *           could not craete the JAXBContext
	 */
	public void scanClassPath() throws IOException, JAXBException
	{
		this.ifToTypeMap.clear();
		this.kNameToClassMap.clear();
		this.classToSchemaMap.clear();
		packageObjectFactoryMap.clear();
		instanceMethodMap.clear();
		cloneableList.clear();

		ClassPath classPath = ClassPath.from(this.getClass().getClassLoader());
		List<Class<?>> xmlTypeList = classPath.getTopLevelClassesRecursive(packagePrefix)
																					.stream()
																					.filter(c ->
																						{
																							boolean loadable = false;
																							try {
																								loadable = c.load() != null;
																							}
																							catch (Throwable t)
																							{
																								loadable = false;
																							}
																							// log.info("LOADABLE {} for {}", loadable, c);
																							return loadable;
																						})
																					.map(c -> c.load())
																					.filter(c ->
																						{
																							boolean hasPackage = c.getPackage() != null;
																							// log.info("HAS_PACKAGE {} for {}", hasPackage, c);
																							return hasPackage;

																						})
																					.filter(c ->
																						{
																							boolean isXMLType = c.getAnnotation(XmlType.class) != null;
																							log.info("IS_GOOD {} for {}", isXMLType, c);
																							return isXMLType;

																						})
																					.collect(Collectors.toList());

		Map<Class<?>, Set<Class<?>>> typeToIfMap = new HashMap<>();
		Map<Class<?>, Set<Class<?>>> ifToTypeMap = new HashMap<>();
		Set<String> packageNames = new HashSet<String>();
		for (Class<?> info : xmlTypeList) {
			packageNames.add(info.getPackage().getName());

			XmlSchema xmlSchema = info.getPackage().getAnnotation(XmlSchema.class);
			this.classToSchemaMap.put(info, xmlSchema);
			Class<?> cInfo = info;
			Set<Class<?>> interfaceSet = typeToIfMap.get(cInfo);
			if (interfaceSet == null)
			{
				interfaceSet = new HashSet<Class<?>>();
				typeToIfMap.put(cInfo, interfaceSet);
			}
			List<Class<?>> ifList = Arrays.asList(cInfo.getInterfaces());
			for (Class<?> k : ifList) {
				interfaceSet.add(k);
				Set<Class<?>> classSet = ifToTypeMap.get(cInfo);
				if (classSet == null)
				{
					classSet = new HashSet<Class<?>>();
					ifToTypeMap.put(k, classSet);
				}
				classSet.add(cInfo);
			}
		}

		for (Map.Entry<Class<?>, Set<Class<?>>> entry : ifToTypeMap.entrySet()) {
			if (entry.getKey().getPackage().getName().startsWith(packagePrefix))
			{
				if (entry.getValue().size() == 1)
				{
					this.ifToTypeMap.put(entry.getKey(), entry.getValue().iterator().next());
				}
				if (entry.getKey().equals(Cloneable.class))
				{
					this.cloneableList.addAll(entry.getValue());
				}
			}
		}

		for (Map.Entry<Class<?>, Set<Class<?>>> entry : typeToIfMap.entrySet()) {
			this.kNameToClassMap.put(entry.getKey().getName(), entry.getKey());
		}

		// Same problem but from another angle.. go through the ObjectFactory
		List<Class<?>> objectFactoryList = classPath.getTopLevelClassesRecursive(packagePrefix)
																								.stream()
																								.filter(c -> c.getSimpleName().equals("ObjectFactory"))
																								.map(c -> c.load())
																								.collect(Collectors.toList());

		packageObjectFactoryMap = new HashMap<>();
		instanceMethodMap = new HashMap<>();
		for (Class<?> factk : objectFactoryList) {
			try {
				Object fact = factk.newInstance();
				packageObjectFactoryMap.put(factk.getPackage().getName(), fact);
				for (Method met : Arrays.asList(factk.getMethods())) {
					if (met.getParameterTypes().length == 0)
					{
						instanceMethodMap.put(met.getReturnType(), met);
					}
				}
			}

			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		typeToIfMap.forEach((k, iSet) -> log.info("{} implements {}", k, iSet.stream().map(i -> i.getSimpleName()).collect(Collectors.joining(" "))));
		ifToTypeMap.forEach((k, iSet) -> log.info("{} is Implemented by [{}]", k, iSet.stream().map(i -> i.getName()).collect(Collectors.joining(", "))));
		log.info("all Packages [{}]", String.join(", ", packageNames));
		// classToSchemaMap.forEach((k, schema) -> log.info("{} ==> {}", k, schema.namespace()));
		log.info("All namespaces = \n\t{}", classToSchemaMap.entrySet().stream().map(entry -> entry.getValue().namespace()).distinct().collect(Collectors.joining("\n\t ")));
		this.ctx = JAXBContext.newInstance(String.join(":", packageNames));

	}

	/**
	 * @param theClassOrInterface
	 *          the class or interface to create an instance from. Function will first try and resolve the interface against a real class, if
	 *          unable it will use the class directly.
	 * @return the writable instance for this interface
	 */
	@SuppressWarnings("unchecked")
	public <T extends I, I> T create(Class<I> theClassOrInterface) {
		if (theClassOrInterface == null) { throw new NullPointerException("Cannot create instances of a null class"); }

		Class<T> ifClass = (Class<T>) this.ifToTypeMap.get(theClassOrInterface);
		if (ifClass == null)
		{
			ifClass = (Class<T>) theClassOrInterface;
		}
		Method met = this.instanceMethodMap.get(ifClass);
		Object instance = this.packageObjectFactoryMap.get(ifClass.getPackage().getName());

		try {
			return (T) met.invoke(instance);
		}
		catch (Exception e) {
			log.error("Could not create instance as commanded", e);
		}
		return null;
	}

	/**
	 * @param instance
	 *          the instance to clone
	 * @return a writable clone of the instance
	 */
	@SuppressWarnings("unchecked")
	public <T extends I, I> T cloneAsWritable(I instance)
	{
		if (instance == null) return null;

		log.info("the Instance class {}", instance.getClass());

		try {
			return (T) instance.getClass().getMethod("clone").invoke(instance);
		}
		catch (Exception e) {
			log.error("couldnot clone this instance", e);
			throw new RuntimeException("Could not clone instance " + instance.toString());
		}
	}

	/**
	 * @param interfaceClass
	 *          the interface that is implemented by the desired JAXBType
	 * @return the Class instance representing this JAXB type
	 */
	@SuppressWarnings("unchecked")
	public <T extends I, I> Class<T> getJaxbClassFor(Class<I> interfaceClass)
	{
		if (this.classToSchemaMap.containsKey(interfaceClass)) { return (Class<T>) interfaceClass; }
		return (Class<T>) this.ifToTypeMap.get(interfaceClass);
	}

	/**
	 * @return the JAXB Context initialized to all the packages found that contained ObjectFactory
	 */
	public JAXBContext getContext()
	{
		return this.ctx;
	}

}
