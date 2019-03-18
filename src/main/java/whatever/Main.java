package whatever;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class Main {
	
	static final File descriptorsFile = new File("workdir/descriptors-3.0.xml");
	static final Path allZipFolder = Paths.get("workdir/from-s3");
	static final Path unpackFolder = Paths.get("workdir/unpack");
	static final Path repackFolder = Paths.get("workdir/repack");
	
	public static void main(String[] args) throws Exception {
		listnames();
//		repack();
	}

	private static void listnames() throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(descriptorsFile);

		doc.getDocumentElement().normalize();
		
		NodeList descriptors = doc.getElementsByTagName("descriptor");
		for (int i = 0; i < descriptors.getLength(); i++) {
			Node node = descriptors.item(i);
			if (node instanceof Element) {
				Element el = (Element) node;
				String kind = el.getAttribute("kind");
				if ("template".equals(kind)) {
					System.out.println(el.getAttribute("name"));
				}
			}
		}
	}

	private static void repack() throws Exception {
		
		//Warning don't use. Java has trouble unpacking the result.
		//See: http://www.lingala.net/zip4j/forum/index.php?topic=268.0
		//Instead use the 'repack.sh' script in the workdir.
		
		repackFolder.toFile().mkdirs();
		
		for (File unpackedProject : unpackFolder.toFile().listFiles()) {
			Path repackedProjectDir = repackFolder.resolve(unpackedProject.getName());
			FileUtils.deleteQuietly(repackedProjectDir.toFile());
			FileUtils.copyDirectory(unpackedProject, repackedProjectDir.toFile());
			
			Path templateDir = repackedProjectDir.resolve("template");
			{	//repack nested 'template' folder
				ZipFile zipFile = new ZipFile(templateDir.toString()+".zip");
				ZipParameters par = new ZipParameters();
				par.setIncludeRootFolder(true);
				par.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
				zipFile.addFolder(templateDir.toFile(), par);
				FileUtils.deleteQuietly(templateDir.toFile());
			}
			
			{	//repack project folder
				ZipFile zipFile = new ZipFile(repackedProjectDir.toFile()+".zip");
				ZipParameters par = new ZipParameters();
				par.setIncludeRootFolder(false);
				par.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
				zipFile.addFolder(repackedProjectDir.toFile(), par);
				FileUtils.deleteQuietly(repackedProjectDir.toFile());
			}			
		}
		
	}

	private static void unpack()
			throws ParserConfigurationException, SAXException, IOException, URISyntaxException, ZipException {
		unpackFolder.toFile().mkdirs();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(descriptorsFile);

		doc.getDocumentElement().normalize();
		
		NodeList descriptors = doc.getElementsByTagName("descriptor");
		for (int i = 0; i < descriptors.getLength(); i++) {
			Node node = descriptors.item(i);
			if (node instanceof Element) {
				Element el = (Element) node;
				System.out.println("--------------------------");
				String url = el.getAttribute("url");
				System.out.println(url);
				String kind = el.getAttribute("kind");
				System.out.println(kind);
				if ("template".equals(kind)) {
					System.out.println("--------------------------");
					Path path = Paths.get(new URI(url).getPath());
					Path zipName = path.getFileName();
					
					Path zipPath = allZipFolder.resolve(zipName);
					String unzipFolderName = zipName.toString(); 
					if (unzipFolderName.endsWith(".zip")) {
						unzipFolderName = unzipFolderName.substring(0, unzipFolderName.lastIndexOf('.'));
						Path unzipFolder = unpackFolder.resolve(unzipFolderName);
						unzipFolder.toFile().mkdirs();
						ZipFile zipFile = new ZipFile(zipPath.toFile());
						zipFile.extractAll(unzipFolder.toString());
						File templateZip = unzipFolder.resolve("template.zip").toFile();
						if (templateZip.isFile()) {
							new ZipFile(templateZip).extractAll(unzipFolder.toString());
						}
						templateZip.delete();
					}
				}
			}
		}
	}

}
