package whatever;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.lingala.zip4j.core.ZipFile;

public class Main {
	
	static final File descriptorsFile = new File("workdir/descriptors-3.0.xml");
	static final Path allZipFolder = Paths.get("workdir/from-s3");
	static final Path unpackFolder = Paths.get("workdir/unpack");
	
	public static void main(String[] args) throws Exception {
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
