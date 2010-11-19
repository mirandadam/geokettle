package org.pentaho.di.ui.i18n;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the definition of which elements to scan in which XML files in which folder
 * 
 * @author matt
 *
 */
public class SourceCrawlerXMLFolder {
	private String folder;
	private String wildcard;
	private List<SourceCrawlerXMLElement> elements;
	private String defaultPackage;
	private List<SourceCrawlerPackageException> packageExceptions;
	private String keyPrefix;
	
	/**
	 * @param folder
	 * @param wildcard
	 * @param elements
	 */
	public SourceCrawlerXMLFolder(String folder, String wildcard, String keyPrefix, List<SourceCrawlerXMLElement> elements, List<SourceCrawlerPackageException> packageExceptions) {
		this.folder = folder;
		this.wildcard = wildcard;
		this.keyPrefix = keyPrefix;
		this.elements = elements;
		this.packageExceptions = packageExceptions;
	}
	
	/**
	 * @param folder
	 * @param wildcard
	 */
	public SourceCrawlerXMLFolder(String folder, String wildcard, String keyPrefix) {
		this(folder, wildcard, keyPrefix, new ArrayList<SourceCrawlerXMLElement>(), new ArrayList<SourceCrawlerPackageException>());
	}
	
	/**
	 * @return the folder
	 */
	public String getFolder() {
		return folder;
	}
	/**
	 * @param folder the folder to set
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}
	/**
	 * @return the wildcard
	 */
	public String getWildcard() {
		return wildcard;
	}
	/**
	 * @param wildcard the wildcard to set
	 */
	public void setWildcard(String wildcard) {
		this.wildcard = wildcard;
	}

	/**
	 * @return the elements
	 */
	public List<SourceCrawlerXMLElement> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<SourceCrawlerXMLElement> elements) {
		this.elements = elements;
	}

	/**
	 * @return the defaultPackage
	 */
	public String getDefaultPackage() {
		return defaultPackage;
	}

	/**
	 * @param defaultPackage the defaultPackage to set
	 */
	public void setDefaultPackage(String defaultPackage) {
		this.defaultPackage = defaultPackage;
	}

	/**
	 * @return the packageExceptions
	 */
	public List<SourceCrawlerPackageException> getPackageExceptions() {
		return packageExceptions;
	}

	/**
	 * @param packageExceptions the packageExceptions to set
	 */
	public void setPackageExceptions(
			List<SourceCrawlerPackageException> packageExceptions) {
		this.packageExceptions = packageExceptions;
	}

	/**
	 * @return the keyPrefix
	 */
	public String getKeyPrefix() {
		return keyPrefix;
	}

	/**
	 * @param keyPrefix the keyPrefix to set
	 */
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}


	
}
