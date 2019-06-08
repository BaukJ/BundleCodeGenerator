package uk.bauk.alexa.utils.languages.annotation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.JavaFileManager.Location;

import org.yaml.snakeyaml.Yaml;

import com.google.j2objc.annotations.ReflectionSupport;
import com.squareup.javapoet.ClassName;

import uk.bauk.alexa.utils.languages.annotation.generators.MethodGenerator;

public class ResourceParser {
	// TODO: add sibling to weight:, if:   for checking params against a value and only allowing that value if true
	// TODO: check for null fields (e.g. in list)
	// TODO: Allow private items, only visible to self to stop clouding the class
	// TODO: Allow sub-classes to help split up messages (e.g. launch.getMessage() / [launch.message]) could do variables/funcs with anonymous classes (needs to conform to interface)
	private final LanguageFactory annotation;
	public final String packageName;
	public final String prefix;
	public final String methodPrefix;
	public final Locale[] locales;
	public final TypeElement typeElement;
	public final Filer filer;
	
	public boolean processed = false;

	public ResourceParser(Elements elements, TypeElement typeElement, Filer filer) throws GeneratorException {
		this.typeElement = typeElement;
		this.filer = filer;
		annotation = typeElement.getAnnotation(LanguageFactory.class);
		this.methodPrefix = annotation.methodPrefix();
		if(annotation.packageName().isEmpty()) {
			packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
		}else {
			packageName = annotation.packageName();
		}
		prefix = annotation.prefix();
		List<Locale> localeList = new ArrayList<Locale>();
		if(annotation.locales().length == 0) {
			throw new GeneratorException("Need to pass in at least one locale");
		}
		for(String locale: annotation.locales()) {
			localeList.add(Locale.forLanguageTag(locale));
		}
		locales = localeList.toArray(new Locale[0]);
	}
	
	public String capitalisedPrefix() {
		return prefix.substring(0, 1).toUpperCase() + prefix.substring(1);
	}
	public String interfaceName() {
		return capitalisedPrefix();
	}
	public ClassName interfaceClassName() {
		return ClassName.get(packageName, interfaceName());
	}
	public String factoryName() {
		return capitalisedPrefix()+"Factory";
	}
	public ClassName factoryClassName() {
		return ClassName.get(packageName, factoryName());
	}
	public String baseName() {
		return capitalisedPrefix()+"Base";
	}
	public ClassName baseClassName() {
		return ClassName.get(packageName, baseName());
	}
	public String localeName(Locale locale) {
		return capitalisedPrefix()+"_"+locale.toString();
	}
	public ClassName localeClassName(Locale locale) {
		return ClassName.get(packageName, localeName(locale));
	}
	
	public static Map<String, Object> getYamlFromFile(InputStream inputStream) {
		Yaml yaml = new Yaml();
		Map<String, Object> data = yaml.loadAs(inputStream, Map.class);
		return data;
	}
	
	public Map<String, MethodGenerator> getLocaleMap(Locale locale) throws GeneratorException{
		List<InputStream> urls = getLocalizedResources(prefix, "yaml", locale);
		if(urls.size() == 0) {
			throw new GeneratorException("Could not find any yaml files for locale '"+locale+"' and prefix '"+prefix+"'. Tried the following resources: "
					+getLocalizedResourcesCandidates(prefix, "yaml", locale));
		}
		Collections.reverse(urls); // So that most precise gets put in last
		Map<String, Object> rawLocaleMap = new HashMap<>();
		urls.stream().map(url -> getYamlFromFile(url)).forEach(map -> {
			rawLocaleMap.putAll(map);
			});
		Map<String, MethodGenerator> localeMap = new HashMap<>();
		for(Entry<String, Object> item: rawLocaleMap.entrySet()) {
			localeMap.put(item.getKey(), new MethodGenerator(this, item.getKey(), item.getValue()));
		}
		return localeMap;
	}
	
	/** Load localized resource for current locale.
	 * 
	 * @param baseName Basename of the resource. May include a path.
	 * @param suffix File extension of the resource.
	 * @return List<URL> for the localized resource (empty if none were found) in descending order of precision. E.g. ["en-GB", "en", ""] 
	 * @throws GeneratorException 
	 */
	public List<InputStream> getLocalizedResources(String baseName, String suffix, Locale locale) throws GeneratorException {
		List<InputStream> urls = new ArrayList<InputStream>();
	    for (String resourceName : getLocalizedResourcesCandidates(baseName, suffix, locale)) {
	        InputStream url = getResourceAsStream(resourceName);
	        if (url != null) {
	            urls.add(url);
	        }
	    }
	    return urls;
	}
	private List<String> getLocalizedResourcesCandidates(String baseName, String suffix, Locale locale){
		List<String> resourceNames = new ArrayList<String>();
	    ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
	    List<Locale> candidateLocales = control.getCandidateLocales(baseName, locale);

	    for (Locale specificLocale : candidateLocales) {
	        String bundleName = control.toBundleName(baseName, specificLocale);
	        String resourceName = control.toResourceName(bundleName, suffix);
	        resourceNames.add(resourceName);
	    }
	    return resourceNames;
	}
	
	
	// Taken from web...
	public List<String> getResourceFiles(String path) throws IOException, GeneratorException {
	    List<String> filenames = new ArrayList<String>();
	    
        InputStream in = getResourceAsStream(path);
		if(in == null) {
			throw new GeneratorException("No resource folder found with prefix: "+path);
		}
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String resource;

        while ((resource = br.readLine()) != null) {
        	if(!resource.matches(".*\\.yaml")) continue;
            filenames.add(resource);
        }

	    return filenames;
	}
	private InputStream getResourceAsStream(String resource) throws GeneratorException {
		try {
			FileObject fileObject = filer.getResource(StandardLocation.CLASS_PATH, "", resource);
			return fileObject.openInputStream();
		} catch (java.io.FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			try {
				throw new GeneratorException("Could not read in source resource file: "+filer.getResource(StandardLocation.CLASS_PATH, "", resource).toUri());
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new GeneratorException("Could not read in source resource folder.");
			}
		}
	}
}
