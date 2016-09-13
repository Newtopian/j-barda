package net.hit.storage;

import java.beans.Introspector;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Generic DocType for any JAXB backed object.
 * 
 * @author edaigneault
 *
 * @param <T>
 *          the type of the JAXB object
 */
public class JAXBDocType<T> extends DocType<T> {

	private Class<T>	classForTypeParam;

	/**
	 * Creates a JAXBDocType
	 * 
	 * @param docTypeName
	 *          Short description of the typeName
	 * @param classForTypeParameter
	 *          class for the typeT
	 */
	public JAXBDocType(String docTypeName, Class<T> classForTypeParameter) {
		super(docTypeName);
		this.classForTypeParam = classForTypeParameter;
		this.setFileWriter(obj ->
			{
				return marshallEvent(obj).getBytes(StandardCharsets.UTF_8);
			});
		this.setFileReader(objBytes ->
			{
				return unmarshallEvent(new String(objBytes, StandardCharsets.UTF_8));
			});
	}

	private String marshallEvent(T object)
	{
		try {
			@SuppressWarnings("unchecked")
			JAXBElement<? extends T> jaxbEvent = new JAXBElement<T>(new QName(Introspector.decapitalize(object.getClass().getSimpleName())), (Class<T>) object.getClass(), object);
			JAXBContext jaxb = JAXBContext.newInstance(object.getClass());
			Marshaller marshaller = jaxb.createMarshaller();
			marshaller.setProperty("jaxb.fragment", true);

			StringWriter writer = new StringWriter();
			marshaller.marshal(jaxbEvent, writer);
			return writer.toString();
		}
		catch (JAXBException e) {
			throw new DataBindingException(e);
		}
	}

	private T unmarshallEvent(String str)
	{
		try {
			// JAXBElement<T> jaxbEvent = new JAXBElement<T>(new QName(Introspector.decapitalize(this.classForTypeParam.getSimpleName())),
			// this.classForTypeParam, object);
			JAXBContext jaxb = JAXBContext.newInstance(this.classForTypeParam);
			Unmarshaller umar = jaxb.createUnmarshaller();
			umar.setProperty("jaxb.fragment", true);

			Source src = new StreamSource(new StringReader(str));
			JAXBElement<T> jaxbEvent = umar.unmarshal(src, this.classForTypeParam);
			return jaxbEvent.getValue();
		}
		catch (JAXBException e) {
			throw new DataBindingException(e);
		}
	}
}
