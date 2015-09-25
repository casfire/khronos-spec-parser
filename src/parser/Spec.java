package parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import parser.registry.Registry;

public class Spec {
	
	public final String   name;
	public final File     file;
	public final Document doc;
	public final Registry reg;
	
	public Spec(String spec) throws ParserConfigurationException, SAXException, IOException {
		name = spec;
		file = new File(name + ".xml");
		if (file.exists()) {
			System.out.println(spec + ".xml already exists.");
		} else {
			System.out.println("Downloading " + spec + ".xml.");
			download(spec, file);
		}
		doc = load(file);
		reg = new Registry(doc.getDocumentElement());
	}
	
	private static Document load(File file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(file);
	}
	
	private static void download(String spec, File file) throws IOException {
		URL website = new URL("https://cvs.khronos.org/svn/repos/ogl/trunk/doc/registry/public/api/" + spec + ".xml");
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(file);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
	
}
