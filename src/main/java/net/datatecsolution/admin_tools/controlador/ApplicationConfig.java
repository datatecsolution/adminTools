package net.datatecsolution.admin_tools.controlador;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@XmlRootElement
//@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationConfig {
	

    private static final JAXBContext CONTEXT;
    public static final ApplicationConfig INSTANCE;

    // configuration properties with defaults
    private int number = 0;
    private String text = "default";
    @XmlElementWrapper
    @XmlElement(name = "text")
    private List<String> texts = new ArrayList<>(Arrays.asList("default1", "default2"));

    ApplicationConfig() {
    }

    static {
        try {
            CONTEXT = JAXBContext.newInstance(ApplicationConfig.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException("JAXB context for " + ApplicationConfig.class + " unavailable.", ex);
        }
        File applicationConfigFile = new File(System.getProperty("admin_tools", new File(System.getProperty("user.dir"), "config.xml").toString()));
        if (applicationConfigFile.exists()) {
            INSTANCE = loadConfig(applicationConfigFile);
        } else {
            INSTANCE = new ApplicationConfig();
        }
    }

    public int getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }

    public List<String> getTexts() {
        return Collections.unmodifiableList(texts);
    }

    public static ApplicationConfig loadConfig(File file) {
        try {
            return (ApplicationConfig) CONTEXT.createUnmarshaller().unmarshal(file);
        } catch (JAXBException ex) {
            throw new IllegalArgumentException("Could not load configuration from " + file + ".", ex);
        }
    }

    // usage
    public static void main(String[] args) {
        System.out.println(ApplicationConfig.INSTANCE.getNumber());
        System.out.println(ApplicationConfig.INSTANCE.getText());
        System.out.println(ApplicationConfig.INSTANCE.getTexts());
    }

}
